package iiuf.awt;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Choice;
import java.awt.Panel;
import java.awt.Label;
import java.awt.Button;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
   A data chooser panel.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class DateChooser 
  extends
  Panel 
  implements
  ActionListener,
  ItemListener
{
  static private final String   B_PLUS  = "+";
  static private final String   B_MINUS = "-";
  
  /** @serial */
  private Choice            hour   = new Choice();
  /** @serial */
  private Choice            minute = new Choice();
  /** @serial */
  private Choice            second = new Choice();
  /** @serial */
  private Choice            month  = new Choice();
  /** @serial */
  private GregorianCalendar date   = new GregorianCalendar();
  /** @serial */
  private Button            minus;
  /** @serial */
  private Button            plus;
  /** @serial */
  private String            B_OK;
  /** @serial */
  private String            B_CANCEL;
  /** @serial */
  private Label             year = new Label();
  /** @serial */
  private Label             day  = new Label();
  /** @serial */
  private char[]            days;
  /** @serial */
  private DayChooser        d_chooser;
  /** @serial */
  private boolean           has_date;

  /**
     Constructs a new day chooser.

     @param has_time Display time chooser.
     @param has_date Display date chooser.
     @param months The menth strings, starting with january.
     @param days_  The day initals, starting with sunday.
   */
  public DateChooser(boolean  has_time,
		     boolean  has_date_,
		     String[] months,
		     char[]   days_) {
    has_date = has_date_;
    days     = days_;
    
    minus  = new Button(B_MINUS);
    plus   = new Button(B_PLUS);
    
    minus.addActionListener(this);
    plus.addActionListener(this);
    month.addItemListener(this);
    
    setLayout(new GridBagLayout());
    for(int i = 0; i < 24; i++)
      hour.add(add0(i));
    for(int i = 0; i < 60; i++) {
      minute.add(add0(i));
      second.add(add0(i));
    }
    for(int i = 0; i < months.length; i++)
      month.add(months[i]);
    Panel time = new Panel();
    time.add(hour);
    time.add(new Label(":", Label.CENTER));
    time.add(minute);
    time.add(new Label(":", Label.CENTER));
    time.add(second);
    
    if(has_date) {
      Panel month_sel = new Panel();
      month_sel.add(day);
      month_sel.add(month);
      month_sel.add(minus);
      month_sel.add(year);
      month_sel.add(plus);
      add(month_sel,  Awt.constraints(true, GridBagConstraints.HORIZONTAL));
      add(new HLine(), Awt.constraints(true, GridBagConstraints.HORIZONTAL));
    }

    if(has_date && has_time)
      add(new HLine(), Awt.constraints(true, GridBagConstraints.HORIZONTAL));

    if(has_time) {
      add(time, Awt.constraints(true, GridBagConstraints.HORIZONTAL));
    }
    
    setDate(date);
  }
    
  public void actionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals(B_MINUS))     setYear(getYear() - 1);
    else if(e.getActionCommand().equals(B_PLUS)) setYear(getYear() + 1);
  }
  
  public void itemStateChanged(ItemEvent e) {
    setMonth(month.getSelectedIndex());
  }
  
  private void done(boolean cancel) {
    if(cancel) date = null;
    setVisible(false);
  }
  
  private String add0(int i) {
    return i < 10 ? "0" + i : "" + i;
  }
  
  // time stuff

  public void setTime(int hour, int minute, int second) {
    date.set(Calendar.HOUR_OF_DAY, hour);   
    date.set(Calendar.MINUTE,      minute);
    date.set(Calendar.SECOND,      second);
    updateTime();
  }

  private void updateTime() {
    hour.select(date.get(Calendar.HOUR_OF_DAY));
    minute.select(date.get(Calendar.MINUTE));
    second.select(date.get(Calendar.SECOND));
  }

  // day stuff
  
  private int getDay() {
    return date.get(Calendar.DAY_OF_MONTH);
  }

  void setDay(int day) {
    date.set(Calendar.DAY_OF_MONTH, day);
    updateDay();
  }
  
  private void updateDay() {
    if(!has_date) return;
    d_chooser.select(getDay());
    day.setText(add0(getDay()) + ".");
  }
  
  private void updateDayChooser() {
    if(!has_date) return;
    if(d_chooser != null)
      remove(d_chooser);
    d_chooser = new DayChooser(this, date, days);
    add(d_chooser, Awt.constraints(true, GridBagConstraints.HORIZONTAL), 2);
    validate();
  }
  
  // month stuff

  private int getMonth() {
    return date.get(Calendar.MONTH);
  }
  
  private void setMonth(int month) {
    if(month != getMonth()) {
      date.set(Calendar.MONTH, month);
      setDay(1);
      updateMonth();
    }
  }
  
  private void updateMonth() {
    if(!has_date) return;
    month.select(getMonth());
    updateDayChooser();
  }

  // year stuff
 
  private int getYear() {
    return date.get(Calendar.YEAR);
  }
  
  private void setYear(int year) {
    date.set(Calendar.YEAR, year);
    updateYear();
    updateDayChooser();
  }
  
  private void updateYear() {
    if(!has_date) return;
    year.setText("" + getYear());
  }
  
 // date stuff

  public void setDate(GregorianCalendar date_) {
    date = date_;
    updateYear();
    updateMonth();
    updateDay();
    updateTime();
  }
  
  public GregorianCalendar getDate() {
    date.set(Calendar.HOUR_OF_DAY,   hour.getSelectedIndex());
    date.set(Calendar.MINUTE,        minute.getSelectedIndex());
    date.set(Calendar.SECOND,        second.getSelectedIndex());
    return date;
  }
}

