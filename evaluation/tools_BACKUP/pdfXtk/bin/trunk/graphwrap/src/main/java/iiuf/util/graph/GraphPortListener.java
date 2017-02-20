package iiuf.util.graph;

import java.util.EventListener;

/**
   Graph port listener interface.<p>
   
   (c) 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface GraphPortListener 
  extends
  EventListener
{
  public void connected(GraphPort port, GraphEdge edge);
  public void disconnected(GraphPort port, GraphEdge edge);
}

/*
  $Log: GraphPortListener.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.5  2001/04/11 19:02:08  schubige
  fixed connection bug and made JSliderSoundlet domable

  Revision 1.4  2001/03/09 15:30:51  schubige
  Added markers to graph panel

  Revision 1.3  2001/02/14 17:25:38  schubige
  implemented resizing, select all and key-shortcuts for graph panel

  Revision 1.2  2001/01/03 15:23:51  schubige
  graph stuff beta

  Revision 1.1  2000/12/28 09:30:37  schubige
  SourceWatch beta
  
*/
