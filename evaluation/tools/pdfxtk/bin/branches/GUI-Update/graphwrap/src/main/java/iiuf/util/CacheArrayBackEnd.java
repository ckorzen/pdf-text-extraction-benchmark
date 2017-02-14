package iiuf.util;

/**
   Cach array back end interface.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public interface CacheArrayBackEnd {
  /**
     The chunk size of the cache array (cace line size).
     
     @return The number of objects per chunk (must be a power of two).
   */
  public int chunkSize();
  /**
     The maximum number of chunks per read/write.

     @return The maximum number of allowed chunks per read/write.
   */
  public int maxChunks();
  /**
     Reads the number of object starting from start.
     
     @start The start index, a multiple of <code>chunkSize()</code>.
     @count The number of objects, a multiple of <code>chunkSize()</code>.
     @param index The start index into <code>data</code>, a multiple of <code>chunkSize()</code>.
     @param count The number of objects to write, a multiple of <code>chunkSize()</code>.
  */
  public void read(long start, Object[] data, int index, int count);
  /**
     Writes <code>count</code> number of object starting from <code>start</code>.
     
     @param start The start index, a multiple of <code>chunkSize()</code>.
     @param data  The objects to write.
     @param index The start index into <code>data</code>, a multiple of <code>chunkSize()</code>.
     @param count The number of objects to write, a multiple of <code>chunkSize()</code>.
  */
  public void write(long start, Object[] data, int index, int count);
}

/*
  $Log: CacheArrayBackEnd.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/11/26 08:53:25  schubige
  add some files to cvs tree
  
*/
