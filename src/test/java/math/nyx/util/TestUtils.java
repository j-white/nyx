package math.nyx.util;

import static org.junit.Assert.assertEquals;
import math.nyx.core.Matrix;

public class TestUtils {
	public static final double DELTA = 0.0001;

	public static void assertMatrixSize(int numColumns, int numRows, Matrix m) {
		assertEquals(numColumns, m.getColumnDimension());
		assertEquals(numRows, m.getRowDimension());
	}

	public static void assertAllEntriesEqual(double expected, Matrix m, double delta) {
		for (int i = 0; i < m.getColumnDimension(); i++) {
			for (int j = 0; j < m.getRowDimension(); j++) {
				assertEquals(expected, m.getEntry(i, j), delta);
			}
		}
	}

	public static void assertMatrixEquals(Matrix A, Matrix B, double delta) {
		int n = A.getRowDimension();
		int m = A.getColumnDimension();
		assertEquals(n, B.getRowDimension());
		assertEquals(m, B.getColumnDimension());
		double a[][] = A.getDataRef();
		double b[][] = B.getDataRef();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				String msg = String.format("a[%d][%d] != b[%d][%d]", i, j, i ,j);
				assertEquals(msg, a[i][j], b[i][j], delta);
			}
		}
	}
}
