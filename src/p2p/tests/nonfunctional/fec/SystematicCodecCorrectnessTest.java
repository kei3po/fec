/**
 * 
 */
package p2p.tests.nonfunctional.fec;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import p2p.fec.SystematicCodec;

/**
 * @author knielsen
 *
 */
public class SystematicCodecCorrectnessTest extends TestCase {

    private static int K = 64;
    private static int N = 128;
    private static int PACKET_SIZE = 1024;
    
    private SystematicCodec codec;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SystematicCodecCorrectnessTest.class);
    }

    /**
     * Constructor for SystematicCodecCorrectnessTest.
     * @param arg0
     */
    public SystematicCodecCorrectnessTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        codec = new SystematicCodec(K, N);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        codec = null;
    }

    /*
     * Test method for 'p2p.fec.SystematicCodec.encode(short[])'
     */
    public void testEncode() {
        for (int i = 0; i < N; i++) {
            singleEncodeRun();
        }
    }
    
    private void singleEncodeRun() {
        Random randomGenerator = new Random(System.currentTimeMillis());

        short[] data = new short[K];
        for (int i = 0; i < K; i++) {
            data[i] = (short)randomGenerator.nextInt(N);
        }
        
        short[] encodedData = codec.encode(data);
        assertEquals(encodedData.length, N);
        
        Set<Integer> indexSet = new TreeSet<Integer>();
        
        short[] encodedDataPrime = new short[K];
        int[] indices = new int[K];
        
        for (int i = 0; i < K; i++) {
            int randomIndex = randomGenerator.nextInt(N);
            while (indexSet.contains(randomIndex)) {
                randomIndex = randomGenerator.nextInt(N);
            }
            indexSet.add(randomIndex);
        }
        
        int index = 0;
        for (int randomIndex : indexSet) {
            encodedDataPrime[index] = encodedData[randomIndex];
            indices[index] = randomIndex;
            index++;
        }
        
        short[] decodedData = codec.decode(encodedDataPrime, indices);
        assertEquals(decodedData.length, K);
        
        for (int i = 0; i < K; i++) {
            assertEquals(data[i], decodedData[i]);
        }        
    }

    /*
     * Test method for 'p2p.fec.SystematicCodec.encodePackets(short[][])'
     */
    public void testEncodePackets() {
        List<Exception> exceptions = new ArrayList<Exception>();
        for (int i = 0; i < N; i++) {
            try {
                singleEncodePacketsRun();                
            }catch (Exception e) {
                exceptions.add(e);
            }
        }
        
        if (exceptions.size() != 0) {
            fail("Number of errors: " + exceptions.size());
        }
    }
    
    private void singleEncodePacketsRun() {
        Random randomGenerator = new Random(System.currentTimeMillis());
        
        short[][] data = new short[K][PACKET_SIZE];
        for (int i = 0; i < K; i++) {
            short[] packet = new short[PACKET_SIZE];
            for (int j = 0; j < PACKET_SIZE; j++) {
                packet[j] = (short)randomGenerator.nextInt(N);
            }
            data[i] = packet;
        }
        
        short[][] encodedData = codec.encodePackets(data);
        assertEquals(rowDimension(encodedData), N);
        assertEquals(columnDimension(encodedData), PACKET_SIZE);
        
        Set<Integer> indexSet = new TreeSet<Integer>();        
        for (int i = 0; i < K; i++) {
            int randomIndex = randomGenerator.nextInt(N);
            while (indexSet.contains(randomIndex)) {
                randomIndex = randomGenerator.nextInt(N);
            }
            indexSet.add(randomIndex);
        }

        short[][] encodedDataPrime = new short[K][PACKET_SIZE];
        int[] indices = new int[K];       

        int index = 0;
        for (int randomIndex : indexSet) {
            encodedDataPrime[index] = encodedData[randomIndex];
            indices[index] = randomIndex;
            index++;
        }
        
        short[][] decodedData = codec.decodePackets(encodedDataPrime, indices);
        assertEquals(rowDimension(decodedData), K);
        assertEquals(columnDimension(decodedData), PACKET_SIZE);
        
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < PACKET_SIZE; j++) {
                assertEquals(data[i][j], decodedData[i][j]);                
            }
        }        
    }
    
    private int rowDimension(short[][] data) {
        return data.length;
    }
    
    private int columnDimension(short[][] data) {
        return data[0].length;
    }
}