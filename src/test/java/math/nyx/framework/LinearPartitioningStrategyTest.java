package math.nyx.framework;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import math.nyx.framework.LinearPartitioningStrategy;
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
public class LinearPartitioningStrategyTest {
	@Autowired
	private LinearPartitioningStrategy lpStrategy;

	@Test(expected = IllegalArgumentException.class)  
	public void getPartitionerWithOddSignalDimension() {
		lpStrategy = lpStrategy.getPartitioner(1);
	}  

	@Test
	public void getDomainAndRangeDimensions() {
		int signalDomainRange[][] = new int[][] {
				{2, 2, 1},
				{4, 2, 1},
				{8, 2, 1},
				{16, 4, 2},
				{32, 8, 4},
				{128, 32, 16},
				{256, 32, 16},
				{1024, 64, 32},
				{196608, 96, 48},
		};
		
		for (int i = 0; i < signalDomainRange.length; i++) {
			int signalDimension = signalDomainRange[i][0];
			int domainDimension = signalDomainRange[i][1];
			int rangeDimension = signalDomainRange[i][2];
			
			lpStrategy = lpStrategy.getPartitioner(signalDimension);
			assertEquals(domainDimension, lpStrategy.getDomainDimension());
			assertEquals(rangeDimension, lpStrategy.getRangeDimension());
		}
	}

	@Test
	public void fetchAndVerifyDomain() {
		int signalDimension = 8;
		lpStrategy = lpStrategy.getPartitioner(signalDimension);

		// Generate a signal with fixed entries
		RealMatrix signal = TestUtils.generateSignal(signalDimension);

		// Grab and verify the first domain block
		SparseRealMatrix fetchOperator = lpStrategy.getDomainFetchOperator(0);
		RealMatrix block = fetchOperator.multiply(signal);
		assertEquals(1, (int)block.getEntry(0, 0));
		assertEquals(2, (int)block.getEntry(1, 0));

		// Grab and verify the last domain block
		fetchOperator = lpStrategy.getDomainFetchOperator(6);
		block = fetchOperator.multiply(signal);
		assertEquals(7, (int)block.getEntry(0, 0));
		assertEquals(8, (int)block.getEntry(1, 0));
	}

	@Test
	public void fetchAndPutRange() {
		int signalDimension = 16;
		lpStrategy = lpStrategy.getPartitioner(signalDimension);

		// Generate a vector with fixed entries
		RealMatrix signal = TestUtils.generateSignal(signalDimension);

		// And create another vector to store the results
		RealMatrix result = new Array2DRowRealMatrix(signalDimension, 1);

		// Iterate over all the range partitions
		int numRangePartitions = lpStrategy.getNumRangePartitions();
		for (int i = 0; i < numRangePartitions; i++) {
			// Fetch the range partition
			SparseRealMatrix rangeFetchOperator = lpStrategy.getRangeFetchOperator(i);
			RealMatrix rangePartition = rangeFetchOperator.multiply(signal);
			
			// Now restore the range partition with the put operator
			SparseRealMatrix putOperator = lpStrategy.getPutOperator(i);
			result = result.add(putOperator.multiply(rangePartition));
		}

		// And compare the two vectors
		assertArrayEquals(signal.getColumn(0), result.getColumn(0), TestUtils.DELTA);
	}
}
