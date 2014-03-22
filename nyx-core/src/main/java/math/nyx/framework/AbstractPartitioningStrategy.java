package math.nyx.framework;

import math.nyx.core.Signal;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

import com.google.common.base.Objects;

public abstract class AbstractPartitioningStrategy implements PartitioningStrategy {

	private final Signal signal;

	private final int scale;

	public AbstractPartitioningStrategy(Signal signal, int scale) {
		this.signal = signal;
		this.scale = scale;
	}

	public Signal getSignal() {
		return signal;
	}

	public int getScale() {
		return scale;
	}

	@Override
	public int getScaledSignalDimension() {
		return signal.getScaledDimension(scale);
	}

	@Override
	public int[] getDomainIndices(int domainBlockIndex) {
		int domainIndices[] = new int[getDomainDimension()];
		getDomainIndices(domainBlockIndex, domainIndices);
		return domainIndices;
	}

	@Override
	public int[] getRangeIndices(int rangeBlockIndex) {
		int rangeIndices[] = new int[getRangeDimension()];
		getRangeIndices(rangeBlockIndex, rangeIndices);
		return rangeIndices;
	}

	@Override
	public SparseRealMatrix getDomainFetchOperator(int domainBlockIndex) {
		int domainIndices[] = getDomainIndices(domainBlockIndex);
		SparseRealMatrix F_I = new OpenMapRealMatrix(getDomainDimension(), getScaledSignalDimension());
		for (int k = 0; k < getDomainDimension(); k++) {
			F_I.setEntry(k, domainIndices[k], 1);
		}
		return F_I;
	}

	@Override
	public SparseRealMatrix getRangeFetchOperator(int rangeBlockIndex) {
		int rangeIndices[] = getRangeIndices(rangeBlockIndex);
		SparseRealMatrix F_I = new OpenMapRealMatrix(getRangeDimension(), getScaledSignalDimension());
		for (int k = 0; k < getRangeDimension(); k++) {
			F_I.setEntry(k, rangeIndices[k], 1);
		}
		return F_I;
	}

	@Override
	public SparseRealMatrix getPutOperator(int rangeBlockIndex) {
		int rangeIndices[] = getRangeIndices(rangeBlockIndex);
		//System.out.printf("Range indices for block %d: %s\n", rangeBlockIndex, Arrays.toString(rangeIndices));
		SparseRealMatrix P_J = new OpenMapRealMatrix(getScaledSignalDimension(), getRangeDimension());
		for (int k = 0; k < getRangeDimension(); k++) {
			P_J.setEntry(rangeIndices[k], k, 1);
		}
		return P_J;
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	            .add("signal", signal)
	            .add("scale", scale)
	            .add("scaledSignalDimension", getScaledSignalDimension())
	            .add("domainDimension", getDomainDimension())
	            .add("rangeDimension", getRangeDimension())
	            .add("numDomainPartitions", getNumDomainPartitions())
	            .add("numRangePartitions", getNumRangePartitions())
	            .toString();
	}
}
