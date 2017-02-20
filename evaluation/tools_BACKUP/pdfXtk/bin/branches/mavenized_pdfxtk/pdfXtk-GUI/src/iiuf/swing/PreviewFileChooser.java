package iiuf.swing;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.swing.filechooser.FileSystemView;
import javax.swing.JFileChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import iiuf.awt.Awt;
import iiuf.util.Util;
import iiuf.util.Strings;

/**
   A file chooser implementation that provides modular previews.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class PreviewFileChooser
  extends
  JFileChooser 
{
  protected Dimension       previewSize = new Dimension(160, 160);
  protected ArrayList       previews    = new ArrayList();
  protected JComboBox       previewSel  = new JComboBox();
  protected AbstractPreview currentPreview;
  protected File            currentFile;
  
  public PreviewFileChooser() {
    init();
  }
  
  public PreviewFileChooser(File currentDirectory) {
    super(currentDirectory);
    init();
  }
  
  public PreviewFileChooser(File currentDirectory, FileSystemView fsv) {
    super(currentDirectory, fsv);
    init();
  }
  
  public PreviewFileChooser(FileSystemView fsv) {
    super(fsv);
    init();
  }
  
  public PreviewFileChooser(String currentDirectoryPath) {
    super(currentDirectoryPath);
    init();
  }
  
  public PreviewFileChooser(String currentDirectoryPath, FileSystemView fsv) {
    super(currentDirectoryPath, fsv);
    init();
  }
  
  private void init() {    
    final JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(previewSel, BorderLayout.NORTH);
    panel.setPreferredSize(previewSize);
    setAccessory(panel);
    addPropertyChangeListener(SELECTED_FILE_CHANGED_PROPERTY, new PropertyChangeListener() {
	public void propertyChange(PropertyChangeEvent e) {
	  Object sel = previewSel.getSelectedItem();
	  previewSel.removeAllItems();
	  currentFile = (File)e.getNewValue();
	  for(Iterator i = previews.iterator(); i.hasNext();) {
	    AbstractPreview p = (AbstractPreview)i.next();	    
	    if(p.accepts(currentFile)) {
	      previewSel.addItem(p);
	      if(sel == null)
		sel = p;
	    }
	  }
	  previewSel.setSelectedItem(sel);
	  setPreview(panel, (AbstractPreview)previewSel.getSelectedItem());
	}
      });
    
    previewSel.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setPreview(panel, (AbstractPreview)previewSel.getSelectedItem());
	}
      });

    addPreview(new ImagePreview());
    addPreview(new AudioPreview());
    addPreview(new TextPreview());
    addPreview(new InfoPreview());
  }
  
  private void setPreview(JPanel panel, AbstractPreview p) {
    if(currentPreview != null) {
      panel.remove(currentPreview);
      currentPreview = null;
    }
    if(p != null) {
      currentPreview = p;
      panel.add(currentPreview, BorderLayout.CENTER);
      panel.validate();
      currentPreview.preview(currentFile);
    }    
  }

  public void setPreviewSize(Dimension size) {
    previewSize = size;    
  }
  
  public void addPreview(AbstractPreview p) {
    previews.add(p);
  }
  
  public static void main(String[] argv) {
    PreviewFileChooser fc = new PreviewFileChooser();
    fc.showOpenDialog(new javax.swing.JFrame());
    System.exit(0);
  }

  static class TextPreview
    extends
    AbstractPreview
  {
    TextView text = new TextView();
    
    TextPreview() {
      setLayout(new BorderLayout());
      add(text, BorderLayout.CENTER);
    }

    public boolean accepts(File f){
      return f.isFile();
    }
    
    public void preview(File f) {      
      try {
	FileReader in = new FileReader(f);
	char[]     t  = new char[1024];
	in.read(t);
	text.setText(new String(t));
	in.close();
      } catch(IOException e) {
	Util.printStackTrace(e);
      }
    }
    
    public String toString() {
      return "Text";
    }
  }

  static class InfoPreview
    extends
    AbstractPreview
  {
    JLabel           name      = new JLabel();
    JLabel           size      = new JLabel();
    JLabel           flags     = new JLabel();
    JLabel           modif     = new JLabel();
    SimpleDateFormat formatter = new SimpleDateFormat("M/d/yyyy hh:mm:ss");
    
    InfoPreview() {
      name.setForeground(Color.black);
      size.setForeground(Color.black);
      flags.setForeground(Color.black);
      modif.setForeground(Color.black);
      setLayout(new GridBagLayout());
      add(new JLabel("Name:"), Awt.constraints(true));
      add(name, Awt.constraints(true));
      add(new JLabel("Size:"), Awt.constraints(true));
      add(size, Awt.constraints(true));
      add(new JLabel("Modified:"), Awt.constraints(true));
      add(modif, Awt.constraints(true));
      add(new JLabel("Flags:"), Awt.constraints(true));
      add(flags, Awt.constraints(true));      
      add(Swing.newJComponent(), Awt.constraints(true, GridBagConstraints.BOTH));
    }

    public boolean accepts(File f) {
      return true;
    }

    public void preview(File f) {
      name.setText(f.getName());
      size.setText((f.length() / 1000) + "KB");
      modif.setText(formatter.format(new Date(f.lastModified())));
      String flgs = "";
      if(f.isDirectory()) flgs += "DIR ";
      if(f.canRead())     flgs += "READ ";
      if(f.canWrite())    flgs += "WRITE ";
      if(f.isHidden())    flgs += "HIDDEN ";
      flags.setText(flgs);
    }
    
    public String toString() {
      return "Info";
    }
  }

  static class TextView
    extends
    JComponent
  {
    String[] lines = new String[0];
    
    public void setText(String text) {
      lines = Strings.split(text, '\n');      
      repaint();
    }
    
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      int h      = g.getFontMetrics().getHeight();
      int i      = 0;
      int height = getHeight();
      g.setColor(Color.white);
      g.fillRect(0, 0, getWidth(), height);
      g.setColor(Color.black);
      for(int y = g.getFontMetrics().getAscent(); y < height; y += h) {
	if(i >= lines.length) break;
	g.drawString(lines[i++], 0, y);
      }
    }    
  }
}

/*
  $Log: PreviewFileChooser.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/07/30 15:27:04  schubige
  adapted for sample based timing

  Revision 1.1  2001/05/08 10:09:57  schubige
  added preview file chooser
  
*/
