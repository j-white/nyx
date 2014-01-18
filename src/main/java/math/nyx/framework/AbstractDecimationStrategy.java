package math.nyx.framework;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public abstract class AbstractDecimationStrategy implements DecimationStrategy {
	@Override
	public SparseRealMatrix getDecimationOperator() {
		final int decimationRatio = getDecimationRatio();
		SparseRealMatrix D = new OpenMapRealMatrix(getRangeDimension(), getDomainDimension());

		int indices[][] = getIndices();
		for (int i = 0; i < indices.length; i++) {
			for (int j = 0; j < indices[i].length; j++) {
				D.setEntry(i, indices[i][j], 1.0f/decimationRatio);
			}
		}
		return D;
	}

	@Override
	public int[][] getIndices() {
		int indices[][] = new int[getRangeDimension()][getDecimationRatio()];
		getIndices(indices);
		return indices;
	}
}
