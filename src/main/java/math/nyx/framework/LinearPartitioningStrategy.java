package math.nyx.framework;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public class LinearPartitioningStrategy extends AbstractPartitioningStrategy {
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
	public int getNumDomainPartitions(int signalDimension) {
		return signalDimension - getDomainDimension(signalDimension);
	}

	@Override
	public int getNumRangePartitions(int signalDimension) {
		return Math.round((float)signalDimension / getRangeDimension(signalDimension));
	}

	@Override
	public SparseRealMatrix getDomainFetchOperator(int domainBlockIndex,
			int domainDimension, int signalDimension) {
		SparseRealMatrix F_I = new OpenMapRealMatrix(domainDimension, signalDimension);
		for (int j = 0; j < domainDimension; j++) {
			F_I.setEntry(j, domainBlockIndex + j, 1);
		}
		return F_I;
	}

	@Override
	public int[] getRangeIndices(int rangeBlockIndex,
			int rangeDimension, int signalDimension) {
		int rangeIndices[] = new int[rangeDimension];
		for (int k = 0; k < rangeDimension; k++) {
			rangeIndices[k] = rangeDimension*rangeBlockIndex + k;
		}
		return rangeIndices;
	}
}
