/**
 * 
 */
package p2p.tests.unit.fec;

import junit.framework.TestCase;

/**
 * @author knielsen
 *
 */
public class TestBitStrings extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestBitStrings.class);
    }

    /**
     * Constructor for TestBitStrings.
     * @param arg0
     */
    public TestBitStrings(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBitString() {
        byte b = -1;
        
        int i = b & 255;
        assertEquals(i, 255);
        assertEquals(b & 255, 255);
    }
}
