% This is aa.cls
% A&A DOCUMENT CLASS -- version 6.1 for LaTeX2e
%
% LaTeX document class for Astronomy and Astrophysics journal
%
%%
%%
%% \CharacterTable
%%  {Upper-case    \A\B\C\D\E\F\G\H\I\J\K\L\M\N\O\P\Q\R\S\T\U\V\W\X\Y\Z
%%   Lower-case    \a\b\c\d\e\f\g\h\i\j\k\l\m\n\o\p\q\r\s\t\u\v\w\x\y\z
%%   Digits        \0\1\2\3\4\5\6\7\8\9
%%   Exclamation   \!     Double quote  \"     Hash (number) \#
%%   Dollar        \$     Percent       \%     Ampersand     \&
%%   Acute accent  \'     Left paren    \(     Right paren   \)
%%   Asterisk      \*     Plus          \+     Comma         \,
%%   Minus         \-     Point         \.     Solidus       \/
%%   Colon         \:     Semicolon     \;     Less than     \<
%%   Equals        \=     Greater than  \>     Question mark \?
%%   Commercial at \@     Left bracket  \[     Backslash     \\
%%   Right bracket \]     Circumflex    \^     Underscore    \_
%%   Grave accent  \`     Left brace    \{     Vertical bar  \|
%%   Right brace   \}     Tilde         \~}
\NeedsTeXFormat{LaTeX2e}[1995/12/01]
\ProvidesClass{aa}[2006/06/01 v6.1
^^JLaTeX document class for Astronomy and Astrophysics journal]
\newcommand\@ptsize{}
\newif\if@restonecol
\newif\if@titlepage
\@titlepagefalse
\newif\if@longauth
\@longauthfalse
\newif\if@online
\@onlinefalse
\newif\if@rnote
\@rnotefalse
\newif\if@oldversion
\@oldversionfalse
\newif\if@letter
\@letterfalse

%
\DeclareOption{a4paper}
   {\setlength\paperheight {297mm}% 
    \setlength\paperwidth  {210mm}}
\DeclareOption{letterpaper}
   {\setlength\paperheight {11in}%
    \setlength\paperwidth  {8.5in}}
\DeclareOption{legalpaper}
   {\setlength\paperheight {14in}%
    \setlength\paperwidth  {8.5in}}
\DeclareOption{10pt}{\renewcommand\@ptsize{0}}
\DeclareOption{11pt}{\renewcommand\@ptsize{1}}
\DeclareOption{12pt}{\renewcommand\@ptsize{2}}
\DeclareOption{twoside}{\@twosidetrue  \@mparswitchtrue}
\DeclareOption{draft}{\setlength\overfullrule{5pt}}
\DeclareOption{final}{\setlength\overfullrule{0pt}}
\DeclareOption{twocolumn}{\@twocolumntrue}
\DeclareOption{leqno}{\input{leqno.clo}}
\DeclareOption{fleqn}{\input{fleqn.clo}}
\DeclareOption{openbib}{%
  \AtEndOfPackage{%
   \renewcommand\@openbib@code{%
      \advance\leftmargin\bibindent
      \itemindent -\bibindent
      \listparindent \itemindent
      \parsep \z@
      }%
   \renewcommand\newblock{\par}}%
}
%
\DeclareOption{longauth}{\@longauthtrue}
\DeclareOption{rnote}{\@rnotetrue}
\DeclareOption{oldversion}{\@oldversiontrue}
\DeclareOption{letter}{\@lettertrue}
%
\let\if@runhead\iffalse
\DeclareOption{runningheads}{\let\if@runhead\iftrue}
\let\if@envcntreset\iffalse
\DeclareOption{envcountreset}{\let\if@envcntreset\iftrue}
\let\if@envcntsect\iffalse
\DeclareOption{envcountsect}{\let\if@envcntsect\iftrue}
\let\if@referee\iffalse
\DeclareOption{referee}{\let\if@referee\iftrue}
\let\if@onecolumn\iffalse
\DeclareOption{onecolumn}{\let\if@onecolumn\iftrue}
\let\if@nyobjects\iffalse
%
\ExecuteOptions{a4paper,twoside,twocolumn,fleqn,final,10pt,runningheads}
\ProcessOptions
\AtEndOfClass{\mathindent\z@}
%
\if\@ptsize 0
%
   \renewcommand\normalsize{%
      \@setfontsize\normalsize\@xpt{11}%
      \abovedisplayskip 8.55\p@ \@plus4\p@ \@minus5\p@
      \abovedisplayshortskip \z@ \@plus3\p@
      \belowdisplayshortskip 6\p@ \@plus3\p@ \@minus3\p@
      \belowdisplayskip \abovedisplayskip
      \let\@listi\@listI}
   \normalsize
   \newcommand\small{%
      \@setfontsize\small\@ixpt{10}%
      \abovedisplayskip 8.5\p@ \@plus3\p@ \@minus4\p@
      \abovedisplayshortskip \z@ \@plus2\p@
      \belowdisplayshortskip 4\p@ \@plus2\p@ \@minus2\p@
      \def\@listi{\leftmargin\leftmargini
                  \parsep 0\p@ \@plus1\p@ \@minus\p@
                  \topsep 8\p@ \@plus2\p@ \@minus4\p@
                  \itemsep0\p@}%
      \belowdisplayskip \abovedisplayskip
   }
   \newcommand\scriptsize{\@setfontsize\scriptsize\@viipt\@viiipt}
   \newcommand\tiny{\@setfontsize\tiny\@viiipt\@ixpt} 
   \newcommand\large{\@setfontsize\large\@xipt{13}}
   \newcommand\Large{\@setfontsize\Large\@xivpt{16}}
   \newcommand\LARGE{\@setfontsize\LARGE\@xviipt{20}}
   \newcommand\huge{\@setfontsize\huge\@xxpt{24}}
   \newcommand\Huge{\@setfontsize\Huge\@xxvpt{30}}
   %
   \setlength\parindent{15\p@}
   \setlength\smallskipamount{3\p@ \@plus 1\p@ \@minus 1\p@}
   \setlength\medskipamount{6\p@ \@plus 2\p@ \@minus 2\p@}
   \setlength\bigskipamount{12\p@ \@plus 4\p@ \@minus 4\p@}
   \setlength\headheight{12\p@}
   \setlength\headsep   {12\p@} % 
   \setlength\topskip   {10\p@}
   \setlength\footskip{30\p@}
   \setlength\maxdepth{.5\topskip}
   %
   \@settopoint\textwidth
   \setlength\marginparsep {10\p@}
   \setlength\marginparpush{5\p@}
   \setlength\oddsidemargin   {-30\p@}
   \setlength\evensidemargin  {-30\p@}
   \setlength\marginparwidth  {48\p@}
   \setlength\topmargin{-40pt}
   \setlength\footnotesep{8\p@}
   \setlength{\skip\footins}{9\p@ \@plus 4\p@ \@minus 2\p@}
   \setlength\floatsep    {12\p@ \@plus 2\p@ \@minus 2\p@}
   \setlength\textfloatsep{18\p@ \@plus 8\p@ \@minus 6\p@}
   \setlength\intextsep   {18\p@ \@plus 2\p@ \@minus 2\p@}
   \setlength\dblfloatsep    {12\p@ \@plus 2\p@ \@minus 2\p@}
   \setlength\dbltextfloatsep{18\p@ \@plus 8\p@ \@minus 6\p@}
   \setlength\@fptop{0\p@ }
   \setlength\@fpsep{12\p@ }
   \setlength\@fpbot{0\p@ \@plus 1fil}
   \setlength\@dblfptop{0\p@ }
   \setlength\@dblfpsep{12\p@ }
   \setlength\@dblfpbot{0\p@ \@plus 1fil}
   \setlength\partopsep{2\p@ \@plus 1\p@ \@minus 1\p@}
   \def\@listi{\leftmargin\leftmargini
               \parsep \z@
               \topsep 8\p@ \@plus2\p@ \@minus4\p@
               \itemsep\parsep}
   \let\@listI\@listi
   \@listi
   \def\@listii {\leftmargin\leftmarginii
                 \labelwidth\leftmarginii
                 \advance\labelwidth-\labelsep
                 \topsep    \z@
                 \parsep    \topsep
                 \itemsep   \parsep}
   \def\@listiii{\leftmargin\leftmarginiii
                 \labelwidth\leftmarginiii
                 \advance\labelwidth-\labelsep
                 \topsep    \z@
                 \parsep    \topsep
                 \itemsep   \parsep}
   \def\@listiv {\leftmargin\leftmarginiv
                 \labelwidth\leftmarginiv
                 \advance\labelwidth-\labelsep}
   \def\@listv  {\leftmargin\leftmarginv
                 \labelwidth\leftmarginv
                 \advance\labelwidth-\labelsep}
   \def\@listvi {\leftmargin\leftmarginvi
                 \labelwidth\leftmarginvi
                 \advance\labelwidth-\labelsep}
\else
   \input{size1\@ptsize.clo}
\fi
%
\let\footnotesize\small
%
\setlength{\textwidth}{184mm}
\setlength{\textheight}{705pt} 
%
\setlength\lineskip{1\p@}
\setlength\normallineskip{1\p@}
\renewcommand\baselinestretch{}
\setlength\parskip{0\p@ \@plus \p@}
\@lowpenalty   51
\@medpenalty  151
\@highpenalty 301
%
\setcounter{topnumber}{4}
\renewcommand\topfraction{.9}
\setcounter{bottomnumber}{2}
\renewcommand\bottomfraction{.5}
\setcounter{totalnumber}{6}
\renewcommand\textfraction{.05}
\renewcommand\floatpagefraction{.95}
\setcounter{dbltopnumber}{3}
\renewcommand\dbltopfraction{.9}
\renewcommand\dblfloatpagefraction{.95}
%
\def\ps@headings{%
    \let\@oddfoot\@empty\let\@evenfoot\@empty
    \def\@evenhead{\small\rlap{\thepage}\hfil\leftmark\hfil}%
    \def\@oddhead{\small\hfil\rightmark\hfil\llap{\thepage}}%
    \let\@mkboth\@gobbletwo
    \let\sectionmark\@gobble
    \let\subsectionmark\@gobble
    }
% make indentations changeable
\def\setitemindent#1{\settowidth{\labelwidth}{#1}%
        \leftmargini\labelwidth
        \advance\leftmargini\labelsep
   \def\@listi{\leftmargin\leftmargini
        \labelwidth\leftmargini\advance\labelwidth by -\labelsep
        \parsep=\parskip
        \topsep=\medskipamount
        \itemsep=\parskip \advance\itemsep by -\parsep}}
\def\setitemitemindent#1{\settowidth{\labelwidth}{#1}%
        \leftmarginii\labelwidth
        \advance\leftmarginii\labelsep
\def\@listii{\leftmargin\leftmarginii
        \labelwidth\leftmarginii\advance\labelwidth by -\labelsep
        \parsep=\parskip
        \topsep=\z@
        \itemsep=\parskip \advance\itemsep by -\parsep}}
% labels of description
\def\descriptionlabel#1{\hspace\labelsep #1\hfil}
% adjusted environment "description"
% if an optional parameter (at the first two levels of lists)
% is present, its width is considered to be the widest mark
% throughout the current list.
\def\description{\@ifnextchar[{\@describe}{\list{}{\labelwidth\z@
          \itemindent-\leftmargin \let\makelabel\descriptionlabel}}}
\let\enddescription\endlist
%
\def\describelabel#1{#1\hfil}
\def\@describe[#1]{\relax\ifnum\@listdepth=0
\setitemindent{#1}\else\ifnum\@listdepth=1
\setitemitemindent{#1}\fi\fi
\list{--}{\let\makelabel\describelabel}}
%
\newdimen\logodepth
\logodepth=1.3cm
\newdimen\headerboxheight
\headerboxheight=143pt
\newdimen\betweenumberspace          % dimension for space between
\betweenumberspace=3.33pt            % number and text of titles.
\newdimen\aftertext                  % dimension for space after
\aftertext=5pt                       % text of title.
\newdimen\headlineindent             % dimension for space between
\headlineindent=1.166cm              % number and text of headings.
\def\andname{and}
\def\keywordname{Key words.}
\def\lastandname{, and}
\def\mailname{{\it Correspondence to\/}:}
\def\email#1{{e-mail: \tt#1}}
% Used only by the publisher
\def\AASection#1{}
%
\newcounter{inst}
\newcounter{auth}
\newdimen\instindent
\newbox\authrun
\newtoks\authorrunning
\newbox\titrun
\newtoks\titlerunning

\def\journalname#1{\gdef\@journalname{#1}}
\gdef\@journalname{Astronomy \&\ Astrophysics}

\def\headnote#1{\gdef\@headnote{#1}}
\def\@headnote{}

\def\dedication#1{\gdef\@dedic{#1}}
\def\@dedic{}

\let\@date\undefined

\if@oldversion
\long\def\abstract#1{\gdef\@abstract{#1}}
\else
\long\def\abstract#1#2#3#4#5{\gdef\@abstract{\number@fw@rds=0%
         \if!#1!\advance\number@fw@rds-1 \else{\sffamily\itshape Context.}\ \pickupw@rd#1\\\let\WC@go\relax\fi
         {\sffamily\itshape Aims.}\
         \if!#2! \@latex@error{The second heading "Aims" is mandatory in the abstract}{}
           {\it The second heading "Aims" is mandatory in the abstract}\\
           \else\advance\number@fw@rds+1\pickupw@rd#2\\\let\WC@go\relax\fi
         {\sffamily\itshape Methods.}\
         \if!#3! \@latex@error{The third heading "Methods" is mandatory in the abstract}{}
           {\it The third heading "Methods" is mandatory in the abstract}\\
           \else\advance\number@fw@rds+1\pickupw@rd#3\\\let\WC@go\relax\fi
         {\sffamily\itshape Results.}\
         \if!#4! \@latex@error{The forth heading "Results" is mandatory in the abstract}{}
           {\it The forth heading "Results" is mandatory in the abstract}\\
            \else\advance\number@fw@rds+1\pickupw@rd#4\let\WC@go\relax\fi
         \if!#5! \else\\\advance\number@fw@rds+1{\sffamily\itshape Conclusions.}\ \pickupw@rd#5\let\WC@go\relax\fi
         \ifnum\number@fw@rds>400
         \@latex@error{Around \number\number@fw@rds\space words in the abstract instead of 300}{}
         \else\relax\fi}}
\fi

\def\keywords#1{\gdef\@keywords{#1}}

\def\institute#1{\gdef\@institute{#1}}

\def\institutename{\par
 \begingroup
 \parindent=0pt
 \parskip=0pt
 \setcounter{inst}{1}%
 \def\and{\par\stepcounter{inst}%
 \hangindent\instindent
 \noindent
 \hbox to\instindent{\hss$^{\theinst}$\enspace}\ignorespaces}%
 \setbox0=\vbox{\def\thanks##1{}\@institute}
 \ifnum\value{inst}>9\relax\setbox0=\hbox{$^{88}$\enspace}%
                 \else\setbox0=\hbox{$^{8}$\enspace}\fi
 \instindent=\wd0\relax
 \ifnum\value{inst}=1\relax
 \else
   \setcounter{inst}{1}%
   \hangindent\instindent
   \noindent
   \hbox to\instindent{\hss$^{\theinst}$\enspace}\ignorespaces
 \fi
 \small
 \ignorespaces
 \@institute\par
 \endgroup}

\def\offprints#1{\begingroup
\def\protect{\noexpand\protect\noexpand}\xdef\@thanks{\@thanks
\protect\footnotetext[0]{\unskip\hskip-1.5em{\it Send offprint requests
to\/}: \ignorespaces#1}}\endgroup\ignorespaces}

\def\mail#1{\gdef\@mail{#1}}
\def\@mail{}

\def\@thanks{}

\def\@fnsymbol#1{\ifcase#1\or\star\or{\star\star}\or{\star\star\star}%
   \or \dagger\or \ddagger\or
   \mathchar "278\or \mathchar "27B\or \|\or **\or \dagger\dagger
   \or \ddagger\ddagger \else\@ctrerr\fi\relax}

\def\inst#1{\unskip$^{#1}$}
\def\fnmsep{\unskip$^,$}

\def\subtitle#1{\gdef\@subtitle{#1}}
\def\@subtitle{}

\def\msnr#1{\gdef\@msnr{#1}}
\def\@msnr{}

\def\idline#1#2{\gdef\@idline{#1}\setcounter{page}{#2}}
\def\@idline{}
\def\doi#1{\gdef\@doi{DOI: 10.1051/0004-6361:#1}}
\def\@doi{(DOI: will be inserted by hand later)}

\def\headnote#1{\gdef\@headnote{#1}}
\def\@headnote{}

\def\ch@ckobl#1#2{\@ifundefined{@#1}
 {\typeout{A\string&A warning: Missing
\expandafter\string\csname#1\endcsname}%
  \csname #1\endcsname{#2}}
 {}}

\def\ch@ckoblabs#1#2#3#4#5#6{\@ifundefined{@#1}
 {\typeout{A\string&A warning: Missing
\expandafter\string\csname#1\endcsname}%
  \csname #1\endcsname{#2}{#3}{#4}{#5}{#6}}
 {}}

\newif\if@normhead \@normheadtrue
\def\hugehead{\@normheadfalse
\onecolumn
\let\thanks=\footnote
\long\def\@makefntext##1{\parindent\bibindent
     \columnwidth\textwidth\advance\columnwidth -\columnsep
     \divide\columnwidth\tw@\hsize\columnwidth \linewidth\columnwidth
     \noindent\hbox to\parindent{\hss$\m@th^{\@thefnmark}$ }##1}%
}

\newcommand\errorref{\@latex@error{Citations are not allowed in the abstract}{}}
\newenvironment{stopref}{\let\cite\errorref\let\citet\errorref\let\citep\errorref}{}
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This counts the words. This is an extract of  "gvol.sty,
% written by Ivan Andrus (gvol@math.byu.edu)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
  \newcount\number@fw@rds
  \long\def\pickupw@rd#1 {%
    \let\WC@go\pickupw@rd #1
    \advance\number@fw@rds+1
    \expandafter\WC@go
   }
% End of wordcount section

\def\maketitle{\par
\ch@ckobl{journalname}{Noname}
\ch@ckobl{date}{Preprint online version: \today}
\ch@ckobl{title}{A title should be given}
\ch@ckobl{author}{Name(s) and initial(s) of author(s) should be given}
\ch@ckobl{institute}{Address(es) of author(s) should be given}
\if@oldversion
\ch@ckobl{abstract}{An abstract should be given}
\else
\ch@ckoblabs{abstract}{An abstract should be given}{An abstract should be given}
{An abstract should be given}{An abstract should be given}{An abstract should be given}
\fi
\ch@ckobl{keywords}{Keywords should be given}
\begingroup
    \begin{stopref}
    \renewcommand\thefootnote{\@fnsymbol\c@footnote}%
    \def\@makefnmark{$^{\@thefnmark}$}%
 \def\lastand{\ifnum\value{inst}=2\relax
                 \unskip{} \andname\
              \else
                 \unskip, \andname\
              \fi}%
 \def\and{\stepcounter{auth}\relax
          \ifnum\value{auth}=\value{inst}%
             \lastand
          \else
             \unskip,
          \fi}%
 \ifnum \col@number=\@ne
   \@maketitle
 \else
   \twocolumn[\@maketitle]%
 \fi
 \thispagestyle{empty}
 \global\@topnum\z@
 \if@normhead
    \@thanks
 \else
    \twocolumn[\vspace{\@tempdima}]
    \addtocounter{page}{-1}
    \if!\the\authorrunning!
       \authorrunning{ }%
    \fi
 \fi
 \if!\@mail!\else
    \footnotetext[0]{\kern-\bibindent\mailname\
    \ignorespaces\@mail}%
 \fi
%
 \if@runhead
    \def\\{\unskip\ \ignorespaces}\def\inst##1{\unskip{}}%
    \def\thanks##1{\unskip{}}\def\fnmsep{\unskip}%
    \def\object##1{##1}%
    \instindent=\textwidth
    \advance\instindent by-2\headlineindent
    \if!\the\titlerunning!\else
      \edef\@title{\the\titlerunning}%
    \fi
    \global\setbox\titrun=\hbox{\small\rm\unboldmath\ignorespaces\@title}%
    \ifdim\wd\titrun>\instindent
       \typeout{^^JA\string&A Warning: Title too long for running head.}%
       \typeout{Please supply a shorter form with \string\titlerunning
                \space prior to \string\maketitle}%
       \global\setbox\titrun=\hbox{\small\rm
       Title Suppressed Due to Excessive Length}%
    \fi
    \xdef\@title{\copy\titrun}%
%
    \if!\the\authorrunning!
    \else
      \setcounter{auth}{1}%
      \edef\@author{\the\authorrunning}%
    \fi
    \ifnum\value{auth}>2
       \def\stripauthor##1\and##2\endauthor{%
       \protected@xdef\@author{##1\unskip\unskip\if!##2!\else
       \ et al.\fi}}%
       \expandafter\stripauthor\@author\and\endauthor
    \else
       \gdef\and{\unskip\ and \ignorespaces}%
       {\def\and{\noexpand\protect\noexpand\and}%
       \protected@xdef\@author{\@author}}
    \fi
    \global\setbox\authrun=\hbox{\small\unboldmath\@author\unskip:
                                 \unhbox\titrun\if@rnote~\itshape\rnotname\fi}%
    \ifdim\wd\authrun>\instindent
    \typeout{^^JA\string&A Warning: The running head built automatically
             from \string\author\space and \string\title
             ^^Jexceeds the pagewidth, please supply a shorter form
             ^^Jwith \string\authorrunning\space and/or
             \string\titlerunning\space prior to \string\maketitle}%
    \global\setbox\authrun=\hbox{\small\rm Please give a shorter version
       with: {\tt\string\authorrunning\space}and/or{\tt\space
       \string\titilerunning\space}prior to{\tt\space\string\maketitle}}%
    \fi
    \xdef\@author{\copy\authrun}%
    \markboth{\@author}{\@author}%
 \fi
%
 \end{stopref}
 \endgroup
 \setcounter{footnote}{0}
 \setcounter{footnote}{0}%
 \global\let\thanks\relax
 \global\let\maketitle\relax
 \global\let\@maketitle\relax
 \global\let\@thanks\@empty
 \global\let\@author\@empty
 \global\let\@date\@empty
 \global\let\@title\@empty
 \global\let\@subtitle\@empty
 \global\let\@abstract\@empty
 \global\let\@keywords\@empty
 \global\let\keywords\relax
 \global\let\title\relax
 \global\let\author\relax
 \global\let\abstract\relax
 \global\let\date\relax
 \global\let\and\relax}

\def\AALogo{\setbox254=\hbox{
\setlength{\tabcolsep}{0pt}
\begin{tabular}{l}
\\[-30pt]
{\fontsize{18}{21}\selectfont\sffamily\bfseries Astronomy} \\[-2.5pt]
{\fontsize{16}{12}\selectfont\sffamily\mdseries \kern8pt\&\raisebox{4pt}{\rule{78pt}{1pt}}} \\[-5pt]
{\fontsize{18}{21}\selectfont\sffamily\bfseries Astrophysics}
\end{tabular}}\copy254}

\def\makeheadbox{{
\hbox to\textwidth{%
\if!\@idline!
\hbox to0pt{\vbox{\hsize=40cc
\baselineskip=12pt\hrule\hbox
{\vrule\kern3pt\vbox{\kern3pt
\hb@xt@ 40cc{{\sl\@journalname}\ manuscript no.~\jobname \hfill\copyright~ESO~\the\year}
\if!\@msnr!\hbox{\today}\else
\hb@xt@ 30cc{\hfil\strut\LARGE\bfseries\smash{\@msnr}}\fi
\kern3pt}\hfil\kern3pt\vrule}\hrule}\hss}\hfill
\else
\vbox{\baselineskip=11pt
\hbox{\@idline}
\hbox{\@doi}
\hbox{\small\rm \copyright~ESO~\the\year}}
\hfill\AALogo
\fi}}}

\def\@maketitle{\newpage
 \normalfont
 \vbox to0pt{\vskip-20pt
 \nointerlineskip
 \makeheadbox\vss}\nointerlineskip
 \if@normhead
    \setbox\authrun=\vbox\bgroup
 \fi
 \begin{center}
 \vskip24pt
 \if@letter{\large \lettername}\fi
 {\LARGE \sffamily\bfseries\boldmath
  \hrule\@height\logodepth\@width\z@
  \pretolerance=10000
  \vspace*{-12pt}
  \pretolerance=10000
 \if!\@headnote!\else
   \noindent
   {\normalfont\itshape\ignorespaces\@headnote\par}\vskip 3.5mm
 \fi
  \noindent\ignorespaces
  \@title
  \if@rnote \\ \itshape \rnotename \fi
  \par}
 \vskip 8pt\relax
 \if!\@subtitle!\else
   {\Large \sffamily\bfseries\boldmath
   \pretolerance=10000
   \noindent\ignorespaces\@subtitle \par}\vskip 10pt
 \fi
%% \lineskip .5em
 \setbox0=\vbox{\setcounter{auth}{1}\def\and{\stepcounter{auth} }%
                \hfuzz=2\textwidth\def\thanks##1{}\@author}%
 \value{inst}=\value{auth}%
 \setcounter{auth}{1}%
 {\large \rmfamily\mdseries \noindent\ignorespaces\@author}
 \if@longauth \else \end{center} \fi
 \vskip6pt
\small\rm
 \if@longauth {\it (Affiliations can be found after the references)}  \else
 \leftskip=.8cm\rightskip=\leftskip
 \institutename\fi
 \vskip 8pt \noindent\@date
 \if!\@dedic!\else
    \par
    \normalsize\it
    \addvspace\baselineskip
    \noindent\@dedic
 \fi
 \if@longauth \end{center} \fi
 \vskip 8pt
 \small\rm
 \if!\@abstract!\else
  \begin{center}
   {\sffamily\bfseries\abstractname}\par
  \end{center}
  \@abstract
 \fi
 \vskip 6pt
 \small\rm
 \if!\@keywords!\else
 \noindent{\sffamily\bfseries\keywordname}\ \@keywords
 \fi
%
 \if@normhead
    \egroup % end of header box
    \@tempdima=\headerboxheight
    \advance\@tempdima by-\ht\authrun
%   \advance\@tempdima by-\pagetotal
    \ifdim\@tempdima>0pt
       \vrule width0pt height\@tempdima
    \fi
    \unvbox\authrun
    \global\@minipagetrue
    \global\everypar{\global\@minipagefalse\global\everypar{}}
 \else
    \typeout{\the\c@auth\space author names and}
    \typeout{\the\c@inst\space institutes processed}
    \global\@tempdima=\pagetotal
    \global\advance\@tempdima by24pt
 \fi
 \vskip24pt
}
%
\DeclareMathAlphabet{\mathsc}{OT1}{cmr}{m}{sc}
\def\testbx{bx}%
\DeclareRobustCommand{\ion}[2]{%
\relax\ifmmode
\ifx\testbx\f@series
{\mathbf{#1\,\mathsc{#2}}}\else
{\mathrm{#1\,\mathsc{#2}}}\fi
\else\textup{#1\,{\mdseries\textsc{#2}}}%
\fi}
%
\def\clearelargs{\def\1{}\def\2{}\def\3{}\def\4{}\count255=0}
\def\druck@lement#1{{}^{\2}_{\3}\mathrm{#1}{}^{\1}_{\4}{}\if@tempswa$\fi}
\DeclareRobustCommand{\element}{\relax\ifmmode\@tempswafalse
\else$\@tempswatrue\fi\clearelargs\def\?{\phantom{0}}\@lement}
\def\@lement#1{\if#1[\expandafter\f@@dargs\else\druck@lement{#1}\fi}
\def\f@@dargs#1]{\advance\count255by\@ne
\expandafter\def\csname\number\count255\endcsname{#1}\@lement}
%
%\def\vec#1{\ensuremath{\mathbf{#1}}}
\def\vec#1{\ensuremath{\mathchoice{\mbox{\boldmath$\displaystyle#1$}}
{\mbox{\boldmath$\textstyle#1$}}
{\mbox{\boldmath$\scriptstyle#1$}}
{\mbox{\boldmath$\scriptscriptstyle#1$}}}}
%
\def\tens#1{\ensuremath{\mathsf{#1}}}
%
\setcounter{secnumdepth}{3}
\newcounter {section}
\newcounter {subsection}[section]
\newcounter {subsubsection}[subsection]
\newcounter {paragraph}[subsubsection]
\newcounter {subparagraph}[paragraph]
\renewcommand\thesection      {\@arabic\c@section}
\renewcommand\thesubsection   {\thesection.\@arabic\c@subsection}
\renewcommand\thesubsubsection{\thesubsection .\@arabic\c@subsubsection}
\renewcommand\theparagraph    {\thesubsubsection.\@arabic\c@paragraph}
\renewcommand\thesubparagraph {\theparagraph.\@arabic\c@subparagraph}
%
\def\sectcounterend{.}

\def\@seccntformat#1{\csname the#1\endcsname\sectcounterend
\hskip\betweenumberspace}

\def\@sect#1#2#3#4#5#6[#7]#8{%
  \ifnum #2>\c@secnumdepth
    \let\@svsec\@empty
  \else
    \refstepcounter{#1}%
    \protected@edef\@svsec{\@seccntformat{#1}\relax}%
  \fi
  \@tempskipa #5\relax
  \ifdim \@tempskipa>\z@
    \begingroup
      #6{%
        \@hangfrom{\hskip #3\relax\@svsec}%
          \raggedright
          \hyphenpenalty \@M%
          \interlinepenalty \@M #8\@@par}%
    \endgroup
    \csname #1mark\endcsname{#7}%
    \addcontentsline{toc}{#1}{%
      \ifnum #2>\c@secnumdepth \else
        \protect\numberline{\csname the#1\endcsname}%
      \fi
      #7}%
  \else
    \def\@svsechd{%
      #6{\hskip #3\relax
      \@svsec #8\hskip\aftertext}%
      \csname #1mark\endcsname{#7}%
      \addcontentsline{toc}{#1}{%
        \ifnum #2>\c@secnumdepth \else
          \protect\numberline{\csname the#1\endcsname}%
        \fi
        #7}}%
  \fi
  \@xsect{#5}}

%
% measures and setting of sections
%
\def\section{\@startsection{section}{1}{\z@}%
    {-16dd plus-4pt minus-4pt}{6dd}{\large\sffamily\bfseries}}
\def\subsection{\@startsection{subsection}{2}{\z@}%
    {-16dd}{6dd}{\normalsize\sffamily\itshape}}
\def\subsubsection{\@startsection{subsubsection}{3}{\z@}%
    {-16dd}{6dd}{\normalsize\sffamily}}
\def\paragraph{\@startsection{paragraph}{4}{\z@}%
    {-16dd}{-1pt}{\normalsize\sffamily}}

\setlength\leftmargini  {\parindent}
\leftmargin  \leftmargini
\setlength\leftmarginii {\parindent}
\setlength\leftmarginiii {1.87em}
\setlength\leftmarginiv  {1.7em}
\setlength\leftmarginv  {.5em}
\setlength\leftmarginvi {.5em}
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
\newcommand\labelitemi{\normalfont\bfseries --}
\newcommand\labelitemii{\normalfont\bfseries --}
\newcommand\labelitemiii{$\m@th\bullet$}
\newcommand\labelitemiv{$\m@th\cdot$}

% redefinition of the "\newtheorem" command.
%
% Usage now:
%
%     \newtheorem{env_nam}{caption}[within]{cap_font}{body_font}
% or  \newtheorem{env_nam}[numbered_like]{caption}{cap_font}{body_font}
% or  \newtheorem*{env_nam}{caption}{cap_font}{body_font}
%
% New is "cap_font" and "body_font". It stands for
% fontdefinition of the caption and the text itself.
%
% "\newtheorem*" gives a theorem without number.
%
% A defined newthoerem environment is used as described
% by Lamport.
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\def\@thmcountersep{}
\def\@thmcounterend{.}

\def\newtheorem{\@ifstar{\@sthm}{\@Sthm}}

% definition of \newtheorem with number

\def\@Sthm#1{\@ifnextchar[{\@othm{#1}}{\@nthm{#1}}}

\def\@xnthm#1#2[#3]#4#5{\expandafter\@ifdefinable\csname #1\endcsname
   {\@definecounter{#1}\@addtoreset{#1}{#3}%
   \expandafter\xdef\csname the#1\endcsname{\expandafter\noexpand
     \csname the#3\endcsname \noexpand\@thmcountersep \@thmcounter{#1}}%
   \expandafter\xdef\csname #1name\endcsname{#2}%
   \global\@namedef{#1}{\@thm{#1}{\csname #1name\endcsname}{#4}{#5}}%
                              \global\@namedef{end#1}{\@endtheorem}}}

\def\@ynthm#1#2#3#4{\expandafter\@ifdefinable\csname #1\endcsname
   {\@definecounter{#1}%
   \expandafter\xdef\csname the#1\endcsname{\@thmcounter{#1}}%
   \expandafter\xdef\csname #1name\endcsname{#2}%
   \global\@namedef{#1}{\@thm{#1}{\csname #1name\endcsname}{#3}{#4}}%
                               \global\@namedef{end#1}{\@endtheorem}}}

\def\@othm#1[#2]#3#4#5{%
  \@ifundefined{c@#2}{\@latexerr{No theorem environment `#2'
  defined}\@eha}%
  {\expandafter\@ifdefinable\csname #1\endcsname
  {\global\@namedef{the#1}{\@nameuse{the#2}}%
  \expandafter\xdef\csname #1name\endcsname{#3}%
  \global\@namedef{#1}{\@thm{#2}{\csname #1name\endcsname}{#4}{#5}}%
  \global\@namedef{end#1}{\@endtheorem}}}}

\def\@thm#1#2#3#4{\refstepcounter
    {#1}\@ifnextchar[{\@ythm{#1}{#2}{#3}{#4}}{\@xthm{#1}{#2}{#3}{#4}}}

\def\@xthm#1#2#3#4{\@begintheorem{#2}{\csname the#1\endcsname}{#3}{#4}%
                    \ignorespaces}

\def\@ythm#1#2#3#4[#5]{\@opargbegintheorem{#2}{\csname
       the#1\endcsname}{#5}{#3}{#4}\ignorespaces}

\def\@begintheorem#1#2#3#4{\trivlist
                 \item[\hskip\labelsep{#3#1\ #2\@thmcounterend}]#4}

\def\@opargbegintheorem#1#2#3#4#5{\trivlist
      \item[\hskip\labelsep{#4#1\ #2\ (#3)\@thmcounterend}]#5}

% definition of \newtheorem* without number

\def\@sthm#1#2{\@Ynthm{#1}{#2}}

\def\@Ynthm#1#2#3#4{\expandafter\@ifdefinable\csname #1\endcsname
   {\global\@namedef{#1}{\@Thm{\csname #1name\endcsname}{#3}{#4}}%
    \expandafter\xdef\csname #1name\endcsname{#2}%
    \global\@namedef{end#1}{\@endtheorem}}}

\def\@Thm#1#2#3{\@ifnextchar[{\@Ythm{#1}{#2}{#3}}{\@Xthm{#1}{#2}{#3}}}

\def\@Xthm#1#2#3{\@Begintheorem{#1}{#2}{#3}\ignorespaces}

\def\@Ythm#1#2#3[#4]{\@Opargbegintheorem{#1}
       {#4}{#2}{#3}\ignorespaces}

\def\@Begintheorem#1#2#3{#3\trivlist
                           \item[\hskip\labelsep{#2#1\@thmcounterend}]}

\def\@Opargbegintheorem#1#2#3#4{#4\trivlist
      \item[\hskip\labelsep{#3#1\ (#2)\@thmcounterend}]}

% definition of divers theorem environments

\if@envcntsect
   \def\@thmcountersep{.}
   \newtheorem{theorem}{Theorem}[section]{\bf}{\it}
\else
   \newtheorem{theorem}{Theorem}{\bf}{\it}
   \if@envcntreset
      \@addtoreset{theorem}{section}
   \else
      \@addtoreset{theorem}{chapter}
   \fi
\fi
\newtheorem*{proof}{Proof}{\it}{\rm}
\newtheorem{corollary}[theorem]{Corollary}{\bf}{\it}
\newtheorem{definition}[theorem]{Definition}{\bf}{\rm}
\newtheorem{example}[theorem]{Example}{\it}{\rm}
\newtheorem{exercise}[theorem]{Exercise}{\it}{\rm}
\newtheorem{lemma}[theorem]{Lemma}{\bf}{\it}
\newtheorem{note}[theorem]{Note}{\it}{\rm}
\newtheorem{problem}[theorem]{Problem}{\it}{\rm}
\newtheorem{proposition}[theorem]{Proposition}{\bf}{\it}
\newtheorem{question}[theorem]{Question}{\it}{\rm}
\newtheorem{remark}[theorem]{Remark}{\it}{\rm}
\newtheorem{solution}[theorem]{Solution}{\it}{\rm}

\def\@takefromreset#1#2{%
    \def\@tempa{#1}%
    \let\@tempd\@elt
    \def\@elt##1{%
        \def\@tempb{##1}%
        \ifx\@tempa\@tempb\else
            \@addtoreset{##1}{#2}%
        \fi}%
    \expandafter\expandafter\let\expandafter\@tempc\csname cl@#2\endcsname
    \expandafter\def\csname cl@#2\endcsname{}%
    \@tempc
    \let\@elt\@tempd}

\def\theopargself{\def\@opargbegintheorem##1##2##3##4##5{\trivlist
      \item[\hskip\labelsep{##4##1\ ##2\ ##3}]##5}
                  \def\@Opargbegintheorem##1##2##3##4{##4\trivlist
      \item[\hskip\labelsep{##3##1\ ##2}]}
      }

\def\squareforqed{\hbox{\rlap{$\sqcap$}$\sqcup$}}
\def\qed{\ifmmode\else\unskip\quad\fi\squareforqed}
%
\newdimen\figgap\figgap=1cc
\long\def\@makesidecaption#1#2{%
   \parbox[b]{\@tempdima}{\captionstyle{\floatlegendstyle
                                         #1\floatcounterend}~#2}}
\def\sidecaption#1\caption{%
\setbox\@tempboxa=\hbox{#1\unskip}%
\if@twocolumn
 \ifdim\hsize<\textwidth\else
   \ifdim\wd\@tempboxa<\columnwidth
      \typeout{Double column float fits into single column -
            ^^Jyou'd better switch the environment. }%
   \fi
 \fi
\fi
\@tempdima=\hsize
\advance\@tempdima by-\figgap
\advance\@tempdima by-\wd\@tempboxa
\ifdim\@tempdima<3cm
    \typeout{\string\sidecaption: No sufficient room for the legend;
             using normal \string\caption. }%
   \unhbox\@tempboxa
   \let\@capcommand=\@caption
\else
   \let\@capcommand=\@sidecaption
   \leavevmode
   \unhbox\@tempboxa
   \hfill
\fi
\refstepcounter\@captype
\@dblarg{\@capcommand\@captype}}

\long\def\@sidecaption#1[#2]#3{\addcontentsline{\csname
  ext@#1\endcsname}{#1}{\protect\numberline{\csname
  the#1\endcsname}{\ignorespaces #2}}\begingroup
    \@parboxrestore
    \@makesidecaption{\csname fnum@#1\endcsname}{\ignorespaces #3}\par
  \endgroup}
%
% Online material
\long\def\onlfig#1#2{\AtEndDocument{\Online\setcounter{figure}{#1}\addtocounter{figure}{-1}#2}\setcounter{figure}{#1}}
\long\def\onltab#1#2{\AtEndDocument{\Online\setcounter{table}{#1}\addtocounter{table}{-1}#2}\setcounter{table}{#1}}
\long\def\onllongtab#1#2{\AtEndDocument{\Online\setlength{\LTcapwidth}{\textwidth}\setcounter{table}{#1}%
\addtocounter{table}{-1}\onecolumn#2\twocolumn}\setcounter{table}{#1}}
\long\def\onllongtabL#1#2{\AtEndDocument{\Online\setlength{\LTcapwidth}{\textheight}\setcounter{table}{#1}%
\addtocounter{table}{-1}\onecolumn#2\twocolumn}\setcounter{table}{#1}}
\long\def\longtab#1#2{\setlength{\LTcapwidth}{\textwidth}%
\setcounter{table}{#1}\addtocounter{table}{-1}\onecolumn#2\twocolumn\setcounter{table}{#1}}
\long\def\longtabL#1#2{\setlength{\LTcapwidth}{\textheight}%
\setcounter{table}{#1}\addtocounter{table}{-1}\onecolumn#2\twocolumn\setcounter{table}{#1}}
%
% Define \acknowledgements
\def\acknowledgements{\par\addvspace{6pt}\tiny\rm
\trivlist\item[\hskip\labelsep{\em Acknowledgements.\/}]}
\def\endacknowledgements{\endtrivlist\addvspace{6pt}}
%
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
               {\list{}{\listparindent 1.5em%
                        \itemindent    \listparindent
                        \rightmargin   \leftmargin
                        \parsep        \z@ \@plus\p@}%
                \item\relax}
               {\endlist}
\newenvironment{quote}
               {\list{}{\rightmargin\leftmargin}%
                \item\relax}
               {\endlist}
\if@compatibility
\newenvironment{titlepage}
    {%
      \if@twocolumn
        \@restonecoltrue\onecolumn
      \else
        \@restonecolfalse\newpage
      \fi
      \thispagestyle{empty}%
      \setcounter{page}\z@
    }%
    {\if@restonecol\twocolumn \else \newpage \fi
    }
\else
\newenvironment{titlepage}
    {%
      \if@twocolumn
        \@restonecoltrue\onecolumn
      \else
        \@restonecolfalse\newpage
      \fi
      \thispagestyle{empty}%
      \setcounter{page}\@ne
    }%
    {\if@restonecol\twocolumn \else \newpage \fi
     \if@twoside\else
        \setcounter{page}\@ne
     \fi
    }
\fi
% Appendix
\newcounter{append}
\setcounter{append}{0}
\newenvironment{appendix}{\par
\def\@hangfrom##1{\setbox\@tempboxa\hbox{{##1}}%
      \hangindent \parindent\noindent\box\@tempboxa}
\def\@seccntformat##1{\csname theap##1\endcsname\hskip\betweenumberspace}
  \setcounter{section}{\value{append}}%
  \setcounter{subsection}{0}%
  \setcounter{figure}{0}%
  \setcounter{table}{0}%
  \newcommand\theapsection{Appendix \@Alph\c@section:}%
  \newcommand\theapsubsection{\@Alph\c@section.\@arabic\c@subsection.}%
  \newcommand\theapsubsubsection{\theapsubsection\@arabic\c@subsubsection.}%
  \renewcommand\thesection{\@Alph\c@section}%
  \renewcommand\thesubsection{\@Alph\c@section.\@arabic\c@subsection}%
  \renewcommand\thesubsubsection{\thesubsection.\@arabic\c@subsubsection}%
  \renewcommand\theequation{\@Alph\c@section.\@arabic\c@equation}%
  \renewcommand\thefigure{\@Alph\c@section.\@arabic\c@figure}%
  \renewcommand\thetable{\@Alph\c@section.\@arabic\c@table}%
  \@addtoreset{equation}{section}%
  \@addtoreset{figure}{section}%
  \@addtoreset{table}{section}%
  \def\subfigures{\stepcounter{figure}\setcounter{auth}{\value{figure}}%
  \setcounter{figure}{0}%
  \def\thefigure{\@Alph\c@section\@arabic\c@auth\alph{figure}}%
  \def\resetsubfig{\setcounter{figure}{\value{auth}}}}}
{
  \setcounter{section}{0}%
  \setcounter{subsection}{0}%
  \setcounter{figure}{0}%
  \setcounter{table}{0}%
  \renewcommand\thesection{\c@section}%
  \renewcommand\thesubsection{\@arabic\c@subsection}%
  \renewcommand\thesubsubsection{\@arabic\c@subsubsection}%
  \renewcommand\theequation{\@arabic\c@equation}%
  \renewcommand\thefigure{\@arabic\c@figure}%
  \renewcommand\thetable{\@arabic\c@table}%
  \addtocounter{append}{1}
}
%
\setlength\arraycolsep{2.5\p@}
\setlength\tabcolsep{6\p@}
\setlength\arrayrulewidth{.4\p@}
\setlength\doublerulesep{2\p@}
\setlength\tabbingsep{\labelsep}
\skip\@mpfootins = \skip\footins
\setlength\fboxsep{3\p@}
\setlength\fboxrule{.4\p@}
\renewcommand\theequation{\@arabic\c@equation}
\newcounter{figure}
\renewcommand\thefigure{\@arabic\c@figure}
\def\fps@figure{tbp}
\def\ftype@figure{1}
\def\ext@figure{lof}
\def\fnum@figure{\figurename\,\thefigure}
\newenvironment{figure}
               {\@float{figure}}
               {\resetsubfig\end@float}
\newenvironment{figure*}
               {\@dblfloat{figure}}
               {\resetsubfig\end@dblfloat}
\let\resetsubfig\relax
\def\subfigures{\stepcounter{figure}\setcounter{auth}{\value{figure}}%
\setcounter{figure}{0}\def\thefigure{\@arabic\c@auth\alph{figure}}%
\def\resetsubfig{\setcounter{figure}{\value{auth}}}}
\newcounter{table}
\renewcommand\thetable{\@arabic\c@table}
\def\fps@table{tbp}
\def\ftype@table{2}
\def\ext@table{lot}
\def\fnum@table{\tablename~\thetable}
\newenvironment{table}
               {\small\@float{table}}
               {\end@float}
\newenvironment{table*}
               {\small\@dblfloat{table}}
               {\end@dblfloat}

\def \@floatboxreset {%
        \reset@font
        \small
        \@setnobreak
        \@setminipage
}

\newlength\abovecaptionskip
\newlength\belowcaptionskip
\setlength\abovecaptionskip{10\p@}
\setlength\belowcaptionskip{0\p@}
\newcommand\leftlegendglue{}

\def\fig@type{figure}

\newdimen\figcapgap\figcapgap=5pt
\newdimen\tabcapgap\tabcapgap=10pt

\@ifundefined{floatlegendstyle}{\def\floatlegendstyle{\bf}}{}
\def\floatcounterend{.}

\long\def\@caption#1[#2]#3{\par\addcontentsline{\csname
  ext@#1\endcsname}{#1}{\protect\numberline{\csname
  the#1\endcsname}{\ignorespaces #2}}\begingroup
    \@parboxrestore
    \@makecaption{\csname fnum@#1\endcsname}{\ignorespaces #3}\par
  \endgroup}

\def\capstrut{\vrule\@width\z@\@height\topskip}
\@ifundefined{captionstyle}{\def\captionstyle{\normalfont\small}}{}

\long\def\@makecaption#1#2{%
 \ifx\@captype\fig@type
   \vskip\figcapgap
   \setbox\@tempboxa\hbox{{\floatlegendstyle #1\floatcounterend}
   \capstrut #2}%
   \ifdim \wd\@tempboxa >\hsize
     {\floatlegendstyle #1\floatcounterend} \capstrut #2\par
   \else
     \hbox to\hsize{\leftlegendglue\unhbox\@tempboxa\hfil}%
   \fi
 \else
   {\floatlegendstyle #1\floatcounterend} \capstrut #2\par
   \vskip\tabcapgap
 \fi}
% Define `acknowledgement' environment
\def\acknowledgement{\par\addvspace{17pt}\tiny\rm
\trivlist\item[\hskip\labelsep
{\it\ackname}]}
\def\endacknowledgement{\endtrivlist\addvspace{6pt}}
% Define `noteadd' environment
\def\noteadd{\par\addvspace{17pt}\small\rm
\trivlist\item[\hskip\labelsep
{\it\noteaddname}]}
\def\endnoteadd{\endtrivlist\addvspace{6pt}}

\DeclareOldFontCommand{\rm}{\normalfont\rmfamily}{\mathrm}
\DeclareOldFontCommand{\sf}{\normalfont\sffamily}{\mathsf}
\DeclareOldFontCommand{\tt}{\normalfont\ttfamily}{\mathtt}
\DeclareOldFontCommand{\bf}{\normalfont\bfseries}{\mathbf}
\DeclareOldFontCommand{\it}{\normalfont\itshape}{\mathit}
\DeclareOldFontCommand{\sl}{\normalfont\slshape}{\@nomath\sl}
\DeclareOldFontCommand{\sc}{\normalfont\scshape}{\@nomath\sc}
\DeclareRobustCommand*\cal{\@fontswitch\relax\mathcal}
\DeclareRobustCommand*\mit{\@fontswitch\relax\mathnormal}
\newcommand\@pnumwidth{1.55em}
\newcommand\@tocrmarg{2.55em}
\newcommand\@dotsep{4.5}
\setcounter{tocdepth}{3}
\newcommand\tableofcontents{%
    \section*{\contentsname
        \@mkboth{%
           \MakeUppercase\contentsname}{\MakeUppercase\contentsname}}%
    \@starttoc{toc}%
    }
\newcommand*\l@part[2]{%
  \ifnum \c@tocdepth >-2\relax
    \addpenalty\@secpenalty
    \addvspace{2.25em \@plus\p@}%
    \begingroup
      \setlength\@tempdima{3em}%
      \parindent \z@ \rightskip \@pnumwidth
      \parfillskip -\@pnumwidth
      {\leavevmode
       \large \bfseries #1\hfil \hb@xt@\@pnumwidth{\hss #2}}\par
       \nobreak
       \if@compatibility
         \global\@nobreaktrue
         \everypar{\global\@nobreakfalse\everypar{}}%
      \fi
    \endgroup
  \fi}
\newcommand*\l@section[2]{%
  \ifnum \c@tocdepth >\z@
    \addpenalty\@secpenalty
    \addvspace{1.0em \@plus\p@}%
    \setlength\@tempdima{1.5em}%
    \begingroup
      \parindent \z@ \rightskip \@pnumwidth
      \parfillskip -\@pnumwidth
      \leavevmode \bfseries
      \advance\leftskip\@tempdima
      \hskip -\leftskip
      #1\nobreak\hfil \nobreak\hb@xt@\@pnumwidth{\hss #2}\par
    \endgroup
  \fi}
\newcommand*\l@subsection{\@dottedtocline{2}{1.5em}{2.3em}}
\newcommand*\l@subsubsection{\@dottedtocline{3}{3.8em}{3.2em}}
\newcommand*\l@paragraph{\@dottedtocline{4}{7.0em}{4.1em}}
\newcommand*\l@subparagraph{\@dottedtocline{5}{10em}{5em}}
\newcommand\listoffigures{%
    \section*{\listfigurename
      \@mkboth{\MakeUppercase\listfigurename}%
              {\MakeUppercase\listfigurename}}%
    \@starttoc{lof}%
    }
\newcommand*\l@figure{\@dottedtocline{1}{1.5em}{2.3em}}
\newcommand\listoftables{%
    \section*{\listtablename
      \@mkboth{%
          \MakeUppercase\listtablename}{\MakeUppercase\listtablename}}%
    \@starttoc{lot}%
    }
\let\l@table\l@figure
\newdimen\bibindent
\setlength\bibindent{\parindent}
\def\@biblabel#1{}
\def\@cite#1#2{{#1\if@tempswa , #2\fi}}
\newenvironment{thebibliography}[1]
     {\begingroup\section*{{\normalsize\refname}
        \@mkboth{\refname}{\refname}}\tiny
      \list{\@biblabel{\@arabic\c@enumiv}}%
           {\settowidth\labelwidth{\@biblabel{#1}}%
           \leftmargin\bibindent
           \setlength{\itemindent}{-\leftmargin}
           \@openbib@code
           \usecounter{enumiv}%
           \let\p@enumiv\@empty
           \renewcommand\theenumiv{\@arabic\c@enumiv}}%
      \sloppy\clubpenalty4000\widowpenalty4000%
      \sfcode`\.\@m}
     {\endlist\par\endgroup\if@longauth\kern6pt\hrule\kern6pt\institutename\@longauthfalse\fi
      \def\@noitemerr{\@latex@warning{Empty `thebibliography' environment}}}%


\newcommand\newblock{\hskip .11em\@plus.33em\@minus.07em}
\let\@openbib@code\@empty
\newenvironment{theindex}
               {\if@twocolumn
                  \@restonecolfalse
                \else
                  \@restonecoltrue
                \fi
                \columnseprule \z@
                \columnsep 35\p@
                \twocolumn[\section*{\indexname}]%
                \@mkboth{\MakeUppercase\indexname}%
                        {\MakeUppercase\indexname}%
                \thispagestyle{plain}\parindent\z@
                \parskip\z@ \@plus .3\p@\relax
                \let\item\@idxitem}
               {\if@restonecol\onecolumn\else\clearpage\fi}
\newcommand\@idxitem{\par\hangindent 40\p@}
\newcommand\subitem{\@idxitem \hspace*{20\p@}}
\newcommand\subsubitem{\@idxitem \hspace*{30\p@}}
\newcommand\indexspace{\par \vskip 10\p@ \@plus5\p@ \@minus3\p@\relax}
\renewcommand\footnoterule{%
  \kern-3\p@
  \hrule\@width.4\columnwidth
  \kern2.6\p@}
\newcommand\@makefntext[1]{%
    \noindent
    \hb@xt@\bibindent{\hss\@makefnmark\enspace}#1}
%
\newcommand\listofobjects{\if@nyobjects
  \IfFileExists{\jobname.obj}%
               {\section*{\listobjectname}%
                \@starttoc{obj}}%
               {\begingroup
                   \makeatletter
                   \if@filesw
                      \newwrite\tf@obj
                      \immediate\openout\tf@obj\jobname.obj
                      \relax
                   \fi
                   \global\@nobreakfalse
                \endgroup}\fi
               }
%
\newcommand*\l@obj[2]{\noindent`#1' on page #2\par}
%
\DeclareRobustCommand{\object}[1]{#1\global\let\if@nyobjects\iftrue
\addcontentsline{obj}{obj}{#1}}
%
\newcommand\contentsname{Contents}
\newcommand\listfigurename{List of Figures}
\newcommand\listobjectname{List of Objects}
\newcommand\listtablename{List of Tables}
\newcommand\refname{References}
\newcommand\indexname{Index}
\newcommand\figurename{Fig.}
\newcommand\tablename{Table}
\newcommand\partname{Part}
\newcommand\appendixname{Appendix}
\newcommand\abstractname{ABSTRACT}
\newcommand\rnotename{(Research Note)}
\newcommand\rnotname{(RN)}
\newcommand\lettername{\textsc{Letter to the Editor}}
\newcommand\ackname{Acknowledgements.}
\newcommand\noteaddname{Note added in proof}
\newcommand\today{}
\edef\today{\ifcase\month\or
  January\or February\or March\or April\or May\or June\or
  July\or August\or September\or October\or November\or December\fi
  \space\number\day, \number\year}
\setlength\columnsep{12pt}
\setlength\columnseprule{0\p@}
\frenchspacing
\clubpenalty=10000
\widowpenalty=10000
\def\thisbottomragged{\def\@textbottom{\vskip\z@ plus.0001fil
\global\let\@textbottom\relax}}
\pagestyle{headings}
\pagenumbering{arabic}
\if@referee
  \setlength{\textwidth}{0.75\textwidth}
  \def\baselinestretch{1.5}
  \let\AALogo\relax
  \onecolumn
  \AtEndDocument{\listofobjects}
\else
\if@onecolumn
\onecolumn
\else
  \twocolumn
\fi\fi
\sloppy
\flushbottom
\def\squareforqed{\hbox{\rlap{$\sqcap$}$\sqcup$}}
\def\sq{\ifmmode\squareforqed\else{\unskip\nobreak\hfil
\penalty50\hskip1em\null\nobreak\hfil\squareforqed
\parfillskip=0pt\finalhyphendemerits=0\endgraf}\fi}

\def\sun{\hbox{$\odot$}}
\def\la{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr<\cr\sim\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr
<\cr\sim\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
<\cr\sim\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
<\cr\sim\cr}}}}}
\def\ga{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr>\cr\sim\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr
>\cr\sim\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
>\cr\sim\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
>\cr\sim\cr}}}}}
\def\degr{\hbox{$^\circ$}}
\def\arcmin{\hbox{$^\prime$}}
\def\arcsec{\hbox{$^{\prime\prime}$}}
\def\utw{\smash{\rlap{\lower5pt\hbox{$\sim$}}}}
\def\udtw{\smash{\rlap{\lower6pt\hbox{$\approx$}}}}
\def\fd{\hbox{$.\!\!^{\rm d}$}}
\def\fh{\hbox{$.\!\!^{\rm h}$}}
\def\fm{\hbox{$.\!\!^{\rm m}$}}
\def\fs{\hbox{$.\!\!^{\rm s}$}}
\def\fdg{\hbox{$.\!\!^\circ$}}
\def\farcm{\hbox{$.\mkern-4mu^\prime$}}
\def\farcs{\hbox{$.\!\!^{\prime\prime}$}}
\def\fp{\hbox{$.\!\!^{\scriptscriptstyle\rm p}$}}
\def\cor{\mathrel{\mathchoice {\hbox{$\widehat=$}}{\hbox{$\widehat=$}}
{\hbox{$\scriptstyle\hat=$}}
{\hbox{$\scriptscriptstyle\hat=$}}}}
\def\sol{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr\sim\cr<\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr\sim\cr
<\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr\sim\cr
<\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
\sim\cr<\cr}}}}}
\def\sog{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr\sim\cr>\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr\sim\cr
>\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
\sim\cr>\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
\sim\cr>\cr}}}}}
\def\lse{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr<\cr\simeq\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr
<\cr\simeq\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
<\cr\simeq\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
<\cr\simeq\cr}}}}}
\def\gse{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr>\cr\simeq\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr
>\cr\simeq\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
>\cr\simeq\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
>\cr\simeq\cr}}}}}
\def\grole{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr>\cr\noalign{\vskip-1.5pt}<\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr
>\cr\noalign{\vskip-1.5pt}<\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
>\cr\noalign{\vskip-1pt}<\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
>\cr\noalign{\vskip-0.5pt}<\cr}}}}}
\def\leogr{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr<\cr\noalign{\vskip-1.5pt}>\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr
<\cr\noalign{\vskip-1.5pt}>\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
<\cr\noalign{\vskip-1pt}>\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
<\cr\noalign{\vskip-0.5pt}>\cr}}}}}
\def\loa{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr<\cr\approx\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr
<\cr\approx\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
<\cr\approx\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
<\cr\approx\cr}}}}}
\def\goa{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr>\cr\approx\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr
>\cr\approx\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr
>\cr\approx\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
>\cr\approx\cr}}}}}
\def\diameter{{\ifmmode\mathchoice
{\ooalign{\hfil\hbox{$\displaystyle/$}\hfil\crcr
{\hbox{$\displaystyle\mathchar"20D$}}}}
{\ooalign{\hfil\hbox{$\textstyle/$}\hfil\crcr
{\hbox{$\textstyle\mathchar"20D$}}}}
{\ooalign{\hfil\hbox{$\scriptstyle/$}\hfil\crcr
{\hbox{$\scriptstyle\mathchar"20D$}}}}
{\ooalign{\hfil\hbox{$\scriptscriptstyle/$}\hfil\crcr
{\hbox{$\scriptscriptstyle\mathchar"20D$}}}}
\else{\ooalign{\hfil/\hfil\crcr\mathhexbox20D}}%
\fi}}

\def\getsto{\mathrel{\mathchoice {\vcenter{\offinterlineskip
\halign{\hfil
$\displaystyle##$\hfil\cr\gets\cr\to\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr\gets
\cr\to\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr\gets
\cr\to\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
\gets\cr\to\cr}}}}}
\def\lid{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr<\cr\noalign{\vskip1.2pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr<\cr
\noalign{\vskip1.2pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr<\cr
\noalign{\vskip1pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
<\cr
\noalign{\vskip0.9pt}=\cr}}}}}
\def\gid{\mathrel{\mathchoice {\vcenter{\offinterlineskip\halign{\hfil
$\displaystyle##$\hfil\cr>\cr\noalign{\vskip1.2pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\textstyle##$\hfil\cr>\cr
\noalign{\vskip1.2pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptstyle##$\hfil\cr>\cr
\noalign{\vskip1pt}=\cr}}}
{\vcenter{\offinterlineskip\halign{\hfil$\scriptscriptstyle##$\hfil\cr
>\cr
\noalign{\vskip0.9pt}=\cr}}}}}
\def\bbbr{{\rm I\!R}} %reelle Zahlen
\def\bbbm{{\rm I\!M}}
\def\bbbn{{\rm I\!N}} %natuerliche Zahlen
\def\bbbf{{\rm I\!F}}
\def\bbbh{{\rm I\!H}}
\def\bbbk{{\rm I\!K}}
\def\bbbp{{\rm I\!P}}
\def\bbbone{{\mathchoice {\rm 1\mskip-4mu l} {\rm 1\mskip-4mu l}
{\rm 1\mskip-4.5mu l} {\rm 1\mskip-5mu l}}}
\def\bbbc{{\mathchoice {\setbox0=\hbox{$\displaystyle\rm C$}\hbox{\hbox
to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\textstyle\rm C$}\hbox{\hbox
to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptstyle\rm C$}\hbox{\hbox
to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptscriptstyle\rm C$}\hbox{\hbox
to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}}}
\def\bbbq{{\mathchoice {\setbox0=\hbox{$\displaystyle\rm
Q$}\hbox{\raise
0.05\ht0\hbox to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\textstyle\rm Q$}\hbox{\raise
0.05\ht0\hbox to0pt{\kern0.4\wd0\vrule height0.9\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptstyle\rm Q$}\hbox{\raise
0.05\ht0\hbox to0pt{\kern0.4\wd0\vrule height0.8\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptscriptstyle\rm Q$}\hbox{\raise
0.05\ht0\hbox to0pt{\kern0.4\wd0\vrule height0.8\ht0\hss}\box0}}}}
\def\bbbt{{\mathchoice {\setbox0=\hbox{$\displaystyle\rm
T$}\hbox{\hbox to0pt{\kern0.25\wd0\vrule height0.95\ht0\hss}\box0}}
{\setbox0=\hbox{$\textstyle\rm T$}\hbox{\hbox
to0pt{\kern0.25\wd0\vrule height0.95\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptstyle\rm T$}\hbox{\hbox
to0pt{\kern0.25\wd0\vrule height0.95\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptscriptstyle\rm T$}\hbox{\hbox
to0pt{\kern0.25\wd0\vrule height0.95\ht0\hss}\box0}}}}
\def\bbbs{{\mathchoice
{\setbox0=\hbox{$\displaystyle\rm S$}\hbox{\raise0.5\ht0\hbox
to0pt{\kern0.38\wd0\vrule height0.45\ht0\hss}\hbox
to0pt{\kern0.52\wd0\vrule height0.5\ht0\hss}\box0}}
{\setbox0=\hbox{$\textstyle \rm S$}\hbox{\raise0.5\ht0\hbox
to0pt{\kern0.38\wd0\vrule height0.45\ht0\hss}\hbox
to0pt{\kern0.52\wd0\vrule height0.5\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptstyle \rm S$}\hbox{\raise0.5\ht0\hbox
to0pt{\kern0.38\wd0\vrule height0.45\ht0\hss}\raise0.05\ht0\hbox
to0pt{\kern0.52\wd0\vrule height0.45\ht0\hss}\box0}}
{\setbox0=\hbox{$\scriptscriptstyle\rm S$}\hbox{\raise0.5\ht0\hbox
to0pt{\kern0.38\wd0\vrule height0.45\ht0\hss}\raise0.05\ht0\hbox
to0pt{\kern0.52\wd0\vrule height0.45\ht0\hss}\box0}}}}
\def\bbbz{{\mathchoice {\hbox{$\sf\textstyle Z\kern-0.4em Z$}}
{\hbox{$\sf\textstyle Z\kern-0.4em Z$}}
{\hbox{$\sf\scriptstyle Z\kern-0.3em Z$}}
{\hbox{$\sf\scriptscriptstyle Z\kern-0.2em Z$}}}}
\def\ts{\thinspace}
%
% Bibliography and bibfile
\def\aj{AJ}%
          % Astronomical Journal
\def\actaa{Acta Astron.}%
          % Acta Astronomica
\def\araa{ARA\&A}%
          % Annual Review of Astron and Astrophys
\def\apj{ApJ}%
          % Astrophysical Journal
\def\apjl{ApJ}%
          % Astrophysical Journal, Letters
\def\apjs{ApJS}%
          % Astrophysical Journal, Supplement
\def\ao{Appl.~Opt.}%
          % Applied Optics
\def\apss{Ap\&SS}%
          % Astrophysics and Space Science
\def\aap{A\&A}%
          % Astronomy and Astrophysics
\def\aapr{A\&A~Rev.}%
          % Astronomy and Astrophysics Reviews
\def\aaps{A\&AS}%
          % Astronomy and Astrophysics, Supplement
\def\azh{AZh}%
          % Astronomicheskii Zhurnal
\def\baas{BAAS}%
          % Bulletin of the AAS
\def\bac{Bull. astr. Inst. Czechosl.}%
          % Bulletin of the Astronomical Institutes of Czechoslovakia 
\def\caa{Chinese Astron. Astrophys.}%
          % Chinese Astronomy and Astrophysics
\def\cjaa{Chinese J. Astron. Astrophys.}%
          % Chinese Journal of Astronomy and Astrophysics
\def\icarus{Icarus}%
          % Icarus
\def\jcap{J. Cosmology Astropart. Phys.}%
          % Journal of Cosmology and Astroparticle Physics
\def\jrasc{JRASC}%
          % Journal of the RAS of Canada
\def\mnras{MNRAS}%
          % Monthly Notices of the RAS
\def\memras{MmRAS}%
          % Memoirs of the RAS
\def\na{New A}%
          % New Astronomy
\def\nar{New A Rev.}%
          % New Astronomy Review
\def\pasa{PASA}%
          % Publications of the Astron. Soc. of Australia
\def\pra{Phys.~Rev.~A}%
          % Physical Review A: General Physics
\def\prb{Phys.~Rev.~B}%
          % Physical Review B: Solid State
\def\prc{Phys.~Rev.~C}%
          % Physical Review C
\def\prd{Phys.~Rev.~D}%
          % Physical Review D
\def\pre{Phys.~Rev.~E}%
          % Physical Review E
\def\prl{Phys.~Rev.~Lett.}%
          % Physical Review Letters
\def\pasp{PASP}%
          % Publications of the ASP
\def\pasj{PASJ}%
          % Publications of the ASJ
\def\qjras{QJRAS}%
          % Quarterly Journal of the RAS
\def\rmxaa{Rev. Mexicana Astron. Astrofis.}%
          % Revista Mexicana de Astronomia y Astrofisica
\def\skytel{S\&T}%
          % Sky and Telescope
\def\solphys{Sol.~Phys.}%
          % Solar Physics
\def\sovast{Soviet~Ast.}%
          % Soviet Astronomy
\def\ssr{Space~Sci.~Rev.}%
          % Space Science Reviews
\def\zap{ZAp}%
          % Zeitschrift fuer Astrophysik
\def\nat{Nature}%
          % Nature
\def\iaucirc{IAU~Circ.}%
          % IAU Cirulars
\def\aplett{Astrophys.~Lett.}%
          % Astrophysics Letters
\def\apspr{Astrophys.~Space~Phys.~Res.}%
          % Astrophysics Space Physics Research
\def\bain{Bull.~Astron.~Inst.~Netherlands}%
          % Bulletin Astronomical Institute of the Netherlands
\def\fcp{Fund.~Cosmic~Phys.}%
          % Fundamental Cosmic Physics
\def\gca{Geochim.~Cosmochim.~Acta}%
          % Geochimica Cosmochimica Acta
\def\grl{Geophys.~Res.~Lett.}%
          % Geophysics Research Letters
\def\jcp{J.~Chem.~Phys.}%
          % Journal of Chemical Physics
\def\jgr{J.~Geophys.~Res.}%
          % Journal of Geophysics Research
\def\jqsrt{J.~Quant.~Spec.~Radiat.~Transf.}%
          % Journal of Quantitiative Spectroscopy and Radiative Trasfer
\def\memsai{Mem.~Soc.~Astron.~Italiana}%
          % Mem. Societa Astronomica Italiana
\def\nphysa{Nucl.~Phys.~A}%
          % Nuclear Physics A
\def\physrep{Phys.~Rep.}%
          % Physics Reports
\def\physscr{Phys.~Scr}%
          % Physica Scripta
\def\planss{Planet.~Space~Sci.}%
          % Planetary Space Science
\def\procspie{Proc.~SPIE}%
          % Proceedings of the SPIE
\let\astap=\aap
\let\apjlett=\apjl
\let\apjsupp=\apjs
\let\applopt=\ao
%
\uchyph=0
% 
\long\def\Online{\if@online\else\@onlinetrue
\clearpage
\def\ps@Onlheadings{%
    \let\@oddfoot\@empty\let\@evenfoot\@empty
    \def\@evenhead{\hfil\leftmark, {\it Online Material p~\thepage}\hfil}%
    \def\@oddhead{\hfil\rightmark, {\it Online Material p~\thepage}\hfil}%
    \let\@mkboth\@gobbletwo
    \let\sectionmark\@gobble
    \let\subsectionmark\@gobble
    }
\setcounter{page}{1}
\pagestyle{Onlheadings}
\twocolumn[\vspace*{4cm}\noindent{\sffamily\bfseries\Huge\centerline{Online Material}}]
\vfill
\clearpage
\fi}
\AtBeginDocument{\@ifpackageloaded{natbib}{\let\bibpreamble\relax\newcommand{\bibfont}{\tiny}%
\renewenvironment{thebibliography}[1]{%
 \bibsection\parindent \z@\bibpreamble\bibfont\list
   {\@biblabel{\arabic{NAT@ctr}}}{\@bibsetup{#1}%
    \setcounter{NAT@ctr}{0}}%
    \ifNAT@openbib
      \renewcommand\newblock{\par}
    \else
      \renewcommand\newblock{\hskip .11em \@plus.33em \@minus.07em}%
    \fi
    \sloppy\clubpenalty4000\widowpenalty4000
    \sfcode`\.=1000\relax
    \let\citeN\cite \let\shortcite\cite
    \let\citeasnoun\cite
 }{\if@longauth\kern6pt\hrule\kern6pt\institutename\@longauthfalse\fi\def\@noitemerr{%
  \PackageWarning{natbib}
     {Empty `thebibliography' environment}}%
  \endlist\vskip-\lastskip}}{}
\@ifpackageloaded{longtable}{%
\def\LT@makecaption#1#2#3{%
  \LT@mcol\LT@cols c{\hbox to\z@{\hss\parbox[t]\LTcapwidth{%
    \small\sbox\@tempboxa{#1{{\bfseries#2.} }#3}%
    \ifdim\wd\@tempboxa>\hsize
      #1{{\bfseries#2.} }#3%
    \else
      \hbox to\hsize{\box\@tempboxa\hfil}%
    \fi
    \endgraf\vskip\baselineskip}%
  \hss}}}}{}
}%AtBeginDocument
%
\endinput
%%
%% End of file `aa.cls'.
