package math.nyx.framework;

import math.nyx.core.Signal;

import org.apache.commons.math.linear.SparseRealMatrix;

public interface PartitioningStrategy {
	public boolean isCompatible(Signal signal, int scale);

	public Signal getSignal();

	public int getScale();
	
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

	public void getDomainIndices(int domainBlockIndex, int domainIndices[]);
	
	public void getRangeIndices(int rangeBlockIndex, int rangeIndices[]);
}
