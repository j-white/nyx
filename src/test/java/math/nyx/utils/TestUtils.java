package math.nyx.utils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

public class TestUtils {
	public static final double DELTA = 0.00001;

	public static RealMatrix generateSignal(int signalDimension) {
		// Generate a signal with with x[0] = 1 and x[k] = x[k-1] + 1
		RealMatrix signal = new Array2DRowRealMatrix(signalDimension, 1);
		for (int i = 0; i < signalDimension; i++) {
			signal.setEntry(i, 0, i+1);
		}
		return signal;
	}
}
