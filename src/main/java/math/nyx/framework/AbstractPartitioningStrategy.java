package math.nyx.framework;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public abstract class AbstractPartitioningStrategy implements PartitioningStrategy {

	public abstract int[] getRangeIndices(int rangeBlockIndex, int rangeDimension, int signalDimension);

	@Override
	public SparseRealMatrix getRangeFetchOperator(int rangeBlockIndex,
			int rangeDimension, int signalDimension) {
		int rangeIndices[] = getRangeIndices(rangeBlockIndex, rangeDimension, signalDimension);
		SparseRealMatrix F_I = new OpenMapRealMatrix(rangeDimension, signalDimension);
		for (int k = 0; k < rangeDimension; k++) {
			F_I.setEntry(k, rangeIndices[k], 1);
		}
		return F_I;
	}

	@Override
	public SparseRealMatrix getPutOperator(int rangeBlockIndex,
			int rangeDimension, int signalDimension) {
		int rangeIndices[] = getRangeIndices(rangeBlockIndex, rangeDimension, signalDimension);
		SparseRealMatrix P_J = new OpenMapRealMatrix(signalDimension, rangeDimension);
		for (int k = 0; k < rangeDimension; k++) {
			P_J.setEntry(rangeIndices[k], k, 1);
		}
		return P_J;
	}
}
