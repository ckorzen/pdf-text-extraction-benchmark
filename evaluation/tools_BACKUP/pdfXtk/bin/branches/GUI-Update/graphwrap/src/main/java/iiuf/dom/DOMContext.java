package iiuf.dom;

import java.util.HashMap;

import org.w3c.dom.Document;

/**
   Context for DOMManager and DOMHandler.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class DOMContext 
  extends
  HashMap 
{
  Document document;
  boolean  fBreak;

  public DOMContext(Document document_) {
    document = document_;
  }

  public Document getDocument() {
    return document;
  }
  
  public void setBreak() {
    fBreak = true;
  }
}

/*
  $Log: DOMContext.java,v $
  Revision 1.1  2002/07/11 12:03:48  ohitz
  Initial checkin

  Revision 1.1  2001/03/28 18:45:35  schubige
  working on dom again
  
*/
