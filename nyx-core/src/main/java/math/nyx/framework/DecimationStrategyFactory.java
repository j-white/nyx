package math.nyx.framework;

public interface DecimationStrategyFactory {
	public DecimationStrategy getDecimator(int rangeDimension, int domainDimension);
}
