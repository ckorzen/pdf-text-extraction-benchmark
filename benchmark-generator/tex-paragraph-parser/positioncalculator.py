"""
Copyright 2016, University of Freiburg.
Chair of Algorithms and Data Structures.
Max Dippel <max.dippel@t-online.de>
"""


import sys
import os
import glob
import getopt
import random
import subprocess
import fileinput
import re
import time
import timeit
import itertools
import multiprocessing
import signal
from multiprocessing.dummy import Pool as ThreadPool
# from gevent import Timeout
from io import StringIO
from tempfile import mkstemp
from shutil import move
from os import remove, close
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import portrait
from reportlab.lib.pagesizes import letter
from reportlab.platypus import PageBreak
from reportlab.lib.colors import PCMYKColor, PCMYKColorSep
from reportlab.lib.colors import Color, black, blue, red
from PyPDF2 import PdfFileWriter, PdfFileReader


help = """
----------------------------------------------------------------------------
\033[1mUsage:\033[0m positioncalculator.py -i <input> -o <output>
----------------------------------------------------------------------------
\033[1mDescription:\033[0m
A tool for linking lines in a LaTeX file with their corresponding bounding
boxes in the respective PDF document. The output is a TXT file of the same
length as the LaTeX file. In line x of the TXT file there is an entry
for each box of LaTeX line x.
The pattern for a bounding box of form [page, minX, minY, width, height] is:

/page minX minY width height/

Calculated bounding boxes can be visualized using respective options.
----------------------------------------------------------------------------
\033[1mOptions:\033[0m

-h  --help              Show instructions and options.

-i  --input             Input file (ending with '.tex') or input folder
                        (ending with '/'). Paths are relative.

-o  --output            Output file (ending with '.pdf') or output folder
                        (ending with '/'). Paths are relative.

-v  --visualize         Enable visualization for 'records' or 'hboxes'.

-m  --multiprocessing   Enable multiprocessing.

-t  --timeout           Timeout duration for processing a single file.
                        Default is 10 seconds.

    --boxseparator      Specify separator between single boxes.
                        Make sure to use quotes for special characters.
                        Default is "/".

    --valueseparator    Specify separator between single values in records.
                        Make sure to use quotes for special characters.
                        Default is " ".
----------------------------------------------------------------------------
"""

# 1 Helper classes and functions
# ------------------------------------------------------------------------------


class DisplayableObject(object):
    """
    Object with rectangle to display on pdf.
    """
    def __init__(self, x, y, width, height):
        self.x = x
        self.y = y
        self.width = width
        self.height = height

    def __repr__(self):
        return "<%s x:%s y:%s w:%s h:%s>" % (self.name, round(self.x),
                                             round(self.y),
                                             round(self.width),
                                             round(self.height))

    def __str__(self):
        return "<%s x:%s y:%s w:%s h:%s>" % (self.name, round(self.x),
                                             round(self.y),
                                             round(self.width),
                                             round(self.height))


class HBox(DisplayableObject):
    """
    Simple HBox class containing records
    """

    def __init__(self, x, y, width, height):
        super(HBox, self).__init__(x, y, width, height)
        self.xrecords = []
        self.krecords = []
        self.line = 0
        self.name = "HBox"


class Record(DisplayableObject):
    """
    Simple record class containing records
    """

    def __init__(self, x, y, width, height, t, p):
        super(Record, self).__init__(x, y, width, height)
        self.type = t
        self.special = False
        self.line = 0
        self.page = p
        self.name = t+"-Record"

    def combine(self, other):
        """
        Combine two records. Therefore merge their rectangles.
        """
        x = min(self.x, other.x)
        y = min(self.y - self.height, other.y - other.height)
        width = max(self.x + self.width, other.x + other.width) - x
        height = max(self.y, other.y) - y
        y = y + height
        return Record(x, y, width, height, self.type, self.page)


def records_isbigger(r1, r2):
    """
    Checks if record r1 is bigger than r2 (regarding the rectangle)
    """
    hcontain = False
    vcontain = False
    if (r1.x <= r2.x and r1.x + r1.width >= r2.x + r2.width):
        hcontain = True
    if (r1.y >= r2.y and r1.y - r1.height <= r2.y - r2.height):
        vcontain = True
    return hcontain and vcontain


