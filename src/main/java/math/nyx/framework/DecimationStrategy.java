package math.nyx.framework;

import org.apache.commons.math.linear.SparseRealMatrix;

public interface DecimationStrategy {
	public SparseRealMatrix getDecimationOperator();

	public int getDecimationRatio();
}
