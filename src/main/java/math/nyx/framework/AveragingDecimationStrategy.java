package math.nyx.framework;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public class AveragingDecimationStrategy implements DecimationStrategy {
	@Override
	public SparseRealMatrix getDecimationOperator(int rangeDimension, int domainDimension) {
		final int decimationRatio = Math.round((float)domainDimension / rangeDimension);

		SparseRealMatrix D = new OpenMapRealMatrix(rangeDimension, domainDimension);
		for (int i = 0; i < rangeDimension; i++) {
			for (int j = 0; j < decimationRatio; j++) {
				D.setEntry(i, i*decimationRatio + j, 1.0f/decimationRatio);
			}
		}
		return D;
	}
}
