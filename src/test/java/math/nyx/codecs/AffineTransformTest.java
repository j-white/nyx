package math.nyx.codecs;

import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class AffineTransformTest {
	@Test
	public void permute() {
		permuteAndVerify(Symmetry.ORIGINAL, new double[]{1, 2, 3, 4});
		permuteAndVerify(Symmetry.ROTATE_180, new double[]{4, 3, 2, 1});
		permuteAndVerify(Symmetry.ROTATE_90, new double[]{3, 1, 4, 2});
		permuteAndVerify(Symmetry.ROTATE_270, new double[]{2, 4, 1, 3});
		permuteAndVerify(Symmetry.FLIP, new double[]{3, 4, 1, 2});
		permuteAndVerify(Symmetry.ROTATE_90_FLIP, new double[]{4, 2, 3, 1});
		permuteAndVerify(Symmetry.ROTATE_180_FLIP, new double[]{2, 1, 4, 3});
		permuteAndVerify(Symmetry.ROTATE_270_FLIP, new double[]{1, 3, 2, 4});
	}
	
	private void permuteAndVerify(Symmetry symmetry, double expected[]) {
		RealMatrix x = TestUtils.generateSignal(expected.length);
		RealMatrix y = AffineTransform.permute(x, symmetry);
		assertArrayEquals(expected, y.getColumn(0), TestUtils.DELTA);
	}
}
