package iiuf.util;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import iiuf.io.UUDecodeStream;
import iiuf.io.UUEncodeStream;

/**
   Some useful data transformation.

   (c) 1999, 2000, 2001, IIUF, DIUF<p>

   @author $Author: ohitz $
   @version $Revision: 1.1 $
*/
public class Trans {
  
  private static byte[] int2byte(int i) {
    byte[] result = new byte[4];
    result[0] = (byte)(i >> 0);
    result[1] = (byte)(i >> 8);
    result[2] = (byte)(i >> 16);
    result[3] = (byte)(i >> 24);
    return result;
  }

  private static int byte2int(byte[] b) {
    return 
      ((((int)b[3]) & 0xFF) << 24) | 
      ((((int)b[2]) & 0xFF) << 16) | 
      ((((int)b[1]) & 0xFF) << 8 ) | 
      ((((int)b[0]) & 0xFF)      );
  }

  public static byte[] object2byte(Object object) 
    throws IOException {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();    
      ObjectOutputStream    out  = new ObjectOutputStream(bout);
      out.writeObject(object);
      out.close();
      return bout.toByteArray();
    }
  
  public static Object byte2object(byte[] b) 
    throws IOException, ClassNotFoundException { 
    ObjectInputStream in = 
      new ObjectInputStream(new ByteArrayInputStream(b));
    Object result = in.readObject();
    in.close();
    return result;
  }

  public static byte[] uuDecode(byte[] uuencoded) 
    throws IOException, ClassNotFoundException {
      UUDecodeStream in  =  new UUDecodeStream(new ByteArrayInputStream(uuencoded)); 
      byte[]         len = new byte[4];
      in.read(len);
      byte[] result = new byte[byte2int(len)];
      in.read(result);
      in.close();
      return result;
    }
  
  public static byte[] uuEncode(byte[] b, int permission, String name)
    throws IOException {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();    
      UUEncodeStream        out  = new UUEncodeStream(bout, permission, name);
      out.write(int2byte(b.length));
      out.write(b);
      out.close();
      return bout.toByteArray();
    }
  
  private static final char[] HEXTAB = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
  
  public static String hex(byte[] b) {
    char result[] = new char[b.length * 2];
    for(int i = 0; i < b.length; i++) {
      result[i * 2]     = HEXTAB[(b[i] >> 4) & 0x0F];
      result[i * 2 + 1] = HEXTAB[b[i] & 0x0F];
    }
    
    return new String(result);
  }

  private static int hex2dec(char c) {
    int result = c - '0';
    return result < 10 ? result : c - 'A' + 10;
  }
  
  public static byte[] unhex(String s) {
    byte[] result = new byte[s.length() / 2];
    
    for(int i = 0; i < s.length(); i += 2)
    result[i / 2] = (byte)((hex2dec(s.charAt(i)) << 4) | (hex2dec(s.charAt(i + 1)))); 
    
    return result;
  }
  
  public static byte[] zip(byte[] unzipped) 
    throws IOException {
      ByteArrayOutputStream bout  = new ByteArrayOutputStream();    
      GZIPOutputStream      zout  = new GZIPOutputStream(bout);
      zout.write(int2byte(unzipped.length));
      zout.write(unzipped);
      zout.close();
      return bout.toByteArray();
    }
  
  public static byte[] unzip(byte[] zipped) 
    throws IOException {
      GZIPInputStream zin = new GZIPInputStream(new ByteArrayInputStream(zipped));
      byte[] len          = new byte[4];
      zin.read(len);
      byte[] result = new byte[byte2int(len)];      
      zin.read(result);
      zin.close();
      return result;
  }

  public static void main(String[] argv) {
    System.out.println("Testing zip/unzip...");
    try { 
      System.out.println("### Zipping 100 * Hello World!");
      String hello = "";
      for(int i = 0; i < 10; i++)
	hello += "[" + i + "] Hello World!\n";
      byte[] unzipped = hello.getBytes();
      System.out.println("Unzipped:" + unzipped.length);
      System.out.println("Zipped:"   + zip(unzipped).length);
      System.out.println("### unzip(zip(hello))");
      System.out.println(new String(unzip(zip(hello.getBytes()))));
    } catch(Exception e) {
      e.printStackTrace();
    }
    System.out.println("Testing uuEncode/uuDecode...");
    try {
      int    p = 0777;
      String n = "test";
      Object enc;
      System.out.println("### uuEncode(new Character('A'))):");
      System.out.println(new String(uuEncode(object2byte(new Character('A')), p, n)));      
      System.out.println("### uuDecode(uuEncode(new Character('A'))):");
      enc = byte2object(uuDecode(uuEncode(object2byte(new Character('A')), p, n)));
      System.out.println(enc.getClass() + ":" + enc);
      System.out.println("### uuEncode(new Integer())):"); 
      System.out.println(new String(uuEncode(object2byte(new Integer(0)), p, n)));
      System.out.println("### uuDecode(uuEncode(new Integer())):"); 
      enc = byte2object(uuDecode(uuEncode(object2byte(new Integer(0)), p, n)));
      System.out.println(enc.getClass() + ":" + enc);
      System.out.println("### uuEncode(\"Hello\")):"); 
      System.out.println(new String(uuEncode(object2byte("Hello World!"), p, n)));
      System.out.println("### uuDecode(uuEncode(\"Hello World!\")):");
      enc = byte2object(uuDecode(uuEncode(object2byte("Hello World!"), p, n)));
      System.out.println(enc.getClass() + ":" + enc);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}

/*
  $Log: Trans.java,v $
  Revision 1.1  2002/07/11 12:00:11  ohitz
  Initial checkin

  Revision 1.3  2001/01/04 16:28:42  schubige
  Header update for 2001 and DIUF

  Revision 1.2  2000/04/25 12:03:35  schubige
  Bibtex db project restart

  Revision 1.1  1999/09/14 11:50:37  schubige
  Added trans
  
*/
