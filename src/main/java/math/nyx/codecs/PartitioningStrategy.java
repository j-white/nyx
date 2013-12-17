package math.nyx.codecs;

import org.apache.commons.math.linear.SparseRealMatrix;

public interface PartitioningStrategy {
	public int getDomainDimension(int signalDimension);

	public int getRangeDimension(int signalDimension);

	public SparseRealMatrix getPutOperator(int rangeBlockIndex, int rangeDimension, int signalDimension);

	public SparseRealMatrix getFetchOperator(int domainBlockIndex, int domainDimension, int signalDimension);
}
