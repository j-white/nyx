package math.nyx.framework.linear;

import static org.junit.Assert.assertEquals;
import math.nyx.core.Signal;
import math.nyx.framework.AbstractPartitioningStrategyTest;
import math.nyx.framework.linear.LinearPartitioningStrategy;
import math.nyx.image.ImageSignal;
import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class LinearPartitioningStrategyTest extends AbstractPartitioningStrategyTest {
	@Autowired
	private LinearPartitioningStrategy lpStrategy;

	@Override
	public LinearPartitioningStrategy getPartitioner(Signal signal, int scale) {
		return lpStrategy.getPartitioner(signal, scale);
	}

	@Test(expected = IllegalArgumentException.class)  
	public void getPartitionerWithOddSignalDimension() {
		lpStrategy = lpStrategy.getPartitioner(new ImageSignal(1));
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
			
			lpStrategy = lpStrategy.getPartitioner(new ImageSignal(signalDimension));
			assertEquals(domainDimension, lpStrategy.getDomainDimension());
			assertEquals(rangeDimension, lpStrategy.getRangeDimension());
		}
	}

	@Test
	public void fetchAndVerifyDomain() {
		int signalDimension = 8;
		lpStrategy = lpStrategy.getPartitioner(new ImageSignal(signalDimension));

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
}
