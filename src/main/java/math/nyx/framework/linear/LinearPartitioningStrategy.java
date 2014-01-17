package math.nyx.framework.linear;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;
import math.nyx.framework.AbstractPartitioningStrategy;

public class LinearPartitioningStrategy extends AbstractPartitioningStrategy {
	private final int domainDimension;
	private final int rangeDimension;

	public LinearPartitioningStrategy() {
		// Default constructor
		domainDimension = 0;
		rangeDimension = 0;
	}

	private LinearPartitioningStrategy(int signalDimension, int numSignalChannels, int scale) {
		super(signalDimension, numSignalChannels, scale);
		domainDimension = calculateDomainDimension();
		rangeDimension = calculateRangeDimension();
	}

	@Override
	public LinearPartitioningStrategy getPartitioner(Signal signal) {
		return getPartitioner(signal.getDimension(), signal.getNumChannels(), 1);
	}

	@Override
	public LinearPartitioningStrategy getPartitioner(Fractal fractal, int scale) {
		return getPartitioner(fractal.getSignalDimension(), fractal.getNumSignalChannels(), scale);
	}

	@Override
	public LinearPartitioningStrategy getPartitioner(int signalDimension, int numSignalChannels, int scale) {
		checkSignalDimension(signalDimension, numSignalChannels, scale);
		return new LinearPartitioningStrategy(signalDimension, numSignalChannels, scale);
	}

	public void checkSignalDimension(int signalDimension, int numSignalChannels, int scale) {
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
			if ((getSignalDimension() / getNumSignalChannels()) % k == 0) {
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
		return getScaledSignalDimension() - domainDimension;
	}

	@Override
	public int getNumRangePartitions() {
		return getScaledSignalDimension() / getRangeDimension();
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
