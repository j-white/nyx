package math.nyx.framework.linear;

import math.nyx.framework.AbstractDecimationStrategy;

public class AveragingDecimationStrategy extends AbstractDecimationStrategy {
	private final int rangeDimension;
	private final int domainDimension;
	private final int decimationRatio;

	public AveragingDecimationStrategy(int rangeDimension, int domainDimension) {
		this.rangeDimension = rangeDimension;
		this.domainDimension = domainDimension;
		this.decimationRatio = (int)Math.floor((float)domainDimension / rangeDimension);
	}

	@Override
	public void getIndices(int indices[][]) {
		for (int i = 0; i < rangeDimension; i++) {
			for (int j = 0; j < decimationRatio; j++) {
				indices[i][j] = i*decimationRatio + j;
			}
		}
	}

	@Override
	public int getDecimationRatio() {
		return decimationRatio;
	}

	@Override
	public int getRangeDimension() {
		return rangeDimension;
	}

	@Override
	public int getDomainDimension() {
		return domainDimension;
	}
}
