package math.nyx.framework.linear;

import math.nyx.framework.DecimationStrategyFactory;

public class AveragingDecimationStrategyFactory implements DecimationStrategyFactory  {
	@Override
	public AveragingDecimationStrategy getDecimator(int rangeDimension, int domainDimension) {
		return new AveragingDecimationStrategy(rangeDimension, domainDimension);
	}
}
