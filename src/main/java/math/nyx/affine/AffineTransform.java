package math.nyx.affine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

import math.nyx.core.AbstractTransform;

import com.google.common.base.Objects.ToStringHelper;

public class AffineTransform extends AbstractTransform {
	private static final long serialVersionUID = 4115274904771797361L;
	private final double scale;
	private final double offset;
	private final Symmetry symmetry;

	public AffineTransform(int domainBlockIndex, int rangeBlockIndex, double distance, double scale, double offset) {
		super(domainBlockIndex, rangeBlockIndex, distance);
		this.scale = scale;
		this.offset = offset;
		this.symmetry = Symmetry.ORIGINAL;
	}

	public AffineTransform(int domainBlockIndex, int rangeBlockIndex, double distance, double scale, double offset, Symmetry symmetry) {
		super(domainBlockIndex, rangeBlockIndex, distance);
		this.scale = scale;
		this.offset = offset;
		this.symmetry = symmetry;
	}

	public double getScale() {
		return scale;
	}

	public double getOffset() {
		return offset;
	}

	public Symmetry getSymmetry() {
		return symmetry;
	}

	public RealMatrix apply(final RealMatrix domain, double minVal, double maxVal) {
		RealMatrix permutedDomain = permute(domain, symmetry);
		int rangeDimension = permutedDomain.getRowDimension();
		Array2DRowRealMatrix K_scale = new Array2DRowRealMatrix(rangeDimension, rangeDimension);
		Array2DRowRealMatrix K_offset = new Array2DRowRealMatrix(rangeDimension, 1);

		// Build the transform
		for (int i = 0; i < rangeDimension; i++) {
			K_offset.setEntry(i, 0, offset);

			for (int j = 0; j < rangeDimension; j++) {
				if (i == j) {
					K_scale.setEntry(i, j, scale);
				}
			}
		}

		// Apply the transform
		Array2DRowRealMatrix range = (Array2DRowRealMatrix) (K_scale.multiply(permutedDomain)).add(K_offset);

		// Bound the transform with the min and max vals
		double data[][] = range.getDataRef();
		for (int i = 0; i < data.length; i++) {
			if (data[i][0] < minVal) {
				data[i][0] = minVal;
			} else if (data[i][0] > maxVal) {
				data[i][0] = maxVal;
			}
		}

		return range;
	}

	public static RealMatrix permute(RealMatrix vector, Symmetry symmetry) {
		if (symmetry == Symmetry.ORIGINAL) return vector;
		Iterator<Double> it = new SymmetryIterator(vector.getColumn(0), symmetry);
		int k = 0;
		RealMatrix permutedVector = new Array2DRowRealMatrix(vector.getRowDimension(), 1);
		while (it.hasNext()) {
			permutedVector.setEntry(k++, 0, it.next());
		}
		return permutedVector;
	}

	@Override
	public Map<String, Double> getKernelParameters() {
		Map<String, Double> params = new HashMap<String, Double>();
		params.put("scale", scale);
		params.put("offset", offset);
		params.put("symmetry", (double)symmetry.ordinal());
		return params;
	}

	@Override
	protected ToStringHelper toStringHelper() {
		return super.toStringHelper()
	            .add("scale", scale)
	            .add("offset", offset)
	            .add("symmetry", symmetry);
	}
}
