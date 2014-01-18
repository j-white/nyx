package math.nyx.framework.square;

import math.nyx.framework.AbstractDecimationStrategy;

public class SquareDecimationStrategy extends AbstractDecimationStrategy {
	private final int rangeDimension;
	private final int domainDimension;
	private final int decimationRatio;

	public SquareDecimationStrategy(int rangeDimension, int domainDimension) {
		this.rangeDimension = rangeDimension;
		this.domainDimension = domainDimension;
		
		//TODO: Verify range and domain partition sizes
		this.decimationRatio = 4;
	}

	public void getIndices(int indices[][]) {
		for (int i = 0; i < rangeDimension; i++) {
			int offset = getOffset(i);
			for (int j = 0; j < decimationRatio; j++) {
				int index = getIndex(j);
				indices[i][j] = offset + index;
			}
		}
	}

	protected int getOffset(int decimatorRow) {
		int rangeWidth = (int)Math.sqrt(rangeDimension);
		int domainWidth = (int)Math.sqrt(domainDimension);
		
		int a = (decimatorRow / rangeWidth) * domainWidth * (domainWidth / rangeWidth);
		int b = (decimatorRow % rangeWidth) * (domainWidth / rangeWidth);

		//System.out.printf("Range width: %d Domain width: %d\n", rangeWidth, domainWidth);
		//System.out.printf("a: %d b: %d a+b: %d\n", a, b, a+b);
		
		return a + b;
	}

	protected int getIndex(int cellNumber) {
		int root = (int)Math.sqrt(domainDimension);
		return ((cellNumber / 2) * root) + (cellNumber % 2);
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
