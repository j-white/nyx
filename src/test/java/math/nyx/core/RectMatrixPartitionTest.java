package math.nyx.core;

import static math.nyx.util.TestUtils.assertAllEntriesEqual;
import static math.nyx.util.TestUtils.assertMatrixEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import math.nyx.core.RectMatrixPartition.Orientation;
import math.nyx.util.MatrixGenerator;
import math.nyx.util.TestUtils;

import org.junit.Test;

public class RectMatrixPartitionTest {
	@Test
	public void numPoints() {
		Matrix m = new Matrix(4,4);
		RectMatrixPartition p = new RectMatrixPartition(m);
		assertEquals(16, p.getNumPoints());
		
		p = new RectMatrixPartition(m, 1, 2, 1, 2);
		assertEquals(4, p.getNumPoints());
	}

	@Test
	public void sum() {
		int n = 5;
		Matrix m = new Matrix(n, n);
		m.fill(n);
		RectMatrixPartition p = new RectMatrixPartition(m);
		assertEquals(Math.pow(n, 3), p.getSumOfPoints(), TestUtils.DELTA);
	}

	@Test
	public void doit() {
		int n = 4;
		Matrix m = new Matrix(n, n);
		m.fill(n);
		RectMatrixPartition p1 = new RectMatrixPartition(m, 0, 1, 0, 1);
		/*RectMatrixPartition p2 = new RectMatrixPartition(m, 2, 3, 0, 1);
		RectMatrixPartition p3 = new RectMatrixPartition(m, 0, 1, 2, 3);*/
		RectMatrixPartition p4 = new RectMatrixPartition(m, 2, 3, 2, 3);
		
		Matrix M = new Matrix(2*n, 2*n);
		p1.setMatrix(M);
		List<Point2D> hull = p1.getConvexHull();
		Point2D topLeftCorner = hull.get(0);
		// Should be clock-wise
		Point2D bottomRightCorner = hull.get(2);
		assertEquals(0, topLeftCorner.x);
		assertEquals(0, topLeftCorner.y);

		assertEquals(2, bottomRightCorner.x);
		assertEquals(2, bottomRightCorner.y);
		
		
		p4.setMatrix(M);
		hull = p4.getConvexHull();
		topLeftCorner = hull.get(0);
		bottomRightCorner = hull.get(2);
		assertEquals(4, topLeftCorner.x);
		assertEquals(4, topLeftCorner.y);

		assertEquals(6, bottomRightCorner.x);
		assertEquals(6, bottomRightCorner.y);
		
	}
	
	@Test
	public void scaleMatrix() {
		int k = 512;
		int n = k*2;
		float scale = (float)k/(float)n;
		int c1 = 8;
		int c2 = 12;
		Matrix m = new Matrix(n, n);

		// Fill the matrix with c1
		m.fill(c1);

		// Fill in a submatrix with c2
		Matrix mm = new Matrix(k, k);
		mm.fill(c2);
		m.setSubMatrix(mm.getDataRef(), k, k);

		// Check a first partition
		RectMatrixPartition p = new RectMatrixPartition(m, 0, k-1, 0, k-1);
		assertEquals(k*k, p.getNumPoints());

		RectMatrixPartition pScaled = p.scale(scale);
		assertEquals((k/2)*(k/2), pScaled.getNumPoints());
		assertAllEntriesEqual(c1, pScaled.getMatrix(), TestUtils.DELTA);

		// Check a second partition
		p = new RectMatrixPartition(m, k, (k*2)-1, k, (k*2)-1);
		assertEquals(k*k, p.getNumPoints());

		pScaled = p.scale(scale);
		assertEquals((k/2)*(k/2), pScaled.getNumPoints());
		assertAllEntriesEqual(c2, pScaled.getMatrix(), TestUtils.DELTA);
	}

	@Test
	public void rotateAndScale() {
		int n = 4;
		Matrix m = MatrixGenerator.generateSquareMatrix(n);
		Matrix mm = MatrixGenerator.generateSquareMatrixScaled(n/2);
		
		// Start by scaling to verify
		RectMatrixPartition p = new RectMatrixPartition(m);
		assertMatrixEquals(mm, p.scale(0.5f).getMatrix(), TestUtils.DELTA);

		// Now rotate and scale
		p = new RectMatrixPartition(m, Orientation.ROTATE_90);
		assertMatrixEquals(mm.rotate(), p.scale(0.5f).getMatrix(), TestUtils.DELTA);
	}

