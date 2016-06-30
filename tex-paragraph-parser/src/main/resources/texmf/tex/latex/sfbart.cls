%%% ====================================================================
%%% @LaTeX-file{
%%%    filename  = "sfbart.cls",
%%%    version   = "1.1",
%%%    date      = "1997/02/11",
%%%    time      = "20:12:53 MET",
%%%    author    = "hans-peter kolb",
%%%    address   = "SFB340, Wilhelmstr.113, D-72074 Tuebingen",
%%%    email     = "kolb@sfs.nphil.uni-tuebingen.de (Internet)",
%%%    supported = "well...",
%%%    abstract  = "This is a LaTeX documentclass for the 
%%%                 ``Arbeitsberichte des SFB340'' series.".
%%% }
%%% ====================================================================

\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{sfbart}[1997/02/06 v1.1 SFB 340 Article Class]

\providecommand\@classname{sfbart}

\DeclareOption{a5paper}{\def\my@paper{\paperheight 297mm\paperwidth 210mm
   \textheight 42pc \textwidth 26.5pc}}    %was: 27pc
\DeclareOption{a5real}{\def\my@paper{\paperheight 210mm\paperwidth 148mm
   \textheight 42pc \textwidth 26.5pc}}    %was: 27pc
\DeclareOption{a4paper}{\def\my@paper{\paperheight 297mm\paperwidth 210mm
   \textheight 54.5pc \textwidth 33pc}}
\DeclareOption{letterpaper}{\def\my@paper{\paperheight 11in\paperwidth 8.5in}}
\DeclareOption{landscape}{\def\land@scape{\@tempdima\paperheight
  \paperheight\paperwidth \paperwidth\@tempdima}}
\DeclareOption{portrait}{\let\land@scape\relax}
\DeclareOption{twoup}{\message{%
    "*** N.B.: 'twoup' needs 'a5paper' to make sense..."}%
    \def\two@up{\input{2up}\source{\magstep0}{\paperwidth}{\paperheight}%
     \target{\magstep0}{297mm}{210mm}\special{landscape}\t@layout\nofiles}}
\DeclareOption{oneup}{\let\two@up\relax}
\DeclareOption{booklet}{\message{%
  "*** N.B.: 'booklet' needs 'twoup' to do anything..."}%
   \def\t@layout{\targetlayout{booklet}}}
\DeclareOption{nobooklet}{\let\t@layout\relax}
\newif\if@customtoc
\DeclareOption{customtoc}{\@customtoctrue}
\DeclareOption{nocustomtoc}{\@customtocfalse}
\newif\if@thmfix\@thmfixfalse
\DeclareOption{thmfix}{\@thmfixtrue}
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{amsart}}

\ExecuteOptions{a5paper,portrait,oneup,nobooklet,customtoc}
\ProcessOptions\relax

\LoadClass{amsart}[1996/10/24]

\my@paper\land@scape\two@up