def records_contain(r1, r2):
    """
    Checks if record r1 and r2 are contained by one another
    (regarding the rectangle)
    """
    hcontain = False
    vcontain = False
    if ((r1.x <= r2.x and r1.x + r1.width >= r2.x + r2.width) or
            (r2.x <= r1.x and r2.x + r2.width >= r1.x + r1.width)):
        hcontain = True
    if ((r1.y >= r2.y and r1.y - r1.height <= r2.y - r2.height) or
            (r2.y >= r1.y and r2.y - r2.height <= r1.y - r1.height)):
        vcontain = True
    return hcontain and vcontain


def records_overlap(r1, r2, margin=0):
    """
    Checks if record r1 and r2 overlap (regarding the rectangle)
    """
    hoverlaps = True
    voverlaps = True
    if (r1.x + margin > r2.x + r2.width) or (r1.x + r1.width < r2.x + margin):
        hoverlaps = False
    if ((r1.y - margin < r2.y - r2.height) or
            (r1.y - r1.height > r2.y - margin)):
        voverlaps = False
    return hoverlaps and voverlaps


# 2 Main Code
# ------------------------------------------------------------------------------


class PositionCalculator(object):
    """
    Main class computing connection between LaTeX lines and PDF bounding boxes
    """

# 2.1 Setup
# ------------------------------------------------------------------------------

    def __init__(self, tex, out):
        """
        tex - the LaTeX file of the pdf.
        out - output directory
        """
        self.tex = tex
        self.compile_tex()
        self.page_hbox = dict()
        self.page_record = dict()
        self.page_special_record = dict()
        self.records = dict()
        self.out = out
        with open(self.tex) as f:
            self.tex_lines = f.read().splitlines()

    def compile_tex(self):
        """
        Generate PDF and synctex file from given LaTeX file using pdflatex
        """
        cmd = ['pdflatex', '-interaction', 'nonstopmode', '--synctex=-1',
               '-output-directory',
               self.tex[0: self.tex.rindex('/')],
               self.tex]
        proc = subprocess.Popen(cmd, stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE)
        proc.communicate()
        self.pdf = self.tex.split('.')[0] + ".pdf"
        self.syc = self.tex.split('.')[0] + ".synctex"

