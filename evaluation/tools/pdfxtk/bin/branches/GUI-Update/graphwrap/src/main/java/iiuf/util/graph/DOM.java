package iiuf.util.graph;

import java.util.Iterator;

import iiuf.dom.DOMManager;
import iiuf.dom.DOMHandler;
import iiuf.dom.DOMContext;
import iiuf.util.NotImplementedException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
   DOM handler for graph.<p>

   (c) 2001, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public abstract class DOM {
  public static final String ATTR_FROM_NODE = "from_node";
  public static final String ATTR_FROM_PORT = "from_port";
  public static final String ATTR_TO_NODE   = "to_node";
  public static final String ATTR_TO_PORT   = "to_port";
  public static final String ATTR_ID        = "id";
  public static final String ATTR_NODES     = "nodes";
  public static final String ATTR_EDGES     = "edges";
  public static final String CTX_NODES      = "nodes";

  private static boolean inited;
  
  public static synchronized void init() {
    if(inited) return;
    inited = true;

    iiuf.util.DOM.init();
    iiuf.awt.DOM.init();
    iiuf.swing.DOM.init();
    iiuf.swing.graph.DOM.init();
    
    DOMManager.register(AbstractGraphModel.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {	  
	  GraphModel  model = (GraphModel)object;	  
	  GraphNode[] nodes = (GraphNode[])DOMManager.get(context, element, ATTR_NODES);
	  context.put(CTX_NODES, nodes);

	  model.add(nodes);	  
	  model.add((GraphEdge[])DOMManager.get(context, element, ATTR_EDGES));
	  
	  return object;
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {
	  GraphModel  model = (GraphModel)object;
	  GraphNode[] nodes = model.nodesArray();
	  context.put(CTX_NODES, nodes);
	  	  
	  DOMManager.put(context, element, ATTR_NODES, nodes);
	  DOMManager.put(context, element, ATTR_EDGES, model.edgesArray());
	  
	  return element;
	}

	public int getVersion() {return 0;}
      });
    
    DOMManager.register(DefaultGraphNode.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {	  	 
	  return object;
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {
	  return element;
	}	

	public int getVersion() {return 0;}
      });
    
    DOMManager.register(DefaultGraphEdge.class, new DOMHandler() {
	public Object fromDOM(DOMContext context, Element element, Object object) {	  
	  return idx2node(context, DOMManager.getInt(element, ATTR_FROM_NODE)).
	    getPort(DOMManager.getInt(element,  ATTR_FROM_PORT)).
	    createEdge(idx2node(context, DOMManager.getInt(element, ATTR_TO_NODE)).
		       getPort(DOMManager.getInt(element,  ATTR_TO_PORT)));
	}
	
	public Element toDOM(DOMContext context, Element element, Object object) {
	  DefaultGraphEdge edge = (DefaultGraphEdge)object;
	  DOMManager.put(element, ATTR_FROM_NODE, node2idx(context, edge.getFromNode()));
	  DOMManager.put(element, ATTR_FROM_PORT, edge.getFromPort().getIndex());
	  DOMManager.put(element, ATTR_TO_NODE,   node2idx(context, edge.getToNode()));
	  DOMManager.put(element, ATTR_TO_PORT,   edge.getToPort().getIndex());
	  return element;
	}

	public int getVersion() {return 0;}

	private int node2idx(DOMContext context, GraphNode node) {
	  GraphNode[] nodes = (GraphNode[])context.get(CTX_NODES);
	  for(int i = 0; i < nodes.length; i++)
	    if(nodes[i] == node)
	      return i;
	  throw new IllegalArgumentException("Node " + node + " not found.");
	}
	
	private GraphNode idx2node(DOMContext context, int index) {
	  return ((GraphNode[])context.get(CTX_NODES))[index];
	}
      });
 }
}

/*
  $Log: DOM.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.7  2001/04/06 09:50:14  schubige
  fixed vendor info, edge creation and format bugs

  Revision 1.6  2001/03/28 21:31:18  schubige
  dom save and load works now (very early version)

  Revision 1.5  2001/03/28 18:44:30  schubige
  working on dom again

  Revision 1.4  2001/03/26 15:35:33  schubige
  fixed format bug

  Revision 1.3  2001/03/22 16:08:19  schubige
  more work on dom stuff

  Revision 1.2  2001/03/21 22:18:14  schubige
  working on dom stuff

  Revision 1.1  2001/03/21 19:37:42  schubige
  started with dom stuff
  
*/
