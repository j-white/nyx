package math.nyx.framework.linear;

import math.nyx.core.Signal;
import math.nyx.framework.PartitioningStrategyFactory;

public class LinearPartitioningStrategyFactory implements PartitioningStrategyFactory {
	public final static int SCALE = 1;

	@Override
	public LinearPartitioningStrategy getPartitioner(Signal signal) {
		return new LinearPartitioningStrategy(signal, SCALE);
	}

	@Override
	public LinearPartitioningStrategy getPartitioner(Signal signal, int scale) {
		return new LinearPartitioningStrategy(signal, scale);
	}
}
