/*
 * (C) 1991, 1990, 1989 by Adobe Systems Incorporated. All rights reserved.
 *
 * This file may be freely copied and redistributed as long as:
 *   1) This entire notice continues to be included in the file,
 *   2) If the file has been modified in any way, a notice of such
 *      modification is conspicuously indicated.
 *
 * PostScript, Display PostScript, and Adobe are registered trademarks of
 * Adobe Systems Incorporated.
 *
 * ************************************************************************
 * THE INFORMATION BELOW IS FURNISHED AS IS, IS SUBJECT TO CHANGE WITHOUT
 * NOTICE, AND SHOULD NOT BE CONSTRUED AS A COMMITMENT BY ADOBE SYSTEMS
 * INCORPORATED. ADOBE SYSTEMS INCORPORATED ASSUMES NO RESPONSIBILITY OR
 * LIABILITY FOR ANY ERRORS OR INACCURACIES, MAKES NO WARRANTY OF ANY
 * KIND (EXPRESS, IMPLIED OR STATUTORY) WITH RESPECT TO THIS INFORMATION,
 * AND EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR PARTICULAR PURPOSES AND NONINFINGEMENT OF THIRD PARTY RIGHTS.
 * ************************************************************************
 */

/*
 * PSlinewrap.c
 *
 *  EDIT HISTORY:
      Glenn Reid <ps-file-person@adobe.com> Tue Jun  6 14:02:26 1989
        -original version
      David  Osborne <cczdao@uk.ac.nott.clan> Tue Oct 17 11:22:57 BST 1989
        - for vms, create new generation of output file
        - change second call to strncpy() to strcpy()
      Dave Love <d.love@dl.ac.uk> Wed Sep  5 14:16:05 1990
        - convert any non-printing characters in strings to \ notation
        - warn about possible chopping errors and check for i/p
              buffer overflow
        - more comprehensive argument checking
        - cleverer breaking done (before special characters)
      Don Markuson <dmm@tiger1.prime.com> Mon Apr  13 18:32:22 EST 1992
        - fixed bug in Dave Love's cleverer breaking before special characters
          in which extra (potentially) random char was inserted at break
        - pointer comparison to 0 cast to be (FILE *) 0 to avoid compiler warns

    SYNOPSIS:
	PSlinewrap [ -<longerthan> ] [ <filename> ]

    EXAMPLES:
	PSlinewrap myfile.ps
	PSlinewrap -255 myfile.ps > tmp

    EXPLANATION:
    Wraps PostScript lines longer than the supplied limit by breaking
    the line.  The default line limit is 78, to conform to standard
    80-column terminals and the programs that line-wrap for them.
    The line breaks according to the following algorithm:

	* At a space, if there is one on the line somewhere.
	* with the \ notation, if it is in the middle of a string body.
	* before the last occurrence of a character in the set "<>{}[]/"
          if there is no space.
	* at the cutoff point otherwise. if it is a comment a
          continuation is done. otherwise we issue a warning. this
	  should only occur if a name is as long as the line limit.

 */

#include <stdio.h>

#define	DEFAULT 78
#define TRUE 1
#define FALSE 0
#define BUFFLEN 1024

int	maxcolumn;	/* column to count lines longer than (good English!) */
long	counter;	/* byte counter */
long	linecount;	/* line number */
long	level;		/* level of string paren nesting */
long	hexlevel=0;	/* level of hex string paren nesting */
long    outlines=1;	/* number of current output line */
long    inlines=1;	/* number of current input line */
char	buff[BUFFLEN];	/* char buffer */
char	tmpbuff[BUFFLEN];	/* char buffer */
short	comment;	/* boolean (is it a PS comment? ) */

/*************************** main *************************/

