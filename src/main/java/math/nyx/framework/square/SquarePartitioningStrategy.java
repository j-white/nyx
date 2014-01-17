package math.nyx.framework.square;

import math.nyx.core.Signal;
import math.nyx.framework.AbstractPartitioningStrategy;

public class SquarePartitioningStrategy extends AbstractPartitioningStrategy {
	private final int scale;
	private final int signalDimension;
	private final int originalSignalWidth;
	private final int scaledSignalWidth;
	private int domainWidth;
	private int rangeWidth;
	private final int domainDimension;
	private final int rangeDimension;

	public SquarePartitioningStrategy() {
		// Default constructor
		scale = 0;
		signalDimension = 0;
		originalSignalWidth = 0;
		scaledSignalWidth = 0;
		domainWidth = 0;
		rangeWidth = 0;
		domainDimension = 0;
		rangeDimension = 0;
	}

	private SquarePartitioningStrategy(Signal signal, int scale) {
		super(signal, scale);
		this.scale = scale;
		signalDimension = signal.getDimension();

		originalSignalWidth = (int)Math.round(Math.sqrt(signalDimension));
		scaledSignalWidth = (int)Math.round(Math.sqrt(getScaledSignalDimension()));
		
		rangeWidth = calculateRangeWidth(signalDimension) * scale;
		domainWidth = 2*rangeWidth;

		domainDimension = domainWidth * domainWidth;
		rangeDimension = rangeWidth * rangeWidth;
	}

	private int calculateRangeWidth(int signalDimension) {
		int sqrtOfSignalDimension = (int)Math.sqrt(signalDimension);
		int upperBound = 2 * (int)Math.log(sqrtOfSignalDimension);

		// Find the largest even square that divides the sqrt of the channel dimension
		int k = 1;
		for (int i = 1; i < upperBound; i++) {
			int n = 2*i;
			if (sqrtOfSignalDimension % n == 0) {
				k = i;
			}
		}

		return Math.max(k, 1);
	}

	@Override
	public boolean isCompatible(Signal signal, int scale) {
		if (signal.getDimension() < 4) {
			return false;
		}
		int root = (int)Math.round(Math.sqrt(signal.getDimension()));
		if (root*root != signal.getDimension()) {
			return false;
		}
		if (scale < 1) {
			return false;
		}
		return true;
	}

	@Override
	public SquarePartitioningStrategy getPartitioner(Signal signal) {
		return getPartitioner(signal, 1);
	}

	@Override
	public SquarePartitioningStrategy getPartitioner(Signal signal, int scale) {
		if(!isCompatible(signal, scale)) {
			throw new IllegalArgumentException("Signal is not compatible with partitioning strategy.");
		}
		return new SquarePartitioningStrategy(signal, scale);
	}

	@Override
	public int getDomainDimension() {
		return domainDimension;
	}

	@Override
	public int getRangeDimension() {
		return rangeDimension;
	}

	@Override
	public int getNumDomainPartitions() {
		int k = originalSignalWidth - (domainWidth / getScale()) + 1;
		return k*k;
	}

	@Override
	public int getNumRangePartitions() {
		return getScaledSignalDimension() / (rangeWidth * rangeWidth);
	}

	@Override
	public void getDomainIndices(int domainBlockIndex, int domainIndices[]) {
		int domainIndexOffset = getBlockOffset(domainBlockIndex, domainWidth, domainWidth, true);
		for (int k = 0; k < domainDimension; k++) {
			int domainIndex = ((k / domainWidth) * scaledSignalWidth) + (k % domainWidth);
			domainIndices[k] = domainIndex + domainIndexOffset;
		}
	}

	@Override
	public void getRangeIndices(int rangeBlockIndex, int rangeIndices[]) {
		int rangeIndexOffset = getBlockOffset(rangeBlockIndex, rangeWidth, rangeWidth, false);
		for (int k = 0; k < rangeDimension; k++) {
			int rangeIndex = ((k / rangeWidth) * scaledSignalWidth) + (k % rangeWidth);
			rangeIndices[k] = rangeIndex + rangeIndexOffset;
		}
	}

	protected int getBlockOffset(int index, int blockWidth, int blockHeight, boolean overlapping) {
		// Assuming that the signal width is equal to the signal height
		int spaceWidth = scaledSignalWidth;
		int spaceHeight = spaceWidth;

		int numBlocksPerRow;
		int numBlocksPerColumn;
		
		if (!overlapping) {
			numBlocksPerRow = spaceWidth / blockWidth;
			numBlocksPerColumn = spaceHeight / blockHeight; 
		} else {
			numBlocksPerRow = (spaceWidth / scale) - (blockWidth / scale) + 1;
			numBlocksPerColumn = (spaceHeight / scale) - (blockHeight / scale) + 1;
		}

		int rowIndex = index / numBlocksPerRow;
		int columnIndex = index % numBlocksPerColumn;
		int offset;

		if (!overlapping) {
			offset = (rowIndex * (numBlocksPerRow * blockWidth * blockHeight)) + (columnIndex * blockWidth);
		} else {
			offset = (rowIndex * spaceWidth) + columnIndex;
			offset *= scale;
		}

		/*
		System.out.printf("\nIndex: %d Width: %d Height: %d Overlapping: %s\n", index, blockWidth, blockHeight, overlapping);
		System.out.printf("Blocks/row: %d Blocks/column: %d\n", numBlocksPerRow, numBlocksPerColumn);
		System.out.printf("Row: %d,  Column %d, Offset: %d\n", rowIndex, columnIndex, offset);
		*/
		
		return offset;
	}

	@Override
	public int getPaddedDimension(Signal signal) {
		// Pad the signal to the closest integer of the form k^2
		int k = (int)Math.ceil(Math.sqrt(signal.getDimension()));
		k = Math.max(k, 2);
		return k * k;
	}
}
