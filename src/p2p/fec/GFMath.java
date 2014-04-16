/**
 * 
 */
package p2p.fec;

/**
 * @author knielsen
 *
 */
public class GFMath {
    public static short GF_SIZE = 255;
    public static short GF_BITS = 8;
    
    private static short gfExpMap[];
    private static int gfLogMap[];
    private static short gfInvMap[];
    private static short gfMultMap[][];
    
    // initialize static lookup arrays
    static {
        gfExpMap = new short[2 * GF_SIZE];
        gfLogMap = new int[GF_SIZE + 1];
        gfInvMap = new short[GF_SIZE + 1];
        gfMultMap = new short[GF_SIZE + 1][GF_SIZE + 1];
        generateLookupMaps();
    }

    private static void generateLookupMaps()
    {
        // 1+x^2+x^3+x^4+x^8 primitive polynomial
        char[] pp = new char[] {'1', '0', '1', '1', '1', '0', '0', '0', '1'};  
        short mask = 1;  // x ** 0 = 1
        
        // generate gfExpMap and gfLogMap
        gfExpMap[GF_BITS] = 0;
        
        for (int i = 0; i < GF_BITS; i++, mask <<= 1) {
            gfExpMap[i] = mask;
            gfLogMap[gfExpMap[i]] = i;
            
            if (pp[i] == '1') {
                gfExpMap[GF_BITS] ^= mask;
            }
        }
        
        gfLogMap[gfExpMap[GF_BITS]] = GF_BITS;
        
        mask = (short)(1 << (GF_BITS - 1));
        for (int i = GF_BITS + 1; i < GF_SIZE; i++) {
            if (gfExpMap[i - 1] >= mask) {
                gfExpMap[i] = (short)
                    (gfExpMap[GF_BITS] ^ ((gfExpMap[i - 1] ^ mask) << 1));
            }else {
                gfExpMap[i] = (short)(gfExpMap[i - 1] << 1);
            }
            
            gfLogMap[gfExpMap[i]] = i;
        }
        
        gfLogMap[0] = 0;
        for (int i = 0; i < GF_SIZE; i++) {
            gfExpMap[i + GF_SIZE] = gfExpMap[i];
        }
        
        // generate gfInvMap
        gfInvMap[0] = 0;
        gfInvMap[1] = 1;
        for (int i = 2; i <= GF_SIZE; i++) {
            gfInvMap[i] = gfExpMap[GF_SIZE - gfLogMap[i]];
        }
        
        // generate gfMultMap
        for (int i = 0; i < GF_SIZE + 1; i++) {
            for (int j = 0; j < GF_SIZE + 1; j++) {
                gfMultMap[i][j] = 
                    gfExpMap[gfMod(gfLogMap[i] + gfLogMap[j])];
            }
        }
        
        for (int j = 0; j < GF_SIZE + 1; j++) {
            gfMultMap[0][j] = gfMultMap[j][0] = 0;
        }
    }
    
    public static short gfMod(int n) {
        while (n >= GF_SIZE) {
            n -= GF_SIZE;
            n = ((n >> GF_BITS) + (n & GF_SIZE));
        }
        return (short)n;
    }
    
    public static short gfExp(short n) {
        return gfExpMap[n];
    }
    
    public static int gfLog(short n) {
        return gfLogMap[n];
    }
    
    public static short gfMultiply(short m, short n) {
        return gfMultMap[m][n];
    }
    
    public static short gfInverse(short n) {
        return gfInvMap[n];
    }
}