class DayChooser
  extends
  Panel 
  implements
  ActionListener
{
  /** @serial */
  DateChooser dc;
  /** @serial */
  Button[]    b_days = new Button[32];
  
  DayChooser(DateChooser dc_, GregorianCalendar date, char[] days) {
    dc = dc_;
    
    setLayout(new GridLayout(7, 7));
    
    for(int i = 0; i < days.length; i++)
      add(new Label("" + days[i], Label.CENTER));
    
    Calendar tmp = (Calendar)date.clone();
    tmp.set(Calendar.DAY_OF_MONTH, 1);
    int skip = tmp.get(Calendar.DAY_OF_WEEK) - 1;
    for(int i = 0; i < skip; i++)
      add(new Component() {});
    
    int count = days(date);
    for(int i = 1; i <= count; i++) {
      b_days[i] = new Button("" + i);
      b_days[i].addActionListener(this);
      add(b_days[i]);
    }

    skip = 49 - getComponentCount();
    for(int i = 0; i < skip; i++)
      add(new Component() {});

    select(date.get(Calendar.DAY_OF_MONTH));
  }
  
  public void actionPerformed(ActionEvent e) {
    dc.setDay(Integer.parseInt(e.getActionCommand()));
  }
  
  public void select(int day) {
    b_days[day].requestFocus();
  }

  final static int[] DAYS = {31,28,31,30,31,30,31,31,30,31,30,31};
  
  private int days(GregorianCalendar date) {
    return date.get(Calendar.MONTH) == 1 ? 
      (date.isLeapYear(date.get(Calendar.YEAR)) ? 29 : 28) :
      DAYS[date.get(Calendar.MONTH)];
  }
}
/*
  $Log: DateChooser.java,v $
  Revision 1.1  2002/07/11 09:20:36  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:28  schubige
  Header update for 2001 and DIUF

  Revision 1.2  1999/11/26 09:14:29  schubige
  intermediate commit

  Revision 1.1  1999/11/26 08:51:15  schubige
  *** empty log message ***

  Revision 1.5  1999/10/07 11:02:12  schubige
  Added red black and binary tree classes

  Revision 1.4  1999/09/14 11:59:39  schubige
  Added @serial and transient for javadoc

  Revision 1.3  1999/09/14 11:48:13  schubige
  Updated some preferences realted classes

  Revision 1.2  1999/09/10 06:54:19  schubige
  Dialogs & requesters are now placed at 1/2 x and 1/3 y of the screen.

  Revision 1.1  1999/09/09 14:32:12  schubige
  Added Line, DateChooser and Dialog
  
*/
