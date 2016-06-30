$! unpack.com  
$! VMS command procedure that unpacks files created by the 
$! arXiv.org shell script "uufiles".
$! G. Bodwin  (gtb@hep.anl.gov) 22 March 1996
$! Note: this command procedure assumes that tar, 
$! gzip, and uudecode have been installed.
$!
$! Invoke unpack.com by issuing the command
$!
$! @unpack
$!
$! Local definitions.  Edit to point to executables on the local system
$! for tar, compress, gzip, uuencode, and uudecode. 
$! If desired, the local definitions can be extracted from this file and
$! included in the user's login.com file.
$!
$! Start of main command procedure
$!
$! Temporarily override user symbol assignments
$!
$ edit:=edit
$ delete:=delete
$!
$ write sys$output ""
$ write sys$output "TYPE NAME OF FILE TO BE UNPACKED"
$ inquire input "Input file"
$ copy 'input' gtbxxxtmp.uu
$!
$! Use the tpu editor to replace periods with dashes in order to make the 
$! filename in the BEGIN line of the uuencoded file VMS compatible.
$! If there are 2 or more periods in the filename, each period except
$! the next-to-last one is replaced with dash. If there is only 1 period in
$! the filename, a dash is appended to the period. 
$!
$! First create a tpu command file 
$!
$ open/write commandfile xxxcommand.tpu
$ goto begintpucommand
$ endtpucommand:
$ close commandfile
$!
$! Now invoke tpu
$! veri = f$verify(0'replace_verify')
$ MSG = F$ENVIRONMENT("MESSAGE")
$ on error then goto errorexit
$ on control_y then goto errorexit
$ SET MESSAGE /NOIDENT/NOTEXT/NOSEVERITY/NOFACILITY
$ DEFINE/USER SYS$INPUT SYS$COMMAND
$!
$ EDIT/TPU/NOINIT/NOSECTION/COMMAND=xxxcommand.tpu/NODISPLAY gtbxxxtmp.uu
$! veri = f$verify(veri)
$ SET MESSAGE 'MSG'
$ delete xxxcommand.tpu.*
$!
$! Find filename, filetype, suffix in "begin" line of input file
$!
$ open/read input_file gtbxxxtmp.uu
$ 10$: read input_file record
$ if f$length(record) .eq. 0 then goto 10$
$ if f$locate("begin",record) .ne. 0 then goto 10$
$ close input_file
$ endrecord=f$element(2," ",record)
$ name=f$element(0,".",endrecord)
$ endrecord2=f$element(1,".",endrecord)
$ filetype=f$element(0,"-",endrecord2)
$ suffix=f$element(1,"-",endrecord2)
$!
$! Decode, decompress, untar (if needed). Use gzip to decompress since
$! it handles both -Z and -gz files and is faster than compress. 
$!
$ uudecode gtbxxxtmp.uu
$ delete gtbxxxtmp.uu.*
$ gzip -d 'name'.'filetype'-'suffix'
$ if filetype .eqs. "tar"
$ then 
$ tar -xvf 'name'.tar
$ delete 'name'.tar.*
$ else
$ write sys$output ""
$ write sys$output "Unpacking ''name'.''filetype'"
$ endif
$!
$! Delete input file?
$!
$ write sys$output ""
$ inquire check_delete "DELETE ''input'? (Y/N, default is N)"
$ if .not. check_delete 
$ then
$ else
$ rename 'input' []gtbxxxtmp.uu
$ delete gtbxxxtmp.uu.*
$ endif
$ exit
$ errorexit:
$ SET MESSAGE 'MSG'
$ exit
$!
$! Start of TPU command file
$!
$ begintpucommand:
$ write commandfile "PROCEDURE substitute(target,replacement,s_range)"
$ write commandfile "    ON_ERROR"
$ write commandfile "       RETURN;"
$ write commandfile "    ENDON_ERROR"
$ write commandfile "    POSITION(BEGINNING_OF (s_range));"
$ write commandfile "    LOOP"
$ write commandfile "       POSITION (SEARCH (target,FORWARD,EXACT));"
$ write commandfile-
  "       IF CURRENT_OFFSET > GET_INFO (END_OF (s_range),"""offset""")"
$ write commandfile "       THEN"
$ write commandfile "       RETURN;"
$ write commandfile "       ELSE"
$ write commandfile "       ERASE_CHARACTER (LENGTH(target));"
$ write commandfile "       COPY_TEXT (replacement);"
$ write commandfile "       ENDIF;"
$ write commandfile "    ENDLOOP;"
$ write commandfile "ENDPROCEDURE;"
$ write commandfile "LOCAL next_file,period_range,sub_range,beg_line_range;"
$ write commandfile "SET(SUCCESS,OFF);"
$ write commandfile "work_buffer := CREATE_BUFFER ("""work""");"
$ write commandfile "POSITION (work_buffer);"
$ write commandfile "ERASE (work_buffer);"
$ write commandfile-
  "next_file := FILE_SEARCH (GET_INFO (COMMAND_LINE,"""FILE_NAME"""));"
$ write commandfile "READ_FILE (next_file);"
$ write commandfile "POSITION (BEGINNING_OF (work_buffer));"
$ write commandfile "SET (OUTPUT_FILE,CURRENT_BUFFER,FILE_PARSE (next_file));"
$ write commandfile "LOOP"
$ write commandfile "   POSITION (SEARCH ("""begin""",FORWARD));"
$ write commandfile "   EXITIF CURRENT_OFFSET = 0;"
$ write commandfile "   MOVE_HORIZONTAL (1)"
$ write commandfile "ENDLOOP;"
$ write commandfile "POSITION (LINE_END);"
$ string:=POSITION (SEARCH (""".""",REVERSE));
$ write commandfile string
$ write commandfile "MOVE_HORIZONTAL (-1);"
$ write commandfile "beg_line_range :=CREATE_RANGE (LINE_BEGIN,MARK(NONE));"
$ write commandfile "MOVE_HORIZONTAL (1);"
$ string:=period_range := SEARCH (""".""",REVERSE,NO_EXACT,beg_line_range);
$ write commandfile string
$ write commandfile "IF period_range = 0"
$ write commandfile "THEN "
$ write commandfile "MOVE_HORIZONTAL (1);"
$ string:=COPY_TEXT ("""-""");
$ write commandfile string
$ write commandfile "ELSE"
$ write commandfile "ERASE_CHARACTER (1);"
$ string:=COPY_TEXT ("""-""");
$ write commandfile string
$ write commandfile "POSITION (BEGINNING_OF (period_range));"
$ write commandfile "MOVE_HORIZONTAL (-1);"
$ write commandfile "sub_range := CREATE_RANGE (LINE_BEGIN,MARK(NONE));"
$ string:=substitute (""".""","""-""",sub_range);
$ write commandfile string
$ write commandfile "ENDIF;"
$ write commandfile "WRITE_FILE (work_buffer);"
$ write commandfile "SET(NO_WRITE, work_buffer);"
$ write commandfile "QUIT;"
$ goto endtpucommand
