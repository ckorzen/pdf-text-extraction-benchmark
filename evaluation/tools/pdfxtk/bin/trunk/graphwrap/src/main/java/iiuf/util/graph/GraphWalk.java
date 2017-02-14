package iiuf.util.graph;

/**
   Graph Walker interface.
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface GraphWalk {
  public void node(GraphModel graph, GraphNode node, Object userobject, int component);
}

/*
  $Log: GraphWalk.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/11/10 08:50:00  schubige
  iiuf tree cleanup iter 2

  Revision 1.1  2000/11/10 07:30:48  schubige
  iiuf tree cleanup iter 1

  Revision 1.2  2000/10/19 08:03:45  schubige
  Intermediate graph component related checkin

  Revision 1.1  2000/07/14 13:48:11  schubige
  Added graph stuff
  
*/
