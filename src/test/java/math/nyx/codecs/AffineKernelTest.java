package math.nyx.codecs;

import static org.junit.Assert.assertEquals;
import math.nyx.core.SignalBlock;
import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class AffineKernelTest {
	@Autowired
	private AffineKernel affineKernel;

	@Test
	public void getAffineTransformForIdenticalBlocks() {
		verifyAffineTransform(TestUtils.generateSignal(4), TestUtils.generateSignal(4), 0.0, 1.0, 0.0);
	}

	@Test
	public void getAffineTransformForBlocksWithScaleAndOffset() {
		int signalDimension = 4;
		double scale = -7.0f;
		double offset = 11.0f;

		RealMatrix domain = TestUtils.generateSignal(signalDimension);
		RealMatrix range = TestUtils.generateSignal(signalDimension);
		range = range.scalarMultiply(scale);
		range = range.scalarAdd(offset);

		verifyAffineTransform(domain, range, 0.0, scale, offset);
	}

	private void verifyAffineTransform(RealMatrix domain, RealMatrix range,
			double expectedDistance, double expectedScale, double expectedOffset) {
		SignalBlock domainBlock = new SignalBlock(0, domain);
		SignalBlock rangeBlock = new SignalBlock(0, range);

		AffineTransform transform = affineKernel.encode(domainBlock, rangeBlock);
		assertEquals(expectedDistance, transform.getDistance(), TestUtils.DELTA);
		assertEquals(transform.toString(), expectedScale, transform.getScale(), TestUtils.DELTA);
		assertEquals(expectedOffset, transform.getOffset(), TestUtils.DELTA);
	}
}
