package math.nyx.core;

import static org.junit.Assert.assertEquals;

import math.nyx.util.MatrixGenerator;
import math.nyx.util.TestUtils;

import org.junit.Test;

import static math.nyx.util.TestUtils.assertAllEntriesEqual;
import static math.nyx.util.TestUtils.assertMatrixEquals;
import static math.nyx.util.TestUtils.assertMatrixSize;

public class MatrixTest {
	@Test
	public void sumOfEntries() {
		int n = 2;
		Matrix m = new Matrix(n, n);
		assertEquals(0, m.getSumOfEntries(), TestUtils.DELTA);
		
		m.fill(1.0);
		assertEquals(n*n, m.getSumOfEntries(), TestUtils.DELTA);
		assertEquals(n*n, m.getSumOfSquaredEntries(), TestUtils.DELTA);

		m.fill(-1.0);
		assertEquals(-n*n, m.getSumOfEntries(), TestUtils.DELTA);
		assertEquals(n*n, m.getSumOfSquaredEntries(), TestUtils.DELTA);

		m.fill(2.0);
		assertEquals(2*n*n, m.getSumOfEntries(), TestUtils.DELTA);
		assertEquals(4*n*n, m.getSumOfSquaredEntries(), TestUtils.DELTA);
	}

	@Test
	public void fill() {
		int n = 2;
		Matrix m = new Matrix(n, n);
		assertAllEntriesEqual(0.0, m, TestUtils.DELTA);
		
		m.fill(1.0);
		assertAllEntriesEqual(1.0, m, TestUtils.DELTA);
	}

	@Test
	public void rotate() {
		double data[][] = new double[][] {
			{1.0, 2.0},
			{3.0, 4.0}
		};

		double dataRotated[][] = new double[][] {
			{3.0, 1.0},
			{4.0, 2.0}
		};

		Matrix m = new Matrix(data);
		Matrix mrotated = new Matrix(dataRotated);

		// Rotate the matrix and verify
		Matrix mm = m.rotate();
		assertMatrixEquals(mrotated, mm, TestUtils.DELTA);

		// Now rotate 3 more times and expect the original matrix
		mm = mm.rotate();
		mm = mm.rotate();
		mm = mm.rotate();
		assertMatrixEquals(m, mm, TestUtils.DELTA);
	}

	@Test
	public void flipVertically() {
		double data[][] = new double[][] {
			{1.0, 2.0},
			{3.0, 4.0}
		};

		double dataFlipped[][] = new double[][] {
			{3.0, 4.0},
			{1.0, 2.0}
		};

		Matrix m = new Matrix(data);
		Matrix mflipped = new Matrix(dataFlipped);

		// Flip the matrix and verify
		Matrix mm = m.flipVertically();
		assertMatrixEquals(mflipped, mm, TestUtils.DELTA);

		// Now flip it back and expect the original matrix
		mm = mm.flipVertically();
		assertMatrixEquals(m, mm, TestUtils.DELTA);
	}

	@Test
	public void scaleConstantNByNMatrix() {
		double fill = 3.14159;
		Matrix m, mm;
		for (int k = 1; k <= 16; k++) {
			int n = k*2;
			m = new Matrix(n, n);
			m.fill(fill);

			mm = m.scale(0.5f);
			assertMatrixSize(k, k, mm);
			assertAllEntriesEqual(fill, m, TestUtils.DELTA);
		}
	}

	@Test
	public void scaleAlgorithmitcallyGeneratedMatrix() {
		int n = 8;
		Matrix m = MatrixGenerator.generateSquareMatrix(2*n);
		Matrix expectedScaledMatrix = MatrixGenerator.generateSquareMatrixScaled(n);		
		Matrix scaledMatrix = m.scale(0.5f);
		assertMatrixEquals(expectedScaledMatrix, scaledMatrix, TestUtils.DELTA);
	}

	@Test
	public void scaleKnownFourByFourMatrix() {
		double data[][] = new double[][] {
			{3.0, 3.0, 1.0, 1.0},
			{1.0, 2.0, 2.0, 2.0},
			{3.0, 1.0, 2.0, 2.0},
			{1.0, 2.0, 1.0, 2.0}
		};

		double data_scaled[][] = new double[][] {
			{2.25, 1.5},
			{1.75, 1.75}
		};

		Matrix m = new Matrix(data);
		Matrix mscaled = new Matrix(data_scaled);
		Matrix mm = m.scale(0.5f);
		assertMatrixEquals(mscaled, mm, TestUtils.DELTA);
	}
}
