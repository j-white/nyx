package math.nyx.codecs;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;
import math.nyx.framework.FractalCodec;
import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class AffineFractalCodecTest {
	@Resource(name="affineFractalCodec")
	private FractalCodec affineCodec;

	@Test
	public void decodeConstantSignal() {
		final int signalDimension = 4;
		final double c = 13.0;

		// Build a simple fractal
		Fractal fractal = new Fractal();
		fractal.setSignalDimension(signalDimension);
		fractal.addTransform(new AffineTransform(0, 0, 0, 0, c));
		fractal.addTransform(new AffineTransform(0, 1, 0, 0, c));
		fractal.addTransform(new AffineTransform(0, 2, 0, 0, c));
		fractal.addTransform(new AffineTransform(0, 3, 0, 0, c));

		// Decode at various scales and verify
		decodeAndVerifyConstantSignalAtVaryingScales(fractal, c);
	}

	private void decodeAndVerifyConstantSignalAtVaryingScales(Fractal fractal, double c) {
		int originalSignalDimesion = fractal.getSignalDimension();
		
		// Decode at various powers and verify
		int powers[] = {1, 2, 3, 4};
		for (int power : powers) {
			int scale = (int)Math.pow(originalSignalDimesion, power-1);
			System.out.println("\nTesting with scale: " + scale);
			Signal signal = affineCodec.decode(fractal, scale);
			System.out.println(signal);
			//assertEquals(signal.getDimension(), scale * fractal.getUnpaddedSignalDimension());
			for(int i = 0; i < signal.getDimension(); i++) {
				String message = String.format("Scale: %d, Row: %d", scale, i);
				assertEquals(message, c, signal.getEntry(i), TestUtils.DELTA);
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
		Fractal fractal = affineCodec.encode(signal);

		// Decode at 1x and 4x
		Signal signal1x = affineCodec.decode(fractal, 1);
		Signal signal4x = affineCodec.decode(fractal, 4);
		
		assertEquals(64, signal4x.getDimension());

		// Decimate the signal decoded at 4x
		RealMatrix D = affineCodec.getDecimationStrategy().getDecimationOperator(signal1x.getDimension(),
				signal4x.getDimension());
		RealMatrix decimatedDecodedSignal = D.multiply(signal4x.getVector()).subtract(signal1x.getVector());
		for (int i = 0; i < 16; i++) {
			assertEquals(0, decimatedDecodedSignal.getEntry(i, 0), TestUtils.DELTA);
		}
	}

	@Test
	public void encodeDecodeConstantSignal() {
		int signalDimension = 4;
		final double c = 29.0;

		// Build a signal with constant entries
		RealMatrix x = new Array2DRowRealMatrix(signalDimension, 1);
		for (int i = 0; i < signalDimension; i++) {
			x.setEntry(i, 0, c);
		}
		Signal signal = new Signal(x);
	
		// Encode it
		Fractal fractal = affineCodec.encode(signal);

		// Decode at various scales and verify
		decodeAndVerifyConstantSignalAtVaryingScales(fractal, c);
	}
}
