package iiuf.util.graph;

import java.util.Collection;

import iiuf.util.Attributable;
import iiuf.util.AttributeFactory;

/**
   Graph model interface.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $
*/
public interface GraphModel 
  extends
  Attributable
{
  public void        addGraphModelListener(GraphModelListener l);
  public void        removeGraphModelListener(GraphModelListener l);

  public void        add(GraphEdge      edge);
  public void        add(GraphEdge      edge, Object[] args);
  public void        add(GraphEdge[]    edges);
  public void        remove(GraphEdge   edge);  
  public void        remove(GraphEdge[] edges);  

  public void        add(GraphNode      node);
  public void        add(GraphNode      node, Object[] args);
  public void        add(GraphNode[]    nodes);
  public void        remove(GraphNode   node);
  public void        remove(GraphNode[] nodes);

  public Collection  edges();
  public Collection  nodes();
  public GraphEdge[] edgesArray();
  public GraphNode[] nodesArray();

  public int         portAttribute(String id,  AttributeFactory factory);
  public int         nodeAttribute(String id,  AttributeFactory factory);
  public int         edgeAttribute(String id,  AttributeFactory factory);
  public int         graphAttribute(String id, AttributeFactory factory);

  public boolean     isEdgeAttribute(int id);
  public boolean     isNodeAttribute(int id);
  public boolean     isGraphAttribute(int id);
  public boolean     isPortAttribute(int id);

  public String      getId(int id);
  public int[]       getIds(String id);
}

/*
  $Log: GraphModel.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.4  2001/04/30 07:33:17  schubige
  added webcom to cvstree

  Revision 1.3  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/12/18 12:39:09  schubige
  Added ports to iiuf.util.graph

  Revision 1.1  2000/11/10 07:33:22  schubige
  iiuf tree cleanup iter 1
  
*/
