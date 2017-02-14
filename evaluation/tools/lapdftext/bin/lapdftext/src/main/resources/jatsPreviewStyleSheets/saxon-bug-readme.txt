SAXON BUG ENCOUNTERED IN TESTING NISO v1 STYLESHEETS USING XPROC

When running one of the stylesheets in the distribution, we encountered
a Saxon bug, which has now been reported and fixed, but which may appear
when using some distributions of Saxon and oXygen 14.

It takes the form of a compile-time error with the disconcerting traceback "null".

The bug appears only when running the OASIS tables to HTML stylesheet
(xslt/oasis-tables/oasis-html.xsl) and only in Saxon 9.4.0.3 when bytecode
compilation is turned on.

This will, however, be the case when running XProc pipelines inside oXygen v14
(distributed with Saxon 9.4.0.3), unless its copy of Saxon has been updated.

To avoid the problem, do any of the following:

1. Run your XProc pipeline outside oXygen (for example, using XML Calabash)
2. Use a Saxon shell stylesheet (with bytecode compiling turned off)
3. Upgrade Saxon and/or oXygen to a more recent release, in which the bug
   has been fixed

