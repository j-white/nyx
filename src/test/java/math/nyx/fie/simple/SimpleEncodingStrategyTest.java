package math.nyx.fie.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import math.nyx.core.AffineMap;
import math.nyx.core.Collage;
import math.nyx.core.ContractiveMap;
import math.nyx.core.Matrix;
import math.nyx.core.Partition;
import math.nyx.core.Point3D;
import math.nyx.core.RMSLinearCombination;
import math.nyx.core.RMSMetric;
import math.nyx.core.RectMatrixPartition;
import math.nyx.core.RectMatrixPartition.Orientation;
import math.nyx.core.RectMatrixPartitionIterator;
import math.nyx.fie.simple.SimpleEncodingStrategy;
import math.nyx.util.CollageIO;
import math.nyx.util.MatrixGenerator;
import math.nyx.util.TestUtils;

import org.junit.Test;

public class SimpleEncodingStrategyTest {
	SimpleEncodingStrategy fic = new SimpleEncodingStrategy(0.000001, 1);
	RMSMetric rmsMetric = new RMSMetric();

	private void encDecConstantMatrixOfSize(int n) throws IOException {
		// Create an n*n matrix
		Matrix m = new Matrix(n, n);
		// Fill it with some constant value
		m.fill(3.14159*(1/n));
		// Check it
		encDecAndVerifyRMS(m, 1, 0.0, TestUtils.DELTA);
	}

	@Test
	public void encDecSmallConstantMatrices() throws IOException {
		for (int n = 8; n < 32; n+=2) {
			encDecConstantMatrixOfSize(n);
		}
	}

	@Test
	public void encDecLargeConstantMatrices() throws IOException {
		for (int n = 32; n <= 128; n=n*2) {
			encDecConstantMatrixOfSize(n);
		}
	}

	@Test
	public void encDecKnownMatrix() throws IOException {
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
		encDecAndVerifyRMS(new Matrix(data), 4, 0.0, TestUtils.DELTA);
	}

	@Test
	public void encDecAlgorithmicallyGeneratedMatrix() throws IOException {
		for (int n = 8; n <= 64; n=n*2) {
			Matrix m = MatrixGenerator.generateInterestingMatrix(n);
			encDecAndVerifyRMS(m, 2*n, 0.0, TestUtils.DELTA);
		}
	}

	private void encDecAndVerifyRMS(Matrix m, int numberOfIterations, double expectedRms, double toleratedRmsDelta) throws IOException {
		// Encode m
		Collage collage = fic.encode(m);

		// Verify
		verifyCollage(collage, m, numberOfIterations, expectedRms, toleratedRmsDelta);

		// Serialize/de-serialize
		File tmpFile = File.createTempFile("collage", "nyx");
		tmpFile.deleteOnExit();
		assertTrue(CollageIO.write(collage, tmpFile));
		collage = CollageIO.read(tmpFile);
		
		// Verify
		verifyCollage(collage, m, numberOfIterations, expectedRms, toleratedRmsDelta);
	}

	private void verifyCollage(Collage collage, Matrix m, int numberOfIterations, double expectedRms, double toleratedRmsDelta) {
		// Decode m from the transforms at the same resolution
		int n = m.getColumnDimension();
		Matrix mm = fic.decode(collage, n, n, numberOfIterations);

		// Verify the RMS from the original image to the decode image
		RectMatrixPartition p0, p1;
		p0 = new RectMatrixPartition(m);
		p1 = new RectMatrixPartition(mm);
		
		double rms = rmsMetric.getDistance(p0, p1);
		if (Math.abs(expectedRms-rms) >= toleratedRmsDelta) {
			System.out.println("Original matrix:\n" + m + "\n");
			System.out.println("Decoded matrix:\n" + mm + "\n");

			Matrix mdiff = m.subtract(mm);
			System.out.println("Difference matrix:\n" + mdiff + "\n");
			ContractiveMap mapWithProblem = getMapWithProblem(collage, mdiff);
			Partition rangeWithProblem = mapWithProblem.getRange();

			System.out.println("Range with problem:\n" + rangeWithProblem + "\n");
			RectMatrixPartition domainForProblematicRange = (RectMatrixPartition)mapWithProblem.getDomain();
			domainForProblematicRange.setMatrix(m);
			System.out.println("Domain for problematic range:\n" + domainForProblematicRange + "\n");
			
			Orientation originalO = domainForProblematicRange.getOrientation();
			domainForProblematicRange.setOrientation(Orientation.ORIGINAL);
			System.out.println("Domain for problematic range re-oriented:\n" + domainForProblematicRange + "\n");
			domainForProblematicRange.setOrientation(originalO);
			
			System.out.println("Range hull:\n" + rangeWithProblem.getConvexHull() + "\n");
			System.out.println("Domain hull:\n" + domainForProblematicRange.getConvexHull() + "\n");

			RMSLinearCombination rmsResult = rmsMetric.getDistanceFromBestLinearCombination(domainForProblematicRange.scale(0.5f), rangeWithProblem);
			double s = rmsResult.getScale();
			double o = rmsResult.getOffset();
		
			System.out.println("Distance from domain to range: " + rmsResult);

			AffineMap testMap = new AffineMap(domainForProblematicRange, rangeWithProblem, s, o);
			Collage t = new Collage(testMap);
			t.apply(mm);
			
			Matrix rangeWithProblemMatrix = rangeWithProblem.getMatrix();
			rangeWithProblem.setMatrix(mm);
			Matrix rangeWithProblemTransformedMatrix = rangeWithProblem.getMatrix();
			System.out.println("Original range:\n" + rangeWithProblemMatrix + "\n");
			System.out.println("Manually transformed:\n" + rangeWithProblemTransformedMatrix + "\n");
			System.out.println("Difference:\n" + rangeWithProblemMatrix.subtract(rangeWithProblemTransformedMatrix) + "\n");
		}
		assertEquals(expectedRms, rms, toleratedRmsDelta);
		
		// Decode m from the transforms at double the resolution
		System.out.println("Testing super-resolution.");
		mm = fic.decode(collage, 2*n, 2*n, numberOfIterations);
		
		// Now scale it down and verify the RMS
		Matrix m3 = mm.scale(0.5f);
		p0 = new RectMatrixPartition(m);
		p1 = new RectMatrixPartition(m3);
		rms = rmsMetric.getDistance(p0, p1);

		assertEquals(expectedRms, rms, 2*n);
	}

	public ContractiveMap getMapWithProblem(Collage t, Matrix m) {
		RectMatrixPartition mp = new RectMatrixPartition(m);
		RectMatrixPartitionIterator it = mp.iterator();
		while (it.hasNext()) {
			Point3D p1 = it.nextPoint();
			if (p1.z != 0) {
				for (ContractiveMap map : t.getMaps()) {
					for (Point3D p2 : map.getRange().getPoints()) {
						if (p1.x == p2.x && p1.y == p2.y) {
							return map;
						}
					}
				}
			}
		}
		throw new RuntimeException("Oups.");
	}
}
