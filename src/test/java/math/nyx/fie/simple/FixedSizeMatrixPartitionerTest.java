package math.nyx.fie.simple;

import static org.junit.Assert.assertEquals;
import java.util.List;

import math.nyx.core.Matrix;
import math.nyx.core.Partition;
import math.nyx.fie.simple.FixedSizeMatrixPartitioner;
import math.nyx.util.TestUtils;

import org.junit.Test;

public class FixedSizeMatrixPartitionerTest {
	FixedSizeMatrixPartitioner fixedSizeMatrixPartitioner = new FixedSizeMatrixPartitioner();

	@Test
	public void verifyPartitionCount() {
		int n = 128;
		Matrix m = new Matrix(n, n);
		m.fill(-1);

		List<Partition> domainPartitions = fixedSizeMatrixPartitioner.partitionDomain(m);
		assertEquals(12769*8, domainPartitions.size());

		Partition domainPartition = domainPartitions.iterator().next();
		assertEquals(Math.pow(fixedSizeMatrixPartitioner.getDomainLength(n), 2),
				domainPartition.getNumPoints(), TestUtils.DELTA);

		List<Partition> rangePartitions = fixedSizeMatrixPartitioner.partitionRange(m);
		assertEquals(256, rangePartitions.size());

		Partition rangePartition = rangePartitions.iterator().next();
		assertEquals(Math.pow(fixedSizeMatrixPartitioner.getRangeLength(n), 2),
				rangePartition.getNumPoints(), TestUtils.DELTA);
	}

	/*
	 * TODO: Needs re-write
	@Test
	public void verifyRangePartitionContent() {
		int k = 2;
		int nn = adaptiveMatrixPartitioner.getRangeLength(64);
		int n = nn*k;
		Matrix m = new Matrix(n, n);

		int kk = 1;
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < k; j++) {
				Matrix mm = new Matrix(nn, nn);
				mm.fill(kk++);
				m.setSubMatrix(mm.getDataRef(), i*nn, j*nn);
			}
		}

		List<Partition> rangePartitions = adaptiveMatrixPartitioner.partitionRange(m);
		assertEquals(64, rangePartitions.size());
		for (int i = 1; i <= (k*k); i++) {
			int expectedSum = (i*nn*nn);
			boolean foundPartition = false;
			for (Partition p : rangePartitions) {
				if (p.getSumOfPoints() == expectedSum) {
					foundPartition = true;
					break;
				}
			}
			assertTrue("Could not find partition with sum " + expectedSum, foundPartition);
		}
	}*/

	@Test
	public void verifyDomainPartitionContent() {
		int k = 3;
		int N = fixedSizeMatrixPartitioner.getDomainLength(64);
		int n = N*k;
		Matrix m = new Matrix(n, n);

		// Number the values in the columns to 1..n
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				m.setEntry(i, j, j+1);
			}
		}

		// Partition the domain
		List<Partition> domainPartitions = fixedSizeMatrixPartitioner.partitionDomain(m);
		assertEquals((n-N+1)*(n-N+1)*8, domainPartitions.size());

		/*
		   This first partition's sum should be:
		     (N*1)+(N*2)+...+(N*N)
			= (N)(1 + 2 + ... + N)
            = (N*N*(N+1)/2)
            
            and generalising this we have:
               (N*j)+(N*(j+1))+...+(N*(j+N-1))
             = (N)[j + (j+1) + ... + (j+N-1)]
             = (N)[(j+N-1)(j+N)/2 - (j-1)(j)/2]
             = (N)[(j+N-1)(j+N)-(j-1)(j)]/2
            and with j=1, we have:
             = (N*N*(N+1)/2)
		 */
		for (int j = 1; j <= (n-N+1); j++) {
			int expectedSum = N * ( (j+N-1)*(j+N) - (j-1)*j )/2;
			int numberOfMatchedPartitions = 0;
			for (Partition p : domainPartitions) {
				if (p.getSumOfPoints() == expectedSum) {
					numberOfMatchedPartitions++;
				}
			}
			assertEquals("Could not find enough partitions with sum " + expectedSum,
							(n-N+1)*8, numberOfMatchedPartitions);
		}
	}
}
