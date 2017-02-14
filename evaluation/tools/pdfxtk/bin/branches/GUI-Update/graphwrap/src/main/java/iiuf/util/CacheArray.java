package iiuf.util;

/**
   Cache array implementation.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>
   
   @author $Author: ohitz $
   @version $Name:  $ $Revision: 1.1 $
*/
public final class CacheArray {
  
  private CacheArrayBackEnd be;
  private Object[]          cache;
  private long[]            indexes;
  private boolean[]         modified;
  private int               M_CHUNK;
  private long              LM_CHUNK;
  private int               CHUNK_SZ;
  private int               MAX_SIZE;
  private long              miss_count;
  private long              access_count;
  
  /**
     Creates a new cache array.
     
     @param size    The numbers of chunks the cache array holds.
     @param backend The backen to use to read/write the chunks.
  */
  public CacheArray(int size, CacheArrayBackEnd backend) {
    be       = backend;
    CHUNK_SZ = be.chunkSize();
    cache    = new Object[size * CHUNK_SZ];
    indexes  = new long[cache.length];
    modified = new boolean[cache.length];
    M_CHUNK  = ~(CHUNK_SZ - 1);
    LM_CHUNK = CHUNK_SZ - 1;
    LM_CHUNK = ~LM_CHUNK;
    MAX_SIZE = be.maxChunks() * CHUNK_SZ;
  }
  
  public Object elementAt(long index) {
    access_count++;
    int    idx    = (int)(index & 0x7FFFFFFF) % cache.length;
    Object result = cache[idx];
    if(indexes[idx] != index || result == null) {
      int  start  = idx   & M_CHUNK;
      long lstart = index & LM_CHUNK;
      flush(start, CHUNK_SZ);
      be.read(lstart, cache, start, CHUNK_SZ);
      for(int i = 0; i < CHUNK_SZ; i++)
	indexes[start + i] = lstart++;
      result = cache[idx];
      miss_count++;
    }
    return result;
  }
  
  private void getChunk(long index, Object[] data, int offset) {
    int idx = (int)(index & 0x7FFFFFFF) % cache.length;
    for(int i = 0; i < CHUNK_SZ; i++) {
      data[offset++] = (indexes[idx] != (index + i) || cache[idx] == null) ? null : cache[idx];
      idx = (idx + 1) % cache.length;
    }
  }
  
  public void resetStat() {
    miss_count   = 0;
    access_count = 0;
  }
  
  public long missCount() {
    return miss_count;
  }
  
  public long accessCount() {
    return access_count;
  }

  public void setElementAt(Object element, long index) {
  }
  
  public Object[] elementsAt(long start, int count) {
    if(count == 1) return new Object[] {elementAt(start)};
    access_count += count;
    
    long     lstart = start & LM_CHUNK; 
    Object[] tmp    = new Object[((count + CHUNK_SZ + CHUNK_SZ) - 1) & M_CHUNK];

    boolean clustering = false;
    long    cl_start  = 0;
    int     cl_count  = 0;
    int     tmp_start = 0;
    for(int i = 0; i < tmp.length; i += CHUNK_SZ) {
      getChunk(lstart + i, tmp, i);
      if(cl_count >= MAX_SIZE) {
	be.read(cl_start, tmp, tmp_start, cl_count);
	miss_count += cl_count;
	cl_start  = lstart + i;
	tmp_start = i;
	cl_count  = 0;
      }
      if(tmp[i] == null) {
	if(!clustering) {
	  clustering = true;
	  cl_start  = lstart + i;
	  tmp_start = i;
	  cl_count  = 0;
	}
	cl_count += CHUNK_SZ;
      }
      else {
	if(clustering) {
	  clustering = false;
	  be.read(cl_start, tmp, tmp_start, cl_count);
	  miss_count += cl_count;
	}
      }
    }
    if(clustering) {
      be.read(cl_start, tmp, tmp_start, cl_count);
      miss_count += cl_count;
    }
    
    // create result
    
    Object[] result = new Object[count];
    System.arraycopy(tmp, (int)(start - lstart), result, 0, count);
    
    // write result back to cache

    for(int i = 0; i < tmp.length; i++) {
      int idx = (int)((lstart + i)& 0x7FFFFFFF) % cache.length;
      cache[idx]    = tmp[i];
      modified[idx] = false;
      indexes[idx]  = lstart + i;
    }

    return result;
  }

