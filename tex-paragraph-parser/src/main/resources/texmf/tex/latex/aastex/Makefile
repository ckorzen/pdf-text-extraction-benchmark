# Makefile for AAS author prepared package.

# Macros to facilitate pkg development.
PACKAGE = aastex30
DATE    = 10 Jan 93
ARCEXT  = tar.Z
PDL     = ps
STYLES  = aasms.sty aaspp.sty aaspptwo.sty aj_pt.sty apjpt.sty art8.sty
GUIDE   = aastex
DVIS    = $(GUIDE).dvi graphics.dvi paper1.dvi paper2.dvi sample1.dvi \
	  sample2.dvi samptbls.dvi kspp.dvi
FILES   = Makefile README aasms.doc aasms.sty aaspp.doc aaspp.sty \
	  aaspptwo.doc aaspptwo.sty apjpt.doc apjpt.sty aj_pt.doc \
	  aj_pt.sty art8.doc art8.sty \
	  aastex.tex eqsecnum.sty flushrt.sty tighten.sty epsf.sty \
	  paper1.tex paper2.tex sample1.tex sample2.tex sample2.aux \
	  samp2tbl.tex samptbls.tex table1.tex table2.tex table3.tex \
	  graphics.tex graphics.aux sgi*.eps kspp.tex

# Add some useful inference rules for LaTeX development.
.SUFFIXES: .doc .sty .tex .dvi .$(PDL)

.doc.sty:
	doc2sty $*

.tex.dvi:
	latex $<

.dvi.$(PDL):
	dvips $< -o $*.$(PDL)

# Generalized targets, housekeeping, etc.
first: $(GUIDE)
all: $(DVIS)
allsty: $(STYLES)

clean:
	$(RM) a.out core *.dvi *.log *.$(PDL)

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

zoo: clean
	$(RM) $(PACKAGE).zoo
	zoo ah $(PACKAGE).zoo $(FILES)

ver: setver
	@echo 'Version in Makefile'
	@egrep '^(PACKAGE|DATE)' Makefile

setver:
	@echo 'Version in paspconf.doc'
	@echo -n 'PACKAGE = pasp'
	@grep 'def.*tex@ver' paspconf.doc | sed 's/[^0-9]//g'
	@echo -n 'DATE    = '
	@grep 'def.*tex@date' paspconf.doc | sed 's/^.*{//' | sed 's/}.*$$//'

# Dependency targets.

$(GUIDE): $(GUIDE).dvi
$(GUIDE).$(PDL): $(GUIDE).dvi
$(GUIDE).dvi: $(GUIDE).tex aaspptwo.sty

graphics: graphics.dvi
graphics.$(PDL): graphics.dvi
graphics.dvi: graphics.tex aasms.sty

kspp: kspp.dvi
kspp.$(PDL): kspp.dvi
kspp.dvi: kspp.tex aaspp.sty

sample1: sample1.dvi
sample1.$(PDL): sample1.dvi
sample1.dvi: sample1.tex aasms.sty

sample2: sample2.dvi
sample2.$(PDL): sample2.dvi
sample2.dvi: sample2.tex samp2tbl.tex aasms.sty aaspp.sty aaspptwo.sty

samptbls: samptbls.dvi
samptbls.$(PDL): samptbls.dvi
samptbls.dvi: samptbls.tex aj_pt.sty apjpt.sty art8.sty table1.tex table2.tex table3.tex