# 2.2 Parsing synctex file
# ------------------------------------------------------------------------------

    def parse_synctex(self):
        """
        Parse self.syc (synctex file)
        """
        self.page_number = 1
        self.hbox_stack = []
        with open(self.syc) as f:
            self.parse_preamble(f)
            self.parse_content(f)
        self.calculate_record_boxes()
        self.refine()

    def parse_preamble(self, f):
        """
        Parse synctex preamble and store information
        """
        for line in f:
            s = line.rstrip('\n').split(':')
            var, value = s[0], s[1]
            if var == "Magnification":
                self.magnification = float(value)
            elif var == "X Offset":
                self.x_offset = float(value)
            elif var == "Y Offset":
                self.y_offset = float(value)
            elif var == "Unit":
                self.unit = float(value)
            elif line.startswith("Content"):
                return

    def parse_content(self, f):
        """
        Parse synctex content and store information
        """
        for line in f:
            line = line.rstrip('\n')
            first = line[0]
            if first == '{':
                self.page_start(line)
            elif first == '}':
                self.page_end(line)
            elif first == '[':
                pass
            elif first == ']':
                pass
            elif first == '(':
                self.hbox_start(line)
            elif first == ')':
                self.hbox_end(line)
            elif first == 'x':
                self.record_start(line)
            elif first == 'k':
                self.record_start(line)
            elif first == 'v':
                self.record_start(line)
            elif first == '$':
                pass
            elif first == 'h':
                pass
            elif first == 'g':
                pass

    def page_start(self, line):
        """
        Call if we encounter a page start
        """
        self.page_number = int(line[1:])
        self.page_hbox[self.page_number] = dict()
        self.page_record[self.page_number] = dict()
        self.page_special_record[self.page_number] = dict()

    def page_end(self, line):
        """
        Call if we encounter a page end
        """
        pass

    def hbox_start(self, line):
        """
        Call if we encounter a hbox start
        """
        # <void vbox record> ::= "v" <link> ":" <point> ":" <size>
        line = line[1:]
        parts = line.split(':')

        # <link> ::= <tag> "," <line>
        link = parts[0]
        line_number = int(link.split(',')[1])

        # <point> ::= <integer> "," <integer>
        point = parts[1].split(',')
        x, y = int(point[0]), int(point[1])

        # <size> ::= <Width> "," <Height> "," <Depth>
        size = parts[2].split(',')
        width, height = int(size[0]), int(size[1])

        box = HBox(self.to_pdf_coordinate(x),
                   self.to_pdf_coordinate(y),
                   self.to_pdf_coordinate(width),
                   self.to_pdf_coordinate(height))
        box.line = line_number
        if line_number not in self.page_hbox[self.page_number]:
            self.page_hbox[self.page_number][line_number] = []
        self.page_hbox[self.page_number][line_number].append(box)
        self.hbox_stack.append(box)

    def hbox_end(self, line):
        """
        Call if we encounter a hbox end
        """
        hbox = self.hbox_stack.pop()

    def record_start(self, line):
        """
        Call if we encounter a record start
        """
        # 3 types of records:
        # <current record> ::= "x" <link> ":" <point>
        # <kern record> ::= "k" <link> ":" <point> ":" <Width>
        # <void vbox record> ::= "v" <link> ":" <point> ":" <size>

        c = line[0]
        if c != "k" and c != "x":
            return
        line = line[1:]
        parts = line.split(':')

        # <link> ::= <tag> "," <line>
        link = parts[0]
        line_number = int(link.split(',')[1])

        tex_line = self.tex_lines[line_number-1]

        # <point> ::= <integer> "," <integer>
        point = parts[1].split(',')
        x, y = int(point[0]), int(point[1])

        width, height = 0, 0
        if c == "k":
            # <size> ::= <Width> "," <Height> "," <Depth>
            size = parts[2].split(',')
            width = int(size[0])

        record = Record(self.to_pdf_coordinate(x),
                        self.to_pdf_coordinate(y),
                        self.to_pdf_coordinate(width),
                        self.to_pdf_coordinate(height), c, self.page_number)
        record.line = line_number

        if (
            tex_line.startswith("\startdata") or
            tex_line.startswith("\enddata") or
            tex_line.startswith("\cleapage") or
                tex_line.startswith("\widetext")):
            return

        if (tex_line.strip() == "" or
                (tex_line.startswith("\\begin") and
                 not tex_line.startswith("\\begin{abstract") and
                 not tex_line.startswith("\\begin{thebib")) or
                tex_line.startswith("\\end{abstract")):
            x = self.page_number
            if line_number not in self.page_special_record[self.page_number]:
                self.page_special_record[x][line_number] = []
            self.page_special_record[x][line_number].append(record)
            record.special = True

        else:
            if line_number not in self.page_record[self.page_number]:
                self.page_record[self.page_number][line_number] = []
            self.page_record[self.page_number][line_number].append(record)

        if c == "k":
            self.hbox_stack[-1].krecords.append(record)
        if c == "x":
            self.hbox_stack[-1].xrecords.append(record)

    def calculate_records_type(self, l, hbox, height):
        """
        Calculate bounding boxes of records (which are only points atm).
        Input is a list of records.
        This is an extra function to differentiate between record types (see
        'calculate_records').
        """
        x = hbox.x
        y = hbox.y
        for record in [r for r in
                       sorted(l, key=lambda r:r.x) if not r.special]:
            record.width = record.x - x
            record.x = x
            record.height = height
            x = record.x + record.width

        for record in [r for r in sorted(l, key=lambda r:r.x) if r.special]:
            record.width = record.x - x
            record.x = x
            record.height = height
            x = record.x + record.width

    def calculate_record_boxes(self):
        """
        Calculate bounding boxes of records (which are only points atm) using
        hboxes. Differentiates between types of records and calls subfunction.
        """
        for i in range(self.page_number):
            i += 1
            last_idx = -1
            last_page = -1

            for line, boxes in sorted(self.page_hbox[i].items(),
                                      key=lambda r: r[0]):

                for hbox in boxes:
                    start = hbox.x
                    height = hbox.height

                    self.calculate_records_type(hbox.xrecords, hbox, height)
                    self.calculate_records_type(hbox.krecords, hbox, height)

