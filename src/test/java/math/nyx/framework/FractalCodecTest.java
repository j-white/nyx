package math.nyx.framework;

import static org.junit.Assert.assertEquals;
import math.nyx.core.Fractal;
import math.nyx.core.Signal;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class FractalCodecTest {
	@Autowired
	@Qualifier("imageCodec")
	private FractalCodec fractalCodec;

	public static Signal generateConstantSignal(final int dimension, final double value) {
		RealMatrix signal = new Array2DRowRealMatrix(dimension, 1);
		for (int i = 0; i < dimension; i++) {
			signal.setEntry(i, 0, value);
		}
		return new Signal(signal);
	}

	@Test
	public void encodeConstantSignalOfArbitraryLengths() {
		for (int k = 1; k <= 101; k+=7) {
			Signal signal = generateConstantSignal(k, k);
			Fractal fractal = fractalCodec.encode(signal);
			Signal decodedSignal = fractal.decode();
			assertEquals(k, decodedSignal.getDimension());
		}
	}
}
