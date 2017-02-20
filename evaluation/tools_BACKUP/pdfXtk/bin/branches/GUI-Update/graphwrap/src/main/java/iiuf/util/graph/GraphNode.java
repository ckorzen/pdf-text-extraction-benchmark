package iiuf.util.graph;

import iiuf.util.Attributable;

/**
   Graph node interface.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $
*/
public interface GraphNode 
  extends
  Attributable
{
  public static final int DEFAULT_PORT = 0;

  public final static int WHITE = 0;
  public final static int GRAY  = 1;
  public final static int BLACK = 2;
  
  public int         getOutCount();
  public GraphEdge[] getOut();
  public int         getInCount();
  public GraphEdge[] getIn();  
  public int         getEdgeCount();
  public GraphEdge[] getEdges();
  public GraphPort[] getPorts();
  public GraphPort   getPort(int index);
  public GraphPort   getDefaultPort();
  public int         getColor();
  public void        setColor(int color);
}

/*
  $Log: GraphNode.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.8  2001/03/22 16:08:19  schubige
  more work on dom stuff

  Revision 1.7  2001/03/21 19:34:06  schubige
  started with dom stuff

  Revision 1.6  2001/03/08 09:32:49  schubige
  intermim checkin

  Revision 1.5  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/12/28 09:29:11  schubige
  SourceWatch beta

  Revision 1.3  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.2  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.1  2000/11/10 07:30:48  schubige
  iiuf tree cleanup iter 1

*/