# 2.3 Record refinement
# ------------------------------------------------------------------------------

    def refine(self):
        """
        Refine current result by merging different record types and setting up
        the main dictionary for requests.
        """
        # prepare normal records
        self.merge_records()
        self.remove_certain_records()
        self.merge_overlap_between_lines()

        # prepare special records
        self.merge_special_records()

        # add certain special records
        self.add_special_records()

        # refine result
        self.remove_certain_records()
        self.remove_contained_records()
        self.refine_records()

        # add certain empty hboxes as records
        self.add_empty_hboxes()

        # prepare final result
        self.set_records_line_dict()

    def add_special_records(self):
        """
        Deal with records which are translated from an "begin" statement in the
        tex file. This is mainly due to error minimization.
        """
        for i in range(self.page_number):
            i += 1
            for line, records in self.page_special_record[i].items():
                tmp = sorted(records, key=lambda r: r.y, reverse=True)
                if len(tmp) == 0:
                    return
                y = tmp[0].y

                idx = line-1
                while (self.tex_lines[idx-1].startswith("%") or
                       self.tex_lines[idx-1].strip() == ""):
                    idx -= 1

                for record in tmp:
                    if record.width < 0:
                        continue

                    flag = False
                    for line2, records2 in sorted(self.page_record[i].items(),
                                                  key=lambda r: r[0]):
                        for record2 in records2:
                            if (records_contain(record, record2) or
                                    records_overlap(record, record2)):

                                flag = True
                                break
                        if flag:
                            break
                    if not flag:
                        if idx not in self.page_record[i]:
                            self.page_record[i][idx] = [record]
                        else:
                            self.page_record[i][idx].append(record)

    def add_empty_hboxes(self):
        """
        Add records for certain empty hboxes
        """
        for i in range(self.page_number):
            i += 1
            for line, hboxes in self.page_hbox[i].items():
                for hbox in hboxes:
                    if len(hbox.krecords) == 0 and len(hbox.xrecords) == 0:
                        record = Record(hbox.x, hbox.y, hbox.width,
                                        hbox.height, "k", self.page_number)

                        record.line = hbox.line

                        if record.width < 1 or record.height < 1:
                            continue

                        flag = False
                        l = sorted(self.page_record[i].items(),
                                   key=lambda r: r[0])
                        for line2, records2 in l:
                            for record2 in records2:
                                if (records_contain(record, record2) or
                                        records_overlap(record, record2)):
                                    flag = True
                                    break
                            if flag:
                                break

                        if not flag:
                            if line not in self.page_record[i]:
                                self.page_record[i][line] = [record]
                            self.page_record[i][line].append(record)
                if line in self.page_record[i]:
                    self.combine_overlapping_records_in_line(i, line)

    def refine_records(self):
        """
        Check whether records have feasible height and adjust otherwise.
        """
        for i in range(self.page_number):
            i += 1
            for line, records in sorted(self.page_record[i].items(),
                                        key=lambda r: r[0]):
                for record in records:
                    if record.height < 2:
                        record.height = 7

    def merge_records(self):
        """
        Merge the differenct record types of lines to get less but bigger
        bounding boxes.
        """
        for i in range(self.page_number):
            i += 1
            for line, records in sorted(self.page_record[i].items(),
                                        key=lambda r: r[0]):
                self.merge_records_in_line(i, line)
                self.combine_overlapping_records_in_line(i, line)

    def merge_special_records(self):
        """
        Merge the differenct special record types of lines to get less but
        bigger bounding boxes.
        """
        for i in range(self.page_number):
            i += 1
            for line, records in sorted(self.page_special_record[i].items(),
                                        key=lambda r: r[0]):
                self.merge_special_records_in_line(i, line)
                self.combine_overlapping_special_records_in_line(i, line)

    def merge_overlap_between_lines(self):
        """
        Merge overlapping records between two adjacent lines
        """
        for i in range(self.page_number):
            i += 1
            last_idx = -1
            last_page = -1
            for line, records in sorted(self.page_record[i].items(),
                                        key=lambda r: r[0]):
                if last_idx != -1 and i == last_page:
                    self.check_records_overlap_between_lines(i, line, last_idx)
                last_idx = line
                last_page = i

    def remove_certain_records(self):
        """
        Remove too high records
        """
        for i in range(self.page_number):
            i += 1
            last_idx = -1
            last_page = -1
            for line, _records in sorted(
                                        self.page_record[i].items(),
                                        key=lambda r: r[0]):
                records = set(_records)
                for record in records:
                    if record.height > 100:
                        for line2, _records2 in sorted(
                                        self.page_record[i].items(),
                                        key=lambda r: r[0]):
                            if line == line2:
                                continue
                            records2 = set(_records2)
                            for record2 in records2:
                                if records_contain(record, record2):
                                    if record in self.page_record[i][line]:
                                        x = line
                                        self.page_record[i][x].remove(record)

    def remove_contained_records(self):
        """
        Remove records fully contained by other records
        """
        for i in range(self.page_number):
            i += 1
            for line, records in sorted(self.page_record[i].items(),
                                        key=lambda r: r[0]):
                tmp = set(records)
                for record in tmp:
                    for line2, records2 in sorted(self.page_record[i].items(),
                                                  key=lambda r: r[0]):
                        if line2 >= line:
                            continue
                        tmp2 = set(records2)
                        for record2 in tmp2:
                            if (records_contain(record, record2)):
                                if records_isbigger(record, record2):
                                    self.page_record[i][line2].remove(record2)
                                else:
                                    if record in self.page_record[i][line]:
                                        x = line
                                        self.page_record[i][x].remove(record)

    def combine_overlapping_special_records_in_line(self, page, line):
        """
        Wrapper function for special records
        """
        self.combine_overlapping_records_in_line(page, line, True)

    def combine_overlapping_records_in_line(self, page, line, special=False):
        """
        Merge overlapping records in one line
        """
        merged = True
        l = []
        if special:
            l = self.page_special_record[page][line]
        else:
            l = self.page_record[page][line]
        tmp = []
        for i in range(len(l)):
            tmp.append(l[i])

        while merged:
            merged = False
            i = 0
            while i < len(tmp):
                flag = False
                j = 0
                while j < len(tmp):
                    if i == j:
                        j += 1
                        continue
                    merged = records_overlap(tmp[i], tmp[j])
                    if merged:
                        tmp.append(tmp[i].combine(tmp[j]))
                        tmp.remove(tmp[i])
                        tmp.remove(tmp[j-1])
                        flag = True
                        break
                    j += 1
                if flag:
                    break
                i += 1

        if special:
            self.page_special_record[page][line] = tmp
        else:
            self.page_record[page][line] = tmp

    def merge_special_records_in_line(self, page, line):
        """
        Wrapper function for special records
        """
        self.merge_records_in_line(page, line, special=True)

    def merge_records_in_line(self, page, line, special=False):
        """
        Merge records in one line
        """
        if special:
            x = [r for r in
                 self.page_special_record[page][line] if r.type == "x"]
        else:
            x = [r for r in self.page_record[page][line] if r.type == "x"]
        xrecords = []
        if len(x) > 0:
            xrecords = self.merge_record_list(x, line)
        krecords = self.merge_records_in_line_type(page, line, "k", special)
        tmp = []
        if len(krecords + xrecords) > 0:
            tmp = self.merge_record_list(krecords + xrecords, line)
        if special:
            self.page_special_record[page][line] = tmp
        else:
            self.page_record[page][line] = tmp

    def check_records_overlap_between_lines(self, page, line, last_line):
        """
        Check if the records of two lines overlap and if so remove the
        overlapping by shorten one record and enlarging the other one.
        """
        if min(len(self.page_record[page][last_line]),
               len(self.page_record[page][line])) == 0:
            return
        self.page_record[page][last_line] = sorted(
            self.page_record[page][last_line], key=lambda r: r.y)
        self.page_record[page][line] = sorted(
            self.page_record[page][line], key=lambda r: r.y)

        for last_line_record in self.page_record[page][last_line]:
            for cur_line_record in self.page_record[page][line]:
                if records_overlap(last_line_record, cur_line_record):
                    if abs(cur_line_record.y - last_line_record.y) > 4:
                        continue
                    diff = (last_line_record.x +
                            last_line_record.width - cur_line_record.x)
                    last_line_record.width -= diff

                    if last_line_record.width < 2:
                        x = last_line
                        self.page_record[page][x].remove(last_line_record)
                    return

    def merge_record_list(self, l, line):
        """
        Merges the records of a record list.
        """
        it = sorted(l, key=lambda r: r.y)
        if (len(it) == 0):
            print(line)
        x = it[0].x
        y = it[0].y
        width = it[0].width
        height = it[0].height
        page = it[0].page
        flag = False
        tmp = []
        for r in it:
            if abs(y - r.y) < 2:
                x = min(x, r.x)
                width = max(max(width, r.x - x + r.width),
                            max(width + x - r.x, r.width))
            else:
                tmp.append(Record(x, y, width, height, "f", page))
                x = r.x
                width = r.width
                y = r.y
                height = r.height
                flag = False
        if not flag:
            tmp.append(Record(x, y, width, height, "f", page))
        return tmp

    def merge_records_in_line_type(self, page, line, t, special):
        """
        Merge records of a certain type t in one line
        """
        tmp = []
        i = 0
        if special:
            l = [r for r in
                 self.page_special_record[page][line] if r.type == t]
        else:
            l = [r for r in self.page_record[page][line] if r.type == t]
        while i < len(l):
            j = i
            while j < len(l):
                x = l[i].x
                while abs(l[i].y - l[j].y) < 2 and abs(x - l[j].x) < 2:
                    x += l[j].width
                    j += 1
                    if j == len(l):
                        break
                tmp.append(Record(l[i].x, l[i].y, x-l[i].x,
                                  l[i].height, l[i].type, l[i].page))
                i = j
                break
            i = j
        return tmp

