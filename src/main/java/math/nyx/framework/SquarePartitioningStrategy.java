package math.nyx.framework;

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

	private SquarePartitioningStrategy(int signalDimension, int scale) {
		super(signalDimension, scale);
		
		sqrtOfScale = (int)Math.round(Math.sqrt(getScale()));
		originalSignalWidth = (int)Math.round(Math.sqrt(getSignalDimension()));
		scaledSignalWidth = (int)Math.round(Math.sqrt(getScaledSignalDimension()));
		
		domainWidth = calculateDomainWidth(getSignalDimension()) * sqrtOfScale;
		rangeWidth = domainWidth - (1 * sqrtOfScale);
		
		domainDimension = domainWidth * domainWidth;
		rangeDimension = rangeWidth * rangeWidth;
	}

	public static int calculateDomainWidth(int signalDimension) {
		// Find the largest square that divides the signal dimension that is
		// less or equal to its square root
		int k = 2;
		int quad = (int)Math.pow(signalDimension, 0.7f);
		int quart = (int)Math.pow(quad, 0.5f);
		for (int i = 1; i <= quart; i++) {
			int n = i*i;
			if (n > quad) {
				break;
			}
			if (signalDimension % n == 0) {
				k = i;
			}
		}
		return Math.max(k, 2);
	}

	@Override
	public void checkSignalDimension(int signalDimension, int scale) {
		if (signalDimension < 4) {
			throw new IllegalArgumentException("Signal dimension must be greater than 4.");
		}
		int root = (int)Math.round(Math.sqrt(signalDimension));
		if (root*root != signalDimension) {
			throw new IllegalArgumentException("Signal dimension must be a square.");
		}
		if (scale < 1) {
			throw new IllegalArgumentException("Scale must be a positive integer.");
		}
		root = (int)Math.round(Math.sqrt(scale));
		if (root*root != scale) {
			throw new IllegalArgumentException("Scale must be a square.");
		}
	}

	@Override
	public SquarePartitioningStrategy getPartitioner(int signalDimension) {
		return getPartitioner(signalDimension, 1);
	}

	@Override
	public SquarePartitioningStrategy getPartitioner(int signalDimension, int scale) {
		checkSignalDimension(signalDimension, scale);
		return new SquarePartitioningStrategy(signalDimension, scale);
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
		int numDomainsPerRow = originalSignalWidth - (domainWidth / sqrtOfScale) + 1;
		int numDomainsPerColumn = numDomainsPerRow;

		int domainColumnIndex = domainBlockIndex % numDomainsPerRow;
		int domainRowIndex = (int)Math.floor((float)domainBlockIndex / numDomainsPerColumn);

		int columnIndexOffset = domainColumnIndex * sqrtOfScale + (domainRowIndex * sqrtOfScale * scaledSignalWidth);

		//System.out.printf("Domains per row: %d, Domains per column: %d\n", numDomainsPerRow, numDomainsPerColumn);
		//System.out.printf("Idx: %d, Row: %d,  Column %d, Offset: %d\n", domainBlockIndex, domainRowIndex, domainColumnIndex, columnIndexOffset);
		SparseRealMatrix F_I = new OpenMapRealMatrix(domainDimension, getScaledSignalDimension());
		for (int k = 0; k < domainDimension; k++) {
			int columnIndex = ((int)Math.floor((float)k / domainWidth) * scaledSignalWidth) + (k % domainWidth);
			F_I.setEntry(k, columnIndex + columnIndexOffset, 1);
		}
		return F_I;
	}

	@Override
	public int[] getRangeIndices(int rangeBlockIndex) {
		int numRangesPerRow = scaledSignalWidth / rangeWidth;
		int numRangePerColumn = numRangesPerRow;

		int rangeColumnIndex = rangeBlockIndex % numRangesPerRow;
		int rangeRowIndex = (int)Math.floor((float)rangeBlockIndex / numRangePerColumn);

		int rowIndexOffset = rangeColumnIndex * sqrtOfScale + (rangeRowIndex * sqrtOfScale * scaledSignalWidth);

		int rangeIndices[] = new int[rangeDimension];
		for (int k = 0; k < rangeDimension; k++) {
			int rowIndex = ((int)Math.floor((float)k / rangeWidth) * scaledSignalWidth) + (k % rangeWidth);
			rangeIndices[k] = rowIndex + rowIndexOffset;
		}
		return rangeIndices;
	}
}
