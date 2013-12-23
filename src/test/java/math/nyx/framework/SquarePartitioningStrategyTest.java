package math.nyx.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class SquarePartitioningStrategyTest extends AbstractPartitioningStrategyTest {
	@Autowired
	private SquarePartitioningStrategy spStrategy;

	@Autowired
	private DecimationStrategy decStrategy;

	@Override
	public SquarePartitioningStrategy getPartitioner(int signalDimension, int numSignalChannels, int scale) {
		return spStrategy.getPartitioner(signalDimension, numSignalChannels, scale);
	}
	
	@Test
	public void getDomainAndRangeDimensions() {
		int signalDomainRange[][] = new int[][] {
				{4, 4, 1},
				{16, 4, 1},
				{256, 4, 1},
				{1024, 9, 4},
				{65536, 25, 16},
		};
		
		for (int i = 0; i < signalDomainRange.length; i++) {
			int signalDimension = signalDomainRange[i][0];
			int domainDimension = signalDomainRange[i][1];
			int rangeDimension = signalDomainRange[i][2];
			
			String message = String.format("Signal dimension: %d", signalDimension);
			spStrategy = spStrategy.getPartitioner(signalDimension, 1, 1);
			assertEquals(message, domainDimension, spStrategy.getDomainDimension());
			assertEquals(message, rangeDimension, spStrategy.getRangeDimension());
		}
	}

	@Test
	public void getNumDomainAndRangePartitions() {
		int signalWidth = 256;
		spStrategy = spStrategy.getPartitioner(signalWidth*signalWidth, 1, 1);
		
		int domainWidth = (int)Math.sqrt(spStrategy.getDomainDimension());
		int rangeWidth = (int)Math.sqrt(spStrategy.getRangeDimension());
		
		
		int p = signalWidth / rangeWidth;
		assertEquals(p*p, spStrategy.getNumRangePartitions());
		
		int q = signalWidth - domainWidth + 1;
		assertEquals(q*q, spStrategy.getNumDomainPartitions());
	}
	
	@Test
	@Ignore
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
		SquarePartitioningStrategy partitioner = spStrategy.getPartitioner(signalDimension, 1, 1);
	
		for (int i = 0; i < columnIndicesToCheck.length; i++) {
			int domainDimension = columnIndicesToCheck[i].length;
			if (domainDimension == 0) {
				continue;
			}

			SparseRealMatrix fetchOperator = partitioner.getDomainFetchOperator(i);
			for (int j = 0; j < domainDimension; j++) {
				String message = String.format("Signal dimension: %d Domain block index: %d Row: %d Column: %d",
												signalDimension, i, j, columnIndicesToCheck[i][j]);
				assertEquals(message, 1, fetchOperator.getEntry(j, columnIndicesToCheck[i][j]), TestUtils.DELTA);
			}
		}
	}

	@Test
	@Ignore
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
		SquarePartitioningStrategy partitioner = spStrategy.getPartitioner(signalDimension, 1, 1);
		int numRangePartitions = partitioner.getNumRangePartitions();
		for (int i = 0; i < numRangePartitions; i++) {
			SparseRealMatrix putOperator = partitioner.getPutOperator(i);
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
		SquarePartitioningStrategy partitioner = spStrategy.getPartitioner(signalDimension, 1, 1);

		for (int i = 0; i < rowIndicesToCheck.length; i++) {
			int rangeDimension = rowIndicesToCheck[i].length;
			if (rangeDimension == 0) {
				continue;
			}
			assertEquals(rangeDimension, partitioner.getRangeDimension());
			
			SparseRealMatrix putOperator = partitioner.getPutOperator(i);
			for (int j = 0; j < rangeDimension; j++) {
				String message = String.format("Signal dimension: %d Range block index: %d Row: %d Column: %d",
												signalDimension, i, rowIndicesToCheck[i][j], j);
				assertEquals(message, 1, putOperator.getEntry(rowIndicesToCheck[i][j], j), TestUtils.DELTA);
			}
		}
	}

	@Test
	public void scaledDomainAndRangePartitions() {
		/*
		  A 4x4 gray-scale image:
			1  2  3  4
			5  6  7  8
			9  10 11 12
			13 14 15 16
		 
		  should have 16 range partitions and 9 domain partitions:
		*/
		double expectedRanges[][] = new double[][] {
			{1},  {2},  {3},  {4},
			{5},  {6},  {7},  {8},
			{9},  {10}, {11}, {12},
			{13}, {14}, {15}, {16},
		};
		
		double expectedDomains[][] = new double[][] {
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
		
		/*
		   An 4x4 image scaled by 4 should yield an 8x8 image:
		    1  2  3  4  5  6  7  8
		    9  10 11 12 13 14 15 16
		    17 18 19 20 21 22 23 24
		    25 26 27 28 29 30 31 32
		    33 34 35 36 37 38 39 40
		    41 42 43 44 45 46 47 48
		    49 50 51 52 53 54 55 56
		    57 58 59 60 61 62 63 64
		   
		   with 16 range partitions and 9 domain partitions:
		*/
		double expectedScaledRanges[][] = new double[][] {
			{1, 2, 9, 10}, {3, 4, 11, 12}, {5, 6, 13, 14}, {7, 8, 15, 16},
			{17, 18, 25, 26}, {19, 20, 27, 28}, {21, 22, 29, 30}, {23, 24, 31, 32},
			{33, 34, 41, 42}, {35, 36, 43, 44}, {37, 38, 45, 46}, {39, 40, 47, 48},
			{49, 50, 57, 58}, {51, 52, 59, 60}, {53, 54, 61, 62}, {55, 56, 63, 64}
		};

		double expectedScaledDomains[][] = new double[][] {
			{1, 2, 3, 4, 9, 10, 11, 12, 17, 18, 19, 20, 25, 26, 27, 28},
			{3, 4, 5, 6, 11, 12, 13, 14, 19, 20, 21, 22, 27, 28, 29, 30},
			{5, 6, 7, 8, 13, 14, 15, 16, 21, 22, 23, 24, 29, 30, 31, 32},
			{17, 18, 19, 20, 25, 26, 27, 28, 33, 34, 35, 36, 41, 42, 43, 44},
			{19, 20, 21, 22, 27, 28, 29, 30, 35, 36, 37, 38, 43, 44, 45, 46},
			{21, 22, 23, 24, 29, 30, 31, 32, 37, 38, 39, 40, 45, 46, 47, 48},
			{33, 34, 35, 36, 41, 42, 43, 44, 49, 50, 51, 52, 57, 58, 59, 60},
			{35, 36, 37, 38, 43, 44, 45, 46, 51, 52, 53, 54, 59, 60, 61, 62},
			{37, 38, 39, 40, 45, 46, 47, 48, 53, 54, 55, 56, 61, 62, 63, 64}
		};

		int signalDimension = 16;
		SquarePartitioningStrategy orignalPartitioner = spStrategy.getPartitioner(signalDimension, 1, 1);
		assertEquals(16, orignalPartitioner.getScaledSignalDimension());
		
		assertEquals(1, orignalPartitioner.getRangeDimension());
		assertEquals(16, orignalPartitioner.getNumRangePartitions());
		
		assertEquals(4, orignalPartitioner.getDomainDimension());
		assertEquals(9, orignalPartitioner.getNumDomainPartitions());

		int scale = 4;
		SquarePartitioningStrategy scaledPartitioner = spStrategy.getPartitioner(signalDimension, 1, scale);
		assertEquals(64, scaledPartitioner.getScaledSignalDimension());
		
		assertEquals(4, scaledPartitioner.getRangeDimension());
		assertEquals(16, orignalPartitioner.getNumRangePartitions());
		
		assertEquals(16, scaledPartitioner.getDomainDimension());
		assertEquals(9, scaledPartitioner.getNumDomainPartitions());

		RealMatrix originalSignal = TestUtils.generateSignal(orignalPartitioner.getScaledSignalDimension());
		RealMatrix scaledSignal = TestUtils.generateSignal(scaledPartitioner.getScaledSignalDimension());

		// Verify the range partitions
		for (int i = 0; i < orignalPartitioner.getRangeDimension(); i++) {
			String message = String.format("Range partition at index %d", i);
			RealMatrix rangePartition = orignalPartitioner.getRangeFetchOperator(i).multiply(originalSignal);
			assertArrayEquals(message, expectedRanges[i], rangePartition.getColumn(0), TestUtils.DELTA);
			
			message = String.format("Scaled range partition at index %d", i);
			RealMatrix rangePartitionInScaled = scaledPartitioner.getRangeFetchOperator(i).multiply(scaledSignal);
			assertArrayEquals(message, expectedScaledRanges[i], rangePartitionInScaled.getColumn(0), TestUtils.DELTA);
		}

		// Verify the domain partitions
		for (int i = 0; i < orignalPartitioner.getNumDomainPartitions(); i++) {
			String message = String.format("Domain partition at index %d", i);
			RealMatrix domainPartition = orignalPartitioner.getDomainFetchOperator(i).multiply(originalSignal);
			assertArrayEquals(message, expectedDomains[i], domainPartition.getColumn(0), TestUtils.DELTA);
			
			message = String.format("Scaled domain partition at index %d", i);
			RealMatrix domainPartitionInScaled = scaledPartitioner.getDomainFetchOperator(i).multiply(scaledSignal);
			//System.out.println(Arrays.toString(expectedScaledDomains[i]));
			//System.out.println(Arrays.toString(domainPartitionInScaled.getColumn(0)));
			assertArrayEquals(message, expectedScaledDomains[i], domainPartitionInScaled.getColumn(0), TestUtils.DELTA);
		}
	}
	
	@Test
	public void getBlockOffset() {
		int signalDimension = 65536;
		int blockWidth = 4;
		SquarePartitioningStrategy partitioner = getPartitioner(signalDimension, 1, 1);
		assertEquals(blockWidth*blockWidth, partitioner.getRangeDimension());
		
		// Non-overlapping
		assertEquals(0, partitioner.getBlockOffset(0, blockWidth, blockWidth, false));
		assertEquals(blockWidth, partitioner.getBlockOffset(1, blockWidth, blockWidth, false));
		
		// Overlapping
		assertEquals(0, partitioner.getBlockOffset(0, blockWidth, blockWidth, true));
		assertEquals(1, partitioner.getBlockOffset(1, blockWidth, blockWidth, true));
	}
}
