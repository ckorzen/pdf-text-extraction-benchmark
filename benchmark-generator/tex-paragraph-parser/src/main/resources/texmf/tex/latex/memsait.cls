% FILE memsait.sty FOR THE PREPARATION OF CAMERA-READY PAPERS FOR
% MEMORIE DELLA SOCIETA` ASTRONOMICA ITALIANA USING LATEX.

% DO NOT MODIFY THIS FILE !!!
% USE THE TEMPLATE FILE memsait.tex TO PREPARE YOUR PAPER.

\typeout{Document Style "memsait", March 1993 --- Version 1.0.}

% PROVIDED BY L.BENACCHIO AND USED IN MEMORIE VOL.64. N.4 (1993)
% SLIGHTLY MODIFIED BY R.PALLAVICINI ON AUGUST 1, 1994 

\def\@ptsize{0}
\@namedef{ds@10pt}{\def\@ptsize{0}}
\@namedef{ds@11pt}{\def\@ptsize{1}}
\@namedef{ds@12pt}{\def\@ptsize{2}}
\def\ds@twoside{\@twosidetrue \@mparswitchtrue}
\def\ds@draft{\overfullrule 5pt}
\@options
\@twosidetrue
\@mparswitchtrue

\lineskip 1pt
\normallineskip 1pt
\def\baselinestretch{1}
 
\def\@normalsize{\@setsize\normalsize{12pt}\xpt\@xpt
  \abovedisplayskip 10pt plus2pt minus5pt
  \belowdisplayskip \abovedisplayskip
  \abovedisplayshortskip \z@ plus3pt
  \belowdisplayshortskip 6pt plus3pt minus3pt}
\def\small{\@setsize\small{11pt}\ixpt\@ixpt
  \abovedisplayskip 8.5pt plus 3pt minus 4pt
  \belowdisplayskip \abovedisplayskip
  \abovedisplayshortskip \z@ plus2pt
  \belowdisplayshortskip 4pt plus2pt minus 2pt
  \def\@listi{\topsep 4pt plus 2pt minus 2pt
    \parsep 2pt plus 1pt minus 1pt
    \itemsep \parsep}}
\def\footnotesize{\@setsize\footnotesize{9.5pt}\viiipt\@viiipt
  \abovedisplayskip 6pt plus 2pt minus 4pt
  \belowdisplayskip \abovedisplayskip
  \abovedisplayshortskip \z@ plus 1pt
  \belowdisplayshortskip 3pt plus 1pt minus 2pt
  \def\@listi{\topsep 3pt plus 1pt minus 1pt
  \parsep 2pt plus 1pt minus 1pt
  \itemsep \parsep}}
\def\scriptsize{\@setsize\scriptsize{8pt}\viipt\@viipt}
\def\tiny{\@setsize\tiny{6pt}\vpt\@vpt}
\def\large{\@setsize\large{14pt}\xiipt\@xiipt}
\def\Large{\@setsize\Large{18pt}\xivpt\@xivpt}
\def\LARGE{\@setsize\LARGE{22pt}\xviipt\@xviipt}
\def\huge{\@setsize\huge{25pt}\xxpt\@xxpt}
\def\Huge{\@setsize\Huge{30pt}\xxvpt\@xxvpt}
 
\normalsize
 
\if@twoside
  \oddsidemargin .65in
  \evensidemargin .65in
  \marginparwidth 1in
\else
  \oddsidemargin .65in
  \evensidemargin .65in
  \marginparwidth 1in
\fi
\marginparsep 11pt
\topmargin 0pt
\headheight 12pt
\headsep 2pc
\footheight 12pt
\footskip 65pt  %25pt 
 
\textheight 540pt
\textwidth 380pt
\columnsep 10pt
\columnseprule 0pt
 
\footnotesep 6pt 
\skip\footins 9pt plus 4pt minus 2pt
\floatsep 12pt plus 2pt minus 2pt
\textfloatsep 20pt plus 2pt minus 4pt
\intextsep 12pt plus 2pt minus 2pt
\@maxsep 20pt
\dblfloatsep 12pt plus 2pt minus 2pt
\dbltextfloatsep 20pt plus 2pt minus 4pt
\@dblmaxsep 20pt
\@fptop 0pt plus 1fil
\@fpsep 8pt plus 2fil
\@fpbot 0pt plus 1fil
\@dblfptop 0pt plus 1fil
\@dblfpsep 8pt plus 2fil
\@dblfpbot 0pt plus 1fil
\marginparpush 5pt

\parskip 0pt plus 1pt
\parindent 15pt
\topsep 8pt plus 2pt minus 4pt
\partopsep 2pt plus 1pt minus 1pt
\itemsep 4pt plus 2pt minus 1pt
\@lowpenalty 51
\@medpenalty 151
\@highpenalty 301
\@beginparpenalty -\@lowpenalty
\@endparpenalty -\@lowpenalty
\@itempenalty -\@lowpenalty
 
\def\part{\par \addvspace{4ex} \@afterindentfalse \secdef\@part\@spart}
\def\@part[#1]#2{%
  \ifnum \c@secnumdepth >\m@ne \refstepcounter{part}
    \addcontentsline{toc}{part}{\thepart \hspace{1em}#1}\else
    \addcontentsline{toc}{part}{#1}\fi
  { \parindent 0pt \raggedright
    \ifnum \c@secnumdepth >\m@ne \Large \bf Part \thepart \par \nobreak \fi
    \huge \bf #2\markboth{}{}\par } \nobreak \vskip 3ex \@afterheading }
\def\@spart#1{{\parindent 0pt \raggedright
  \huge \bf #1\par} \nobreak \vskip 3ex \@afterheading }
\def\section{\@startsection {section}{1}{\z@}{-3.25ex plus -1ex minus
  -.2ex}{1.5ex plus .2ex}{\normalsize\bf}}
\def\subsection{\@startsection{subsection}{2}{\z@}{-3.25ex plus -1ex minus
  -.2ex}{1.5ex plus .2ex}{\normalsize}}
\def\subsubsection{\@startsection{subsubsection}{3}{\z@}{-3.25ex plus
  -1ex minus -.2ex}{1.5ex plus .2ex}{\normalsize\it}}
\def\paragraph{\@startsection{paragraph}{4}{\z@}{-3.25ex plus -1ex minus
  -.2ex}{1.5ex plus .2ex}{\normalsize\rm}}
\def\subparagraph{\@startsection{subparagraph}{5}{\z@}{3.25ex plus 1ex minus
  .2ex}{-1em}{\normalsize\it}}
 
\setcounter{secnumdepth}{3}
 
\def\appendix{\section*{Appendix}
  \setcounter{section}{0}
  \setcounter{subsection}{0}
  \def\thesection{\Alph{section}}}
 
\leftmargini 25pt
\leftmarginii 22pt
\leftmarginiii 18.7pt
\leftmarginiv 17pt
\leftmarginv 10pt
\leftmarginvi 10pt
\leftmargin\leftmargini
\labelwidth\leftmargini \advance\labelwidth-\labelsep
\labelsep 5pt
\parsep 4pt plus 2pt minus 1pt
\def\@listi{\leftmargin\leftmargini}
\def\@listii{\leftmargin\leftmarginii
  \labelwidth\leftmarginii \advance\labelwidth-\labelsep
  \topsep 4pt plus 2pt minus 1pt
  \parsep 2pt plus 1pt minus 1pt
  \itemsep \parsep}
\def\@listiii{\leftmargin\leftmarginiii
  \labelwidth\leftmarginiii \advance\labelwidth-\labelsep
  \topsep 2pt plus 1pt minus 1pt
  \parsep \z@ \partopsep 1pt plus 0pt minus 1pt
  \itemsep \topsep}
\def\@listiv{\leftmargin\leftmarginiv
  \labelwidth\leftmarginiv\advance\labelwidth-\labelsep}
\def\@listv{\leftmargin\leftmarginv
  \labelwidth\leftmarginv\advance\labelwidth-\labelsep}
\def\@listvi{\leftmargin\leftmarginvi
  \labelwidth\leftmarginvi\advance\labelwidth-\labelsep}
 
\def\kapmathindent{1pc}

\def\labelenumi{\arabic{enumi}.}
\def\theenumi{\arabic{enumi}}
\def\labelenumii{\alph{enumii})}
\def\theenumii{\alph{enumii}}
\def\p@enumii{\theenumi}
\def\labelenumiii{\it\roman{enumiii}.}
\def\theenumiii{\roman{enumiii}}
\def\p@enumiii{\theenumi(\theenumii)}
\def\labelenumiv{\Alph{enumiv}.}
\def\theenumiv{\Alph{enumiv}}
\def\p@enumiv{\p@enumiii\theenumiii}
 
\def\labelitemi{$\bullet$}
\def\labelitemii{\bf --}
\def\labelitemiii{$\ast$}
\def\labelitemiv{$\cdot$}
 
\def\verse{\let\\=\@centercr
  \list{}{\itemsep\z@ \itemindent -1.5em\listparindent \itemindent
  \rightmargin\leftmargin\advance\leftmargin 1.5em}\item[]}
\let\endverse\endlist
\def\quotation{\list{}{\listparindent 1.5em 
\itemindent\listparindent
\rightmargin\leftmargin
\parsep 0pt plus 1pt}\item[]}
\let\endquotation=\endlist
\def\quote{\list{}{\rightmargin\leftmargin}\item[]}
\let\endquote=\endlist
 
\def\kap@list{\topsep 0pt plus 1pt \partopsep 0pt plus 1pt
  \itemsep \z@ \parsep 0pt plus 1pt
  \leftmargini 0em \leftmarginii 1em \leftmarginiii 1em
  \leftmarginiv 1em \leftmarginv 1em \leftmarginvi 1em
  \rightmargin \z@ \listparindent \z@ \itemindent \z@ }
 
\def\@mklab#1{#1 \hfil}
 
\def\kap@enumerate[#1]{\ifnum \@enumdepth >3 \@toodeep\else
  \advance\@enumdepth \@ne
  \edef\@enumctr{enum\romannumeral\the\@enumdepth}
  \list{\csname label\@enumctr\endcsname}{\kap@list \usecounter{\@enumctr}
  \settowidth\labelwidth{#1.} \leftmargin\labelwidth
  \advance\leftmargin\labelsep \def\makelabel##1{\hfil##1}}\fi}
\def\enumerate{\@ifnextchar[{\kap@enumerate}{\kap@enumerate[00]}}
 
\def\descriptionlabel#1{\hspace\labelsep \bf #1\/}
\def\description{\list{}{\labelwidth\z@\itemindent-\leftmargin
\let\makelabel\descriptionlabel}}
\let\enddescription\endlist

\def\theequation{\arabic{equation}}
 
\def\titlepage{\@restonecolfalse\if@twocolumn\@restonecoltrue\onecolumn
 \else \newpage \fi \thispagestyle{empty}\c@page\z@}
\def\endtitlepage{\if@restonecol\twocolumn \else \newpage \fi}
 
\arraycolsep 0.25em \tabcolsep 6pt \arrayrulewidth .4pt \doublerulesep 2pt
\tabbingsep \labelsep
 
\skip\@mpfootins = \skip\footins
\fboxsep = 3pt \fboxrule = .4pt
 
\newcounter {part}
\newcounter {section}
\newcounter {subsection}[section]
\newcounter {subsubsection}[subsection]
\newcounter {paragraph}[subsubsection]
\newcounter {subparagraph}[paragraph]
 
\def\thepart{\Roman{part}} \def\thesection {\arabic{section}}
\def\thesubsection {\thesection.\arabic{subsection}}
\def\thesubsubsection {\thesubsection.\arabic{subsubsection}}
\def\theparagraph {\thesubsubsection.\arabic{paragraph}}
\def\thesubparagraph {\theparagraph.\arabic{subparagraph}}
 
\def\@pnumwidth{1.55em}
\def\@tocrmarg {2.55em}
\def\@dotsep{4.5}
\setcounter{tocdepth}{3}
 
\def\tableofcontents{\section*{Contents\markboth{CONTENTS}{CONTENTS}}
 \@starttoc{toc}}
\def\l@part#1#2{\addpenalty{\@secpenalty}
 \addvspace{2.25em plus 1pt} \begingroup
 \@tempdima 3em \parindent \z@ \rightskip \@pnumwidth \parfillskip
-\@pnumwidth
 {\large \bf \leavevmode #1\hfil \hbox to\@pnumwidth{\hss #2}}\par
 \nobreak \endgroup}
\def\l@section#1#2{\addpenalty{\@secpenalty} \addvspace{1.0ex plus 1pt}
\@tempdima 1.5em \begingroup
 \parindent \z@ \rightskip \@pnumwidth
 \parfillskip -\@pnumwidth
 \leavevmode #1\hfil \hbox to\@pnumwidth{\hss #2}\par
 \endgroup}
\def\l@subsection{\@dottedtocline{2}{1.5em}{2.3em}}
\def\l@subsubsection{\@dottedtocline{3}{3.8em}{3.2em}}
\def\l@paragraph{\@dottedtocline{4}{7.0em}{4.1em}}
\def\l@subparagraph{\@dottedtocline{5}{10em}{5em}}
\def\listoffigures{\section*{List of Figures\markboth
 {LIST OF FIGURES}{LIST OF FIGURES}}\@starttoc{lof}}
\def\l@figure{\@dottedtocline{1}{1.5em}{2.3em}}
\def\listoftables{\section*{List of Tables\markboth
 {LIST OF TABLES}{LIST OF TABLES}}\@starttoc{lot}}
\let\l@table\l@figure
 
\newif\if@restonecol
\def\theindex{\@restonecoltrue\if@twocolumn\@restonecolfalse\fi
\columnseprule \z@
\columnsep 35pt\twocolumn[\section*{Index}]
 \markboth{INDEX}{INDEX}\thispagestyle{plain}\parindent\z@
 \parskip\z@ plus .3pt\relax\let\item\@idxitem}
\def\@idxitem{\par\hangindent 40pt}
\def\subitem{\par\hangindent 40pt \hspace*{20pt}}
\def\subsubitem{\par\hangindent 40pt \hspace*{30pt}}
\def\endtheindex{\if@restonecol\onecolumn\else\clearpage\fi}
\def\indexspace{\par \vskip 10pt plus 5pt minus 3pt\relax}
 
\def\footnoterule{\noindent\rule{70pt}{.5pt}\par\vspace{3pt}}
\long\def\@makefntext#1{\parindent 1em\noindent
 \hbox to 1.5em{\hss$^{\@thefnmark}$} \footnotesize#1}
 
\setcounter{topnumber}{2}
\def\topfraction{.8}
\setcounter{bottomnumber}{1}
\def\bottomfraction{.3}
\setcounter{totalnumber}{3}
\def\textfraction{.2}
\def\floatpagefraction{.8}
\setcounter{dbltopnumber}{2}
\def\dbltopfraction{.7}
\def\dblfloatpagefraction{.5}
 
\long\def\@makecaption#1#2{\vskip 10pt
 \ifnum\cap@type=1 \setbox\@tempboxa\hbox{\small #1 #2}
  \ifdim \wd\@tempboxa >\hsize \unhbox\@tempboxa\small\par \else
   \hbox to\hsize{\hfil\box\@tempboxa\hfil} \fi \fi
 \ifnum\cap@type=2 \setbox\@tempboxa\hbox{\small #2}
  \ifdim\wd\@tempboxa>\hsize #1 \unhbox\@tempboxa\small \else
   #1 \hbox to\hsize{\hfil\box\@tempboxa\hfil} \fi \fi}
 
\newcounter{figure}
\def\thefigure{\@arabic\c@figure}
\def\fps@figure{tbp}
\def\ftype@figure{1}
\def\ext@figure{lof}
\def\fnum@figure{\small{Fig.~\thefigure.}}
\def\figure{\def\cap@type{1} \@float{figure}}
\let\endfigure\end@float
\@namedef{figure*}{\@dblfloat{figure}}
\@namedef{endfigure*}{\end@dblfloat}
\newcounter{table}
\def\thetable{\@Roman\c@table}
\def\fps@table{tbp}
\def\ftype@table{2}
\def\ext@table{lot}
\def\fnum@table{\small\centerline{TABLE \thetable}}
\def\table{\def\cap@type{2} \small\@float{table}}
\let\endtable\end@float
\@namedef{table*}{\@dblfloat{table}}
\@namedef{endtable*}{\end@dblfloat}
 
\newbox\@runtitbox
\newbox\@runautbox
\newcount\@authors
\@authors=0
\newcount\@curraut
\@curraut=1
\newcount\@institutes
\@institutes=0
\newcount\@currinst
\@currinst=0
\newcount\@current
\@current=0
 
\def\author#1{}
\def\institute#1{}
\def\title#1{}
\def\subtitle#1{}
 
\long\def\opening#1\end#2{\gdef\@opening{#1}\end{#2}}
\def\endopening{}
\gdef\@opening{}
 
\def\maketitle{\ifx\@opening\@empty\@kapwarning\fi
 \begingroup \checkfront  \def\thefootnote{\fnsymbol{footnote}}
 \def\@makefnmark{\hbox{$^{\@thefnmark}$\,}}
 \def\title##1{\baselineskip=1.5\baselineskip {\Large\sf ##1} \par\vskip 50pt%
 \baselineskip=.6666667\baselineskip\ignorespaces}
 \def\subtitle##1{{\it ##1} \par\vskip 1em\ignorespaces}
 \if@twocolumn  \twocolumn[\@maketitle]
 \else\newpage \global\@topnum\z@ \begin{flushleft} \@maketitle \end{flushleft}
 \fi%
 \@thanks \endgroup
 \setcounter{footnote}{0} \let\maketitle\relax \let\@maketitle\relax
 \gdef\@thanks{}\gdef\@author{}\gdef\@title{}\let\thanks\relax}
 
\gdef\@kapwarning{\typeout{ Warning: Use "opening" environment
 for title etc.}\typeout{ Please refer to Author's Instructions.}}
 
\def\checkfront{\begingroup \def\institute##1{\global\advance\@institutes by 1}
 \setbox\@runtitbox=\vbox{\@opening} \endgroup}
 
\def\@maketitle{\@title\small\@author\par\vskip 30pt\@pubdate}
 
\def\@title{\begingroup \@opening \endgroup}
 
\def\@author{\begingroup\def\title##1{}\def\subtitle##1{}
 \loop \ifnum\@currinst<\@institutes \checkaut\printaut\repeat\endgroup}
 
\def\checkaut{\def\author##1{\ifnum\@current=\@currinst%
 \global\advance\@authors by 1 \fi}
 \def\institute##1{\global\advance\@current by 1}
 \setbox\@runtitbox=\vbox{\@opening} \@current=0}
 
\def\printaut{\@and\def\author##1{\ifnum\@current=\@currinst
 \global\advance\@curraut by 1 \ifnum\@curraut<\@authors\@format{##1},\fi
 \ifnum\@curraut=\@authors\@format{##1} and\fi
 \ifnum\@curraut>\@authors\@format{##1}\ignorespaces\fi\fi}
 \def\institute##1{\ifnum\@current=\@currinst\par\vskip .5ex {\it
 ##1}\par\vskip 1ex\ignorespaces\fi
 \global\advance\@current by 1}
 \@opening \@authors=0 \@curraut=1 \@current=0
 \global\advance\@currinst by 1}
 
\def\@and{\ifnum\@institutes>1%
 \global\advance\@currinst by 1 \ifnum\@currinst=\@institutes%
 and\par\vskip 1ex\fi \global\advance\@currinst by -1 \fi}
 
\newif\ifthanks
 
\def\@format#1{\begingroup\def\thanks##1{\global\thankstrue}
 \setbox\@runtitbox=\vbox{#1}\endgroup
 \ifthanks\def\formatname##1\thanks##2{\uppercase{##1}\thanks{##2}}%
 \formatname#1\else\def\formatname##1{\uppercase{##1}}\formatname{#1}\fi
 \global\thanksfalse}
 
\def\abstract{\def\keywords{\par\vskip 1ex\noindent\footnotesize{\bf Key words: }}
 \footnotesize\if@twocolumn\section*{Abstract}
 \else{\noindent\bf ABSTRACT. }\ignorespaces\fi}
\def\endabstract{\par\let\keywords\relax\normalsize}
 
\def\today{\ifcase\month\or
 January\or February\or March\or April\or May\or June\or
 July\or August\or September\or October\or November\or December\fi
 \space\number\day, \number\year}
 
\ps@plain \pagenumbering{arabic} \onecolumn \if@twoside\else\raggedbottom\fi
 
\def\acknowledgements{\section*{Acknowledgements}}

\gdef\@copyrightfoot{\hbox{}\hfill\hbox{}}
 
\newread\@editorfile
\newif\ifkap
\let\kapdocument\document
\def\document{\kapdocument \openin\@editorfile=\jobname.kap
 \ifeof\@editorfile\closein\@editorfile
 \else\closein\@editorfile \input{\jobname.kap} \kaptrue \setlastpage
 \typeout{ -- File `\jobname.tex' processed for:}
 \typeout{ -- \@journal}\fi \maketitle\par}
 
