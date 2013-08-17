package math.nyx.core;

import static org.junit.Assert.assertEquals;
import math.nyx.core.Matrix;
import math.nyx.core.Partition;
import math.nyx.core.RMSMetric;
import math.nyx.core.RectMatrixPartition;
import math.nyx.util.MatrixGenerator;
import math.nyx.util.TestUtils;

import org.junit.Test;

public class RMSMetricTest {
	private RMSMetric rmsMetric = new RMSMetric();

	@Test
	public void getDistanceForKnownMatrices() {
		Matrix m1 = new Matrix(2, 2);
		m1.fill(0);
		RectMatrixPartition p1 = new RectMatrixPartition(m1);
		
		RMSLinearCombination rmsResult = rmsMetric.getDistanceFromBestLinearCombination(p1, p1);
		assertEquals(0.0, rmsResult.getRms(), TestUtils.DELTA);

		m1.fill(1);
		p1 = new RectMatrixPartition(m1);
		assertEquals(0.0, rmsMetric.getDistance(p1, p1), TestUtils.DELTA);

		m1 = new Matrix(new double[][] {
			{0.1, 2.6},
			{-4, 14}
		});
		p1 = new RectMatrixPartition(m1);

		Matrix m2 = new Matrix(new double[][] {
			{41, 22},
			{2, 4.1}
		});
		RectMatrixPartition p2 = new RectMatrixPartition(m2);
		
		rmsResult = rmsMetric.getDistanceFromBestLinearCombination(p2, p2);
		assertEquals(0.0, rmsResult.getRms(), TestUtils.DELTA);
		
		
		rmsResult = rmsMetric.getDistanceFromBestLinearCombination(p1, p2);
		assertEquals(15.86716, rmsResult.getRms(), TestUtils.DELTA);
		assertEquals(0, rmsResult.getScale(), TestUtils.DELTA);
		assertEquals(19.20889, rmsResult.getOffset(), TestUtils.DELTA);
	}

	@Test
	public void getDistanceFromScaledMatrix() {
		int range_n = 8;
		int domain_n = 2*range_n;

		// Generate a non-constant domain matrix
		Matrix domainMatrix = MatrixGenerator.generateSquareMatrix(domain_n);
		// Use it's scaled version for the domain
		Matrix rangeMatrix = MatrixGenerator.generateSquareMatrixScaled(range_n);

		Partition domainPartitionUnscaled = new RectMatrixPartition(domainMatrix);
		Partition domainPartitionScaled = domainPartitionUnscaled.scale(0.5f);	
		
		Partition rangePartition = new RectMatrixPartition(rangeMatrix);
		double rms = rmsMetric.getDistance(domainPartitionScaled, rangePartition);
		assertEquals(0, rms, TestUtils.DELTA);
	}
	
	@Test
	public void getDistanceToZeroMatrix() {
		int n = 8;
		Matrix m = MatrixGenerator.generateSquareMatrix(n);
		Matrix mzero = new Matrix(n, n);
		
		Partition pm = new RectMatrixPartition(m);
		Partition pmzero = new RectMatrixPartition(mzero);

		RMSLinearCombination rmsResult = rmsMetric.getDistanceFromBestLinearCombination(pm, pmzero);
		assertEquals(0, rmsResult.getRms(), TestUtils.DELTA);
		assertEquals(0, rmsResult.getScale(), TestUtils.DELTA);
		assertEquals(0, rmsResult.getOffset(), TestUtils.DELTA);
	}
}
