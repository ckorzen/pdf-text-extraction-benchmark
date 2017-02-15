%%
%% This is file `rspublic.cls'
%% Royal Society Class File for Authors
%% Submitting to Series A Journals
%%
%% Developed by Jonathan Wainwright,
%% based on the Standard `article.cls',
%% and on the local file `rs.cls'
%%
\def\fileversion{v1.01}
\def\filedate{1999/10/01}
\NeedsTeXFormat{LaTeX2e}[1995/12/01]
\ProvidesClass{rspublic}[\filedate\space\fileversion]
\immediate\write16{(Royal Society Class File for
 Authors Submitting to Series A Journals)}
\newcommand{\@journal}{}
\@twocolumnfalse
\@twosidetrue
\@mparswitchfalse
\DeclareOption{A4}
   {\setlength\paperheight {297mm}%
    \setlength\paperwidth  {210mm}}
\ExecuteOptions{A4}
\ProcessOptions
\RequirePackage{amsmath}
\RequirePackage{amssymb}
\RequirePackage{amsthm}
\input{size10.clo}
\setlength\oddsidemargin   {14\p@} % to compensate for
\setlength\evensidemargin  {16\p@} % twoside shift
\setlength\marginparwidth  {60\p@}
\setlength\hoffset{7mm}
\setlength\voffset{-15mm}
\setlength\lineskip{1\p@}
\setlength\normallineskip{1\p@}
\setlength\overfullrule{0pt}
\setlength\textwidth{30pc}
\normalsize
\setlength\textheight{48\baselineskip}
\renewcommand\baselinestretch{}
\setlength\parskip{0\p@}
\@lowpenalty   51
\@medpenalty  151
\@highpenalty 301
\setcounter{topnumber}{3}
\renewcommand\topfraction{.99}
\setcounter{bottomnumber}{1}
\renewcommand\bottomfraction{.3}
\setcounter{totalnumber}{3}
\renewcommand\textfraction{.07}
\renewcommand\floatpagefraction{.93}
\gdef\@author{\mbox{}}
\def\author{\@ifnextchar [{\@authortwo}{\@authorone}}%{\@dblarg\@authortwo}
\def\@authortwo[#1]#2{\gdef\@author{#2}\gdef\@shortauthor{#1}}
\def\@authorone#1{\gdef\@author{#1}
 \begingroup\gdef\@shortauthor{#1}\endgroup}
\def\shortauthor#1{\gdef\@shortauthor{#1}}
\gdef\@shortauthor{}
\gdef\@title{\mbox{}}
\def\title{\@dblarg\@titletwo}
\def\@titletwo[#1]#2{\gdef\@title{#2}\gdef\@shorttitle{#1}}
\def\shorttitle#1{\gdef\@shorttitle{#1}}
\gdef\@shorttitle{}
\def\affiliation#1{\gdef\@affiliation{#1}}
\gdef\@affiliation{}
\newsavebox{\RSfootbox}
\sbox{\RSfootbox}{\footnotesize\itshape }
\def\ps@headings{\let\@mkboth\markboth
\def\@oddhead{\phantom{\rm \thepage}\hfil{\it \@shorttitle}
\hfil\rm \thepage}
\def\@oddfoot{\usebox{\RSfootbox}\hfil}
\def\@evenhead{\rm \thepage \hfil \it \@shortauthor%
 \hfil\phantom{\rm \thepage}}
\def\@evenfoot{\@oddfoot}
\def\sectionmark##1{\markboth{##1}{}}
\def\subsectionmark##1{\markright{##1}}}
\def\ps@titlepage{\let\@mkboth\@gobbletwo
\def\@oddfoot{\raisebox{0pt}[8pt][0pt]{%
  \parbox[t]{\textwidth}{\footnotesize\usebox{\RSfootbox}
  \hfill \TeX\ Paper}}}
\def\@oddhead{}\def\@evenhead{}\let\@evenfoot\@oddfoot
\def\sectionmark##1{}\def\subsectionmark##1{}}
\newcommand\maketitle{\par
  \begingroup
      \newpage
      \global\@topnum\z@   % Prevents figures from going at top of page.
      \@maketitle
    \thispagestyle{titlepage}\@thanks
  \endgroup
  \setcounter{footnote}{0}%
  \global\let\thanks\relax
  \global\let\maketitle\relax
  \global\let\@maketitle\relax
  \global\let\@thanks\@empty
  \global\let\@author\@empty
  \global\let\@date\@empty
  \global\let\@title\@empty
  \global\let\title\relax
  \global\let\author\relax
  \global\let\date\relax
  \global\let\and\relax
}
\def\@maketitle{%
  \newpage
  \null
  \vskip 1pc%
  \begin{center}%
  \let \footnote \thanks
    {\bfseries\LARGE \@title \par}%
    \vskip 12pt plus 2pt minus 1pt%
    {\scshape B\kern.8pty\kern4pt\@author \par}
    \vskip 4pt plus 1pt%
    {\itshape \@affiliation \par}
    \vskip 8pt plus 1pt%
    \end{center}%
  \par
}
\setcounter{secnumdepth}{3}
\let\appendixmark\@gobble
\newcounter {section}
\newcounter {appendix}
\@addtoreset{subsection}{appendix}
\newcounter {subsection}[section]
\newcounter {subsubsection}[subsection]
\renewcommand\thesection      {\@arabic\c@section}
\renewcommand\theappendix     {\appendixname~\@Alph\c@appendix}
\renewcommand\thesubsection   {\@alph\c@subsection}
\renewcommand\thesubsubsection{\@roman\c@subsubsection}
\newcommand\section{\setcounter{equation}{0}%
   \renewcommand{\@seccntformat}[1]{%
   \csname thesection\endcsname.\hspace{0.5em}}%
   \@startsection{section}{1}{\z@}{-3.5ex \@plus -1ex \@minus -.2ex}%
                                  {1.6ex \@plus.2ex}%
                                  {\centering\normalfont\large\bfseries}}
\newcommand\appendix{\setcounter{equation}{0}%
   \renewcommand\theequation{\@Alph\c@appendix\thinspace\arabic{equation}}%
   \renewcommand{\@seccntformat}[1]{%
   \csname theappendix\endcsname.\hspace{0.5em}}%
   \@startsection{appendix}{1}{\z@}{-3.5ex \@plus -1ex \@minus -.2ex}%
                                  {1.6ex \@plus.2ex}%
                                  {\centering\normalfont\large\bfseries}}
\newcommand\subsection{\renewcommand{\@seccntformat}[1]{%
   \normalfont\rmfamily(\normalfont\itshape
   \csname thesubsection\endcsname\/% Italic correction
   \normalfont\rmfamily)\hspace{0.5em}}%
   \@startsection{subsection}{2}{\z@}{3.25ex\@plus 1ex \@minus .2ex}%
                                     {1.4ex \@plus .2ex}%
                                     {\centering\normalfont\itshape}}
\newcommand\subsubsection{\renewcommand{\@seccntformat}[1]{%
   \normalfont\rmfamily(\csname thesubsubsection\endcsname)\hspace{0.5em}}%
   \@startsection{subsubsection}{3}{\z@}{3.25ex\@plus 1ex \@minus .2ex}%
                                        {1.4ex \@plus .2ex}%
                                        {\raggedright\normalfont
                                         \normalsize\itshape}}
\setlength\leftmargini  {2.5em}
\leftmargin  \leftmargini
\setlength\leftmarginii  {2.2em}
\setlength\leftmarginiii {1.87em}
\setlength\leftmarginiv  {1.7em}
\setlength\leftmarginv  {1em}
\setlength\leftmarginvi {1em}
\setlength  \labelsep  {.5em}
\setlength  \labelwidth{\leftmargini}
\addtolength\labelwidth{-\labelsep}
\@beginparpenalty -\@lowpenalty
\@endparpenalty   -\@lowpenalty
\@itempenalty     -\@lowpenalty
\renewcommand\theenumi{\@arabic\c@enumi}
\renewcommand\theenumii{\@alph\c@enumii}
\renewcommand\theenumiii{\@roman\c@enumiii}
\renewcommand\theenumiv{\@Alph\c@enumiv}
\newcommand\labelenumi{\theenumi.}
\newcommand\labelenumii{(\theenumii)}
\newcommand\labelenumiii{\theenumiii.}
\newcommand\labelenumiv{\theenumiv.}
\renewcommand\p@enumii{\theenumi}
\renewcommand\p@enumiii{\theenumi(\theenumii)}
\renewcommand\p@enumiv{\p@enumiii\theenumiii}
\newcommand\labelitemi{$\m@th\bullet$}
\newcommand\labelitemii{\normalfont\bfseries --}
\newcommand\labelitemiii{$\m@th\ast$}
\newcommand\labelitemiv{$\m@th\cdot$}
\newenvironment{description}
               {\list{}{\labelwidth\z@ \itemindent-\leftmargin
                        \let\makelabel\descriptionlabel}}
               {\endlist}
\newcommand*\descriptionlabel[1]{\hspace\labelsep
                                \normalfont\bfseries #1}
\newsavebox{\keywords}
\newenvironment{abstract}[1]{%
\sbox{\keywords}{\begin{minipage}{28pc}\centering
\medskip\footnotesize\bfseries\mathversion{bold} Keywords: #1\end{minipage}}
\par\normalsize\noindent\ignorespaces}
{\par\usebox{\keywords}\medskip\par}
\newenvironment{acknowledgements}{%
  \par\medskip\noindent\small\ignorespaces}{\par}
\newenvironment{verse}
               {\let\\\@centercr
                \list{}{\itemsep      \z@
                        \itemindent   -1.5em%
                        \listparindent\itemindent
                        \rightmargin  \leftmargin
                        \advance\leftmargin 1.5em}%
                \item\relax}
               {\endlist}
\newenvironment{quotation}
               {\list{}{\listparindent 1.5em%TandT
                        \itemindent    \listparindent
                        \rightmargin   \leftmargin
                        \parsep        \z@ \@plus\p@}%
                \item\relax}
               {\endlist}
\newenvironment{quote}
               {\list{}{\rightmargin\leftmargin}%
                \item\relax}
               {\endlist}
\setlength\tabcolsep{6\p@}
\setlength\arraycolsep{5\p@}
\setlength\arrayrulewidth{.4\p@}
\setlength\doublerulesep{2\p@}
\setlength\tabbingsep{\labelsep}
\skip\@mpfootins = \skip\footins
\setlength\fboxsep{3\p@}
\setlength\fboxrule{.4\p@}
\renewcommand\theequation{\thesection.\@arabic\c@equation}
\newlength\abovecaptionskip
\newlength\belowcaptionskip
\setlength\abovecaptionskip{3\p@ \@plus 3\p@ \@minus 1\p@}
\setlength\belowcaptionskip{3\p@ \@plus 3\p@ \@minus 1\p@}
\setlength\textfloatsep{8\p@ \@plus 6\p@ \@minus 4\p@}
\def\flcap@type{}
\long\def\@makecaption#1#2{%
  \vskip\abovecaptionskip\begingroup\small
  \sbox\@tempboxa{{\normalfont\rmfamily #1.} \flcap@type #2}%
  \ifdim \wd\@tempboxa > 2\hsize
    \unhbox\@tempboxa\par
  \else
    \global \@minipagefalse
    \centering \unhbox\@tempboxa\par
  \fi
  \endgroup\vskip\belowcaptionskip}
\long\def\longcaption#1{%
  \sbox\@tempboxa{{\small (#1)}}%
  \ifdim \wd\@tempboxa > \hsize
    {\small (#1)}\par
  \else
    \centerline{\unhbox\@tempboxa}\par
  \fi
  \vskip\belowcaptionskip}
\newcounter{figure}
\renewcommand\thefigure{\@arabic\c@figure}
\def\fps@figure{tp}
\def\ftype@figure{1}
\def\ext@figure{lof}
\def\fnum@figure{\figurename~\thefigure}
\newenvironment{figure}
               {\@float{figure}}
               {\end@float}
\newcounter{table}
\renewcommand\thetable{\@arabic\c@table}
\def\fps@table{tp}
\def\ftype@table{1}
\def\ext@table{lot}
\def\fnum@table{\tablename~\thetable}
\newenvironment{table}
               {\def\flcap@type{\itshape}%
                \@float{table}\small}
               {\end@float}
\def\plaintable{\@float{table}}
\let\endplaintable\end@float
\let\endtable\end@float
\renewcommand{\arraystretch}{1.05}

\newtheorem{theorem}{Theorem}[section]
\newtheorem{lemma}[theorem]{Lemma}
\newtheorem{corollary}[theorem]{Corollary}
\newtheorem{proposition}[theorem]{Proposition}

\DeclareOldFontCommand{\rm}{\normalfont\rmfamily}{\mathrm}
\DeclareOldFontCommand{\sf}{\normalfont\sffamily}{\mathsf}
\DeclareOldFontCommand{\tt}{\normalfont\ttfamily}{\mathtt}
\DeclareOldFontCommand{\bf}{\normalfont\bfseries}{\mathbf}
\DeclareOldFontCommand{\it}{\normalfont\itshape}{\mathit}
\DeclareOldFontCommand{\sl}{\normalfont\slshape}{\@nomath\sl}
\DeclareOldFontCommand{\sc}{\normalfont\scshape}{\@nomath\sc}
\DeclareRobustCommand*\cal{\@fontswitch\relax\mathcal}
\DeclareRobustCommand*\mit{\@fontswitch\relax\mathnormal}
\newcommand\@pnumwidth{2.55em}
\newcommand\@tocrmarg{2.55em}
\setcounter{tocdepth}{2}
\newcommand\tableofcontents{%
%    \addvspace{-2em \@plus\p@}%
    \section*{\contentsname}%
    \@starttoc{toc}%
    \addvspace{1em \@plus\p@}%
    }
\newcommand*\l@section[2]{%
  \ifnum \c@tocdepth >\z@
    \renewcommand\numberline[1]{\hb@xt@\@tempdima{##1.\hfil}}
    \addpenalty\@secpenalty
    \addvspace{\z@ \@plus\p@}%
    \setlength\@tempdima{1.5em}%
    \begingroup
      \parindent \z@ \rightskip \@pnumwidth
      \parfillskip -\@pnumwidth
      \leavevmode
      \advance\leftskip\@tempdima
      \hskip -\leftskip
      #1\nobreak\hfil \nobreak\hb@xt@\@pnumwidth{\hss #2}\par
    \endgroup
  \fi}
\newcommand*\l@appendix[2]{%
  \ifnum \c@tocdepth >\z@
    \renewcommand\numberline[1]{\hb@xt@\@tempdima{##1.\hfil}}
    \addpenalty\@secpenalty
    \addvspace{\z@ \@plus\p@}%
    \setlength\@tempdima{6em}%
    \begingroup
      \parindent \z@ \rightskip \@pnumwidth
      \parfillskip -\@pnumwidth
      \leavevmode
      \advance\leftskip\@tempdima
      \advance\leftskip -4.5em
      \hskip -\leftskip
      #1\nobreak\hfil \nobreak\hb@xt@\@pnumwidth{\hss #2}\par
    \endgroup
  \fi}
\newcommand*\l@subsection[2]{%
  \ifnum \c@tocdepth > \@ne
    \renewcommand\numberline[1]{\hb@xt@2em{%
        \textup{(}{\itshape##1\/}\textup{)}\hfil}}
    \setlength\@tempdima{1.5em}%
    \begingroup
      \parindent \@tempdima \rightskip \@pnumwidth
      \parfillskip -\@pnumwidth
      \leavevmode
      \advance\leftskip\@tempdima
      \hskip -\leftskip
      #1\nobreak\hfil \nobreak\hb@xt@\@pnumwidth{\hss #2}\par
    \endgroup
  \fi
}
\newcommand*{\l@subsubsection}{\@dottedtocline{3}{3.8em}{3.2em}}
\newdimen\bibindent
\setlength\bibindent{1.5em}
\newenvironment{thebibliography}
     {\section*{\refname}
      \list{}{\setlength\labelwidth{0pt}
              \setlength\leftmargin{1em}
              \setlength\itemindent{-1em}
              \setlength\itemsep{-3pt}}
              \small
      \sloppy\clubpenalty6000\widowpenalty6000%
      \sfcode`\.\@m}
     {\endlist}
