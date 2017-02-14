package iiuf.util.graph;

/**
   Graph exception, thrown when graph invariants are violated.

   (c) 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public class GraphException 
  extends
  Exception 
{
  public GraphException() {}
  public GraphException(String msg) {
    super(msg);
  }
}

/*
  $Log: GraphException.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:43  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/11/10 10:07:03  schubige
  iiuf tree cleanup iter 3

  Revision 1.1  2000/11/10 07:30:48  schubige
  iiuf tree cleanup iter 1

  Revision 1.1  2000/07/14 13:48:11  schubige
  Added graph stuff
  
*/
