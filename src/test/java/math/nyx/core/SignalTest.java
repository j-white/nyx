package math.nyx.core;

import static org.junit.Assert.assertEquals;
import math.nyx.image.ImageSignal;
import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;

public class SignalTest {
	@Test
	public void testPSNR() {
		double c1 = 8, c2 = 100;
		RealMatrix v1 = new Array2DRowRealMatrix(8, 1);
		RealMatrix v2 = new Array2DRowRealMatrix(8, 1);
		for (int i = 0; i < 8; i++) {
			v1.setEntry(i, 0, c1);
			v2.setEntry(i, 0, c2);
		}

		Signal s1 = new ImageSignal(v1);
		Signal s2 = new ImageSignal(v2);

		assertEquals(0, s1.getPSNR(s1), TestUtils.DELTA);
		assertEquals(-0.1758528, s1.getPSNR(s2), TestUtils.DELTA);
		assertEquals(s2.getPSNR(s1), s1.getPSNR(s2), TestUtils.DELTA);
	}
}
