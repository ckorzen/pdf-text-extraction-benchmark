$! pack.com
$! VMS command procedure that creates tar-compressed uuencoded files 
$! for arXiv.org archives.
$! Has essentially the same functionality as the arXiv.org 
$! unix shell script "uufiles".
$! G. Bodwin  (gtb@hep.anl.gov) 6 February 1995
$! Note: this command procedure assumes that tar, compress, 
$! gzip, and uuencode have been installed.
$! 
$! Invoke pack.com by issuing the command
$!
$! @pack -gz -#
$!
$! where -gz is an optional modifier specifying gzip compression
$! (the default is compress), and -# is an optional modifier specifying
$! the speed of gzip compression. -1 is fastest (least compressed) and 
$! -9 is slowest. -6 is the default. 
$!
$! Local definitions.  Edit to point to executables on the local system
$! for tar, compress, gzip, uuencode and uudecode.
$! If desired, the local definitions can be extracted from this file and
$! included in the user's login.com file.
$!
$ tar== "$userdisk:[fred.xxx]vmstar.exe"
$ compress == "$userdisk:[fred.xxx]compress.exe"
$ gzip=="$userdisk:[fred.xxx]gzip.exe"
$ uuencode=="$userdisk:[fred.xxx]uuencode.exe"
$ uudecode=="$userdisk:[fred.xxx]uudecode.exe"
$!
$! Start of main command procedure
$!
$! Temporarily override user symbol assignments 
$!
$ edit:=edit
$ delete:=delete
$!
$! Determine compression routine
$!
$ write sys$output ""
$ if f$edit(p1,"lowercase") .eqs. "-gz"
$ then
$ suffix="gz"
$ speed=p2
$ write sys$output "Will use gz-compression"
$ else
$ suffix="Z"
$ write sys$output "Will use Z-compression 
$ write sys$output "For gz-compression, specify @pack -gz"
$ write sys$output-
  "For slowest (optimal) gz-compression, specify @pack -gz -9"
