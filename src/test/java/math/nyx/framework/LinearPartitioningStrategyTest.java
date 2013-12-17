package math.nyx.framework;

import static org.junit.Assert.assertEquals;

import math.nyx.framework.LinearPartitioningStrategy;
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
public class LinearPartitioningStrategyTest {
	@Autowired
	private LinearPartitioningStrategy lpStrategy;

	@Test(expected = IllegalArgumentException.class)  
	public void getDomainDimensionWithOddNumber() {  
		lpStrategy.getDomainDimension(1);
	}  

	@Test(expected = IllegalArgumentException.class)  
	public void getRangeDimensionWithOddNumber() {  
		lpStrategy.getRangeDimension(1);
	}

	@Test
	public void getDomainDimension() {
		assertEquals(2, lpStrategy.getDomainDimension(2));
		assertEquals(2, lpStrategy.getDomainDimension(4));
		assertEquals(2, lpStrategy.getDomainDimension(8));
		assertEquals(4, lpStrategy.getDomainDimension(16));
		assertEquals(8, lpStrategy.getDomainDimension(32));
		assertEquals(32, lpStrategy.getDomainDimension(128));
		assertEquals(32, lpStrategy.getDomainDimension(256));
		assertEquals(64, lpStrategy.getDomainDimension(1024));
		assertEquals(96, lpStrategy.getDomainDimension(196608));
	}

	@Test
	public void getRangeDimension() {
		assertEquals(1, lpStrategy.getRangeDimension(2));
		assertEquals(1, lpStrategy.getRangeDimension(4));
		assertEquals(1, lpStrategy.getRangeDimension(8));
		assertEquals(2, lpStrategy.getRangeDimension(16));
		assertEquals(4, lpStrategy.getRangeDimension(32));
		assertEquals(16, lpStrategy.getRangeDimension(128));
		assertEquals(16, lpStrategy.getRangeDimension(256));
		assertEquals(32, lpStrategy.getRangeDimension(1024));
		assertEquals(48, lpStrategy.getRangeDimension(196608));
	}

	@Test
	public void getFetchOperator() {
		int signalDimension = 8;
		int domainDimension = 2;

		// Generate a signal with fixed entries
		RealMatrix signal = TestUtils.generateSignal(signalDimension);

		// Grab and verify the first domain block
		SparseRealMatrix fetchOperator = lpStrategy.getFetchOperator(0, domainDimension, signalDimension);
		RealMatrix block = fetchOperator.multiply(signal);
		assertEquals(1, (int)block.getEntry(0, 0));
		assertEquals(2, (int)block.getEntry(1, 0));

		// Grab and verify the last domain block
		fetchOperator = lpStrategy.getFetchOperator(6, domainDimension, signalDimension);
		block = fetchOperator.multiply(signal);
		assertEquals(7, (int)block.getEntry(0, 0));
		assertEquals(8, (int)block.getEntry(1, 0));
	}

	@Test
	public void getPutOperator() {
		int signalDimension = 8;
		int rangeDimesion = 2;

		RealMatrix block = TestUtils.generateSignal(rangeDimesion);

		// Put and verify the first range block		
		SparseRealMatrix putOperator = lpStrategy.getPutOperator(0, rangeDimesion, signalDimension);
		RealMatrix signal = putOperator.multiply(block);
		assertEquals(1, (int)signal.getEntry(0, 0));
		assertEquals(2, (int)signal.getEntry(1, 0));
		assertEquals(0, (int)signal.getEntry(2, 0));

		// Put and verify the last range block
		putOperator = lpStrategy.getPutOperator(3, rangeDimesion, signalDimension);
		signal = putOperator.multiply(block);
		assertEquals(0, (int)signal.getEntry(5, 0));
		assertEquals(1, (int)signal.getEntry(6, 0));
		assertEquals(2, (int)signal.getEntry(7, 0));
	}
}