main ( argc, argv ) int argc; char *argv[];
{
    FILE *inputfd;
    short quit;
    register int ch, lastchar;
    register int position;

    quit = FALSE;
    linecount = 0;
    counter = 0;
    level = 0;
    maxcolumn = DEFAULT;
    comment = FALSE;

  /* check command-line arguments */
    switch ( argc ) {
    case 0:
    case 1: {
	inputfd = stdin;
	break;
    }
    case 2:
	if ( argv[1][0] == '-' ) {
	    sscanf ( argv[1], "-%d", &maxcolumn );
	    if ( maxcolumn < 0 ) maxcolumn = -maxcolumn;
	    inputfd = stdin;
	    break;
	}
    case 3: {
      if (argc == 3) /* no fall-through */
	if ( argv[1][0] == '-' ) {
	    sscanf ( argv[1], "-%d", &maxcolumn );
	    if ( maxcolumn < 0 ) maxcolumn = -maxcolumn;
	} else {
	    /* fprintf ( stderr, "Invalid switch: %s\n", argv[1] ); */
	  fprintf(stderr,"Usage: %s [-<longerthan>] [file]\n",argv[0]);
	    exit ( 1 );
	}
	inputfd = fopen ( argv[argc-1], "r" );
	if ( inputfd <= (FILE *) 0 ) { /*DMM BUGFIX*/
	    fprintf ( stderr, "Cannot open %s.\n", argv[argc-1] );
	    exit ( 1 );
	}
#ifdef vms			/* ensure stdout goes to new generation of
				   input file  ---dao */
	if (freopen(argv[argc-1], "w", stdout) == NULL) {
	    fprintf ( stderr, "Cannot open new generation of %s for output.\n",
		argv[argc-1] );
	    exit ( 1 );
	}
#endif /*vms*/
	break;
      }
    default: {
	  fprintf(stderr,"Usage: %s [-<longerthan>] [file]\n",argv[0]);
	  exit(1);
	}
    } /* switch */

  /* main loop of program */
    fprintf (stderr,"Checking for lines longer than %d bytes...\n",maxcolumn);
    lastchar = ' ';
    ch = getc ( inputfd );
    while ( ch != EOF /* && !quit*/ ) {
	if ( ch != '\n' ) {
	    if (counter >= BUFFLEN) {
	      fprintf(stderr,"Input buffer length exceeded on line %d --
%%sorry.\n",inlines);
	      exit(1);
	    }
	    buff[counter] = ch;
	    buff[counter+1] = 0;
	    /* check for non-printing characters (only inside strings)*/
	    if (level != 0 && (ch>127 || ch<32)) {
	      if (counter+3 >= BUFFLEN) {
		fprintf(stderr,"Input buffer length exceeded on line %d --
%%sorry.\n",inlines);
		exit(1);
	      }
	      else {
		sprintf(buff+counter,"\\%03.3o\0",ch);
		counter = counter+3;
	      }
	    }
	    counter++;
	    if (!comment) {
	      if ( lastchar != '\\' && ch == '(' ) level++;
	      if ( lastchar != '\\' && ch == ')' ) level--;
	      if (ch == '<' && level<=0) hexlevel++;
	      if (ch == '>' && level<=0) hexlevel--;
	      if ( ch == '%' && level == 0 ) comment = TRUE;
	    }
	    if ( counter >= maxcolumn ) {
		/* search backward and only break the line at a space */
		for ( position=counter; position >= 0; position-- ) {
		    if ( buff[position] == ' ' ) {
			strncpy ( tmpbuff, buff, position + 1 );
			tmpbuff [ position ] = 0;	/* nix the space */
			printf ( "%s", tmpbuff );
			if ( level != 0 ) printf ( "\\" );
			printf ( "\n" );
			outlines++;
			if ( comment ) printf ( "%%%%+ " );
			break;
		    }
		}
		if (position <= 0 && !comment)
		  /* search backward and try to break before a special
		     character. `%' is already taken care of and `(' is
		     awkward since it could be escaped */
		  for ( position=counter-1; /* -1 to avoid matching terminating null */
		       position >= 0; position-- ) {
		    if (strchr("<>{}[]/",buff[position]) != NULL) {
		      strncpy ( tmpbuff, buff, position );
		      tmpbuff [ position ] = 0; /*DMM BUGFIX was position+1 ??*/
		      printf ( "%s", tmpbuff );
		      if ( level != 0 ) printf ( "\\" );
		      printf ( "\n" );
		      outlines++;
		      break;
		    }
		  }
		/* if no spaces are found, break the line and hope */
		if ( position <= 0 ) {
		    if ( level <= 0 ) {
			printf ( "%s\n", buff );
			if (!comment && hexlevel <= 0)
			  fprintf(stderr,"Warning: no good break found -- check o/p line
%d.\n",outlines);
		    } else {
			printf ( "%s\\\n", buff );
		    }
		    if ( comment ) printf ( "%%%%+ " );
		    outlines++;
		    counter = 0;
		    buff[counter] = 0;
		} else {
		    strcpy ( tmpbuff, (char *)(buff+position) ); /* was strncpy ---dao */
		    strcpy ( buff, tmpbuff );
		    counter = strlen ( buff );
		    buff[counter] = 0;
		}
		linecount++;
	    }
	} else { /* read newline */
	    comment = FALSE;
	    if ( counter ) {
		printf ( "%s\n", buff );
		inlines++;
		outlines++;
	    }
	    counter = 0;
	}
	if ( lastchar == '\\' && ch == '\\' ) ch = ' ';
	lastchar = ch;
	ch = getc ( inputfd );
    } /* while */
    if ( linecount ) {
	fprintf ( stderr, "PSlinewrap: %d lines wrapped.\n", linecount );
    }

} /* main */


