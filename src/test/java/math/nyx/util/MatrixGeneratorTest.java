package math.nyx.util;

import math.nyx.core.Matrix;
import math.nyx.core.Partition;
import math.nyx.core.RMSLinearCombination;
import math.nyx.core.RMSMetric;
import math.nyx.core.RectMatrixPartition;
import math.nyx.util.MatrixGenerator.MatrixGeneratorVisitor;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class MatrixGeneratorTest {
	private class MyMatrixGeneratorVisitor implements MatrixGeneratorVisitor {
		Matrix lastDomainMatrix = null;
		RectMatrixPartition lastDomainPartition;

		public void generatedDomain(Matrix domain) {
			lastDomainMatrix = domain;
			lastDomainPartition = new RectMatrixPartition(lastDomainMatrix);
		}

		public void generatedRange(Matrix range) {
			RMSMetric rmsMetric = new RMSMetric();
			RectMatrixPartition rangePartition = new RectMatrixPartition(range);

			boolean foundDomainWithZeroDistanceToRange = false;
			for (Partition p : lastDomainPartition.getDifferentOrientations()) {
				RMSLinearCombination rmsResult = rmsMetric.getDistanceFromBestLinearCombination(p.scale(0.5f), rangePartition);
				if (rmsResult.getRms() == 0.0) {
					foundDomainWithZeroDistanceToRange = true;
					break;
				}
			}

			assertTrue(foundDomainWithZeroDistanceToRange);
		}
	};

	@Test
	public void verifyRangeRmsIsAlwayZero() {
		for (int n = 8; n <= 512; n=n*2) {
			MyMatrixGeneratorVisitor mmgv = new MyMatrixGeneratorVisitor();
			MatrixGenerator.generateInterestingMatrix(n, mmgv);
		}
	}	
}
