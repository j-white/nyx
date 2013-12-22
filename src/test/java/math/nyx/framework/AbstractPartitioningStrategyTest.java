package math.nyx.framework;

import static org.junit.Assert.assertArrayEquals;
import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.junit.Ignore;
import org.junit.Test;

public abstract class AbstractPartitioningStrategyTest {
	public abstract PartitioningStrategy getPartitioner(int signalDimension, int numSignalChannel, int scale);

	@Test
	public void fetchAndPutRangePartitions() {
		// Decode at various powers and verify
		int signalDimension = 16;
		int powers[] = {1, 2, 3, 4};
		for (int power : powers) {
			int scale = (int)Math.pow(signalDimension, power-1);
			PartitioningStrategy partitioner = getPartitioner(signalDimension, 1, scale);
			fetchAndPutRangePartitions(partitioner);
		}
	}
	
	@Test
	@Ignore
	public void fetchAndPutRangePartitionsOfLargeSignal() {
		int signalDimension = 65536;
		PartitioningStrategy partitioner = getPartitioner(signalDimension, 1, 1);
		fetchAndPutRangePartitions(partitioner);
	}

	private void fetchAndPutRangePartitions(PartitioningStrategy partitioner) {
		// Generate a vector with fixed entries
		RealMatrix signal = TestUtils.generateSignal(partitioner.getScaledSignalDimension());

		// And create another vector to store the results
		RealMatrix result = new Array2DRowRealMatrix(partitioner.getScaledSignalDimension(), 1);

		// Iterate over all the range partitions
		int numRangePartitions = partitioner.getNumRangePartitions();
		for (int i = 0; i < numRangePartitions; i++) {
			// Fetch the range partition
			SparseRealMatrix rangeFetchOperator = partitioner.getRangeFetchOperator(i);
			RealMatrix rangePartition = rangeFetchOperator.multiply(signal);

			// Now restore the range partition with the put operator
			SparseRealMatrix putOperator = partitioner.getPutOperator(i);
			result = result.add(putOperator.multiply(rangePartition));
		}

		// And compare the two vectors
		String message = String.format("Signal dimension: %d Scale: %d Num partitions: %d",
				partitioner.getSignalDimension(), partitioner.getScale(), numRangePartitions);
		assertArrayEquals(message, signal.getColumn(0), result.getColumn(0), TestUtils.DELTA);
	}
}
