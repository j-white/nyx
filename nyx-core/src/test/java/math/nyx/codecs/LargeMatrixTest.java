package math.nyx.codecs;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.junit.Test;

public class LargeMatrixTest {
	@Test
	public void multiplyLargeMatrices() {
		int N = 131079; // Fails with 131080
		int M = 16384;

		RealMatrix X = new Array2DRowRealMatrix(N, 1);
		SparseRealMatrix A = new OpenMapRealMatrix(M, N);
		for (int j = 0; j < M; j++) {
			A.setEntry(j, j, 1);
		}
		A.multiply(X);
	}
}