# 2.4 Refine result for output
# ------------------------------------------------------------------------------

    def set_records_line_dict(self):
        """
        Calculate main dictionary for requests. E.g. if someone gives a line as
        input we output the specific bounding boxes
        """
        for i in range(self.page_number):
            i += 1
            for line, records in self.page_record[i].items():
                if line not in self.records:
                    self.records[line] = []
                self.records[line] += records

    def calculate_position(self, line):
        """
        Calculate the position of a LaTeX line in corresponding PDF
        line - index of line in LaTeX file
        """
        if line in self.records:
            print(line, ":", self.records[line])
        else:
            print(line, ":", "No records found!")

    def write_output(self, box_separator="/", value_separator=" "):
        """
        Write computed result as output in txt file
        """
        f = open(self.out.split('.')[0] + ".txt", 'w')
        for l in range(len(self.tex_lines)):
            line = l + 1
            if line in self.records:
                for record in self.records[line]:
                    f.write(box_separator)
                    f.write(str(record.page) + value_separator +
                            str(record.x) + value_separator +
                            str(record.y) + value_separator +
                            str(record.width) + value_separator +
                            str(record.height))

            f.write(box_separator + "\n")
        f.close()


# 2.5 Visualization
# ------------------------------------------------------------------------------

    def visualize(self, vis, idx):
        """
        Visualize the calculated bounding boxes with their specific line number
        in the LaTeX file.
        """
        # open original pdf
        inputStream1 = open(self.pdf, "rb")
        existing_pdf = PdfFileReader(inputStream1)

        # create and draw canvas as pdf
        can = canvas.Canvas("tmp/tmp" + idx + ".pdf", pagesize=letter)
        can.setStrokeColor(blue)
        can.setFillColor(blue)
        can.setLineWidth(1)
        can.setFont("Helvetica", 10)
        s = set()
        height_offset = float(existing_pdf.getPage(0).mediaBox[3])

        for i in range(self.page_number):
            self.visualize_page(i+1, can, height_offset, vis)

        can.showPage()
        can.save()

        # open pdf created by drawing canvas
        inputStream2 = open("tmp/tmp" + idx + ".pdf", "rb")
        new_pdf = PdfFileReader(inputStream2)

        output = PdfFileWriter()
        # combine original PDF with PDF containing visual rectangles
        for i in range(new_pdf.getNumPages()-1):
            page = existing_pdf.getPage(i)
            page.mergePage(new_pdf.getPage(i))
            output.addPage(page)
        outputname = self.out
        outputStream = open(outputname, "wb")
        output.write(outputStream)
        os.remove("tmp/tmp" + idx + ".pdf")

        # close all streams
        outputStream.close()
        inputStream1.close()
        inputStream2.close()

    def visualize_page(self, page, can, height_offset, vis):
        """
        Visualize page <i> on canvas <can>
        """
        it = self.page_record[page].items()
        if vis == "hboxes":
            it = self.page_hbox[page].items()
        for line_number, records in it:
            if len(records) == 0:
                continue
            self.visualize_line(line_number, records, can, page, height_offset)
        can.showPage()

    def visualize_line(self, line_number, records, can, page, height_offset):
        """
        Visualize line <line_number> by drawing records on canvas
        """
        random.seed(line_number*page)
        can.setLineWidth(1)
        r = random.randrange(20, 80, 1)/100.0
        g = random.randrange(20, 80, 1)/100.0
        b = random.randrange(20, 80, 1)/100.0
        nontransparent = Color(r, g, b)
        transparent = Color(r, g, b, 0.3)
        can.setStrokeColor(nontransparent)
        can.setFillColor(transparent)
        text = str(line_number)

        for rect in records:
            can.rect(rect.x, height_offset-rect.y,
                     rect.width-2, rect.height, fill=1)

        can.setFillColor(nontransparent)
        can.drawString(20 + (line_number % 2) * 500,
                       height_offset-records[0].y, str(line_number))

