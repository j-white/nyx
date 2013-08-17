package math.nyx.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import math.nyx.util.TestUtils;

import org.junit.Test;

public class AffineMapTest {
	@Test
	public void verifyKnownTransform() {
		int n = 4;
		Matrix m = new Matrix(n, n);

		// 3x3 partition for the domain
		Partition domain = new RectMatrixPartition(m, 0, 2, 0, 2);
		// 2x2 partition for the range
		Partition range = new RectMatrixPartition(m, 0, 1, 0, 1);

		AffineMap w = new AffineMap(domain, range);

		Matrix A = w.getXYTransform();
		Matrix B = new Matrix(new double[][] {
				{0.5, 0, 0},
				{0, 0.5, 0},
				{0, 0, 1}
		});

		assertTrue(A + " !+ " + B, A.equals(B));

		double z = 0;
		Point3D t = w.applyToPoint(new Point3D(0, 0, z));
		assertEquals(t.x, 0, TestUtils.DELTA);
		assertEquals(t.y, 0, TestUtils.DELTA);

		t = w.applyToPoint(new Point3D(2, 2, z));
		assertEquals(t.x, 1, TestUtils.DELTA);
		assertEquals(t.y, 1, TestUtils.DELTA);
		
		t = w.applyToPoint(new Point3D(2, 0, z));
		assertEquals(t.x, 1, TestUtils.DELTA);
		assertEquals(t.y, 0, TestUtils.DELTA);

		t = w.applyToPoint(new Point3D(0, 2, z));
		assertEquals(t.x, 0, TestUtils.DELTA);
		assertEquals(t.y, 1, TestUtils.DELTA);

		t = w.applyToPoint(new Point3D(1, 1, z));
		assertEquals(t.x, 0.5, TestUtils.DELTA);
		assertEquals(t.y, 0.5, TestUtils.DELTA);
	}
}
