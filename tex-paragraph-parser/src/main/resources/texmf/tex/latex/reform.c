/*       reform.c   (PG, 8/91, 3/92, 5/92, 10/92)
   this program reformats .tex files to be sent out via electronic mail.
   lines of more than width=80 characters get carriage returns inserted at
   space or tab characters (in the absence of which the line is included 
   anyway with a comment that it cannot be broken automatically),
   and removes any trailing spaces or tab characters from lines.
   it automatically inserts % signs when splitting off the trailing end
   of lines already commented out.
   it also inserts {} before any occurrences of `From' or `~' at beginning
   of line, and before `.' if alone on a line, and converts all appearances 
   of `>From' to ` From' (this may be undesirable?).
            ---------------------------------------------------------
   Usage: compile this c program and move the compiled version to a 
   directory on your path and call it `reform' (e.g.: cc reform.c -o reform)
   then the command
                          cat file.tex | reform > newfile.tex
            or            reform < file.tex > newfile.tex
   will save the reformatted version of file.tex as newfile.tex.
   alternatively          alias reformat 'cat \!$ | reform > new\!$'
            or            alias reformat 'reform < \!$ > new\!$'
   in .cshrc allows the equivalent command
                          reformat file.tex
*/
#define MAXLINE 10000
#define width 80
#include <stdio.h>

main()
{char line[MAXLINE]; int j;
while((j=getline(line,MAXLINE))>0)
{while (--j >= 0)
    if (line[j] != ' ' && line[j] != '\t' && line[j] != '\n') break;
 line[j+1]='\n'; line[j+2]='\0'; cutline(line);} }

getline(s,lim)
char s[]; int lim;
{int c,i=0;
   while (i < lim-1 && (c=getchar()) != EOF && c != '\n') s[i++]= c;
   if (c=='\n') s[i++]=c; else if (c != EOF) 
         {fprintf(stderr,"warning: line in excess of %i char\n",MAXLINE);
       printf("%%%% WARNING: CORRUPTED BY REFORM, LINE IN EXCESS OF %i CHAR\n",
                     MAXLINE);}
   s[i]='\0'; return(i);}

cutline(line)
char line[];
{int i=0,j=-1,m=-1,k,n; char aux[MAXLINE];
 while ((k=index(line,">From")) > -1) line[k] = ' ';
 if (index(line,"From") == 0 || index(line,"~") == 0 ||
       (index(line,".") == 0 && strlen(line) == 2) )
          {strcpy(aux,"{}"); strcat(aux,line); strcpy(line,aux);}
 if (strlen(line)<=width) {printf("%s",line); return;}
 k=width; while (--k>=0)
           if (line[k] == ' ' || line[k] == '\t') break;
 if (k<0) 
   {fprintf(stderr,"warning: a line cannot be broken before %i char\n", width);
    printf("%%%% FOLLOWING LINE CANNOT BE BROKEN BEFORE %i CHAR\n",width);
    printf("%s",line); return;}
 while ((m=index(&line[j+1],"%")) >= 0) 
  {j=j+m+1; n=0; while (j-n-1>=0 && line[j-n-1]=='\\') n++; if (n%2==0) break;}
 if (((m >= 0) && (j <= k))) {strcpy(aux,"%%"); i=2;}
 j=k+1; while ((aux[i++] = line[j++]) != '\0');
 while (--k>=0) if (line[k] != ' ' && line[k] != '\t') break;
 line[k+1]='\n'; line[k+2]='\0'; printf("%s",line); cutline(aux); return;}

strcat(s,t)
char s[],t[];
{int i=0,j=0; while (s[i] != '\0') i++; while ((s[i++] = t[j++]) != '\0');}

index(s,t)
char s[], t[];
{int i,j,k;
 for (i=0; s[i] != '\0'; i++)
  {for (j=i, k=0; t[k] !='\0' && s[j] == t[k] ; j++, k++) ;
       if (t[k]=='\0')  return(i); }
 return(-1);}