  public void setElementsAt(long start, Object[] elements) {
  }
  
  public void flush() {
    flush(0, cache.length);
  }
  
  private void flush(int start, int count) {
    start &= M_CHUNK;
    count += CHUNK_SZ + CHUNK_SZ - 1;
    count &= M_CHUNK;
    if(start + count > cache.length)
      count = cache.length - start;
    for(int i = 0; i < count; i+= CHUNK_SZ) {
      boolean dowrite = false;
      for(int j = 0; j < CHUNK_SZ; j++) {
	dowrite |= modified[start + i + j];
	modified[start + i + j] = false;
      }
      if(dowrite)
	be.write(indexes[start + i], cache, start + i, CHUNK_SZ);
    }
  }
  
  public void clear() {
    for(int i = 0; i < cache.length; i++) {
      cache[i]    = null;
      modified[i] = false;
    }
  }

  static int N_TESTS;
  static int CHUNK_SIZE;
  
  public static void main(String[] argv) {
    N_TESTS    = Integer.parseInt(argv[0]);
    CHUNK_SIZE = Integer.parseInt(argv[1]);
    
    CacheArray ca = new CacheArray(N_TESTS, new CacheArrayBackEnd() {
	public int  chunkSize() {return CHUNK_SIZE;}
	public int  maxChunks() {return Integer.MAX_VALUE;}
	public void read(long start, Object[] data, int idx, int count) {
	  for(long i = 0; i < count; i++)
	    data[idx + (int)i] = new Long(start + i);
	}
	public void     write(long start, Object[] data, int idx, int count) {}
      });
    
    System.out.print("Testing random access...");
    for(int i = 0; i < N_TESTS; i++) {
      long r = Util.longRandom(Long.MAX_VALUE);
      if(r != ((Long)ca.elementAt(r)).longValue())
	System.out.println("Mismatch:" + r + " != " + ((Long)ca.elementAt(r)).longValue());
    }
    System.out.println("ok. miss:" + (double)(((double)ca.missCount() * 100.0) / (double)ca.accessCount()) + "%");
    ca.clear();
    ca.resetStat();

    System.out.print("Testing sequential access...");
    for(int i = 0; i < N_TESTS; i++) {
      long r = i;
      if(r != ((Long)ca.elementAt(r)).longValue())
	System.out.println("Mismatch:" + r + " != " + ((Long)ca.elementAt(r)).longValue());
    }
    System.out.println("ok. miss:" + (double)(((double)ca.missCount() * 100.0) / (double)ca.accessCount()) + "%");
    ca.clear();
    ca.resetStat();
    
    System.out.print("Testing random block access...");
    for(int i = 0; i < N_TESTS; i++) {
      long r = Util.longRandom(Long.MAX_VALUE);
      Object[] result = ca.elementsAt(r, 100);
      for(int j = 0; j < 100; j++) {
	if(r != ((Long)result[j]).longValue())
	  System.out.println("Mismatch:" + r + " != " + ((Long)result[j]).longValue());
	r++;
      }
    }
    System.out.println("ok. miss:" + (double)(((double)ca.missCount() * 100.0) / (double)ca.accessCount()) + "%");
  }
}

/*
  $Log: CacheArray.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.2  2001/01/04 16:28:41  schubige
  Header update for 2001 and DIUF

  Revision 1.1  1999/11/26 08:53:25  schubige
  add some files to cvs tree
  
*/
