package math.nyx.codecs;

import static org.junit.Assert.assertEquals;

import math.nyx.core.SignalBlock;
import math.nyx.core.Signal;
import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class AffineFractalCodecTest {
	@Autowired
	private AffineFractalCodec affineCodec;

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

		AffineTransform transform = affineCodec.getAffineTransform(domainBlock, rangeBlock);
		assertEquals(expectedDistance, transform.getDistance(), TestUtils.DELTA);
		assertEquals(transform.toString(), expectedScale, transform.getScale(), TestUtils.DELTA);
		assertEquals(expectedOffset, transform.getOffset(), TestUtils.DELTA);
	}

	@Test
	public void decodeConstantSignal() {
		final int signalDimension = 2;
		final double c = 13.0;

		// Build a simple fractal
		AffineFractal fractal = new AffineFractal();
		fractal.setSignalDimension(signalDimension);
		fractal.addTransform(new AffineTransform(0, 0, 0, 0, c));
		fractal.addTransform(new AffineTransform(0, 1, 0, 0, c));

		// Decode at various scales and verify
		decodeAndVerifyConstantSignalAtVaryingScales(fractal, c);
	}

	private void decodeAndVerifyConstantSignalAtVaryingScales(AffineFractal fractal, double c) {
		// Decode at various scales and verify
		int scales[] = {1, 2, 3, 8, 16, 32, 256};
		for (int scale : scales) {
			Signal signal = affineCodec.decode(fractal, scale);
			assertEquals(signal.getDimension(), scale * fractal.getSignalDimension());
			for(int i = 0; i < scale * fractal.getSignalDimension(); i++) {
				assertEquals(c, signal.getEntry(i), TestUtils.DELTA);
			}
		}
	}

	@Test
	public void decodeAndDecimate() {
		RealMatrix x = new Array2DRowRealMatrix(16, 1);
		x.setColumn(0, new double[]{
				1.0, 2.0,
				2.0, 2.0,
				3.0, 4.0,
				3.0, 3.0,
				1.0, 10.0,
				2.0, 20.0,
				3.0, 30.0,
				4.0, 40.0
		});
		Signal signal = new Signal(x);

		// Encode it
		AffineFractal fractal = affineCodec.encode(signal);
		for (AffineTransform f : fractal.getTransforms()) {
			System.out.println(f);
		}

		// Decode at 1x and 4x
		Signal signal1x = affineCodec.decode(fractal, 1);
		System.out.println(signal1x);
		Signal signal2x = affineCodec.decode(fractal, 4);

		// Decimate the signal decoded at 4x
		RealMatrix D = affineCodec.getDecimationStrategy().getDecimationOperator(16, 16 * 4);
		RealMatrix decimatedDecodedSignal = D.multiply(signal2x.getVector()).subtract(signal1x.getVector());
		for (int i = 0; i < 16; i++) {
			assertEquals(0, decimatedDecodedSignal.getEntry(i, 0), 0.00001);
		}
	}

	@Test
	public void encodeDecodeConstantSignal() {
		int signalDimension = 2;
		final double c = -29.0;

		// Build a signal with constant entries
		RealMatrix x = new Array2DRowRealMatrix(signalDimension, 1);
		for (int i = 0; i < signalDimension; i++) {
			x.setEntry(i, 0, c);
		}
		Signal signal = new Signal(x);
	
		// Encode it
		AffineFractal fractal = affineCodec.encode(signal);

		// Decode at various scales and verify
		decodeAndVerifyConstantSignalAtVaryingScales(fractal, c);
	}
}
