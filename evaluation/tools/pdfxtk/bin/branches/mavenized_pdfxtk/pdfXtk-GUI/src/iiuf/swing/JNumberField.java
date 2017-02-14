package iiuf.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;

import iiuf.util.Util;
import iiuf.util.Strings;

/**
   <code>JTextField</code> that accept numbers (decimal) only.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class JNumberField 
  extends 
  JTextField 
{
  public static final int    MINIMUM       = 0;
  public static final int    VALUE         = 1;
  public static final int    MAXIMUM       = 2;
  public static final String ALLOWED_CHARS = "0123456789-.";
  
  protected double            min = Double.MIN_VALUE;
  protected double            max = Double.MAX_VALUE;
  protected boolean           integer;
  protected int               property;
  protected BoundedRangeModel model;
  protected boolean           reloadVal = true;
  protected double            value;
  
  public JNumberField() {}
  
  public JNumberField(int columns, BoundedRangeModel model_, int property_) {
    super(columns);
    model = model_;
    model.addChangeListener(new ChangeListener() {
	public void stateChanged(ChangeEvent e) {initFromModel();}
      });
    property = property_;
    integer  = true;
    initFromModel();
    init();    
  }
  
  public JNumberField(Document doc, double number, int columns) {
    super(doc, "" + number, columns);
    init();
  }
    
  public JNumberField(int columns) {
    super(columns);
    init();
  }
  
  public JNumberField(int columns, double min, double max) {
    super(columns);
    setMinimum(min);
    setMaximum(max);
    init();
  }
  
  public JNumberField(double value, double min, double max) {
    super(value + "");
    setMinimum(min);
    setMaximum(max);
    init();
  }
  
  public JNumberField(double number, int columns, double min, double max) {
    super("" + number, columns);
    setMinimum(min);
    setMaximum(max);
    init();
  }
  
  public JNumberField(Document doc, int number, int columns) {
    super(doc, "" + number, columns);
    integer = true;
    init();
  }
    
  public JNumberField(int columns, int min, int max) {
    super(columns);
    setMinimum(min);
    setMaximum(max);
    integer = true;
    init();
  }
  
  public JNumberField(double value, int min, int max) {
    super(value + "");
    setMinimum(min);
    setMaximum(max);
    integer = true;
    init();
  }
  
  public JNumberField(int number, int columns, int min, int max) {
    super("" + number, columns);
    setMinimum(min);
    setMaximum(max);
    integer = true;
    init();
  }
  
  private void init() {
    addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  setNumber(getNumber());
	}
      });
    setHorizontalAlignment(RIGHT);
  }
  
  private void initFromModel() {
    switch(property) {
    case MINIMUM: setNumber(model.getMinimum()); break;
    case MAXIMUM: setNumber(model.getMaximum()); break;
    case VALUE:  
      _setNumber(model.getValue());   
      setMinimum(model.getMinimum());
      setMaximum(model.getMaximum());
      break;
    }
  }

  public double getMinimum() {
    return min;
  }

  public double getMaximum() {
    return max;
  }

  public void setMinimum(double min_) {
    min = min_;
    if(getNumber() < min)
      setNumber(min);
  }

  public void setMaximum(double max_) {
    max = max_;
    if(getNumber() > max)
      setNumber(max);
  }
  
  public double getNumber() {
    if(reloadVal) {
      value = reloadVal();
      reloadVal = false;
    }
    return value;
  }

  private double reloadVal() {    
    try {
      return Double.parseDouble(getText());
    } catch(Exception e) {
      return Double.NaN;
    }
  }
  
  private void _setNumber(double number) {
    value = number;
    if(integer)
      setText("" + (int)value);
    else
      setText("" + value);
  }
  
  public void setNumber(double number) {
    if(Double.isNaN(number)) {
      setText("");
      return;
    }

    if(number > max) number = max;
    if(number < min) number = min;

    if(model != null) {
      switch(property) {
      case MINIMUM: model.setMinimum((int)number); break;
      case VALUE:   model.setValue((int)number);   break;
      case MAXIMUM: model.setMaximum((int)number); break;
      }    
    }
    
    if(number == getNumber()) return;
    
    _setNumber(number);    
  }
  
  public class NumberDocument extends PlainDocument {
    int insertStringCnt;
    
    public void insertString(int off, String str, AttributeSet a)
      throws BadLocationException {
      
      if(str != null)
	str = Strings.filter(str, ALLOWED_CHARS);
      
      super.insertString(off, str, a);

      reloadVal = true;
    }
  }
  
  protected Document createDefaultModel() {
    return new NumberDocument();
  }
}

/*
  $Log: JNumberField.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.8  2001/04/02 05:44:48  schubige
  played around with beat source

  Revision 1.7  2001/03/30 17:33:25  schubige
  modified beat soundlet

  Revision 1.6  2001/03/12 17:52:00  schubige
  Added version support to sourcewatch and enhanced soundium

  Revision 1.5  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.4  2001/03/07 17:36:28  schubige
  soundium properties panel beta

  Revision 1.3  2001/03/07 07:45:09  schubige
  soundium properites panel

  Revision 1.2  2001/03/05 17:55:07  schubige
  Still working on soundium properties panel

  Revision 1.1  2001/03/02 17:52:49  schubige
  Enhanced sourcewatch and worked on soundium properties panel
  
*/
