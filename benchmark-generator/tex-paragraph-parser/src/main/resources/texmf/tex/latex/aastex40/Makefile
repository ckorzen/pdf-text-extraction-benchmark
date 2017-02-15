# Makefile for AAS author prepared package.

# Macros to facilitate pkg development.
PACKAGE = aastex40
DATE    = 01 Sep 94
ARCEXT  = tar.Z
PDL     = ps
STYLES  = aasms4.sty aaspp4.sty aas2pp4.sty aj_pt4.sty apjpt4.sty art8.sty
#STYDIR  = /local/lib/tex/inputs
STYDIR  = /u1/cbiemes/tex/inputs
GUIDE   = aastex
DVIS    = $(GUIDE).dvi graphics.dvi sample1.dvi \
	  sample2.dvi samptbls.dvi kspp.dvi
FILES   = RelNotes.v40 aj_pt4.sty  Makefile amssym.sty samp2tbl.tex \
	  apjpt4.doc sample1.tex README  apjpt4.sty  sample2.tex \
	  art8.doc  samptbls.tex Changes  art8.sty \
	  sgi9259.eps aasms4.doc  complext.tex  sgi9279.eps aasms4.sty \
	  epsf.sty  sgi9289.eps aaspp4.doc  eqsecnum.sty  table1.tex \
	  aaspp4.sty  table2.tex aas2pp4.doc  flushrt.sty  table3.tex \
	  aas2pp4.sty  graphics.tex  tighten.sty aastex.tex  kspp.tex \
	  aj_pt4.doc ams.ps


# Add some useful inference rules for LaTeX development.
.SUFFIXES: .doc .sty .tex .dvi .$(PDL)

.doc.sty:
	doc2sty $*

.tex.dvi:
	latex209 $<

.dvi.$(PDL):
	dvips $< -o $*.$(PDL)

# Generalized targets, housekeeping, etc.
first: $(GUIDE)
all: $(DVIS)
allsty: $(STYLES)

install: $(STYDIR)/aasms4.sty $(STYDIR)/aaspp4.sty $(STYDIR)/aas2pp4.sty \
	 $(STYDIR)/aj_pt4.sty $(STYDIR)/apjpt4.sty $(STYDIR)/art8.sty

$(STYDIR)/aasms4.sty: aasms4.sty
	cp aasms4.sty $(STYDIR)
$(STYDIR)/aaspp4.sty: aaspp4.sty
	cp aaspp4.sty $(STYDIR)
$(STYDIR)/aas2pp4.sty: aas2pp4.sty
	cp aas2pp4.sty $(STYDIR)
$(STYDIR)/aj_pt4.sty: aj_pt4.sty
	cp aj_pt4.sty $(STYDIR)
$(STYDIR)/apjpt4.sty: apjpt4.sty
	cp apjpt4.sty $(STYDIR)
$(STYDIR)/art8.sty: art8.sty
	cp art8.sty $(STYDIR)

clean:
	$(RM) a.out core *.dvi *.log

veryclean: clean
	$(RM) *.aux
	$(RM) $(PACKAGE).$(ARCEXT)
	$(RM) $(STYLES)

tar: clean
	$(RM) $(PACKAGE).$(ARCEXT)
	tar -cvf - $(FILES) | compress -c > /tmp/$(PACKAGE).$(ARCEXT)
	mv /tmp/$(PACKAGE).$(ARCEXT) .

uu: tar
	uuencode $(PACKAGE).$(ARCEXT) $(PACKAGE).$(ARCEXT) > /tmp/$(PACKAGE).uu
	mv /tmp/$(PACKAGE).uu $(HOME)

unZ:
	zcat $(HOME)/$(PACKAGE).$(ARCEXT) | tar -xvf -

zip: clean
	$(RM) $(PACKAGE).zip
	zip $(PACKAGE).zip $(FILES)

zoo: clean
	$(RM) $(PACKAGE).zoo
	zoo ah $(PACKAGE).zoo $(FILES)

ver: setver
	@echo 'Version in Makefile'
	@egrep '^(PACKAGE|DATE)' Makefile

setver:
	@echo 'Version in aasms4.doc'
	@echo -n 'PACKAGE = aastex'
	@grep 'def.*tex@ver' aasms4.doc | sed 's/[^0-9]//g'
	@echo -n 'DATE    = '
	@grep 'def.*tex@date' aasms4.doc | sed 's/^.*{//' | sed 's/}.*$$//'

# Dependency targets.

$(GUIDE): $(GUIDE).dvi
$(GUIDE).$(PDL): $(GUIDE).dvi
$(GUIDE).dvi: $(GUIDE).tex aas2pp4.sty

graphics: graphics.dvi
graphics.$(PDL): graphics.dvi
graphics.dvi: graphics.tex aasms4.sty

kspp: kspp.dvi
kspp.$(PDL): kspp.dvi
kspp.dvi: kspp.tex aaspp4.sty

sample1: sample1.dvi
sample1.$(PDL): sample1.dvi
sample1.dvi: sample1.tex aasms4.sty

sample2: sample2.dvi
sample2.$(PDL): sample2.dvi
sample2.dvi: sample2.tex samp2tbl.tex aasms4.sty aaspp4.sty aas2pp4.sty

samptbls: samptbls.dvi
samptbls.$(PDL): samptbls.dvi
samptbls.dvi: samptbls.tex aj_pt4.sty apjpt4.sty art8.sty table1.tex table2.tex table3.tex
