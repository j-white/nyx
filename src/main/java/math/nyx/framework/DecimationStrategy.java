package math.nyx.framework;

import org.apache.commons.math.linear.SparseRealMatrix;

public interface DecimationStrategy {
	public int[][] getIndices();

	public void getIndices(int indices[][]);

	public SparseRealMatrix getDecimationOperator();

	public int getDecimationRatio();

	public int getRangeDimension();
	
	public int getDomainDimension();
}
