package iiuf.swing;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.applet.Applet;
import java.applet.AudioClip;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import iiuf.awt.Awt;
import iiuf.util.Util;

/**
   Audio "preview" for preview file selector.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
   @see iiuf.swing.PreviewFileChooser
*/
public class AudioPreview 
  extends
  AbstractPreview 
  implements
  Runnable
{
  Loader loader;
  JLabel status = new JLabel();
  String statusMsg;
  String name;
  
  public AudioPreview() {
    setLayout(new GridBagLayout());
    add(Swing.newButton(Resource.PLAY, new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  if(loader != null)
	    loader.playClip();
	}
      }), Awt.constraints(false, GridBagConstraints.HORIZONTAL));
    add(Swing.newButton(Resource.STOP, new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  if(loader != null)
	    loader.stopClip();
	}
      }), Awt.constraints(true, GridBagConstraints.HORIZONTAL));
    add(status, Awt.constraints(true, GridBagConstraints.BOTH));
  }
  
  public boolean accepts(File f) {    
    String s = f.getName().toLowerCase(); 
    return (s.endsWith(".au")  || s.endsWith(".rmf") ||
	    s.endsWith(".wav") || s.endsWith(".aif") ||
	    s.endsWith(".aiff"));
  }
  
  public void preview(File file) {
    try {
      if(loader != null)
	loader.stopClip();
      name = file.getName();
      setStatus("Loading " + name + "...");
      loader = new Loader(file.toURL());
    } catch(MalformedURLException e) {
      Util.printStackTrace(e);
    }    
  }
  
  void setStatus(String msg) {
    statusMsg = msg;
    SwingUtilities.invokeLater(this);
  }
  
  public void run() {
    status.setText(statusMsg);
  }

  public String toString() {
    return "Audio";
  }
  
  class Loader 
    extends
    Thread
  {
    AudioClip clip;
    URL       url;
    
    Loader(URL url_) {
      url = url_;
      start();
    }
    
    public void run() {
      clip = Applet.newAudioClip(url);
      if(loader == this)
	playClip();
    }
    
    void playClip() {
      if(clip != null) {
	clip.stop();
	clip.play();
      }
      setStatus("Playing " + name + "...");
    }
    
    void stopClip() {
      if(clip != null)
	clip.stop();
      setStatus("Stopped");
    }
  }
}

/*
  $Log: AudioPreview.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/05/08 10:09:57  schubige
  added preview file chooser
  
*/
