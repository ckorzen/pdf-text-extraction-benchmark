package iiuf.swing.graph;

import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.Rectangle;

/**
   Interface for graph edge routers / painters.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface GraphRouter {
  public void setupEdges(GraphPanel panel, GraphEdge[] edges, Component[] nodes);
  public void init();
}

/*
  $Log: GraphRouter.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.2  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.5  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.4  2000/12/29 08:03:55  schubige
  SourceWatch beta debug iter 1

  Revision 1.3  2000/12/28 09:29:10  schubige
  SourceWatch beta

  Revision 1.2  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:56:20  schubige
  Added graph view stuff
  
*/
