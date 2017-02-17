package at.ac.tuwien.dbai.pdfwrap.utils;

import at.ac.tuwien.dbai.pdfwrap.model.document.GenericSegment;
import org.junit.Test;

public class SegmentUtilsTest {

    @Test
    public void testXCollision() {
        GenericSegment s1 = new GenericSegment( 10, 20, 10, 20 );
        GenericSegment s2 = new GenericSegment( 15, 25, 15, 25 );
        assert SegmentUtils.intersects( s1, s2 );
    }


}
