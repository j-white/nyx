package math.nyx.framework.square;

import math.nyx.framework.DecimationStrategyFactory;

public class SquareDecimationStrategyFactory implements DecimationStrategyFactory  {
	@Override
	public SquareDecimationStrategy getDecimator(int rangeDimension, int domainDimension) {
		return new SquareDecimationStrategy(rangeDimension, domainDimension);
	}
}