\def\editor#1{\gdef\@kapident{\tt #1: \jobname.tex - Date: \today~Time:
 \kaptime}}
\gdef\@kapident{}
\def\journal#1{\@ifundefined{@#1}{\typeout{ Warning: Journal code
 `#1' does not exist.}}{\csname @#1\endcsname}
 \gdef\@copyrightfoot{\footnotesize{\it\@journal}
 {\bf\@volume:} \number\@firstpage--\number\@lastpage,
 \number\@pubyear.\hfill\hbox{}\\
 \copyright~\number\@pubyear~{\it\@printline}\hfill\hbox{}\\[1em]
 \normalsize\hbox{}\hfill\@kapident}}
\gdef\@journal{}
\gdef\@printline{\@kap. \@prin\@country.}
\gdef\@kap{Kluwer Academic Publishers}
\gdef\@prin{Printed in }
\def\country#1{\gdef\@country{#1}}
\gdef\@country{\@defaultcountry}
\def\defaultcountry#1{\gdef\@defaultcountry{#1}}
\gdef\@defaultcountry{Belgium}
\def\volume#1{\gdef\@volume{#1}}
\gdef\@volume{0}
\def\pubyear#1{\gdef\@pubyear{#1}}
\gdef\@pubyear{\year}
\def\firstpage#1{\gdef\@firstpage{#1} \c@page=0 \global\advance\c@page by #1}
\gdef\@firstpage{1}
\def\lastpage#1{\@lastpage=#1}
\def\setlastpage{\@ifundefined{r@lastpage}{}{\expandafter\@setlastp\r@lastpage}}
\def\@setlastp#1#2{\@lastpage=#2}
\newcount\@lastpage
\@lastpage=1
\def\received#1{\gdef\@pubdate{(#1)}}
\gdef\@pubdate{\@date}
\def\@expa{\gdef\@journal{Experimental Astronomy}}
\def\@celmech{\gdef\@journal{Celestial Mechanics and Dynamical Astronomy}
 \defaultcountry{The Netherlands}}
\def\@solar{\gdef\@journal{Solar Physics}}
\def\@space{\gdef\@journal{Space Science Reviews}}
 
\newcount\@hour
\newcount\@minute
\def\kaptime{\@hour=\time \divide\@hour by 60 \number\@hour:%
 \multiply\@hour by 60 \@minute=\time \global\advance\@minute by -\@hour%
 \ifnum\@minute<10 0\number\@minute\else\number\@minute\fi}
 
\let\kapenddocument\enddocument
\def\enddocument{\label{lastpage} \kapenddocument}
 
\def\@sect#1#2#3#4#5#6[#7]#8{\ifnum #2>\c@secnumdepth
     \def\@svsec{}\else
     \refstepcounter{#1}\edef\@svsec{\csname the#1\endcsname.\hskip 1ex }\fi
     \@tempskipa #5\relax
      \ifdim \@tempskipa>\z@
        \begingroup #6\relax
          \@hangfrom{\hskip #3\relax\@svsec}{\interlinepenalty \@M #8\par}
        \endgroup
       \csname #1mark\endcsname{#7}\addcontentsline
         {toc}{#1}{\ifnum #2>\c@secnumdepth \else
                      \protect\numberline{\csname the#1\endcsname}\fi
                    #7}\else
        \def\@svsechd{#6\hskip #3\@svsec #8\csname #1mark\endcsname
                      {#7}\addcontentsline
                           {toc}{#1}{\ifnum #2>\c@secnumdepth \else
                             \protect\numberline{\csname the#1\endcsname}\fi
                       #7}}\fi
     \@xsect{#5}}
% 
\def\@fnsymbol#1{\ifcase#1\or *\or **\or ***\or
   \ddagger\or \ddagger\ddagger\or \ddagger\ddagger\ddagger\or
   \ast\or \ast\ast\or \ast\ast\ast \else\@ctrerr\fi\relax}
 
\def\thebibliography#1{\section*{References}
  %\footnotesize\list
  \normalsize\list
  {\relax}{\setlength{\labelsep}{0em}
        \setlength{\itemindent}{-\bibhang}
        \setlength{\itemsep}{0pt}
        \setlength{\parsep}{0pt}
        \setlength{\leftmargin}{\bibhang}}
    \def\newblock{\hskip .11em plus .33em minus .07em}
    \sloppy\clubpenalty4000\widowpenalty4000
    \sfcode`\.=1000\relax}%
 
