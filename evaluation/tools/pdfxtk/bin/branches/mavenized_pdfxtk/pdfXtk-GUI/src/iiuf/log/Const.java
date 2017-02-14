package iiuf.log;

import javax.swing.ImageIcon;

/**
   Various logger related constants.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface Const {
  public static final int LOG_EMERG       = 0; /* system is unusable */
  public static final int LOG_ALERT       = 1; /* action must be taken immediately */
  public static final int LOG_CRIT        = 2; /* critical conditions */
  public static final int LOG_ERR         = 3; /* error conditions */
  public static final int LOG_WARNING     = 4; /* warning conditions */
  public static final int LOG_NOTICE      = 5; /* normal but significant condition */
  public static final int LOG_INFO        = 6; /* informational */
  public static final int LOG_DEBUG       = 7; /* debug-level messages */
  public static final int LOG_ALL         = 8; /* '*' in config, all levels */
  
  public static final String[] LOG_STRINGS = {
    "Emergency",
    "Alert",
    "Critical",
    "Error",
    "Warning",
    "Notice",
    "Info",
    "Debug",
  };
  
  public static final ImageIcon[] LOG_ICONS = {
    new ImageIcon(Const.class.getResource("prio0.gif")),
    new ImageIcon(Const.class.getResource("prio1.gif")),
    new ImageIcon(Const.class.getResource("prio2.gif")), 
    new ImageIcon(Const.class.getResource("prio3.gif")), 
    new ImageIcon(Const.class.getResource("prio4.gif")),
    new ImageIcon(Const.class.getResource("prio5.gif")), 
    new ImageIcon(Const.class.getResource("prio6.gif")), 
    new ImageIcon(Const.class.getResource("prio7.gif")), 
  };

  public static final String STDOUT = "stdout";
  public static final String STDERR = "stderr";
}

/*
  $Log: Const.java,v $
  Revision 1.1  2002/07/11 12:24:01  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:36  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/10/10 16:32:11  schubige
  Added subtree display to TreeView, fixed some bugs

  Revision 1.1  2000/10/09 06:47:56  schubige
  Updated logger stuff
  
*/
