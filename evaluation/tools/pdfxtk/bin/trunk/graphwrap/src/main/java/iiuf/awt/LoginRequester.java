package iiuf.awt;

import java.awt.Frame;
import java.awt.Label;
import java.awt.Button;
import java.awt.TextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
   A login (user/password) requester.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class LoginRequester
  extends 
  Dialog
  implements 
  ActionListener 
{
  
  transient private String    username;
  transient private String    password;
  transient private String    posText;
  transient private TextField u_text;
  transient private TextField p_text;
  
  public LoginRequester(Frame parent, String title, String user_label, String password_label, 
			String posText_, String negText) {
    
    super(parent, title, true);
    setLayout(new GridBagLayout());
    posText = posText_;
    
    add(new Label(user_label), Awt.constraints(false, GridBagConstraints.HORIZONTAL));
    u_text = new TextField(30);
    try{u_text.setText(System.getProperty("user.name",""));}
    catch(Exception e) {}
    add(u_text, Awt.constraints(true, GridBagConstraints.HORIZONTAL));
    
    add(new Label(password_label), Awt.constraints(false, GridBagConstraints.HORIZONTAL));
    p_text = new TextField(30);
    p_text.setEchoChar('*');
    add(p_text, Awt.constraints(true, GridBagConstraints.HORIZONTAL));
    
    Button pos = new Button(posText);
    add(pos, Awt.constraints(false, 10, 4, GridBagConstraints.HORIZONTAL));
    pos.addActionListener(this);
    
    Button neg = new Button(negText);
    add(neg, Awt.constraints(true, 10, 4, GridBagConstraints.HORIZONTAL));
    neg.addActionListener(this);
    
    pack();
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals(posText)) {
      username = u_text.getText();
      password = p_text.getText();
    }
    else
      username = password = null;
    close(username == null ? DIALOG_CANCEL : 1);
  }
  
  public boolean done(int exit_code) {return true;}

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }
}
/*
  $Log: LoginRequester.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.5  2001/01/04 16:28:29  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/01/18 11:15:39  schubige
  First beta release of vote server / votlet

  Revision 1.3  1999/11/26 10:00:38  schubige
  updated for new awt package

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:16  schubige
  *** empty log message ***

*/
