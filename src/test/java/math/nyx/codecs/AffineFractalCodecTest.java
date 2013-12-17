package math.nyx.codecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import math.nyx.core.SignalBlock;
import math.nyx.core.Signal;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.junit.Before;
import org.junit.Test;

public class AffineFractalCodecTest {
	private AffineFractalCodec affineCodec;
	private final double delta = 0.00001;

	@Before
	public void setUp() {
		affineCodec = new AffineFractalCodec();
	}

	private RealMatrix generateSignal(int signalDimension) {
		// Generate a signal with with x[0] = 1 and x[k] = x[k-1] + 1
		RealMatrix signal = new Array2DRowRealMatrix(signalDimension, 1);
		for (int i = 0; i < signalDimension; i++) {
			signal.setEntry(i, 0, i+1);
		}
		return signal;
	}

	@Test
	public void isPowerOfTwo() {
		assertFalse(affineCodec.isPowerOfTwo(0));
		assertFalse(affineCodec.isPowerOfTwo(-2));
		assertFalse(affineCodec.isPowerOfTwo(3));
		assertFalse(affineCodec.isPowerOfTwo(-3));
		assertFalse(affineCodec.isPowerOfTwo(11));

		assertTrue(affineCodec.isPowerOfTwo(1));
		assertTrue(affineCodec.isPowerOfTwo(2));
		assertTrue(affineCodec.isPowerOfTwo(4));
		assertTrue(affineCodec.isPowerOfTwo(1024));
	}

	@Test(expected = IllegalArgumentException.class)  
	public void getDomainDimensionWithOddNumber() {  
		affineCodec.getDomainDimension(1);
	}  

	@Test(expected = IllegalArgumentException.class)  
	public void getRangeDimensionWithOddNumber() {  
		affineCodec.getRangeDimension(1);
	}

	@Test
	public void getDomainDimension() {
		assertEquals(2, affineCodec.getDomainDimension(2));
		assertEquals(2, affineCodec.getDomainDimension(4));
		assertEquals(2, affineCodec.getDomainDimension(8));
		assertEquals(4, affineCodec.getDomainDimension(16));
		assertEquals(8, affineCodec.getDomainDimension(32));
		assertEquals(32, affineCodec.getDomainDimension(128));
		assertEquals(32, affineCodec.getDomainDimension(256));
		assertEquals(64, affineCodec.getDomainDimension(1024));
		assertEquals(96, affineCodec.getDomainDimension(196608));
	}

	@Test
	public void getRangeDimension() {
		assertEquals(1, affineCodec.getRangeDimension(2));
		assertEquals(1, affineCodec.getRangeDimension(4));
		assertEquals(1, affineCodec.getRangeDimension(8));
		assertEquals(2, affineCodec.getRangeDimension(16));
		assertEquals(4, affineCodec.getRangeDimension(32));
		assertEquals(16, affineCodec.getRangeDimension(128));
		assertEquals(16, affineCodec.getRangeDimension(256));
		assertEquals(32, affineCodec.getRangeDimension(1024));
		assertEquals(48, affineCodec.getRangeDimension(196608));
	}

	@Test
	public void getFetchOperator() {
		int signalDimension = 8;
		int domainDimension = 2;

		// Generate a signal with fixed entries
		RealMatrix signal = generateSignal(signalDimension);

		// Grab and verify the first domain block
		SparseRealMatrix fetchOperator = affineCodec.getFetchOperator(0, domainDimension, signalDimension);
		RealMatrix block = fetchOperator.multiply(signal);
		assertEquals(1, (int)block.getEntry(0, 0));
		assertEquals(2, (int)block.getEntry(1, 0));

		// Grab and verify the last domain block
		fetchOperator = affineCodec.getFetchOperator(6, domainDimension, signalDimension);
		block = fetchOperator.multiply(signal);
		assertEquals(7, (int)block.getEntry(0, 0));
		assertEquals(8, (int)block.getEntry(1, 0));
	}

	@Test
	public void getDecimationOperator() {
		// Generate
		RealMatrix signal = generateSignal(4);

		// Decimate
		SparseRealMatrix decimationOperator = affineCodec.getDecimationOperator(2, 4);
		RealMatrix decimated = decimationOperator.multiply(signal);
		
		// Verify
		assertEquals((1.0 + 2.0)/2.0, decimated.getEntry(0, 0), delta);
		assertEquals((3.0 + 4.0)/2.0, decimated.getEntry(1, 0), delta);
	}

	@Test
	public void getPutOperator() {
		int signalDimension = 8;
		int rangeDimesion = 2;

		RealMatrix block = generateSignal(rangeDimesion);

		// Put and verify the first range block		
		SparseRealMatrix putOperator = affineCodec.getPutOperator(0, rangeDimesion, signalDimension);
		RealMatrix signal = putOperator.multiply(block);
		assertEquals(1, (int)signal.getEntry(0, 0));
		assertEquals(2, (int)signal.getEntry(1, 0));
		assertEquals(0, (int)signal.getEntry(2, 0));

		// Put and verify the last range block
		putOperator = affineCodec.getPutOperator(3, rangeDimesion, signalDimension);
		signal = putOperator.multiply(block);
		assertEquals(0, (int)signal.getEntry(5, 0));
		assertEquals(1, (int)signal.getEntry(6, 0));
		assertEquals(2, (int)signal.getEntry(7, 0));
	}

	@Test
	public void getAffineTransformForIdenticalBlocks() {
		verifyAffineTransform(generateSignal(4), generateSignal(4), 0.0, 1.0, 0.0);
	}

	@Test
	public void getAffineTransformForBlocksWithScaleAndOffset() {
		int signalDimension = 4;
		double scale = -7.0f;
		double offset = 11.0f;

		RealMatrix domain = generateSignal(signalDimension);
		RealMatrix range = generateSignal(signalDimension);
		range = range.scalarMultiply(scale);
		range = range.scalarAdd(offset);

		verifyAffineTransform(domain, range, 0.0, scale, offset);
	}

	private void verifyAffineTransform(RealMatrix domain, RealMatrix range,
			double expectedDistance, double expectedScale, double expectedOffset) {
		SignalBlock domainBlock = new SignalBlock(0, domain);
		SignalBlock rangeBlock = new SignalBlock(0, range);

		AffineTransform transform = affineCodec.getAffineTransform(domainBlock, rangeBlock);
		assertEquals(expectedDistance, transform.getDistance(), delta);
		assertEquals(transform.toString(), expectedScale, transform.getScale(), delta);
		assertEquals(expectedOffset, transform.getOffset(), delta);
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
				assertEquals(c, signal.getEntry(i), delta);
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
		RealMatrix D = affineCodec.getDecimationOperator(16, 16 * 4);
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

		// The number of transforms should equal the number of range blocks
		int numTransforms = Math.round((float)signalDimension / affineCodec.getRangeDimension(signalDimension));
		List<AffineTransform> transforms = fractal.getTransforms();
		assertEquals(numTransforms, transforms.size());

		// Decode at various scales and verify
		decodeAndVerifyConstantSignalAtVaryingScales(fractal, c);
	}
}