# 2.6 Other
# ------------------------------------------------------------------------------

    def to_pdf_coordinate(self, point):
        """
        Helper function to convert a synctex position into a PDF position.
        """
        return (self.unit * point) / 65781.76 * (1000.0 / self.magnification)

# ------------------------------------------------------------------------------
# ------------------------------------------------------------------------------


processes = []  # store running processes

counter = None  # counter for successful processes
timeout = 10  # timout duration for single process

# output options
box_separator = '/'  # separator between single records
value_separator = ' '  # separator between single values in records

note = """
********************************************************************
* NOTE: as you're using multiprocessing there won't be any error   *
*       messages in case of a single process failing. However, you *
*       will get notified if a process times out.                  *
********************************************************************
"""


def init(args):
    """
    Init for each process to share counter.
    """
    global counter
    counter = args


def _process(inp, out, vis, idx):
    """
    Function combining several steps of action (for using
    multiprocessing.Process on one function instead of several single ones).
    """
    try:
        position_calculator = PositionCalculator(inp, out)
        position_calculator.parse_synctex()
        if vis != "":
            position_calculator.visualize(vis, str(idx))
        position_calculator.write_output(box_separator, value_separator)
        global counter
        if counter is None:
            return
        # += operation is not atomic, so we need to get a lock:
        lock = multiprocessing.Lock()
        with lock:
            counter.value += 1
    except FileNotFoundError:
        print("Failed to compile %s." % p)
    except UnicodeDecodeError:
        print("Failed to compile %s." % p)


