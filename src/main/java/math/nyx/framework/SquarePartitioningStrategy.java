package math.nyx.framework;

import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.springframework.util.Assert;

public class SquarePartitioningStrategy implements PartitioningStrategy {
	
	private int getSignalWidth(int signalDimension) {
		int signalWidth = (int)Math.round(Math.sqrt(signalDimension));
		if (signalWidth*signalWidth != signalDimension) {
			throw new IllegalArgumentException("Signal dimension must be a square.");
		}
		return signalWidth;
	}

	@Override
	public int getDomainDimension(int signalDimension) {
		int signalWidth = getSignalWidth(signalDimension);
		int powerOfTwo = (int)Math.floor(Math.log(signalWidth) / Math.log(2) - 1);
		int domainWidth = signalWidth / (int)Math.pow(2, powerOfTwo);
		return domainWidth * domainWidth;
	}

	@Override
	public int getRangeDimension(int signalDimension) {
		int domainWidth = (int)Math.round(Math.sqrt(getDomainDimension(signalDimension)));
		int rangeWidth = domainWidth - 1;
		return rangeWidth * rangeWidth;
	}

	@Override
	public int getNumDomainPartitions(int signalDimension) {
		int signalWidth = getSignalWidth(signalDimension);
		int domainWidth = (int)Math.round(Math.sqrt(getDomainDimension(signalDimension)));
		int k = signalWidth - domainWidth + 1;
		return k*k;
	}

	@Override
	public int getNumRangePartitions(int signalDimension) {
		return Math.round((float)signalDimension / getRangeDimension(signalDimension));
	}

	@Override
	public SparseRealMatrix getFetchOperator(int domainBlockIndex,
			int domainDimension, int signalDimension) {
		int signalWidth = getSignalWidth(signalDimension);
		int domainWidth = (int)Math.round(Math.sqrt(domainDimension));
		Assert.isTrue(domainWidth * domainWidth == domainDimension, "Domain dimension must be a square.");
		Assert.isTrue(signalWidth % domainWidth == 0, "Domain width must divide the signal width.");

		int numDomainsPerRow = signalWidth - domainWidth + 1;
		int numDomainsPerColumn = numDomainsPerRow;

		int domainColumnIndex = domainBlockIndex % numDomainsPerRow;
		int domainRowIndex = (int)Math.floor((float)domainBlockIndex / numDomainsPerColumn);

		int columnIndexOffset = domainColumnIndex + (domainRowIndex * signalWidth);

		SparseRealMatrix F_I = new OpenMapRealMatrix(domainDimension, signalDimension);
		for (int k = 0; k < domainDimension; k++) {
			int columnIndex = ((int)Math.floor((float)k / domainWidth) * signalWidth) + (k % domainWidth);
			F_I.setEntry(k, columnIndex + columnIndexOffset, 1);
		}
		return F_I;
	}

	@Override
	public SparseRealMatrix getPutOperator(int rangeBlockIndex,
			int rangeDimension, int signalDimension) {
		int signalWidth = getSignalWidth(signalDimension);
		int rangeWidth = (int)Math.round(Math.sqrt(rangeDimension));
		Assert.isTrue(rangeWidth * rangeWidth == rangeDimension, "Range dimension must be a square.");
		Assert.isTrue(signalWidth % rangeWidth == 0, "Range width must divide the signal width.");

		int numRangesPerRow = signalWidth / rangeWidth;
		int numRangePerColumn = numRangesPerRow;

		int rangeColumnIndex = rangeBlockIndex % numRangesPerRow;
		int rangeRowIndex = (int)Math.floor((float)rangeBlockIndex / numRangePerColumn);

		int rowIndexOffset = rangeColumnIndex + (rangeRowIndex * rangeWidth * signalWidth);

		SparseRealMatrix P_J = new OpenMapRealMatrix(signalDimension, rangeDimension);
		for (int k = 0; k < rangeDimension; k++) {
			int rowIndex = ((int)Math.floor((float)k / rangeWidth) * signalWidth) + (k % rangeWidth);
			P_J.setEntry(rowIndex + rowIndexOffset, k, 1);
		}
		return P_J;
	}
}
