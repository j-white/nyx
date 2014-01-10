package math.nyx.framework;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;

import org.apache.commons.math.linear.SparseRealMatrix;

public interface PartitioningStrategy {
	public int getPaddedDimension(Signal signal);

	public PartitioningStrategy getPartitioner(Signal signal);

	public PartitioningStrategy getPartitioner(Fractal fractal, int scale);

	public PartitioningStrategy getPartitioner(int signalDimension, int numSignalChannels, int scale);

	public int getScale();

	public int getSignalDimension();
	
	public int getScaledSignalDimension();

	public int getDomainDimension();

	public int getRangeDimension();

	public int getNumDomainPartitions();

	public int getNumRangePartitions();

	public SparseRealMatrix getDomainFetchOperator(int domainBlockIndex);

	public SparseRealMatrix getRangeFetchOperator(int rangeBlockIndex);

	public SparseRealMatrix getPutOperator(int rangeBlockIndex);

	public int[] getDomainIndices(int domainBlockIndex);

	public int[] getRangeIndices(int rangeBlockIndex);
}