def process(inp, out, vis, idx):
    """
    Wrapping function controlling workflow.
    Therefore checks for timeout of processes.
    """
    print("Processing", inp)
    tp = ThreadPool(1)
    res = tp.apply_async(_process, args=(inp, out, vis, idx))
    try:
        out = res.get(timeout)  # Wait timeout seconds for func to complete.
        return out
    except multiprocessing.TimeoutError:
        print("TIMEOUT:", inp)
        kill_pdflatex()
        tp.terminate()
        raise


def process_instance(inp, out, vis, idx, suc=None):
    """
    Call single process instance and handle it (differentiate between
    multi and single processing).
    """
    p = multiprocessing.Process(target=process,
                                args=(inp, out, vis, idx))
    p.start()
    return process_check_timeout(p)


def process_check_timeout(p):
    """
    Check for a single process if there is a timeout.
    """
    start_time = timeit.default_timer()
    p.join(timeout)
    if p.is_alive():
        p.terminate()
        print("TIMEOUT after " +
              str(timeit.default_timer()-start_time))
        kill_pdflatex()
        return False
    return True


def clean_up():
    """
    Remove all temporary data.
    """
    print("Cleaning up ...")
    files = glob.glob(os.path.dirname(os.path.abspath(__file__)) +
                      '/tmp/*')
    for f in files:
        os.remove(f)
    kill_pdflatex()


