package math.nyx.framework.square;

import math.nyx.framework.DecimationStrategy;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public class SquareDecimationStrategy implements DecimationStrategy {
	@Override
	public SparseRealMatrix getDecimationOperator(int rangeDimension, int domainDimension) {
		final int decimationRatio = 4;
		SparseRealMatrix D = new OpenMapRealMatrix(rangeDimension, domainDimension);
		for (int i = 0; i < rangeDimension; i++) {
			int offset = getOffset(i, rangeDimension, domainDimension);
			for (int j = 0; j < decimationRatio; j++) {
				int index = getIndex(j, rangeDimension, domainDimension);
				D.setEntry(i, offset + index, 1.0f / decimationRatio);
			}
		}
		return D;
	}

	protected int getOffset(int decimatorRow, int rangeDimension, int domainDimension) {
		int rangeWidth = (int)Math.sqrt(rangeDimension);
		int domainWidth = (int)Math.sqrt(domainDimension);
		
		int a = (decimatorRow / rangeWidth) * domainWidth * (domainWidth / rangeWidth);
		int b = (decimatorRow % rangeWidth) * (domainWidth / rangeWidth);

		//System.out.printf("Range width: %d Domain width: %d\n", rangeWidth, domainWidth);
		//System.out.printf("a: %d b: %d a+b: %d\n", a, b, a+b);
		
		return a + b;
	}

	protected int getIndex(int cellNumber, int rangeDimension, int domainDimension) {
		int root = (int)Math.sqrt(domainDimension);
		return ((cellNumber / 2) * root) + (cellNumber % 2);
	}
}
