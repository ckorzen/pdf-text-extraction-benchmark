package iiuf.swing.graph;

import java.awt.Container;

/**
   Graph node component interface. Implement this interface if you like to
   have access to advanced GraphPanel features.<p>
   
   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public interface GraphNodeComponent {
  public final static int    NOT_ROTATABLE = -1;
  public final static int    ANGLE_0       = 0;
  public final static int    ANGLE_90      = 9000;
  public final static int    ANGLE_180     = 18000;
  public final static int    ANGLE_270     = 27000;
  public final static int    ANGLE_360     = 36000;

  public final static int    MIN_ANGLE     = 1;
  public final static int    MAX_ANGLE     = ANGLE_360 - 1;

  public final static int    ANGLE_TO_DEG  = 100;
  public final static double ANGLE_TO_RAD  = ANGLE_360 / (Math.PI * 2);
  
  public GraphNodePort getGraphNodePort(int index);
  public void          addComponent(GraphPanel panel, int idx);
  public int           getMinimalRotation();
  public int           getRotation();
  public void          setRotation(int angle);
  public void          dispose();
}
/*
  $Log: GraphNodeComponent.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.7  2001/04/06 09:50:14  schubige
  fixed vendor info, edge creation and format bugs

  Revision 1.6  2001/03/19 16:13:26  schubige
  soundium without drag cursor

  Revision 1.5  2001/03/08 09:32:49  schubige
  intermim checkin

  Revision 1.4  2001/02/23 17:23:11  schubige
  Added loop source to soundium and fxed some bugs along

  Revision 1.3  2001/02/23 11:03:15  schubige
  try to recover table_source.png

  Revision 1.2  2001/02/22 15:59:23  schubige
  Worked on FileReader soundlet

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.1  2001/02/15 16:01:56  schubige
  Improved graph panel, fixed some soundium bugs

  
*/
