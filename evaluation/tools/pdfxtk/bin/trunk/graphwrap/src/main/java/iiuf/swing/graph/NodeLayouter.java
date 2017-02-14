package iiuf.swing.graph;

import java.awt.Dimension;

import iiuf.util.graph.GraphModel;

/**
   Node layouter interface.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface NodeLayouter {
  public Dimension layout(GraphPanel panel, GraphModel graph);
  public void      activate();
  public void      deactivate();
  public boolean   allowsNodeLocationChange();
}

/*
  $Log: NodeLayouter.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.1  2001/02/17 09:54:22  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.6  2001/01/04 16:28:39  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.4  2000/11/10 10:46:53  schubige
  iiuf tree cleanup iter 3

  Revision 1.3  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.2  2000/10/03 08:39:39  schubige
  Added tree view and contect menu stuff

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:56:20  schubige
  Added graph view stuff
  
*/
