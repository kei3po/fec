/**
 * 
 */
package p2p.fec;

/**
 * @author knielsen
 */
public class SystematicCodec {
    private static short GF_SIZE = 255;
    private static short GF_BITS = 8;
    
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
    
    private short[][] matrix;
    private int k;
    private int n;
    
    public SystematicCodec(int k, int n) {
        matrix = new short[n][k];
        this.k = k;
        this.n = n;
        generateMatrix(k, n);
    }
    
    public short[] encode(short[] data) {
        if (data.length != this.k) {
            throw new IllegalArgumentException(
                    "data vector must be dimension k: '" + this.k + "'");
        }
        
        short[][] dataTranspose = new short[this.k][1];
        for (int i = 0; i < this.k; i++) {
            dataTranspose[i][0] = data[i];
        }
        
        short[][] result = matrixMultiply(matrix, dataTranspose);
        short[] resultTranspose = new short[this.n];
        for (int i = 0; i < this.n; i++) {
            resultTranspose[i] = (short)result[i][0];
        }
        
        return resultTranspose;
    }
    
    public short[][] encodePackets(short[][] data) {
        if (data.length != this.k) {
            throw new IllegalArgumentException(
                    "data matrix must have row dimension k: '" + this.k + "'");
        }
        
        short[][] result = matrixMultiply(matrix, data);
        return result;
    }
    
    public short[] decode(short[] data, int[] indices) {
        if (data.length != this.k && indices.length != this.k) {
            throw new IllegalArgumentException(
                    "data is of inappropriate dimension");
        }
        
        short[][] decodeMatrix = buildDecodeMatrix(indices);
        short[][] dataTranspose = new short[k][1];
        for (int i = 0; i < this.k; i++) {
            dataTranspose[i][0] = data[i];
        }
        
        short[][] result = matrixMultiply(decodeMatrix, dataTranspose);
        short[] resultTranspose = new short[this.k];
        for (int i = 0; i < this.k; i++) {
            resultTranspose[i] = result[i][0];
        }
        
        return resultTranspose;
    }
    
    public short[][]decodePackets(short[][] data, int[]indices) {
        if (data.length != this.k && indices.length != this.k) {
            throw new IllegalArgumentException(
                    "data is of inappropriate dimension");
        }

        short[][] decodeMatrix = buildDecodeMatrix(indices);
        short[][] result = matrixMultiply(decodeMatrix, data);
        return result;
    }
    
    private short[][] buildDecodeMatrix(int[] indices) {
        if (indices.length != this.k) {
            throw new IllegalArgumentException("must have k:'" + this.k +
                    "' indices");
        }
        
        short[][] submatrix = new short[this.k][this.k];
        for (int i = 0; i < indices.length; i++) {
            for (int col = 0; col < this.k; col++) {
                submatrix[i][col] = matrix[indices[i]][col]; 
            }
        }
        
        invertMatrix(submatrix);
        return submatrix;
    }
    
    private void generateMatrix(int k, int n) {
        short tmp[][] = new short[n][k];
        tmp[0][0] = 1;
        for (int i = 1; i < k; i++) {
            tmp[0][i] = 0;
        }
        
        for (int row = 0; row < n - 1; row++) {
            for (int col = 0; col < k; col++) {
                tmp[row + 1][col] = gfExpMap[gfMod(row * col)];
            }
        }
        
        invertVandermonde(subsetRows(tmp, 0, k));
        short[][] lowerMatrix = 
            matrixMultiply(subsetRows(tmp, k, n), subsetRows(tmp, 0, k));
        
        for (int row = 0; row < k; row++) {
            for (int col = 0; col < k; col++) {
                if (row == col) {
                    matrix[row][col] = 1;
                }else {
                    matrix[row][col] = 0;
                }
            }
        }

        for (int row = k; row < n; row++) {
            for (int col = 0; col < k; col++) {
                matrix[row][col] = lowerMatrix[row - k][col];
            }
        }
    }
    
    private short[][] subsetRows(short[][] a, int start, int end) {
        if (end <= start || end > a.length) {
            throw new IllegalArgumentException(
                    "bad values for start and/or end");
        }
        
        short[][] result = new short[end - start][a[0].length];
        for (int i = start; i < end; i++) {
            result[i - start] = a[i];
        }
        return result;
    }
    
