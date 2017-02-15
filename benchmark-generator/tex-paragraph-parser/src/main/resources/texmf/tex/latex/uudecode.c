/* uudecode utility.
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

/*=====================================================================\
| uudecode [FILE ...]						       |
| 								       |
| Create the specified FILE, decoding as you go.  Used with uuencode.  |
\=====================================================================*/

#include <pwd.h>
#include "getopt.h"

struct passwd *getpwnam ();

static struct option longopts[] =
{
  { "version", 0, 0, 'v' },
  { "help", 0, 0, 'h' },
  { NULL, 0, 0, 0 }
};

static int decode _((const char *));
static void usage _((int));

/* The name this program was run with. */
const char *program_name;

/* Single character decode.  */
#define	DEC(c)	(((c) - ' ') & 077)

static int
decode (const char *filename)
{
  struct passwd *pw;
  register int n;
  register char ch, *p;
  int mode, n1;
  char buf[2 * BUFSIZ];
  char *outname;

  /* Search for header line.  */

  do
    {
      if (fgets (buf, sizeof (buf), stdin) == NULL)
	{
	  error (0, 0, "%s: no `begin' line", filename);
	  return 1;
	}
    }
  while (strncmp (buf, "begin ", 6) != 0)
    ;

  sscanf (buf, "begin %o %s", &mode, buf);

  /* Handle ~user/file format.  */

  if (buf[0] != '~')
    outname = buf;
  else
    {
      p = buf + 1;
      while (*p != '/')
	++p;
      if (*p == '\0')
	{
	  error (0, 0, "%s: illegal ~user", filename);
	  return 1;
	}
      *p++ = '\0';
      pw = getpwnam (buf + 1);
      if (pw == NULL)
	{
	  error (0, 0, "%s: no user `%s'", filename, buf + 1);
	  return 1;
	}
      n = strlen (pw->pw_dir);
      n1 = strlen (p);
      outname = (char *) alloca ((size_t) (n + n1 + 2));
      memcpy (outname + n + 1, p, (size_t) (n1 + 1));
      memcpy (outname, pw->pw_dir, (size_t) n);
      outname[n] = '/';
    }

  /* Create output file and set mode.  */

  if (freopen (outname, "w", stdout) == NULL
#if HAVE_FCHMOD      
      || fchmod (fileno (stdout), mode & (S_IRWXU | S_IRWXG | S_IRWXO))
#else
      || chmod (outname, mode & (S_IRWXU | S_IRWXG | S_IRWXO))
#endif
      )
    {
      error (0, errno, "%s: %s", outname, filename);
      return 1;
    }

  /* For each input line:  */

  while (1)
    {
      if (fgets (buf, sizeof(buf), stdin) == NULL)
	{
	  error (0, 0, "%s: short file", filename);
	  return 1;
	}
      p = buf;

      /* N is used to avoid writing out all the characters at the end of
	 the file.  */

      n = DEC (*p);
      if (n <= 0)
	break;
      for (++p; n > 0; p += 4, n -= 3)
	{
	  if (n >= 3)
	    {
	      ch = DEC (p[0]) << 2 | DEC (p[1]) >> 4;
	      putchar (ch);
	      ch = DEC (p[1]) << 4 | DEC (p[2]) >> 2;
	      putchar (ch);
	      ch = DEC (p[2]) << 6 | DEC (p[3]);
	      putchar (ch);
	    }
	  else
	    {
	      if (n >= 1)
		{
		  ch = DEC (p[0]) << 2 | DEC (p[1]) >> 4;
		  putchar (ch);
		}
	      if (n >= 2)
		{
		  ch = DEC (p[1]) << 4 | DEC (p[2]) >> 2;
		  putchar (ch);
		}
	    }
	}
    }

  if (fgets (buf, sizeof(buf), stdin) == NULL
      || strcmp (buf, "end\n"))
    {
      error (0, 0, "%s: no `end' line", filename);
      return 1;
    }

  return 0;
}

static void
usage (int status)
{
  if (status != 0)
    fprintf (stderr, "Try `%s --help' for more information.\n", program_name);
  else
    {
      printf ("\
Usage: %s [FILE]...\n", program_name);
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
  int exit_status;

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

  if (optind == argc)
    exit_status = decode ("stdin") == 0 ? EXIT_SUCCESS : EXIT_FAILURE;
  else
    {
      exit_status = EXIT_SUCCESS;
      do
	{
	  if (freopen (argv[optind], "r", stdin) != NULL)
	    {
	      if (decode (argv[optind]) != 0)
		exit_status = EXIT_FAILURE;
	    }
	  else
	    {
	      error (0, errno, "%s", argv[optind]);
	      exit_status = EXIT_FAILURE;
	    }
	  optind++;
	}
      while (optind < argc);
    }

  exit (exit_status);
}
