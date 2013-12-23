package math.nyx.framework;

import static org.junit.Assert.assertEquals;

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
public class AveragingDecimationStrategyTest {
	@Autowired
	private AveragingDecimationStrategy adStrategy;

	private SparseRealMatrix getDecimator(int rangeDimension, int domainDimension) {
		return adStrategy.getDecimationOperator(rangeDimension, domainDimension);
	}

	@Test
	public void getDecimationOperator() {
		// Generate
		RealMatrix signal = TestUtils.generateSignal(4);

		// Decimate
		SparseRealMatrix decimator = getDecimator(2, 4);
		RealMatrix decimated = decimator.multiply(signal);

		// Verify
		assertEquals((1.0 + 2.0)/2.0, decimated.getEntry(0, 0), TestUtils.DELTA);
		assertEquals((3.0 + 4.0)/2.0, decimated.getEntry(1, 0), TestUtils.DELTA);
	}
}
