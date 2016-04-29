from diff_new import Diff
from util import to_formatted_paragraphs
import collections
from operator import lt
from queue import PriorityQueue
import util

class DocumentStructureRestorer:
    def __init__(self, actual, target, junk=[]):
        """Creates a new DocumentStructureRestorer object for the two given 
        input strings."""
        
        self.actual = actual
        self.target = target
        self.junk = junk

        self.num_para_splits = 0
        self.num_para_merges = 0
        self.num_para_rearranges = 0
        self.num_spurious_paras = 0
        self.num_missing_paras = 0

        self.num_spurious_words = 0
        self.num_missing_words = 0

    def restore(self):
        """Keeps track of operations to perform to restore the actual document
        into the target document."""

        # Split 'actual' and 'target' into paragraphs.
        actual_paragraphs = to_formatted_paragraphs(self.actual, to_protect=self.junk)
        target_paragraphs = to_formatted_paragraphs(self.target, to_protect=self.junk)

        # Run diff on the paragraphs TODO: Refactor the returned values of diff.
        diff = Diff(actual_paragraphs, target_paragraphs)
        diff_result = diff.run(rearrange=True)

        self.num_para_splits = self.count_para_split_ops(diff, diff_result)
        self.num_para_rearranges = self.count_para_rearrange_ops(diff, diff_result)
        self.num_para_merges = self.count_para_merge_ops(diff, diff_result)
        self.num_spurious_paras = self.count_num_spurious_paras(diff, diff_result)
        self.num_missing_paras = self.count_num_missing_paras(diff, diff_result)
        # self.restore_words()

        return (self.num_para_splits, self.num_para_merges, 
                self.num_para_rearranges, self.num_missing_paras, 
                self.num_spurious_paras)

    def count_para_split_ops(self, diff, diff_result):
        # Reconstruct the origin structure of paragraphs in 'target' and the 
        # structure of paragraphs in 'target' after diff.
        #target_paras = {}
        #for item in diff.target_flatten:
        #    original_para_index = item.pos_stack[0]
        #    target_paras.setdefault(original_para_index, []).append(item)

        target_paras = {}
        full = []
        for common in diff_result[1].commons:
            full.extend(c.source_matched for c in common.items)
        for replace in diff_result[1].replaces:
            if not util.ignore(replace, self.junk):
                full.extend(insert.source for insert in replace.insert.items)
        full.sort()

        for item in full:
            original_para_index = item.pos_stack[0]
            target_paras.setdefault(original_para_index, []).append(item)

        # Count split operations. A paragraph must be split if 
        # (1) a target paragraph contains several actual paragraphs.
        # (2) the inner order in a target paragraph is mixed.
        prev_item = None
        queue = PriorityQueue()
        num_para_splits = 0
        for index in sorted(target_paras):
            para = target_paras[index]
            for item in para:
                if prev_item:
                    item_old_paragraph = item.pos_stack[0]
                    item_old_inner = item.pos_stack[1]
                    item_new_paragraph = item.new_pos_stack[0]
                    item_new_inner = item.new_pos_stack[1]
                    prev_item_old_paragraph = prev_item.pos_stack[0]
                    prev_item_old_inner = prev_item.pos_stack[1]
                    prev_item_new_paragraph = prev_item.new_pos_stack[0]
                    prev_item_new_inner = prev_item.new_pos_stack[1]

                    queue_length = len(queue.queue)
                    while not queue.empty() and queue.queue[0] < item_new_inner:
                        queue.get()
                    num_para_splits += queue_length > len(queue.queue)

                    # Split paragraph if it contains several actual paragraphs.
                    if item_old_paragraph == prev_item_old_paragraph:
                        if item_new_paragraph != prev_item_new_paragraph:
                            num_para_splits += 1
                        # Split paragraph if the inner order is mixed.
                        # If the order in paragraph is '2 1', 1 split is needed.
                        # If the order in paragraph is '2 1 3', 2 splits are needed.
                        if item_new_paragraph == prev_item_new_paragraph and \
                           item_new_inner < prev_item_new_inner:
                            num_para_splits += 1
                            # Put the prev item into queue to check for a later
                            # item that is larger than the prev (and we have to 
                            # count another split.)
                            queue.put(prev_item_new_inner)
                prev_item = item
        return num_para_splits

    def count_para_rearrange_ops(self, diff, diff_result):
        # Reconstruct the origin structure of paragraphs in 'target' and the 
        # structure of paragraphs in 'target' after diff.
        target_paras = {}
        full = []
        for common in diff_result[1].commons:
            full.extend(c.source_matched for c in common.items)
        for replace in diff_result[1].replaces:
            if not util.ignore(replace, self.junk):
                full.extend(insert.source for insert in replace.insert.items)
        full.sort()

        for item in full:
            original_para_index = item.pos_stack[0]
            target_paras.setdefault(original_para_index, []).append(item)

        # Count the number of rearranged paragraphs. That is the sum of the 
        # number of rearranged paragraphs whose index is smaller than the index 
        # of its previous paragraph.
        prev_item = None
        num_para_rearranges = 0
        for index in sorted(target_paras):
            para = target_paras[index]

            for item in para:
                if prev_item:
                    actual_lt = lt(item.pos_stack, prev_item.pos_stack)
                    target_lt = lt(item.new_pos_stack, prev_item.new_pos_stack)

                    num_para_rearranges += (actual_lt != target_lt)

                prev_item = item
        return num_para_rearranges 

    def count_para_merge_ops(self, diff, diff_result):
        # Reconstruct the origin structure of paragraphs in 'target' and the 
        # structure of paragraphs in 'target' after diff.        

        rearranged_paras = {}
        full = []
        for common in diff_result[1].commons:
            full.extend(c.source_matched for c in common.items)
        for replace in diff_result[1].replaces:
            if not util.ignore(replace, self.junk):
                full.extend(insert.source for insert in replace.insert.items)
        full.sort()

        for item in full:
            original_para_index = item.new_pos_stack[0]
            rearranged_paras.setdefault(original_para_index, []).append(item)

        prev_item = None
        num_para_merges = 0
        for index in sorted(rearranged_paras):
            para = rearranged_paras[index]
            for item in para:
                if prev_item:
                    item_old_paragraph = item.pos_stack[0]
                    prev_item_old_paragraph = prev_item.pos_stack[0]
                    item_new_paragraph = item.new_pos_stack[0]
                    prev_item_new_paragraph = prev_item.new_pos_stack[0]
                    
                    if item_old_paragraph != prev_item_old_paragraph and \
                       item_new_paragraph == prev_item_new_paragraph:
                        num_para_merges += 1
                prev_item = item
        return num_para_merges

    def count_num_missing_paras(self, diff, diff_result):
        # Reconstruct the origin structure of paragraphs in 'target' and the 
        # structure of paragraphs in 'target' after diff.
        actual_paras = {}
        full = []
        for common in diff_result[1].commons:
            full.extend(c.source for c in common.items)
        for replace in diff_result[1].replaces:
            if not util.ignore(replace, self.junk):
                full.extend(delete.source for delete in replace.delete.items)
        full.sort()

        for item in full:
            original_para_index = item.pos_stack[0]
            actual_paras.setdefault(original_para_index, []).append(item)

        num_missing_paras = 0
        # Count the number of merged paragraphs. That is the sum of the numbers 
        # of unique 'rearranged paragraph indices' in each actual paragraph - 1.
        for index in sorted(actual_paras):
            para = actual_paras[index]
            unique_indexes = set([item.new_pos_stack[0] for item in para])
            num_missing_paras += not any(item.pos_updated for item in para)

        return num_missing_paras

    def count_num_spurious_paras(self, diff, diff_result):
        # Reconstruct the origin structure of paragraphs in 'target' and the 
        # structure of paragraphs in 'target' after diff.
        target_paras = {}
        full = []
        for common in diff_result[1].commons:
            full.extend(c.source_matched for c in common.items)
        for replace in diff_result[1].replaces:
            if not util.ignore(replace, self.junk):
                full.extend(insert.source for insert in replace.insert.items)
        full.sort()

        for item in full:
            original_para_index = item.pos_stack[0]
            target_paras.setdefault(original_para_index, []).append(item)

        num_spurious_paras = 0
        # Count the number of split paragraphs. That is the sum of the numbers 
        # of unique 'rearranged paragraph indices' in each target paragraph - 1.
        for index in sorted(target_paras):
            para = target_paras[index]
            unique_indexes = set([item.new_pos_stack[0] for item in para])
            num_spurious_paras += not any(item.pos_updated for item in para)

        return num_spurious_paras

if __name__ == "__main__":
    a = """A B C D"""
    b = """C D 

    A B"""

    restorer = DocumentStructureRestorer(a, b)
    restorer.restore()
