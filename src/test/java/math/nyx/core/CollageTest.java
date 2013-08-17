package math.nyx.core;

import math.nyx.util.TestUtils;

import org.junit.Test;

import static math.nyx.util.TestUtils.assertMatrixEquals;
import static org.junit.Assert.assertTrue;

public class CollageTest {
	@Test
	public void verifyKnownTransformWithConstantMatrices() {
		int n = 4;
		double c = 3.14159;

		Matrix m = new Matrix(n, n);
		m.fill(c);

		// 3x3 partition for the domain
		Partition domain = new RectMatrixPartition(m, 0, 2, 0, 2);

		// 2x2 partitions for the range
		Partition r[] = new Partition[] {
			new RectMatrixPartition(m, 0, 1, 0, 1),
			new RectMatrixPartition(m, 0, 1, 2, 3),
			new RectMatrixPartition(m, 2, 3, 0, 1),
			new RectMatrixPartition(m, 2, 3, 2, 3),
		};

		// Map the domain to the ranges
		// The target matrix is constant, so s_i = 0, and o_i = c
		ContractiveMap w[] = new ContractiveMap[] {
			new AffineMap(domain, r[0], 0, c),
			new AffineMap(domain, r[1], 0, c),
			new AffineMap(domain, r[2], 0, c),
			new AffineMap(domain, r[3], 0, c)
		};

		// Create a transform from the maps
		Collage W = new Collage(w);

		// Apply it to a copy of the original matrix
		Matrix mm = m.copy();
		/*W.apply(mm);
		assertMatrixEquals(m, mm, TestUtils.DELTA);*/

		// Now apply it to a matrix filled with 0s
		mm.fill(0);
		mm = W.apply(mm);
		assertMatrixEquals(m, mm, TestUtils.DELTA);
	}

	@Test
	public void verifyKnownTransform() {
		double data[][] = new double[][] {
			{1, 1, 2, 2, 1, 1, 1, 1},
			{1, 1, 2, 2, 1, 1, 1, 1},
			{3, 3, 1, 1, 1, 1, 1, 1},
			{3, 3, 1, 1, 1, 1, 1, 1},
			{1, 2, 2, 2, 9, 6, 8, 29},
			{3, 1, 2, 2, 7, 7, 7, 29},
			{1, 2, 1, 2, 7, 7, 29, 7},
			{3, 1, 3, 1, 6, 9, 29, 8},
		};
		Matrix m = new Matrix(data);

		Partition domain = new RectMatrixPartition(m, 0, 3, 1, 4);
		Partition range = new RectMatrixPartition(m, 0, 1, 4, 5);

		RMSMetric rmsMetric = new RMSMetric();
		RMSLinearCombination rmsResult = rmsMetric.getDistanceFromBestLinearCombination(domain.scale(0.5f), range);
		
		AffineMap testMap = new AffineMap(domain, range, rmsResult.getScale(), rmsResult.getOffset());
		Collage t = new Collage(testMap);

		Matrix mcopy = m.copy();
		t.apply(mcopy);

		if (!m.equals(mcopy)) {
			System.out.println("Original matrix: \n"+ m +"\n");
			System.out.println("Transformed matrix: \n"+ mcopy +"\n");
			System.out.println("Delta: \n"+ m.subtract(mcopy) +"\n");
		}
		assertTrue(m.equals(mcopy));
	}
}
