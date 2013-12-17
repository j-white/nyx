package math.nyx.codecs;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public class LinearPartitioningStrategy implements PartitioningStrategy {
	private void checkSignalDimesion(int signalDimension) {
		if (signalDimension < 1 || signalDimension % 2 != 0) {
			throw new IllegalArgumentException("Signal dimension must be an even positive integer.");
		}
	}

	@Override
	public int getDomainDimension(int signalDimension) {
		checkSignalDimesion(signalDimension);

		int N = Math.round((float)signalDimension / 4);
		int M = Math.min((int)Math.floor(Math.log(signalDimension) / Math.log(1.1)), N);

		// If M is odd, make it even
		if ((M+1) % 2 == 0) {
			M--;
		}

		// Find the largest positive even integer bounded by M that divides the signal dimension
		for (int k = M; k > 0; k -= 2) {
			if (signalDimension % k == 0) {
				return k;
			}
		}

		return 2;
	}

	@Override
	public int getRangeDimension(int signalDimension) {
		checkSignalDimesion(signalDimension);
		return Math.round((float)getDomainDimension(signalDimension)/2);
	}

	@Override
	public SparseRealMatrix getPutOperator(int rangeBlockIndex,
			int rangeDimension, int signalDimension) {
		SparseRealMatrix P_J = new OpenMapRealMatrix(signalDimension, rangeDimension);
		for (int j = 0; j < rangeDimension; j++) {
			P_J.setEntry(rangeDimension*rangeBlockIndex + j, j, 1);
		}
		return P_J;
	}

	@Override
	public SparseRealMatrix getFetchOperator(int domainBlockIndex,
			int domainDimension, int signalDimension) {
		SparseRealMatrix F_I = new OpenMapRealMatrix(domainDimension, signalDimension);
		for (int j = 0; j < domainDimension; j++) {
			F_I.setEntry(j, domainBlockIndex + j, 1);
		}
		return F_I;
	}
}