%%%%%%%%%%%%%%%%%%%% For current class only
\newlength\tocindent\tocindent\parindent
\if@customtoc\relax\else
  \@tempdima\textwidth\addtolength{\@tempdima}{-2\tocindent}
   \let\save@starttoc\@starttoc
    \def\@starttoc#1#2{%
        \@tempdima\textwidth\addtolength{\@tempdima}{-2\tocindent}%
         \noindent\hskip\tocindent\begin{minipage}{\@tempdima}%
          \save@starttoc{#1}{#2}\end{minipage}}
\fi
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\def\enddoc@text{\ifx\@empty\@translators \else\par\bigskip\@settranslators\fi
  \ifx\@empty\addresses \else\null\par\vfill\@setaddresses\fi%
}
%
\def\@setaddresses{\par
  \nobreak \begingroup
\footnotesize
  \def\author##1{\nobreak\addvspace\bigskipamount}%
  \def\\{\unskip, \ignorespaces}%
  \interlinepenalty\@M
  \def\address##1##2{\begingroup
     \par\addvspace\bigskipamount\noindent
     \@ifnotempty{##1}{{\itshape\ignorespaces##1\unskip}\/: }%
     {\scshape\ignorespaces##2}\par\endgroup}%
  \def\curraddr##1##2{\begingroup
     \@ifnotempty{##2}{\nobreak\noindent{\itshape Current address}%
       \@ifnotempty{##1}{ (\ignorespaces##1\unskip)}\/:\space
       ##2\par\endgroup}}%
  \def\email##1##2{\begingroup
     \@ifnotempty{##2}{\nobreak\noindent{\itshape E-mail address}%
       \@ifnotempty{##1}{ (\ignorespaces##1\unskip)}\/:\space
       \ttfamily##2\par\endgroup}}%
  \def\urladdr##1##2{\begingroup
     \@ifnotempty{##2}{\nobreak\indent{\itshape URL}%
       \@ifnotempty{##1}{ (\ignorespaces##1\unskip)}\/:\space
       \ttfamily##2\par\endgroup}}%
  \addresses
  \endgroup
}
%
\def\ps@firstpage{\ps@plain
  \def\@oddfoot{\normalfont\scriptsize \hfil\thepage\hfil
     \global\topskip\normaltopskip}%
  \let\@evenfoot\@oddfoot
  \def\@oddhead{\series@logo\hss}%
  \let\@evenhead\@oddhead % in case an article starts on a left-hand page
}
%
\def\series@logo{\begin{tabular}{l}\unskip%
   {\usefont{OT1}{cmss}{sbc}{n}\fontsize{10}{6\p@}\selectfont%
   Arbeitspapiere des SFB  340}\\[-2pt] \unskip%
   {\usefont{OT1}{cmss}{m}{n}\fontsize{8}{6\p@}\selectfont%
   Bericht Nr.\,\currentissue, \currentmonth~\currentyear}
   \end{tabular}}
\def\copyrightyearmodC{??}
\def\copyrightyear{19\copyrightyearmodC}
\def\ISSN{0947-6954}
\def\currentvolume{00}
\def\currentyear{\copyrightyear}
\def\currentissue{00}
\def\currentmonth{Xxxx}
\newinsert\copyins
\skip\copyins=1.9pc
\count\copyins=1000 % magnification factor, 1000 = 100%
\dimen\copyins=.5\textheight % maximum allowed per page
\def\cpright{Die AutorInnen}
\def\@setcopyright{%
\ifx\@empty\serieslogo@
   \else
   \insert\copyins{\hsize\textwidth
     \parfillskip\z@ \leftskip\z@\@plus.9\textwidth
     \fontsize{8}{6\p@}\usefont{OT1}{cmss}{m}{n}\selectfont
     \everypar{}%
     \vskip-\skip\copyins \nointerlineskip
     \noindent\vrule\@width\z@\@height\skip\copyins
     ISSN \ISSN/\copyrightyearmodC\break
     \text{\copyright}\,\copyrightyear\ \cpright\par
     \kern\z@}%
 \fi
  }
%
\renewenvironment{abstract}{%
  \ifx\maketitle\relax
    \ClassWarning{\@classname}{Abstract should precede
      \protect\maketitle\space in AMS documentclasses; reported}%
  \fi
  \global\setbox\abstractbox=\vtop \bgroup
  \normalfont\Small
  \Small
  \list{}{\labelwidth\z@
    \leftmargin\normalparindent \rightmargin\leftmargin
    \listparindent\normalparindent \itemindent\z@
    \parsep\z@ \@plus\p@
    \let\fullwidthdisplay\relax
    }%
  \item[\hskip\labelsep\scshape\abstractname.]%
}{%
  \endlist\egroup
  \ifx\@setabstract\relax \@setabstracta \fi
}
%
\def\@setabstracta{%
  \ifvoid\abstractbox
  \else
    \skip@30\p@ \advance\skip@-\lastskip
    \advance\skip@-\baselineskip \vskip\skip@
    \box\abstractbox
    \prevdepth\z@ % because \abstractbox is a vtop
    % (urspr"unglich: \vskip -14\p@ statt \prevdepth\z@)
  \fi
}
%
\renewcommand\thepart          {\Roman{part}}
%
\def\specialsection{\@startsection{section}{1}%
   \z@{\linespacing\@plus\linespacing}{.5\linespacing}%
   {\normalsize\itshape\centering}}
\def\section{\@startsection{section}{1}%
   \z@{\linespacing\@plus\linespacing}{.5\linespacing}%
   {\normalfont\scshape\centering}}
\def\subsection{\@startsection{subsection}{2}%
   \z@{.7\linespacing\@plus.5\linespacing}{-.7em}%
   {\normalfont\bfseries}}
\def\subsubsection{\@startsection{subsubsection}{3}%
   \z@{.5\linespacing\@plus.5\linespacing}{-.7em}%
   {\normalfont\itshape}}
\def\paragraph{\@startsection{paragraph}{4}%
   \z@{.5\linespacing\@plus.3\linespacing}{-.5em}%
   {\normalfont\itshape}}
\def\subparagraph{\@startsection{subparagraph}{5}%
   \z@{.5\linespacing\@plus.3\linespacing}{-.5em}%
   {\normalfont\itshape}}
%
\AtBeginDocument{%
  \@for\@tempa:=-1,0,1,2,3,4,5\do{%
    \@ifundefined{r@tocindent\@tempa}{%
      \@xp\gdef\csname r@tocindent\@tempa\endcsname{0pt}}{}%
  }%
}
\def\@writetocindents{%
  \begingroup
  \@for\@tempa:=-1,0,1,2,3,4,5\do{%
    \immediate\write\@auxout{%
      \string\newlabel{tocindent\@tempa}{%
        \csname r@tocindent\@tempa\endcsname}}%
}%
\endgroup}
%
\def\@tocline#1#2#3#4#5#6#7{\relax
  \ifnum #1>\c@tocdepth % then omit
  \else
    \par \addpenalty\@secpenalty\addvspace{#2}%
    \begingroup \hyphenpenalty\@M
    \@ifempty{#4}{%
      \@tempdima\csname r@tocindent\number#1\endcsname\relax
    }{%
      \@tempdima#4\relax
    }%
    \if@customtoc\parindent\z@\leftskip0pt\else\parindent\z@\leftskip#3\fi%
    \relax \advance\leftskip\@tempdima\relax
    \rightskip\@pnumwidth plus1em \parfillskip-\@pnumwidth
    \if@customtoc
     \leavevmode\hskip-\@tempdima
     \centerline{#5 #6\relax\quad{\upshape #7}}\par
    \else
     #5\leavevmode\hskip-\@tempdima #6\relax
     \hfil\hbox to\@pnumwidth{\@tocpagenum{#7}}\par
    \fi
      \nobreak
      \endgroup
\fi}
%
\if@customtoc
  \def\l@section{\@tocline{1}{1.2ex}{}{}{}}
  \def\my@dot{}
  \def\contentsname{}
\else
  \def\l@section{\@tocline{1}{0pt}{0pt}{}{}}
  \def\my@dot{.}
\fi
\renewcommand{\tocsection}[3]{%
  \indentlabel{\@ifnotempty{#2}{\ignorespaces{\upshape #1 #2\my@dot}\quad}}#3}
  \let\tocsubsection\tocsection
  \let\tocsubsubsection\tocsection
  \let\tocparagraph\tocsection
  \let\tocsubparagraph\tocsubsection
  \let\tocpart\tocsection
  \let\tocchapter\tocsection
  \let\tocappendix\tocchapter
\if@customtoc
  \def\l@subsection{\@tocline{2}{2pt}{}{5pc}{\itshape}}
  \def\l@subsubsection{\@tocline{3}{2pt}{}{5pc}{\itshape}}
  \def\l@paragraph{\@tocline{4}{2pt}{}{5pc}{\itshape}}
  \def\l@subparagraph{\@tocline{5}{2pt}{}{5pc}{\itshape}}
  \def\l@part{\@tocline{-1}{1.2ex plus1pt}{}{}{\scshape}}
  \def\l@chapter{\@tocline{0}{1.2ex plus1pt}{}{}{\bfseries}}
  \def\l@figure{\@tocline{0}{3pt plus2pt}{}{}{}}
\else
  \def\l@subsection{\@tocline{2}{0pt}{.7em}{5pc}{}}
  \def\l@subsubsection{\@tocline{3}{0pt}{1.4em}{7pc}{}}
  \def\l@paragraph{\@tocline{4}{0pt}{1.4em}{9pc}{}}
  \def\l@subparagraph{\@tocline{5}{0pt}{1.4em}{9pc}{}}
  \def\l@part{\@tocline{-1}{12pt plus2pt}{0pt}{}{\hfil\scshape}}
  \def\l@chapter{\@tocline{0}{8pt plus1pt}{0pt}{}{}}
  \def\l@figure{\@tocline{0}{3pt plus2pt}{0pt}{}{}}
\fi
\let\l@table=\l@figure
%
\renewcommand{\descriptionlabel}[1]{\hspace\labelsep \itshape\mdseries #1:}
%
\if@thmfix
\def\th@plain{%
  \theoremheadfont{\scshape}% heading font small caps
  \theoremnotefont{\mdseries\upshape}%
  \theorempreskipamount.5\baselineskip\@plus.2\baselineskip
                                    \@minus.2\baselineskip
  \theorempostskipamount\theorempreskipamount
  \itshape
}
\def\th@definition{%
  \theoremheadfont{\bfseries}% heading font bold
  \theoremnotefont{\mdseries\upshape}%
  \theorempreskipamount.5\baselineskip\@plus.2\baselineskip
                                    \@minus.2\baselineskip
  \theorempostskipamount\theorempreskipamount
  \upshape
}
\def\th@remark{%
  \theoremheadfont{\itshape}% heading font italics
  \theoremnotefont{\mdseries\upshape}%
  \theorempreskipamount.5\baselineskip\@plus.2\baselineskip
                                    \@minus.2\baselineskip
  \theorempostskipamount\theorempreskipamount
  \upshape
}
\else
\def\th@plain{%
  \thm@headfont{\scshape}% heading font small caps
  \let\thmhead\thmhead@plain \let\swappedhead\swappedhead@plain
  \thm@preskip.5\baselineskip\@plus.2\baselineskip
                                    \@minus.2\baselineskip
  \thm@postskip\thm@preskip
  \itshape
}
\def\th@definition{%
  \thm@headfont{\bfseries}% heading font boldface
  \let\thmhead\thmhead@plain \let\swappedhead\swappedhead@plain
  \thm@preskip.5\baselineskip\@plus.2\baselineskip
                                    \@minus.2\baselineskip
  \thm@postskip\thm@preskip
  \upshape
}
\def\th@remark{%
  \thm@headfont{\itshape}% heading font italics
  \let\thmhead\thmhead@plain \let\swappedhead\swappedhead@plain
  \thm@preskip.5\baselineskip\@plus.2\baselineskip
                                    \@minus.2\baselineskip
  \thm@postskip\thm@preskip
  \upshape
}
\fi
\renewenvironment{proof}[1][\proofname]{\par
  \normalfont
  \topsep6\p@\@plus6\p@ \trivlist
  \item[\hskip\labelsep\itshape
    #1\@addpunct{.}]\ignorespaces
}{%
  \qed\endtrivlist
}
\if@compatibility
\let\@newpf\proof
\fi

\calclayout

\endinput %
