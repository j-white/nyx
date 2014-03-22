package math.nyx.framework;

import math.nyx.core.Signal;

public interface PartitioningStrategyFactory {

	public PartitioningStrategy getPartitioner(Signal signal);

	public PartitioningStrategy getPartitioner(Signal signal, int scale);

}
