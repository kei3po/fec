/**
 * 
 */
package p2p.tests.nonfunctional.fec;

import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import p2p.fec.SystematicCodec;
import p2p.tests.util.StopWatch;

/**
 * @author knielsen
 *
 */
public class SystematicCodecTimingTest extends TestCase {

    private static int K = 64;
    private static int N = 128;
    private static int PACKET_SIZE = 1024;
    
    private SystematicCodec codec;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SystematicCodecTimingTest.class);
    }

    /**
     * Constructor for SystematicCodecTimingTest.
     * @param arg0
     */
    public SystematicCodecTimingTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        this.codec = new SystematicCodec(K, N);
        stopWatch.stop();
        stopWatch.printDuration(System.out);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        
        this.codec = null;
    }

    /*
     * Test method for 'p2p.fec.SystematicCodec.encode(short[])'
     */
    public void testEncode() {
        short[] data = new short[K];
        for (int i = 0; i < K; i++) {
            data[i] = (short)(i);
        }
        
        StopWatch stopWatch = new StopWatch();
        
        stopWatch.start();
        short[] encodedData = codec.encode(data);
        stopWatch.stop();
        stopWatch.printDuration(System.out);
        
        assertEquals(encodedData.length, N);
        
        Set<Integer> indexSet = new TreeSet<Integer>();
        Random randomGenerator = new Random(System.currentTimeMillis());
        
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
        
        stopWatch.start();
        short[] decodedData = codec.decode(encodedDataPrime, indices);
        stopWatch.stop();
        stopWatch.printDuration(System.out);
        
        for (int i = 0; i < K; i++) {
            assertEquals(data[i], decodedData[i]);
        }
    }
    
    public void testPacketCodec() {
        Random randomGenerator = new Random(System.currentTimeMillis());
        StopWatch stopWatch = new StopWatch();
        
        short[][] data = new short[K][PACKET_SIZE];
        for (int i = 0; i < K; i++) {
            short[] packet = new short[PACKET_SIZE];
            for (int j = 0; j < PACKET_SIZE; j++) {
                packet[j] = (short)randomGenerator.nextInt(N);
            }
            data[i] = packet;
        }
        
        stopWatch.start();
        short[][] encodedData = codec.encodePackets(data);
        stopWatch.stop();
        stopWatch.printDuration(System.out);
        
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
        
        stopWatch.start();
        short[][] decodedData = codec.decodePackets(encodedDataPrime, indices);
        stopWatch.stop();
        stopWatch.printDuration(System.out);
        
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