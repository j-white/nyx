package math.nyx.framework;

import org.apache.commons.math.linear.SparseRealMatrix;

public interface PartitioningStrategy {
	public int getDomainDimension(int signalDimension);

	public int getRangeDimension(int signalDimension);

	public int getNumDomainPartitions(int signalDimension);

	public int getNumRangePartitions(int signalDimension);

	public SparseRealMatrix getDomainFetchOperator(int domainBlockIndex, int domainDimension, int signalDimension);

	public SparseRealMatrix getRangeFetchOperator(int rangeBlockIndex, int rangeDimension, int signalDimension);

	public SparseRealMatrix getPutOperator(int rangeBlockIndex, int rangeDimension, int signalDimension);
}