\newlength{\bibhang}
\setlength{\bibhang}{1.4em}
 
\let\@internalcite\cite
\def\cite{\@ifstar{\citeyear}{\citefull}}
\def\citefull{\def\astroncite##1##2{##1, ##2}\@internalcite}
\def\citeyear{\def\astroncite##1##2{##2}\@internalcite}
 
\def\@citex[#1]#2{\if@filesw\immediate\write\@auxout{\string\citation{#2}}\fi
  \def\@citea{}\@cite{\@for\@citeb:=#2\do
    {\@citea\def\@citea{; }\@ifundefined
       {b@\@citeb}{{\bf ?}\@warning
       {Citation `\@citeb' on page \thepage \space undefined}}%
{\csname b@\@citeb\endcsname}}}{#1}}
 
\def\@cite#1#2{(#1\if@tempswa , #2\fi)}
\def\@biblabel#1{}
 
\def\article#1#2#3#4#5{\bibitem{} #1: #2, {\it #3\/} {\bf #4}, #5\par}
\def\reftitle#1#2#3#4#5#6{\bibitem{} #1: #2, `#3', {\it #4\/} {\bf #5}, #6\par}
\def\book#1#2#3#4#5{\bibitem{} #1: #2, {\it #3}, #4\cond@output{#5}\par}
\def\inproceedings#1#2#3#4#5#6{\bibitem{} #1: #2, in #3, ed(s).,
 {\it #4}, #5, #6\par}
 
\def\cond@output#1{\def\test@param{#1}\if\test@param\@empty\ \else, #1\fi}
 
\let\oldvec=\vec
 
\def\vec#1{\ifcat#1\alpha\mbox{\boldmath $#1$}\else\mbox{\bf #1}\fi}
\def\@begintheorem#1#2{\it \trivlist \item[\hskip
 \labelsep{\rm \uppercase{#1}\ #2.}]}
\def\@opargbegintheorem#1#2#3{\it \trivlist
      \item[\hskip \labelsep{\rm \uppercase{#1}\ #2\ {(}#3{)}.}]}
 
\frenchspacing


\newif\ifpagefooterline	\pagefooterlinefalse
\newdimen\pagefooterlineskip  \pagefooterlineskip=1.5pt 

\def\@footerline#1#2#3{\ifpagefooterline\vbox{\hrule \vskip\pagefooterlineskip
    \hbox to \textwidth{\rlap{#1}\hss{#2}\hss\llap{#3}}}%
    \else{\rlap{#1}\hss{#2}\hss\llap{#3}}\fi}

\newcommand\oddpagefooter[3]{\def\@oddfoot{\@footerline{#1}{#2}{#3}}}

\newcommand\evenpagefooter[3]{\def\@evenfoot{\@footerline{#1}{#2}{#3}}}

\newcommand\pagefooter[3]{%
	\oddpagefooter{#1}{#2}{#3}\evenpagefooter{#1}{#2}{#3}}


