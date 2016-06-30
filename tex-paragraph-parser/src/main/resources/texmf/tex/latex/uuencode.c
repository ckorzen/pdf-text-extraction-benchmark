/* uuencode utility.
   Copyright (C) 1994 Free Software Foundation, Inc.

   This product is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   This product is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this product; see the file COPYING.  If not, write to
   the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.  */

/* Copyright (c) 1983 Regents of the University of California.
   All rights reserved.
   
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   are met:
   1. Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
   3. All advertising materials mentioning features or use of this software
      must display the following acknowledgement:
	 This product includes software developed by the University of
	 California, Berkeley and its contributors.
   4. Neither the name of the University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
   ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
   ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
   FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
   OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
   LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
   OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.  */

/* Reworked to GNU style by Ian Lance Taylor, ian@airs.com, August 93.  */

#include "system.h"

/*=======================================================\
| uuencode [INPUT] OUTPUT				 |
| 							 |
| Encode a file so it can be mailed to a remote system.	 |
\=======================================================*/

#include "getopt.h"

#define	RW (S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH)

static struct option longopts[] =
{
  { "version", 0, 0, 'v' },
  { "help", 0, 0, 'h' },
  { NULL, 0, 0, 0 }
};

static void encode _((void));
static void usage _((int));

/* The name this program was run with. */
const char *program_name;

/* ENC is the basic 1 character encoding function to make a char printing.  */
#define	ENC(c) ((c) ? ((c) & 077) + ' ': '`')

/*------------------------------------------------.
| Copy from IN to OUT, encoding as you go along.  |
`------------------------------------------------*/

static void
encode (void)
{
  register int ch, n;
  register char *p;
  char buf[80];

  while ((n = fread (buf, 1, 45, stdin)) != 0)
    {
      ch = ENC (n);
      if (putchar (ch) == EOF)
	break;
      for (p = buf; n > 0; n -= 3, p += 3)
	{
	  ch = *p >> 2;
	  ch = ENC (ch);
	  if (putchar (ch) == EOF)
	    break;
	  ch = ((*p << 4) & 060) | ((p[1] >> 4) & 017);
	  ch = ENC (ch);
	  if (putchar (ch) == EOF)
	    break;
	  ch = ((p[1] << 2) & 074) | ((p[2] >> 6) & 03);
	  ch = ENC (ch);
	  if (putchar (ch) == EOF)
	    break;
	  ch = p[2] & 077;
	  ch = ENC (ch);
	  if (putchar (ch) == EOF)
	    break;
	}
      if (putchar ('\n') == EOF)
	break;
    }
  if (ferror (stdin))
    error (EXIT_FAILURE, 0, "read error");
  ch = ENC ('\0');
  putchar (ch);
  putchar ('\n');
}

static void
usage (int status)
{
  if (status != 0)
    fprintf (stderr, "Try `%s --help' for more information.\n", program_name);
  else
    {
      printf ("\
Usage: %s [INFILE] REMOTEFILE\n", program_name);
      printf ("\n\
  -h, --help      display this help and exit\n\
  -v, --version   output version information and exit\n");
    }
  exit (status);
}

int
main (int argc, char *const *argv)
{
  int opt;
  struct stat sb;
  int mode;

  program_name = argv[0];

  while (opt = getopt_long (argc, argv, "hv", longopts, (int *) NULL),
	 opt != EOF)
    {
      switch (opt)
	{
	case 'h':
	  usage (EXIT_SUCCESS);

	case 'v':
	  printf ("GNU %s %s\n", PRODUCT, VERSION);
	  exit (EXIT_SUCCESS);

	case 0:
	  break;

	default:
	  usage (EXIT_FAILURE);
	}
    }

  switch (argc - optind)
    {
    case 2:

      /* Optional first argument is input file.  */

      if (!freopen (argv[optind], "r", stdin) || fstat (fileno (stdin), &sb))
	error (EXIT_FAILURE, errno, "%s", argv[optind]);
      mode = sb.st_mode & (S_IRWXU | S_IRWXG | S_IRWXO);
      optind++;
      break;

    case 1:
      mode = RW & ~umask (RW);
      break;

    case 0:
    default:
      usage (EXIT_FAILURE);
    }

#if S_IRWXU != 0700
choke me - Must translate mode argument
#endif

  printf ("begin %o %s\n", mode, argv[optind]);
  encode ();
  printf ("end\n");
  if (ferror (stdout))
    error (EXIT_FAILURE, 0, "write error");
  exit (EXIT_SUCCESS);
}