\newenvironment{thediscbibliography}
     {\subsubsection*{Additional references}
      \list{}{\setlength\labelwidth{0pt}
              \setlength\leftmargin{1em}
              \setlength\itemindent{-1em}
              \setlength\itemsep{-3pt}}
              \small
      \sloppy\clubpenalty6000\widowpenalty6000%
      \sfcode`\.\@m}
     {\endlist}
\newcommand\newblock{\hskip .11em\@plus.33em\@minus.07em}
\newcommand\subitem{\@idxitem \hspace*{20\p@}}
\newcommand\subsubitem{\@idxitem \hspace*{30\p@}}
\@addtoreset{footnote}{page}
\renewcommand\footnoterule{}
\renewcommand{\thefootnote}{\fnsymbol{footnote}}
\renewcommand{\@fnsymbol}[1]{\ifcase#1\or \dag\or \ddag\or \P\or \|\or
 \dag\dag\or \ddag\ddag\or \P\P\or \|\| \else\@ctrerr\fi\relax}
\renewcommand{\@makefnmark}{\mbox{\@thefnmark}}
\newcommand\@makefntext[1]{%
    \parindent 1em%
    \noindent
    \hb@xt@2.2em{\hss\@makefnmark\hskip.5em}#1}
\newcommand\contentsname{Contents}
\newcommand\refname{References}
\newcommand\indexname{Index}
\newcommand\figurename{Figure}
\newcommand\tablename{Table}
\newcommand\appendixname{Appendix}
\setlength\columnsep{10\p@}
\setlength\columnseprule{0\p@}
\renewcommand{\theequation}{\thesection.\arabic{equation}}
%\pagestyle{fancy}
\ps@headings
\pagenumbering{arabic} \frenchspacing \clubpenalty=4000
\widowpenalty=10000

\newcommand{\rd}{\mathrm{d}}            % Roman d for differential
\newcommand{\re}{\mathrm{e}}            % Roman e for exponential
\newcommand{\ri}{\mathrm{i}}            % Roman i for imaginary number
\DeclareMathOperator{\sgn}{sgn}           % Sign
\DeclareMathOperator{\Real}{Re}  % Real part
\DeclareMathOperator{\Imag}{Im}  % Imaginary part
\newcommand{\const}{\mathrm{const.}}    % Abbreviation for `constant'
\newcommand{\3}{\kern2.5pt}

\def\bi{\begin{itemize}}
\def\ei{\end{itemize}}
\def\bq{\begin{quotation}}
\def\eq{\end{quotation}}
%\newtheorem{theorem}{Theorem}

\def\thedemobiblio#1{\smallskip\par
 \list{}{\labelwidth 0pt \leftmargin 1em \itemindent -1em \itemsep 1pt}
 \small \parindent 0pt
 \parskip 1.5pt plus .1pt\relax
 \def\newblock{\hskip .11em plus .33em minus .07em}
 \sloppy\clubpenalty4000\widowpenalty4000
 \sfcode`\.=1000\relax}
\let\endthedemobiblio=\endlist
\def\TandT{T\kern-.1667em\lower.5ex\hbox{\&}\kern-.1667em T}
\endinput
%%
%% End of file `rspublic.cls'.
