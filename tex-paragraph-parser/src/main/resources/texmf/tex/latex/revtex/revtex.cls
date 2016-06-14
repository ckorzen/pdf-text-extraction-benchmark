%%%%  Start of file revtex.cls %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%
%% This file allows REVTeX v3.0 to function correctly under
%% the 12/94 release of LaTeX 2e. Put this file wherever
%% revtex.sty is. Continue to use \documentstyle{revtex}
%% (with the correct options) and REVTeX will run normally
%% in compatibility mode. Thanks to David Carlisle for
%% pointing out this fix. 
		  
\ifx\every@math@size\undefined\else		 
  \let\old@expast\@expast
  \def\@expast#1{\old@expast{#1}\let\@tempa\reserved@a}
\fi

\input{revtex.sty}
%%%%  End of file revtex.cls %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\endinput
  A couple of issues have arisen with the release of LaTeX 2e, and
specifically with the 12/95 release. One is that the internal table/array
commands of LaTeX have changed. This affects REVTeX's redefinitions,
but there is a workaround. The following should be saved to filename
revtex.cls and put into the same directory as your version 3.0 revtex.sty
file. You should continue to use compatibility mode (i.e., \documentstyle
rather than \documentclass) as this is not a full-fledged 2e class.

  Also, in compatibility mode REVTeX handles the amsfonts and amssymb
options by looking for the amsfonts.sty and amssymb.sty files. This 
should work as long as you are using the beta of the AMS LaTeX files 
that are now on CTAN. When amssymb.sty, amsfonts.sty, amsgen.sty, and
all the .fd files were made available to TeX (after unpacking via
amslatex.ins and amsfonts.ins) REVTeX seemed to run OK with the amsfonts
and amssymb style options. No testing was done with older macros (if
there were any, ever . . .).



Chris Hamlin 
American Institute of Physics, 500 Sunnyside Blvd, Woodbury, NY 11797-2999
Internet: chamlin@aip.org   Phone: 516/576-2335   FAX: 516/349-7669
