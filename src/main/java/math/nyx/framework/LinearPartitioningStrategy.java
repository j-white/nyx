package math.nyx.framework;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public class LinearPartitioningStrategy extends AbstractPartitioningStrategy {
	private final int domainDimension;
	private final int rangeDimension;

	public LinearPartitioningStrategy() {
		// Default constructor
		domainDimension = 0;
		rangeDimension = 0;
	}

	private LinearPartitioningStrategy(int signalDimension, int scale) {
		super(signalDimension, scale);
		domainDimension = calculateDomainDimension();
		rangeDimension = calculateRangeDimension();
	}

	@Override
	public LinearPartitioningStrategy getPartitioner(int signalDimension) {
		return getPartitioner(signalDimension, 1);
	}

	@Override
	public LinearPartitioningStrategy getPartitioner(int signalDimension, int scale) {
		checkSignalDimension(signalDimension, scale);
		return new LinearPartitioningStrategy(signalDimension, scale);
	}

	public void checkSignalDimension(int signalDimension, int scale) {
		if (signalDimension < 1 || signalDimension % 2 != 0) {
			throw new IllegalArgumentException("Signal dimension must be an even positive integer.");
		}
		if (scale < 1) {
			throw new IllegalArgumentException("Scale must be a positive integer.");
		}
	}

	private int calculateDomainDimension() {
		int N = Math.round((float)getSignalDimension() / 4);
		int M = Math.min((int)Math.floor(Math.log(getSignalDimension()) / Math.log(1.1)), N);

		// If M is odd, make it even
		if ((M+1) % 2 == 0) {
			M--;
		}

		// Find the largest positive even integer bounded by M that divides the signal dimension
		for (int k = M; k > 0; k -= 2) {
			if (getSignalDimension() % k == 0) {
				return k * getScale();
			}
		}

		return 2 * getScale();
	}

	private int calculateRangeDimension() {
		return domainDimension / 2;
	}

	@Override
	public int getDomainDimension() {
		return domainDimension;
	}

	@Override
	public int getRangeDimension() {
		return rangeDimension;
	}

	@Override
	public int getNumDomainPartitions() {
		return getSignalDimension() - domainDimension;
	}

	@Override
	public int getNumRangePartitions() {
		return getSignalDimension() / getRangeDimension();
	}

	@Override
	public SparseRealMatrix getDomainFetchOperator(int domainBlockIndex) {
		SparseRealMatrix F_I = new OpenMapRealMatrix(domainDimension, getScaledSignalDimension());
		for (int j = 0; j < domainDimension; j++) {
			F_I.setEntry(j, (domainBlockIndex * getScale()) + j, 1);
		}
		return F_I;
	}

	@Override
	public int[] getRangeIndices(int rangeBlockIndex) {
		int rangeIndices[] = new int[rangeDimension];
		for (int k = 0; k < rangeDimension; k++) {
			rangeIndices[k] = rangeDimension*rangeBlockIndex + k;
		}
		return rangeIndices;
	}
}
