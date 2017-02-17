package at.ac.tuwien.dbai.pdfwrap.comparators;


import at.ac.tuwien.dbai.pdfwrap.model.document.TextLine;
import at.ac.tuwien.dbai.pdfwrap.model.graph.AdjacencyEdge;
import at.ac.tuwien.dbai.pdfwrap.utils.SerializationUtil;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;


public class EdgeAttributeComparatorTest {
    EdgeAttributeComparator cmp;
    TextLine tl1;
    TextLine tl2;
    TextLine tl3;
    TextLine tl4;

    AdjacencyEdge<TextLine> ae1;
    AdjacencyEdge<TextLine> ae2;
    AdjacencyEdge<TextLine> ae3;


    @Before
    public void setUp() {
        cmp = new EdgeAttributeComparator();

        tl1 = new TextLine(100, 150, 100, 110, "This is line 1", "Helvetica", 10);
        tl2 = new TextLine(100, 190, 88, 98, "This is line 2, below line 1", "Helvetica", 10);
        tl3 = new TextLine(100, 190, 70, 78, "... and another line, some distance below", "Helvetica", 10);
        tl4 = new TextLine(100, 190, 58, 68, "... and another line with the same spacing", "Helvetica", 10);

        // shorter edge
        ae1 = new AdjacencyEdge<TextLine>(tl1, tl2, AdjacencyEdge.REL_BELOW);

        // longer edge
        ae2 = new AdjacencyEdge<TextLine>(tl2, tl3, AdjacencyEdge.REL_BELOW);

        // yet another shorter edge
        ae3 = new AdjacencyEdge<TextLine>(tl3, tl4, AdjacencyEdge.REL_BELOW);
    }



    @Test
    public void sortShorterEdgeComparator() {

        assertTrue( "Comparator compare fail", cmp.compare(ae1, ae2) < 0);

        ArrayList list = new ArrayList();
        list.add( ae2 );
        list.add( ae1 );

        Collections.sort( list, cmp );

        assertEquals( "list sorting failed", ae1, list.get( 0 ) );
    }



    @Test
    public void smallerWidthDifference() {
        assertTrue( "Comparator compare fail", cmp.compare(ae3, ae1) < 0);
        assertTrue( "Comparator compare fail", cmp.compare(ae3, ae2) < 0);

        ArrayList list = new ArrayList();
        list.add( ae1 );
        list.add( ae3 );

        Collections.sort( list, cmp );

        assertEquals( "list sorting failed", ae3, list.get( 0 ) );
    }


    @Test
    public void complexSortCondition() throws URISyntaxException {
        URL url = EdgeAttributeComparatorTest.class.getClassLoader().getResource( "testComparator1.xml" );

        try {
            File file = new File( url.toURI() );
            EdgeAttributeComparator cmp = new EdgeAttributeComparator();
            List col = new ArrayList( SerializationUtil.readDump( file ) );

            Collections.sort( col, cmp );

            assertTrue( true );

        } catch ( Exception e ) {
            fail( "sorting failed: " + e );
        }
    }

}
