# NISO Journal Article Tag Suite (JATS) version 1.0
## Preview XSLT stylesheets

For documentation on these stylesheets, see:
* quickstart.pdf -- "Bare bones" instructions
* user-docs.pdf -- User documentation including assumptions and limitations
* technical-docs.pdf -- More technical documentation, especially
                      for those who wish to customize their
                      environment or the stylesheets

Or the same documents in HTML format.

These same documents are included with tagging according to
NISO JATS 1.0, from which the PDF and HTML was
generated:
* quickstart.xml
* user-docs.xml
* technical-docs.xml

As such, they are suitable for testing the stylesheets.

As described in the documentation, these XSLT 1.0 transformations
will work standalone:
* xslt/main/jats-html.xsl
* xslt/main/jats-xslfo.xsl

To provide additional functionality, these stylesheets are also
called into pipelines, which may be invoked using stylesheets
included in the shells/saxon directory:
* jats-APAcit-html.xsl
* jats-APAcit-xslfo.xsl
* jats-PMCcit-html.xsl
* jats-PMCcit-print-fo.xsl
* jats-PMCcit-web-html.xsl
* jats-PMCcit-xhtml.xsl
* jats-PMCcit-xslfo.xsl
* jats-oasis-PMCcit-fo.xsl

As described in the documentation, these are XSLT 2.0 with Saxon
extensions, and require a recent version of the Saxon processor
to run. (Tested under Saxon 9.4.)

Or if XProc is preferred, use the XProc versions (with the same names)
given in the shells/xproc directory.

Plus, there is a CSS stylesheet to be used with HTML results of
any of these processes (including the HTML versions of the
documentation):
*jats-preview.css

The file saxon-bug-readme.txt describes a bug that *may* be encountered
when using one of the stylesheets with certain versions of Saxon (or
with XProc invoking Saxon), which is most easily addressed by upgrading
the tool (to a copy distributed since the bug was reported and fixed).

The following files are included in this distribution
(as described in the documentation).  Directories are in **bold**.

* jats-preview.css
* quickstart.html
* quickstart.pdf
* quickstart.xml
* readme.txt
* README.md
* saxon-bug-readme.txt
* technical-docs.html
* technical-docs.pdf
* technical-docs.xml
* userguide.html
* userguide.pdf
* userguide.xml
* **shells**
    * **saxon**
        * jats-APAcit-html.xsl
        * jats-APAcit-xslfo.xsl
        * jats-oasis-PMCcit-fo.xsl
        * jats-PMCcit-html.xsl
        * jats-PMCcit-print-fo.xsl
        * jats-PMCcit-web-html.xsl
        * jats-PMCcit-xhtml.xsl
        * jats-PMCcit-xslfo.xsl
    * **xproc**
        * jats-APAcit-html.xpl
        * jats-APAcit-xslfo.xpl
        * jats-oasis-PMCcit-fo.xpl
        * jats-PMCcit-html.xpl
        * jats-PMCcit-print-fo.xpl
        * jats-PMCcit-web-html.xpl
        * jats-PMCcit-xhtml.xpl
        * jats-PMCcit-xslfo.xpl
* **xslt**
    * **citations-prep**
        * jats-APAcit.xsl
        * jats-PMCcit.xsl
    * **main**
        * jats-html.xsl
        * jats-xslfo.xsl
        * shell-utility.xsl
         *xhtml-tables-fo.xsl
        * **oasis-tables**
        * oasis-table-html.xsl
        * oasis-table-support.xsl
        * oasis-table.sch
    * **post**
        * xhtml-ns.xsl
    * **prep**
        * jats-printfilter.xsl
        * jats-webfilter.xsl
        * oasis-tables-html.xsl

----------------------------------------------------------------------------------------------------

Our policy on merging changes:

These stylesheets are provided as a point of entry for JATS users who may not have 
the resources to create them from scratch. Because there are many varied 
implementations of JATS, you should have no expectation that these stylesheets will 
create production ready files in any arbitrary system. Instead, the stylesheets should be 
customized for your particular needs. 

Because we view these stylesheets as a template for a customized solution, not 
the solution itself, we will accept changes that fix an actual bug, but we will 
not merge in changes that we view as “customization”. For example, we will accept 
changes that fix a problem which otherwise leads to failure in creating a final output 
file, but we will not accept changes that focus on presentational aspects of the final 
output (such as font changes, margin changes, graphics sizing, etc).  

If you believe that you have a customization of the stylesheets that would benefit 
other users please fork this repository and announce your fork on the JATS mailing list, 
http://www.mulberrytech.com/JATS/JATS-List/.

------------------------------------------------------------------------------------------
 
This work is in the public domain and may be reproduced, published or 
otherwise used without the permission of the National Library of Medicine (NLM).

We request only that the NLM is cited as the source of the work.

Although all reasonable efforts have been taken to ensure the accuracy and 
reliability of the software and data, the NLM and the U.S. Government  do 
not and cannot warrant the performance or results that may be obtained  by
using this software or data. The NLM and the U.S. Government disclaim all 
warranties, express or implied, including warranties of performance, 
merchantability or fitness for any particular purpose.

----------------------------------------------------------------------------------------------------
