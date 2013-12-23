package math.nyx.framework;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public class SquarePartitioningStrategy extends AbstractPartitioningStrategy {
	private final int originalSignalWidth;
	private final int scaledSignalWidth;
	private final int domainWidth;
	private final int rangeWidth;
	private final int domainDimension;
	private final int rangeDimension;
	private final int sqrtOfScale;

	public SquarePartitioningStrategy() {
		// Default constructor
		originalSignalWidth = 0;
		scaledSignalWidth = 0;
		domainWidth = 0;
		rangeWidth = 0;
		domainDimension = 0;
		rangeDimension = 0;
		sqrtOfScale = 0;
	}

	private SquarePartitioningStrategy(int signalDimension, int numSignalChannels, int scale) {
		super(signalDimension, numSignalChannels, scale);
		
		sqrtOfScale = (int)Math.round(Math.sqrt(getScale()));
		originalSignalWidth = (int)Math.round(Math.sqrt(getSignalDimension()));
		scaledSignalWidth = (int)Math.round(Math.sqrt(getScaledSignalDimension()));
		
		domainWidth = calculateDomainWidth(getSignalDimension(), getNumSignalChannels()) * sqrtOfScale;
		rangeWidth = domainWidth - (1 * sqrtOfScale);
		
		domainDimension = domainWidth * domainWidth;
		rangeDimension = rangeWidth * rangeWidth;
	}

	public static int calculateDomainWidth(int signalDimension, int numSignalChannels) {
		int channelDimension = signalDimension / numSignalChannels;
		int a = (int)Math.pow(signalDimension, 0.4f);
		int b = (int)Math.pow(a, 0.4f);

		int k = 2;
		for (int i = 2; i <= b; i++) {
			int n = (i-1)*(i-1);
			if (n > a) {
				break;
			}
			if (channelDimension % n == 0) {
				k = i;
			}
		}
		return Math.max(k, 2);
	}

	@Override
	public void checkSignalDimension(int signalDimension, int numSignalChannels, int scale) {
		if (signalDimension < 4) {
			throw new IllegalArgumentException("Signal dimension must be greater than 4.");
		}
		int root = (int)Math.round(Math.sqrt(signalDimension));
		if (root*root != signalDimension) {
			throw new IllegalArgumentException("Signal dimension must be a square: " + signalDimension);
		}
		if (scale < 1) {
			throw new IllegalArgumentException("Scale must be a positive integer.");
		}
		if (signalDimension % numSignalChannels != 0) {
			throw new IllegalArgumentException("The number of channels must divide the signal dimension.");
		}
		root = (int)Math.round(Math.sqrt(scale));
		if (root*root != scale) {
			throw new IllegalArgumentException("Scale must be a square.");
		}
	}

	@Override
	public SquarePartitioningStrategy getPartitioner(Signal signal) {
		return getPartitioner(signal.getDimension(), signal.getNumChannels(), 1);
	}

	@Override
	public SquarePartitioningStrategy getPartitioner(Fractal fractal, int scale) {
		return getPartitioner(fractal.getSignalDimension(), fractal.getNumSignalChannels(), scale);
	}

	@Override
	public SquarePartitioningStrategy getPartitioner(int signalDimension, int numSignalChannels, int scale) {
		checkSignalDimension(signalDimension, numSignalChannels, scale);
		return new SquarePartitioningStrategy(signalDimension, numSignalChannels, scale);
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
		int k = originalSignalWidth - (domainWidth / sqrtOfScale) + 1;
		return k*k;
	}

	@Override
	public int getNumRangePartitions() {
		return getScaledSignalDimension() / (rangeWidth * rangeWidth);
	}

	@Override
	public SparseRealMatrix getDomainFetchOperator(int domainBlockIndex) {
		int domainIndexOffset = getBlockOffset(domainBlockIndex, domainWidth, domainWidth, true);

		SparseRealMatrix F_I = new OpenMapRealMatrix(domainDimension, getScaledSignalDimension());
		for (int k = 0; k < domainDimension; k++) {
			int domainIndex = ((k / domainWidth) * scaledSignalWidth) + (k % domainWidth);
			F_I.setEntry(k, domainIndex + domainIndexOffset, 1);
		}
		return F_I;
	}

	@Override
	public int[] getRangeIndices(int rangeBlockIndex) {
		int rangeIndexOffset = getBlockOffset(rangeBlockIndex, rangeWidth, rangeWidth, false);

		int rangeIndices[] = new int[rangeDimension];
		for (int k = 0; k < rangeDimension; k++) {
			int rangeIndex = ((k / rangeWidth) * scaledSignalWidth) + (k % rangeWidth);
			rangeIndices[k] = rangeIndex + rangeIndexOffset;
		}

		return rangeIndices;
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
			numBlocksPerRow = (spaceWidth / sqrtOfScale) - (blockWidth / sqrtOfScale) + 1;
			numBlocksPerColumn = (spaceHeight / sqrtOfScale) - (blockHeight / sqrtOfScale) + 1;
		}

		int rowIndex = index / numBlocksPerRow;
		int columnIndex = index % numBlocksPerColumn;
		int offset;

		if (!overlapping) {
			offset = (rowIndex * (numBlocksPerRow * blockWidth * blockHeight)) + (columnIndex * blockWidth);
		} else {
			offset = (rowIndex * spaceWidth) + columnIndex;
			offset *= sqrtOfScale;
		}
		
		/*
		System.out.printf("\nIndex: %d Width: %d Height: %d Overlapping: %s\n", index, blockWidth, blockHeight, overlapping);
		System.out.printf("Blocks/row: %d Blocks/column: %d\n", numBlocksPerRow, numBlocksPerColumn);
		System.out.printf("Row: %d,  Column %d, Offset: %d\n", rowIndex, columnIndex, offset);
		*/
		
		return offset;
	}
}
