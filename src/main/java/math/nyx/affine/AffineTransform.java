package math.nyx.affine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math.linear.Array2DColumnRealMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

import math.nyx.core.AbstractTransform;
import math.nyx.core.Signal;

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

	@Override
	public RealMatrix apply(final RealMatrix domain, Signal signal) {
		return apply(domain, signal, false);
	}

	@Override
	public RealMatrix apply(RealMatrix domain, Signal signal, boolean inPlace) {
		RealMatrix permutedDomain = permute(domain, symmetry, inPlace);
		int rangeDimension = permutedDomain.getRowDimension();

		RealMatrix range = null;
		if (inPlace) {
			range = domain;
			if (domain instanceof Array2DColumnRealMatrix) {
				double data[][] = ((Array2DColumnRealMatrix)range).getDataRef();
				for (int i = 0; i < rangeDimension; i++) {
					data[0][i] = data[0][i] * scale + offset; 
				}
			} else {
				for (int i = 0; i < rangeDimension; i++) {
					range.setEntry(i, 0, domain.getEntry(i, 0) * scale + offset);
				}
			}
		} else {
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
			range = (Array2DRowRealMatrix) (K_scale.multiply(permutedDomain)).add(K_offset);
		}

		// Bound the transform with the min and max vals of the underlying signal
		double minVal = signal.getMinVal();
		double maxVal = signal.getMaxVal();
		if (range instanceof Array2DColumnRealMatrix) {
			double data[][] = ((Array2DColumnRealMatrix)range).getDataRef();
			for (int i = 0; i < data.length; i++) {
				if (data[0][i] < minVal) {
					data[0][i] = minVal;
				} else if (data[0][i] > maxVal) {
					data[0][i] = maxVal;
				}
			}
		} else {
			for (int i = 0; i < rangeDimension; i++) {
				if (range.getEntry(i, 0) < minVal) {
					range.setEntry(i, 0, minVal);
				} else if (range.getEntry(i, 0) > maxVal) {
					range.setEntry(i, 0, maxVal);
				}
			}
		}

		return range;
	}

	public static RealMatrix permute(RealMatrix vector, Symmetry symmetry, boolean inPlace) {
		if (symmetry == Symmetry.ORIGINAL) return vector;

		//TODO: getColumn() creates a copy of the underlying vector, try to avoid this
		Iterator<Double> it = new SymmetryIterator(vector.getColumn(0), symmetry);
		int k = 0;
		RealMatrix permutedVector = vector;
		if (!inPlace) {
			permutedVector = new Array2DRowRealMatrix(vector.getRowDimension(), 1);
		}
		while (it.hasNext()) {
			permutedVector.setEntry(k++, 0, it.next());
		}
		return permutedVector;
	}

	@Override
	public Map<String, Object> getKernelParameters() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("scale", String.format("%.4f", scale));
		params.put("offset", String.format("%.4f", offset));
		params.put("symmetry", symmetry);
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