	@Test
	public void getPointsInDomain() {
		int n = 3;
		Matrix m = MatrixGenerator.generateSquareMatrix(n);

		Partition p = new RectMatrixPartition(m, Orientation.ORIGINAL);
		List<Point3D> points = p.getPoints();
		assertEquals(n*n, points.size());

		assertEquals(points.get(0), new Point3D(0, 0, 1));
		assertEquals(points.get(1), new Point3D(1, 0, 2));
		assertEquals(points.get(2), new Point3D(2, 0, 3));
		assertEquals(points.get(3), new Point3D(0, 1, 4));
		assertEquals(points.get(4), new Point3D(1, 1, 5));
		assertEquals(points.get(5), new Point3D(2, 1, 6));
		assertEquals(points.get(6), new Point3D(0, 2, 7));
		assertEquals(points.get(7), new Point3D(1, 2, 8));
		assertEquals(points.get(8), new Point3D(2, 2, 9));
	}

	private void checkDifferentOrientationsAtOffset(int startRow, int startColumn) {
		// All 8 possible orientations of a 2x2 matrix
		double dataOrientation[][][] = new double[][][] {
				// Rotate clock-wise
				{
					{1.0, 2.0},
					{3.0, 4.0}
				},
				{
					{3.0, 1.0},
					{4.0, 2.0}
				},
				{
					{4.0, 3.0},
					{2.0, 1.0}
				},
				{
					{2.0, 4.0},
					{1.0, 3.0}
				},
				// Now rotate clock-wise and flip
				{
					{3.0, 4.0},
					{1.0, 2.0}
				},
				{
					{4.0, 2.0},
					{3.0, 1.0}
				},
				{
					{2.0, 1.0},
					{4.0, 3.0}
				},
				{
					{1.0, 3.0},
					{2.0, 4.0}
				}
		};

		// Create a partition using the first orientation
		Matrix m = new Matrix(8, 8);
		m.fill(5);

		m.setSubMatrix(dataOrientation[0], startRow, startColumn);
		Partition p = new RectMatrixPartition(m, startRow, startRow+1, startColumn, startColumn+1);
		List<Point2D> hull = p.getConvexHull();

		// Retrieve all the different orientations, including the original
		List<Partition> orientedPartitions = p.getDifferentOrientations();
		assertEquals(8, orientedPartitions.size());
		
		/*
		System.out.println("Original matrix m:\n" + m + "\n");
		System.out.println("Partition p:\n" + p + "\n");
		for (Partition orientedPartition : orientedPartitions) {
			System.out.println("Orientation:\n" + orientedPartition + "\n");
		}
		*/
		
		// Iterate over all of the known orientations
		for (int i = 0; i < dataOrientation.length; i++) {
			// Create a matrix from this orientation
			Matrix mm = new Matrix(dataOrientation[i]);

			// Now try and find a match in list
			boolean foundMatch = false;
			for (Partition orientedPartition : orientedPartitions) {
				RectMatrixPartition orientedMatrixPartition = (RectMatrixPartition)orientedPartition;
				if (mm.equals(orientedMatrixPartition.getMatrix())) {
					foundMatch = true;
					// Make sure the hull remains unchanged
					verifyHull(hull, orientedMatrixPartition.getConvexHull());
				}
			}
			assertTrue("No match found for orientation " + i, foundMatch);
		}
	}

	@Test
	public void getDifferentOrientations() {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				checkDifferentOrientationsAtOffset(i,j);
			}
		}
	}

	private void verifyHull(List<Point2D> expectedHull, List<Point2D> hull) {
		int k = expectedHull.size();
		assertEquals(k, hull.size());
		for (int i = 0; i < k; i++) {
			assertEquals(expectedHull.get(i), hull.get(i));
		}
	}

	@Test
	public void doTheRectanglesIntersect() {
		Matrix m = new Matrix(4,4);
		RectMatrixPartition p1 = new RectMatrixPartition(m, 0, 1, 0, 1);
		RectMatrixPartition p2 = new RectMatrixPartition(m, 1, 1, 1, 2);
		assertTrue(p1.intersectsWith(p1));
		assertFalse(p1.intersectsWith(p2));
	}
}
