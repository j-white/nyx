package math.nyx.framework.linear;

import math.nyx.core.Signal;
import math.nyx.framework.AbstractPartitioningStrategy;

public class LinearPartitioningStrategy extends AbstractPartitioningStrategy {
	private final int scale;
	private final int signalDimension;
	private final int domainDimension;
	private final int rangeDimension;

	public LinearPartitioningStrategy() {
		// Default constructor
		domainDimension = 0;
		rangeDimension = 0;
		signalDimension = 0;
		scale = 0;
	}

	private LinearPartitioningStrategy(Signal signal, int scale) {
		super(signal, scale);
		this.scale = scale;
		signalDimension = signal.getDimension();
		domainDimension = calculateDomainDimension();
		rangeDimension = calculateRangeDimension();
	}

	@Override
	public LinearPartitioningStrategy getPartitioner(Signal signal) {
		return getPartitioner(signal, 1);
	}

	@Override
	public LinearPartitioningStrategy getPartitioner(Signal signal, int scale) {
		if(!isCompatible(signal, scale)) {
			throw new IllegalArgumentException("Signal is not compatible with partitioning strategy.");
		}
		return new LinearPartitioningStrategy(signal, scale);
	}

	@Override
	public boolean isCompatible(Signal signal, int scale) {
		if (signal.getDimension() < 1 || signal.getDimension() % 2 != 0) {
			return false;
		} else if (scale < 1) {
			return false;
		} else {
			return true;
		}
	}

	private int calculateDomainDimension() {
		int N = Math.round((float)signalDimension / 4);
		int M = Math.min((int)Math.floor(Math.log(signalDimension) / Math.log(1.1)), N);

		// If M is odd, make it even
		if ((M+1) % 2 == 0) {
			M--;
		}

		// Find the largest positive even integer bounded by M that divides the signal dimension
		for (int k = M; k > 0; k -= 2) {
			if ((signalDimension / getSignal().getNumChannels()) % k == 0) {
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
		return signalDimension / getDomainDimension();
	}

	@Override
	public int getNumRangePartitions() {
		return getSignal().getScaledDimension(scale) / getRangeDimension();
	}

	@Override
	public void getDomainIndices(int domainBlockIndex, int domainIndices[]) {
		for (int k = 0; k < domainDimension; k++) {
			domainIndices[k] = domainBlockIndex * getScale() + k;
		}
	}

	@Override
	public void getRangeIndices(int rangeBlockIndex, int rangeIndices[]) {
		for (int k = 0; k < rangeDimension; k++) {
			rangeIndices[k] = rangeDimension*rangeBlockIndex + k;
		}
	}

	@Override
	public int getPaddedDimension(Signal signal) {
		// If the signal dimension is odd, pad it to the next (even) integer
		if (signal.getDimension() % 2 == 1) {
			return signal.getDimension() + 1;
		} else {
			return signal.getDimension();
		}
	}
}
