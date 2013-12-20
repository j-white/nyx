package math.nyx.framework;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

public class SquarePartitioningStrategy extends AbstractPartitioningStrategy {
	private final int signalWidth;
	private final int domainWidth;
	private final int rangeWidth;
	private final int domainDimension;
	private final int rangeDimension;

	public SquarePartitioningStrategy() {
		// Default constructor
		signalWidth = 0;
		domainWidth = 0;
		rangeWidth = 0;
		domainDimension = 0;
		rangeDimension = 0;
	}

	private SquarePartitioningStrategy(int signalDimension, int scale) {
		super(signalDimension, scale);

		signalWidth = (int)Math.round(Math.sqrt(getSignalDimension()));
		int powerOfTwo = (int)Math.floor(Math.log(signalWidth) / Math.log(2) - 1);
		domainWidth = signalWidth / (int)Math.pow(2, powerOfTwo);
		rangeWidth = domainWidth - 1;
		
		domainDimension = domainWidth * domainWidth;
		rangeDimension = rangeWidth * rangeWidth;
	}

	@Override
	public void checkSignalDimension(int signalDimension, int scale) {
		int root = (int)Math.round(Math.sqrt(signalDimension));
		if (root*root != signalDimension) {
			throw new IllegalArgumentException("Signal dimension must be a square.");
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
		int k = signalWidth - domainWidth + 1;
		return k*k;
	}

	@Override
	public int getNumRangePartitions() {
		return getSignalDimension() / (rangeWidth * rangeWidth);
	}

	@Override
	public SparseRealMatrix getDomainFetchOperator(int domainBlockIndex) {
		int numDomainsPerRow = signalWidth - domainWidth + 1;
		int numDomainsPerColumn = numDomainsPerRow;

		int domainColumnIndex = domainBlockIndex % numDomainsPerRow;
		int domainRowIndex = (int)Math.floor((float)domainBlockIndex / numDomainsPerColumn);

		int columnIndexOffset = domainColumnIndex + (domainRowIndex * signalWidth);
	
		SparseRealMatrix F_I = new OpenMapRealMatrix(domainDimension, getSignalDimension());
		for (int k = 0; k < domainDimension; k++) {
			int columnIndex = ((int)Math.floor((float)k / domainWidth) * signalWidth) + (k % domainWidth);
			F_I.setEntry(k, columnIndex + columnIndexOffset, 1);
		}
		return F_I;
	}

	@Override
	public int[] getRangeIndices(int rangeBlockIndex) {
		int numRangesPerRow = signalWidth / rangeWidth;
		int numRangePerColumn = numRangesPerRow;

		int rangeColumnIndex = rangeBlockIndex % numRangesPerRow;
		int rangeRowIndex = (int)Math.floor((float)rangeBlockIndex / numRangePerColumn);

		int rowIndexOffset = rangeColumnIndex + (rangeRowIndex * rangeWidth * signalWidth);

		int rangeIndices[] = new int[rangeDimension];
		for (int k = 0; k < rangeDimension; k++) {
			int rowIndex = ((int)Math.floor((float)k / rangeWidth) * signalWidth) + (k % rangeWidth);
			rangeIndices[k] = rowIndex + rowIndexOffset;
		}
		return rangeIndices;
	}
}
