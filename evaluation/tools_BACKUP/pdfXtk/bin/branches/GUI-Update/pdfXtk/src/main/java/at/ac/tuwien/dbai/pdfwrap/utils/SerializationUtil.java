package at.ac.tuwien.dbai.pdfwrap.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


/**
 * simple util to dump data
 *
 * @author ck
 * @since v0.9
 */
public class SerializationUtil {

    public static void dumpObjects( Collection objects ) throws IOException {
        StringBuffer buffer = new StringBuffer();

        XStream xstream = new XStream(new DomDriver());
        buffer.append( xstream.toXML( objects ) );

        File tmp = File.createTempFile( "hua",".xml" );
        IOUtils.write(  buffer.toString(),new FileOutputStream( tmp ));

        System.out.println( tmp.getAbsolutePath() );
    }


    public static Collection readDump( File file ) throws IOException {
        ArrayList<Object> ret = new ArrayList<Object>();
        XStream xstream = new XStream(new DomDriver());

        return ( Collection) xstream.fromXML(  IOUtils.toString( file.toURI() ) );
    }



}
