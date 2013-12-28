package math.nyx.framework;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

import com.google.common.base.Objects;

public abstract class AbstractPartitioningStrategy implements PartitioningStrategy {

	private final int signalDimension;

	private final int numSignalChannels;

	private final int scale;

	public AbstractPartitioningStrategy() {
		signalDimension = 0;
		numSignalChannels = 0;
		scale = 0;
	}

	public AbstractPartitioningStrategy(int signalDimension, int numSignalChannels, int scale) {
		this.signalDimension = signalDimension;
		this.numSignalChannels = numSignalChannels;
		this.scale = scale;
	}

	public int getScale() {
		return scale;
	}

	public int getSignalDimension() {
		return signalDimension;
	}

	public int getNumSignalChannels() {
		return numSignalChannels;
	}

	@Override
	public int getScaledSignalDimension() {
		return signalDimension * scale;
	}

	public abstract void checkSignalDimension(int signalDimension, int numSignalChannels, int scale);

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
	            .add("signalDimension", signalDimension)
	            .add("scale", scale)
	            .add("scaledSignalDimension", getScaledSignalDimension())
	            .add("domainDimension", getDomainDimension())
	            .add("rangeDimension", getRangeDimension())
	            .add("numDomainPartitions", getNumDomainPartitions())
	            .add("numRangePartitions", getNumRangePartitions())
	            .toString();
	}
}
