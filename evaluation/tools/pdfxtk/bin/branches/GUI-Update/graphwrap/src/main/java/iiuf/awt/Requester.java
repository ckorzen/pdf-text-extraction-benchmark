package iiuf.awt;

import java.awt.Frame;
import java.awt.Label;
import java.awt.Button;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

/**
   A generic requester.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class Requester
extends 
Dialog 
implements
ActionListener {
  
  transient private String  posText;
  transient private boolean result;
  
  public Requester(Frame parent, String title, String lines, String button) {
    this(parent, title, lines, null, button);
  }
  
  public Requester(Frame parent, String title, String lines, String posText_, String negText) {
    super(parent, title, true);
    setLayout(new GridBagLayout());
    posText = posText_;
    
    add(new MultiLineLabel(lines), Awt.constraints(true));
    
    if(posText != null) {
      Button pos = new Button(posText);
      add(pos, Awt.constraints(false, 10, 4, GridBagConstraints.HORIZONTAL));
      pos.addActionListener(this);
    } 

    if(negText != null) {
      Button neg = new Button(negText);
      add(neg, Awt.constraints(true, 10, 4, GridBagConstraints.HORIZONTAL));
      neg.addActionListener(this);
    }

    pack();
  }
  
  public void actionPerformed(ActionEvent e) {
    result = e.getActionCommand().equals(posText);
    close(result ? 1 : DIALOG_CANCEL);
  }
  
  public boolean done(int exit_code) {return true;}
  
  public boolean positive() {
    setVisible(true);
    return result;
  }
}
/*
  $Log: Requester.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.4  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.3  2000/01/18 11:15:39  schubige
  First beta release of vote server / votlet

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***

  Revision 1.6  1999/09/14 11:48:13  schubige
  Updated some preferences realted classes

  Revision 1.5  1999/09/10 06:54:19  schubige
  Dialogs & requesters are now placed at 1/2 x and 1/3 y of the screen.

  Revision 1.4  1999/09/09 14:57:55  juillera
  Updated for new iiuf.util.Dialog

  Revision 1.3  1999/09/03 15:50:08  schubige
  Changed to new header & log conventions.
  
*/