$ endif
$!
$! Determine input directory
$!
$ write sys$output ""
$ write sys$output "TYPE NAME OF DIRECTORY CONTAINING INPUT FILES"
$ write sys$output "(CR yields current default directory)"
$ inquire input_dir "Input directory"
$ default_dir=f$environment("default")
$ if input_dir .nes. "" then set default 'input_dir'
$ on error then goto defaultexit
$!
$! Determine files to be packed
$!
$ write sys$output ""
$ write sys$output "TYPE INPUT FILENAMES SEPARATED BY SPACES (WILDCARDS OK)"
$ inquire input_files "Input files"
$!
$! Check to see if there is more than 1 input file
$!
$ if f$locate(" ",input_files) .eq. f$length(input_files)
$ then 
$ if f$locate("*",input_files) .eq. f$length(input_files)
$ then 
$ xtar=""
$ else
$ xtar="tar"
$ endif
$ else
$ xtar="tar"
$ endif
$!
$! Determine output filename
$!
$ if xtar .eqs. ""
$ then
$ outname=f$parse(input_files,,,"name")
$ else
$ write sys$output ""
$ write sys$output "TYPE OUTPUT FILENAME"
$ write sys$output "DO NOT INCLUDE DIRECTORY NAME OR FILETYPE."
$ write sys$output "(Filetype will be set to .uu.)"
$ inquire outname "Output file name"
$!
$! Discard filetype if supplied by user
$!
$ outname=f$extract(0,f$locate(".",outname),outname)
$ endif
$!
$! Change outname to lower case
$!
$ outname=f$edit(outname,"lowercase")
$!
$! Tar, compress, uuencode, define symbols for use in building header
$!
$ if xtar .eqs. ""
$ then
$ copy 'input_files' 'default_dir''input_files'
$ tarfile=f$edit(input_files,"lowercase")
$ else
$ tar -cvf 'default_dir''outname'.tar 'input_files'
$ tarfile="''outname'.tar"
$ endif
$ set default 'default_dir'
$ if suffix .eqs. "gz"
$ then
$ gzip -f 'speed' 'tarfile'
$ dash="-"
$ uncompressvms="gzip -d"
$ if xtar .eqs. ""
$ then
$ uncompressunix="gunzip"
$ else
$ uncompressunix="gunzip -c"
$ endif
$ else
$! 
$! Rename the tarfile to get around filename length limit in compress
$!
$ rename 'tarfile' gtbxxtmp.tar
$ compress gtbxxtmp.tar
$!
$! Restore the correct name to the compressed file 
$!
$ rename gtbxxtmp.tar_Z 'tarfile'_Z
$ dash="_"
$ uncompressvms="compress -d"
$ if xtar .eqs. ""
$ then
$ uncompressunix="uncompress"
$ else
$ uncompressunix="zcat"
$ endif
$ endif
$ uuencode 'tarfile''dash'"''suffix'" gtbxxxtmp.uu
$ delete 'tarfile''dash''suffix'.*
$!
$! Create header file
$!
$ open/write headfile xxxheader.txt
$ goto beginheader
$ endheader:
$ close headfile
$!
$! Use the tpu editor to replace  the file suffix _Z with .Z or -gz 
$! with .gz in the BEGIN line of the uuencoded file.
$!
$! First create a tpu command file
$!
$ open/write commandfile xxxcommand.tpu
$ goto begintpucommand
$ endtpucommand:
$ close commandfile
$!
$! Now invoke tpu
$! (Routine extracted from "Replace" by Ken Selvia
$! UCS_KAS@SHSUODIN  (BITNET))
$!
$! veri = f$verify(0'replace_verify')
$ MSG = F$ENVIRONMENT("MESSAGE")
$ on error then goto errorexit
$ on control_y then goto errorexit
$ SET MESSAGE /NOIDENT/NOTEXT/NOSEVERITY/NOFACILITY
$ DEFINE/USER SYS$INPUT SYS$COMMAND
$ EDIT/TPU/NOINIT/NOSECTION/COMMAND=xxxcommand.tpu/NODISPLAY  gtbxxxtmp.uu
$! veri = f$verify(veri)
$ delete xxxcommand.tpu.*
$ purge gtbxxxtmp.uu
$ copy xxxheader.txt,gtbxxxtmp.uu 'outname'.uu
$ SET MESSAGE 'MSG'
$ write sys$output ""
$ write sys$output "WRITING ''outname'.uu"
$ delete gtbxxxtmp.uu.*
$ delete xxxheader.txt.*
$ exit
$ defaultexit:
$ set default 'default_dir'
$ exit
$ errorexit:
$ SET MESSAGE 'MSG'
$ exit
$!
$! Start of TPU command file
$! (Routine extracted from "Replace" by Ken Selvia
$! UCS_KAS@SHSUODIN  (BITNET))
$!
$ begintpucommand:
$ write commandfile "PROCEDURE substitute(target,replacement,size)"
$ write commandfile "    ON_ERROR"
$ write commandfile "        RETURN;"
$ write commandfile "    ENDON_ERROR"
$ write commandfile "!    LOOP"
$ write commandfile "        POSITION (SEARCH (target,FORWARD,EXACT));"
$ write commandfile "        ERASE_CHARACTER (size);"
$ write commandfile "        COPY_TEXT (replacement);"
$ write commandfile "!    ENDLOOP;"
$ write commandfile "ENDPROCEDURE"
$ write commandfile "!"
$ write commandfile "! Execute "
$ write commandfile "!"
$ write commandfile "LOCAL next_file, search_string, replace_string,"
$ write commandfile "      lower_string, search_size;"
$ write commandfile "SET(FACILITY_NAME, """REPLACE""");"
$ write commandfile "SET(SUCCESS,OFF);"
$ string:=search_string :="""''tarfile'''dash'''suffix'""";
$ write commandfile string
$ string:=replace_string :="""''tarfile'.''suffix'""";
$ write commandfile string
$ write commandfile "search_size := LENGTH (search_string);"
$ write commandfile "work_buffer := CREATE_BUFFER ("""work""");"
$ write commandfile "POSITION (work_buffer);"
$ write commandfile "ERASE (work_buffer);"
$ write commandfile-
  "next_file := FILE_SEARCH (GET_INFO (COMMAND_LINE,"""FILE_NAME"""));"
$ write commandfile "READ_FILE (next_file);"
$ write commandfile "POSITION (BEGINNING_OF (work_buffer));"
$ write commandfile-
  "SET (OUTPUT_FILE,CURRENT_BUFFER,FILE_PARSE (next_file));"
$ write commandfile-
  "substitute(search_string,replace_string,search_size);"
$ write commandfile "WRITE_FILE (work_buffer);"
$ write commandfile "SET(NO_WRITE, work_buffer);"
$ write commandfile "QUIT;"
$ goto endtpucommand
$!
$!
$!Start of header file
$!
$ beginheader:
$write headfile-
 "#!/bin/csh -f"
$write headfile-
 "# this uuencoded ''suffix'-compressed ''xtar' file created by pack.com"
$!
$!Put in a line containing the string uufiles so that the shell script 
$!texview can determine how the file was packed.
$!
$write headfile-
 "# pack.com is a VMS command procedure that has essentially the same"
$write headfile-
 "# functionality as the UNIX shell script uufiles"
$write headfile-
 "# for more information, contact G. Bodwin (gtb@hep.anl.gov)"
$write headfile-
 "# if you are on a unix machine this file will unpack itself:"
$write headfile-
 "# strip off any mail header and call resulting file, e.g., ''outname'.uu"
$write headfile-
 "# (uudecode ignores these header lines and starts at begin line below)"
$write headfile-
 "# then say        csh ''outname'.uu"
$write headfile-
 "# or explicitly execute the commands (generally more secure):"
$write headfile-
 "#    uudecode ''outname'.uu ; ''uncompressunix' ''tarfile'.''suffix' ;"
$ if xtar .nes. "" then write headfile-
 "#    tar -xvf ''tarfile'"
$write headfile-
 "# on a VAX/VMS machine, use unpack.com or"
$write headfile-
 "# first use an editor to change the filename in the "
$write headfile-
 "# ""begin"" line below to ''tarfile'''dash'''suffix', then execute"
$write headfile-
 "#    uudecode ''outname'.uu"
$write headfile-
 "#    ''uncompressvms' ''tarfile'''dash'''suffix'"
$ if xtar .nes. "" then write headfile-
  "#    tar -xvf ''tarfile'"
$write headfile-
 "#"
$write headfile-
 "uudecode $0"
$write headfile-
 "chmod 644 ''tarfile'.''suffix'"
$ if xtar .eqs. ""
$ then
$write headfile-
 "''uncompressunix' ''tarfile'.''suffix'"
$write headfile-
 "rm $0"
$ else
$write headfile-
 "''uncompressunix'  ''tarfile'.''suffix' | tar -xvf -"
$write headfile-
 "rm $0 ''tarfile'.''suffix'"
$ endif
$write headfile-
 "exit"
$write headfile-
 ""
$ goto endheader

