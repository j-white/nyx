package math.nyx.framework.square;

import math.nyx.core.Signal;
import math.nyx.framework.PartitioningStrategyFactory;

public class SquarePartitioningStrategyFactory implements PartitioningStrategyFactory {
	public final static int SCALE = 1;

	@Override
	public SquarePartitioningStrategy getPartitioner(Signal signal) {
		return new SquarePartitioningStrategy(signal, SCALE);
	}

	@Override
	public SquarePartitioningStrategy getPartitioner(Signal signal, int scale) {
		return new SquarePartitioningStrategy(signal, scale);
	}
}