    private static short gfMod(int n) {
        while (n >= GF_SIZE) {
            n -= GF_SIZE;
            n = ((n >> GF_BITS) + (n & GF_SIZE));
        }
        return (short)n;
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
    
    private short[] addConstantMultiple(short[] destRow, short[] srcRow,
            short constant) {
        if (destRow.length != srcRow.length) {
            throw new IllegalArgumentException(
                    "destination and source must have same magnitude");
        }
        
        for (int i = 0; i < destRow.length; i++) {
            destRow[i] ^= gfMultMap[constant][srcRow[i]];
        }
        
        return destRow;
    }
    
    private short[][] matrixMultiply(short[][] a, short[][] b) {
        if (a == null || b == null || a[0].length != b.length) {
            throw new IllegalArgumentException(
                    "matrices do not match up for multiplication");
        }
        
        int n = a.length;
        int k = a[0].length;
        int m = b[0].length;
        
        short[][] c = new short[n][m];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < m; col++) {
                for (int i = 0; i < k; i++) {
                    c[row][col] ^= 
                        gfMultMap[a[row][i]][b[i][col]];
                }
            }
        }
        return c;
    }
    
    private short[][] invertMatrix(short[][] a) {
        if (a == null || !isSquare(a)) {
            throw new IllegalArgumentException("matrix must be square");
        }
        
        int irow, icol;
        int k = a.length;
        int[] indxc = new int[k];
        int[] indxr = new int[k];
        int[] ipiv = new int[k];
        
        short[] idrow = new short[k];
        
        for (int i = 0; i < k; i++) {
            ipiv[i] = 0;
            idrow[i] = 0;
        }
        
        for (int col = 0; col < k; col++) {
            short[] pivotRow;
            
            irow = icol = -1;
            if (ipiv[col] != 1 && a[col][col] != 0) {
                irow = col;
                icol = col;
            }else {
                for (int row = 0; row < k; row++) {
                    if (ipiv[row] != 1) {
                        for (int i = 0; i < k; i++) {
                            if (ipiv[i] == 0) {
                                if (a[row][i] != 0) {
                                    irow = row;
                                    icol = i;
                                    break;
                                }
                            }else if (ipiv[i] > 1) {
                                throw new IllegalArgumentException(
                                        "matrix is singular");
                            }
                        }
                    }
                    
                    if (irow != -1 && icol != -1) {
                        break;
                    }
                }                

                if (irow == -1 && icol == -1) {
                     throw new IllegalArgumentException(
                    "could not find pivot");
                }
            }
            
            ++ipiv[icol];
            if (irow != icol) {
                for (int i = 0; i < k; i++) {
                    short tmp = a[irow][i];
                    a[irow][i] = a[icol][i];
                    a[icol][i] = tmp;
                }
            }
            
            indxr[col] = irow;
            indxc[col] = icol;
            
            pivotRow = a[icol];
            
            short c = pivotRow[icol];
            if (c == 0) {
                throw new RuntimeException("matrix is singular");
            }
            
            if (c != 1) {
                c = gfInvMap[c];
                pivotRow[icol] = 1;
                for (int i = 0; i < k; i++) {
                    pivotRow[i] = gfMultMap[c][pivotRow[i]];
                }
            }
            
            idrow[icol] = 1;
            if (!rowEqual(pivotRow, idrow)) {
                for (int i = 0; i < k; i++) {
                    short[] p = a[i];
                    if (i != icol) {
                        c = p[icol];
                        p[icol] = 0;
                        addConstantMultiple(p, pivotRow, c);
                    }
                }
            }
            idrow[icol] = 0;
        }
        
        for (int col = k-1; col >= 0; col--) {
            if (indxr[col] != indxc[col]) {
                for (int row = 0; row < k; row++) {
                    short tmp = a[row][indxr[col]];
                    a[row][indxr[col]] = a[row][indxc[col]];
                    a[row][indxc[col]] = tmp;
                }
            }
        }
        
        return a;
    }
    
    private short[][] invertVandermonde(short[][] a) {
        if (a == null || !isSquare(a)) {
            throw new IllegalArgumentException("matrix must be square");
        }
        
        int k = a.length;
        
        short[] c = new short[k];
        short[] b = new short[k];
        short[] p = new short[k];
        
        for (int i = 0, j = 1; i < k; i++) {
            c[i] = 0;
            p[i] = a[i][j];
        }
        
        c[k-1] = p[0];
        for (int i = 1; i < k; i++) {
            short p_i = p[i];
            for (int j = (k - 1) - (i - 1); j < k - 1; j++) {
                c[j] ^= gfMultMap[p_i][c[j + 1]];
            }
            c[k - 1] ^= p_i;
        }
        
        for (int row = 0; row < k; row++) {
            short xx = p[row];
            short t = 1;
            b[k - 1] = 1;
            for (int i = k - 2; i >= 0; i--) {
                b[i] = (short)(c[i + 1] ^ gfMultMap[xx][b[i + 1]]);
                t = (short)(gfMultMap[xx][t] ^ b[i]);
            }
            
            for (int col = 0; col < k; col++) {
                a[row][col] = gfMultMap[gfInvMap[t]][b[col]];
            }
        }
        return a;
    }
    
    private boolean rowEqual(short[] row1, short[] row2) {
        boolean isEqual = false;
        if (row1.length == row2.length) {
            for (int i = 0; i < row1.length; i++) {
                if ((isEqual = (row1[i] == row2[i])) == false) {
                    break;
                }
            }
        }
        return isEqual;
    }
    
    private boolean isSquare(short[][] a) {
        boolean isSquare = false;
        if (a != null) {
            for (int row = 0; row < a.length; row++) {
                if (!(isSquare = a[row].length == a.length)) {
                    break;
                }
            }
        }
        return isSquare;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                buffer.append("(");
                buffer.append(i);
                buffer.append(",");
                buffer.append(j);
                buffer.append(") = ");
                buffer.append(matrix[i][j]);
                buffer.append(" ");
            }
            buffer.append('\n');
        }

        return buffer.toString();
    }
}
