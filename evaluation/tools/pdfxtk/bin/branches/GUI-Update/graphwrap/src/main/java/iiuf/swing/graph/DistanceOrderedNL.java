package iiuf.swing.graph;

import java.awt.Dimension;
import java.awt.Component;

import iiuf.util.graph.GraphModel;
import iiuf.util.graph.GraphWalk;
import iiuf.util.graph.GraphNode;
import iiuf.util.graph.DefaultGraphNode;
import iiuf.util.graph.Utilities;

/**
   Node layouter implementation that orders nodes starting by their distance from a source node.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class DistanceOrderedNL
  implements
  NodeLayouter
{
  public static final int NORTH = 0;
  public static final int EAST  = 1;
  public static final int SOUTH = 2;
  public static final int WEST  = 3;
  
  private DefaultGraphNode start;
  private int              direction;
  private static final int OFF = 10;
  
  public DistanceOrderedNL() {
    setDirection(EAST);
  }
  
  public DistanceOrderedNL(DefaultGraphNode start, int direction) {
    setStart(start);
    setDirection(direction);
  }

  public void setDirection(int direction_) {
    direction = direction_;
  }

  public void setStart(DefaultGraphNode start_) {
    start = start_;
  }
  
  class IterState {
    int    x;
    int    y;
    int    maxx;
    int    maxy;
    double d;
    int    COMPONENT;
    int    EDGE;
    
    IterState(int component, int edge) {
      COMPONENT = component;
      EDGE = edge;      
    }
  }
  
  public Dimension layout(final GraphPanel panel, GraphModel graph) {
    if(start == null)
      setStart((DefaultGraphNode)graph.nodesArray()[0]);
    
    IterState s = new IterState(panel.COMPONENT, panel.GRAPH_EDGE);
    
    Utilities.breadthFirst(graph, start, s, true, new GraphWalk() {
	public void node(GraphModel g, GraphNode node_, Object state_, int component) {
	  DefaultGraphNode node  = (DefaultGraphNode)node_;
	  IterState        state = (IterState)state_;
	  Component        cmp   = (Component)node.get(state.COMPONENT);
	  if(cmp == null) 
	    return;
	  if(panel.isDragging(node))
	    return;
	  Dimension        d     = cmp.getPreferredSize();
	  
	  if(node.distance != state.d) {
	    state.y = 0;
	    state.x = state.maxx + OFF;
	    state.d = node.distance;
	  }
	  cmp.setBounds(state.x, state.y, d.width, d.height);
	  
	  if(state.y > state.maxy)           state.maxy = state.y;
	  if(state.x + d.width > state.maxx) state.maxx = state.x + d.width;
	  state.y += d.height + OFF;
	}
      });
    
    return new Dimension(s.maxx, s.maxy);
  }

  public boolean allowsNodeLocationChange() {
    return false;
  }

  public void activate()   {}
  public void deactivate() {}
}

/*
  $Log: DistanceOrderedNL.java,v $
  Revision 1.1  2002/07/11 12:09:52  ohitz
  Initial checkin

  Revision 1.4  2001/05/11 11:30:26  schubige
  fns demo final

  Revision 1.3  2001/03/11 17:59:38  schubige
  Fixed various soundium and iiuf.swing.graph bugs

  Revision 1.2  2001/03/07 17:36:28  schubige
  soundium properties panel beta

  Revision 1.1  2001/02/17 09:54:21  schubige
  moved graph stuff to iiuf.swing.graph, started work on rotatable GraphNodeComponents

  Revision 1.7  2001/02/15 16:00:43  schubige
  Improved graph panel, fixed some soundium bugs

  Revision 1.6  2001/01/04 16:28:38  schubige
  Header update for 2001 and DIUF

  Revision 1.5  2001/01/04 12:12:36  schubige
  fixed bugs reported by iiuf.dev.java.Verify

  Revision 1.4  2001/01/03 09:39:10  schubige
  graph stuff beta

  Revision 1.3  2000/11/10 10:46:53  schubige
  iiuf tree cleanup iter 3

  Revision 1.2  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.1  2000/08/17 16:22:14  schubige
  Swing cleanup & TreeView added

  Revision 1.2  2000/07/28 12:06:54  schubige
  Graph stuff update

  Revision 1.1  2000/07/14 13:56:20  schubige
  Added graph view stuff
  
*/
