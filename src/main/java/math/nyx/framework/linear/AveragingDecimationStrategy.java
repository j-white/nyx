package math.nyx.framework.linear;

import math.nyx.framework.DecimationStrategy;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public class AveragingDecimationStrategy implements DecimationStrategy {
	private final int rangeDimension;
	private final int domainDimension;
	private final int decimationRatio;

	public AveragingDecimationStrategy(int rangeDimension, int domainDimension) {
		this.rangeDimension = rangeDimension;
		this.domainDimension = domainDimension;
		this.decimationRatio = (int)Math.floor((float)domainDimension / rangeDimension);
	}

	@Override
	public SparseRealMatrix getDecimationOperator() {
		SparseRealMatrix D = new OpenMapRealMatrix(rangeDimension, domainDimension);
		for (int i = 0; i < rangeDimension; i++) {
			for (int j = 0; j < decimationRatio; j++) {
				D.setEntry(i, i*decimationRatio + j, 1.0f/decimationRatio);
			}
		}
		return D;
	}

	@Override
	public int getDecimationRatio() {
		return decimationRatio;
	}

	public int getRangeDimension() {
		return rangeDimension;
	}

	public int getDomainDimension() {
		return domainDimension;
	}
}
