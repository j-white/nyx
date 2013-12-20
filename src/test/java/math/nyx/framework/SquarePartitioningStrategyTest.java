package math.nyx.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class SquarePartitioningStrategyTest {
	@Autowired
	private SquarePartitioningStrategy spStrategy;

	@Autowired
	private DecimationStrategy decStrategy;

	@Test
	public void getFetchOperator() {
		/*
		   A 4x4 gray-scale image:
			1  2  3  4
			5  6  7  8
			9  10 11 12
			13 14 15 16

		  domainBlockIndex: 0, domainDimension: 4, signalDimension: 16 should look like:
		  	#    0 1 2 3 4 5 6 ... 15 #
			0  | 1 0 0 0 0 0 0 ... 0 |       P[0, 0] = 1
			1  | 0 1 0 0 0 0 0 ... 0 |       P[1, 1] = 1
			2  | 0 0 0 0 1 0 0 ... 0 |       P[2, 4] = 1
			3  | 0 0 0 0 0 1 0 ... 0 |       P[3, 5] = 1

		  domainBlockIndex: 1, domainDimension: 4, signalDimension: 16 should look like:
		  	#    0 1 2 3 4 5 6 7 ... 15 #
			0  | 0 1 0 0 0 0 0 0 ... 0 |       P[0, 1] = 1
			1  | 0 0 1 0 0 0 0 0 ... 0 |       P[1, 2] = 1
			2  | 0 0 0 0 0 1 0 0 ... 0 |       P[2, 5] = 1
			3  | 0 0 0 0 0 0 1 0 ... 0 |       P[3, 6] = 1

          domainBlockIndex: 2, domainDimension: 4, signalDimension: 16 should look like:
		  	#    0 1 2 3 4 5 6 7 ... 15 #
			0  | 0 0 1 0 0 0 0 0 ... 0 |       P[0, 2] = 1
			1  | 0 0 0 1 0 0 0 0 ... 0 |       P[1, 3] = 1
			2  | 0 0 0 0 0 0 1 0 ... 0 |       P[2, 6] = 1
			3  | 0 0 0 0 0 0 0 1 ... 0 |       P[3, 7] = 1

		   A 6x6 gray-scale image:
			1  2  3  4  5  6
			7  8  9  10 11 12
			13 14 15 16 17 18
			19 20 21 22 23 24
			25 26 27 28 29 30
			31 32 33 34 35 36

          domainBlockIndex: 0, domainDimension: 9, signalDimension: 36 should look like:
            #    0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 ... 35 #
			0  | 1 0 0 0 0 0 0 0 0 0 0  0  0  0  0  0  ... 0  |       P[0,  0] = 1
			1  | 0 1 0 0 0 0 0 0 0 0 0  0  0  0  0  0  ... 0  |       P[1,  1] = 1
			2  | 0 0 1 0 0 0 0 0 0 0 0  0  0  0  0  0  ... 0  |       P[2,  2] = 1
			3  | 0 0 0 0 0 0 1 0 0 0 0  0  0  0  0  0  ... 0  |       P[3,  6] = 1
			4  | 0 0 0 0 0 0 0 1 0 0 0  0  0  0  0  0  ... 0  |       P[4,  7] = 1
			5  | 0 0 0 0 0 0 0 0 1 0 0  0  0  0  0  0  ... 0  |       P[5,  8] = 1
			6  | 0 0 0 0 0 0 0 0 0 0 0  0  1  0  0  0  ... 0  |       P[6, 12] = 1
			7  | 0 0 0 0 0 0 0 0 0 0 0  0  0  1  0  0  ... 0  |       P[7, 13] = 1
            8  | 0 0 0 0 0 0 0 0 0 0 0  0  0  0  1  0  ... 0  |       P[8, 14] = 1

          domainBlockIndex: 12, domainDimension: 9, signalDimension: 36 should look like:
            #    0 ... 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 ... 35 #
			0  | 0 ... 1  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  ... 0  |       P[0, 18] = 1
			1  | 0 ... 0  1  0  0  0  0  0  0  0  0  0  0  0  0  0  0  ... 0  |       P[1, 19] = 1
			2  | 0 ... 0  0  1  0  0  0  0  0  0  0  0  0  0  0  0  0  ... 0  |       P[2, 20] = 1
			3  | 0 ... 0  0  0  0  0  0  1  0  0  0  0  0  0  0  0  0  ... 0  |       P[3, 24] = 1
			4  | 0 ... 0  0  0  0  0  0  0  1  0  0  0  0  0  0  0  0  ... 0  |       P[4, 25] = 1
			5  | 0 ... 0  0  0  0  0  0  0  0  1  0  0  0  0  0  0  0  ... 0  |       P[5, 26] = 1
			6  | 0 ... 0  0  0  0  0  0  0  0  0  0  0  0  1  0  0  0  ... 0  |       P[6, 30] = 1
			7  | 0 ... 0  0  0  0  0  0  0  0  0  0  0  0  0  1  0  0  ... 0  |       P[7, 31] = 1
            8  | 0 ... 0  0  0  0  0  0  0  0  0  0  0  0  0  0  1  0  ... 0  |       P[8, 32] = 1
		 */

		// Verify the indices for fetch operator of a 4x4 image
		int columnIndicesToCheck[][] = new int[][] {
				{0, 1, 4, 5},
				{1, 2, 5, 6},
				{2, 3, 6, 7}
		};
		checkEntriesInFetchOperator(columnIndicesToCheck, 16);

		// Verify the indices for fetch operator of a 6x6 image
		columnIndicesToCheck = new int[][] {
				{0, 1, 2, 6, 7, 8, 12, 13, 14},
				{}, {}, {}, 
				{}, {}, {}, {},
				{}, {}, {}, {},
				{18, 19, 20, 24, 25, 26, 30, 31, 32}
		};
		checkEntriesInFetchOperator(columnIndicesToCheck, 36);
	}

	private void checkEntriesInFetchOperator(int columnIndicesToCheck[][], int signalDimension) {
		for (int i = 0; i < columnIndicesToCheck.length; i++) {
			int domainDimension = columnIndicesToCheck[i].length;
			if (domainDimension == 0) {
				continue;
			}

			SparseRealMatrix fetchOperator = spStrategy.getFetchOperator(i, domainDimension, signalDimension);
			for (int j = 0; j < domainDimension; j++) {
				String message = String.format("Signal dimension: %d Domain block index: %d Row: %d Column: %d",
												signalDimension, i, j, columnIndicesToCheck[i][j]);
				assertEquals(message, 1, fetchOperator.getEntry(j, columnIndicesToCheck[i][j]), TestUtils.DELTA);
			}
		}
	}

	@Test
	public void getPutOperator() {
		/*
		   A 4x4 gray-scale image:
			1  2  3  4
			5  6  7  8
			9  10 11 12
			13 14 15 16

		  rangeBlockIndex: k, rangeDimension: 1, signalDimension: 16 should look like:
		  	#    0 #
			0  | 0 |       P[0, k] = 1
			.    . 
			k  | 1 |
			.    .
			15 | 0 |
		 */

		int signalDimension = 16;
		int rangeDimension = spStrategy.getRangeDimension(signalDimension);
		int numRangePartitions = spStrategy.getNumRangePartitions(signalDimension);
		for (int i = 0; i < numRangePartitions; i++) {
			SparseRealMatrix putOperator = spStrategy.getPutOperator(i, rangeDimension, signalDimension);
			assertEquals(1, putOperator.getEntry(i, 0), TestUtils.DELTA);
		}

		/*
		   A 6x6 gray-scale image:
			1  2  3  4  5  6
			7  8  9  10 11 12
			13 14 15 16 17 18
			19 20 21 22 23 24
			25 26 27 28 29 30
			31 32 33 34 35 36

		  rangeBlockIndex: 0, rangeDimension: 4, signalDimension: 16 should look like:
		  	#    0 1 2 3 #
			0  | 1 0 0 0 |       P[0, 0] = 1
			1  | 0 1 0 0 |       P[1, 1] = 1
			2  | 0 0 0 0 |       P[6, 2] = 1
			3  | 0 0 0 0 |       P[7, 3] = 1
			4  | 0 0 0 0 |
			5  | 0 0 0 0 |
			6  | 0 0 1 0 |
			7  | 0 0 0 1 |
			8  | 0 0 0 0 |
			.       ...   
			35 | 0 0 0 0 |
		 */
		// Verify the indices for fetch operator of a 6x6 image
		int rowIndicesToCheck[][] = new int[][] {
				{0, 1, 6, 7}
		};
		checkEntriesInPutOperator(rowIndicesToCheck, 36);
	}

	private void checkEntriesInPutOperator(int rowIndicesToCheck[][], int signalDimension) {
		for (int i = 0; i < rowIndicesToCheck.length; i++) {
			int rangeDimension = rowIndicesToCheck[i].length;
			if (rangeDimension == 0) {
				continue;
			}

			SparseRealMatrix putOperator = spStrategy.getPutOperator(i, rangeDimension, signalDimension);
			for (int j = 0; j < rangeDimension; j++) {
				String message = String.format("Signal dimension: %d Range block index: %d Row: %d Column: %d",
												signalDimension, i, rowIndicesToCheck[i][j], j);
				assertEquals(message, 1, putOperator.getEntry(rowIndicesToCheck[i][j], j), TestUtils.DELTA);
			}
		}
	}

	@Test
	public void partitionSmallSquareImage() {
		/* A 4x4 gray-scale image:
			1  2  3  4
			5  6  7  8
			9  10 11 12
			13 14 15 16
			
		  will get mapped to a vector as:
			1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16
			
		  the square domain partitioner should return 9 * 2*2 overlapping 
		  square matrix partitions that look like:
		*/
		double expectedDomains[][] = new double[][]{
			{1, 2, 5, 6},
			{2, 3, 6, 7},
			{3, 4, 7, 8},
			{5, 6, 9, 10},
			{6, 7, 10, 11},
			{7, 8, 11, 12},
			{9, 10, 13, 14},
			{10, 11, 14, 15},
			{11, 12, 15, 16}
		};

		// Verify the domain and range partition sizes
		int signalDimension = 16;
		int domainDimension = spStrategy.getDomainDimension(signalDimension);
		int rangeDimension = spStrategy.getRangeDimension(signalDimension);
		assertEquals(4, domainDimension);
		assertEquals(1, rangeDimension);

		int numDomainPartitions = spStrategy.getNumDomainPartitions(signalDimension);
		int numRangePartitions = spStrategy.getNumRangePartitions(signalDimension);
		assertEquals(expectedDomains.length, numDomainPartitions);
		assertEquals(16, numRangePartitions);

		// Verify the domain partitions retrieved via the fetch operator
		RealMatrix signal = TestUtils.generateSignal(signalDimension);
		for (int i = 0; i < numDomainPartitions; i++) {
			SparseRealMatrix fetchOperator = spStrategy.getFetchOperator(i, domainDimension, signalDimension);
			RealMatrix domain = fetchOperator.multiply(signal);
			assertArrayEquals("Comparing domain partition " + i, expectedDomains[i],
							  domain.getColumn(0), TestUtils.DELTA);
		}

		// Verify the range partitions restored via the put operator
		SparseRealMatrix decimationOperator = decStrategy.getDecimationOperator(rangeDimension, domainDimension);
		RealMatrix decodedSignal = new Array2DRowRealMatrix(signalDimension, 1);
		for (int i = 0; i < numRangePartitions; i++) {
			// Create a new domain partition with constant values
			RealMatrix domainPartition = new Array2DRowRealMatrix(domainDimension, 1);
			domainPartition = domainPartition.scalarAdd(i+1);
			
			// Decimate it
			RealMatrix rangePartition = decimationOperator.multiply(domainPartition);
			
			// Now restore it via the put operator
			SparseRealMatrix putOperator = spStrategy.getPutOperator(i, rangeDimension, signalDimension);
			decodedSignal = decodedSignal.add(putOperator.multiply(rangePartition));
		}
		assertArrayEquals(signal.getColumn(0), decodedSignal.getColumn(0), TestUtils.DELTA);
	}
}
