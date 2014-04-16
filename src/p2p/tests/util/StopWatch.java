/**
 * 
 */
package p2p.tests.util;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author knielsen
 */
public class StopWatch {
    private long startTime;
    private long duration;
    
    public StopWatch() {
    }
    
    public void start() {
        this.startTime = System.nanoTime();
    }
    
    public void stop() {
        this.duration = System.nanoTime() - this.startTime;
    }
    
    public void reset() {
        this.startTime = 0;
        this.duration = 0;
    }
    
    public long duration() {
        return this.duration;
    }
    
    public void printDuration(OutputStream out) {
        String message = "duration: " + duration + "ns";
        
        if (out instanceof PrintStream) {
            ((PrintStream)out).println(message); 
        }else {
            new PrintStream(out).println(message);            
        }
    }
}