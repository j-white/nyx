package math.nyx.framework;

import org.apache.commons.math.linear.SparseRealMatrix;

public interface PartitioningStrategy {
	public PartitioningStrategy getPartitioner(int signalDimension);

	public PartitioningStrategy getPartitioner(int signalDimension, int scale);

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
}