def kill_pdflatex():
    """
    Kill running pdflatex processes (as they easily get stuck).
    """
    print("Terminating pdflatex.")
    cmd = ['pkill', 'pdflatex']
    proc = subprocess.Popen(cmd, stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE)
    proc.communicate()

pool = None

if __name__ == '__main__':
    processes = []
    inp = ""
    out = ""
    false_input = False
    vis = ""
    multipr = False
    # read command line args
    myopts, args = getopt.getopt(sys.argv[1:], "i:o:v:mt:h",
                                 ["input=", "output=", "visualize=",
                                  "multiprocessing", "timeout=", "help",
                                  "valueseparator=", "boxseparator="])

    for o, a in myopts:
        if o in ['-h', '--help']:
            print(help)
            exit(1)
        elif o in ['-i', '--input']:
            inp = a
        elif o in ['-o', '--output']:
            out = a
        elif o in ['-v', '--visualize'] and (a == "records" or a == "hboxes"):
            vis = a
        elif o in ['-m', '--multiprocessing']:
            multipr = True
        elif o in ['-t', '--timeout']:
            timeout = int(a)
        elif o == '--valueseparator':
            value_separator = a
        elif o == '--boxseparator':
            box_separator = a
        else:
            false_input = True
    if inp == "":
        false_input = True
    if out == "":
        out = "vis.pdf"
    if false_input:
        print(help)
        exit(1)
    position_calulator = None

    # single file input
    if inp.endswith('.tex'):
        if out.endswith('/'):
            print("Invalid output file: %s" % out)
            exit(1)
        process_instance(inp, out, vis, False, 1)
        print("... done.")

    # folder input (parse every .tex file)
    elif inp.endswith('/'):
        if not os.path.exists(inp):
            print("Invalid input path: %s" % inp)
            exit(1)
        if not os.path.exists(out) or not out.endswith('/'):
            print("Invalid output path: %s" % out)
            exit(1)
        if multipr:
            print(note)
        total = 0   # count total files
        # count successful parsed files
        counter = multiprocessing.Value('i', 0)
        start_time = timeit.default_timer()
        pool = multiprocessing.Pool(processes=multiprocessing.cpu_count(),
                                    initializer=init, initargs=(counter,))
        for root, dirs, files in os.walk(inp):
            for f in files:
                if f.endswith('.tex') and not f.endswith('pp.tex'):
                    p = os.path.join(root, f)
                    total += 1
                    name = out + "vis_" + f.split('.')[0] + ".pdf"
                    if multipr:
                        pool.apply_async(process, args=(p, name, vis, total))

                    else:
                        process_instance(p, name, vis, total)
        pool.close()
        pool.join()
        print("Success on", counter.value, "/", total, "after",
              str(timeit.default_timer()-start_time), "seconds.")
        clean_up()
        print("... done.")
    else:
        print("Invalid input: %s" % inp)
        exit(1)

