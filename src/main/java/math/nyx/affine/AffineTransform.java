package math.nyx.affine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.math.linear.Array2DColumnRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.springframework.util.Assert;

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
	public void apply(RealMatrix domain, Signal signal) {
		permute(domain, symmetry);

		int dimension = domain.getRowDimension();
		for (int i = 0; i < dimension; i++) {
			domain.setEntry(i, 0, domain.getEntry(i, 0) * scale + offset);
		}

		// Bound the transform with the min and max vals of the underlying signal
		double minVal = signal.getMinVal();
		double maxVal = signal.getMaxVal();
		for (int i = 0; i < dimension; i++) {
			if (domain.getEntry(i, 0) < minVal) {
				domain.setEntry(i, 0, minVal);
			} else if (domain.getEntry(i, 0) > maxVal) {
				domain.setEntry(i, 0, maxVal);
			}
		}
	}

	@Override
	public void apply(Array2DColumnRealMatrix domain, Signal signal) {
		permute(domain, symmetry);

		double data[][] = domain.getDataRef();
		int dimension = domain.getRowDimension();
		for (int i = 0; i < dimension; i++) {
			data[0][i] = data[0][i] * scale + offset; 
		}
		
		// Bound the transform with the min and max vals of the underlying signal
		double minVal = signal.getMinVal();
		double maxVal = signal.getMaxVal();
		for (int i = 0; i < data[0].length; i++) {
			if (data[0][i] < minVal) {
				data[0][i] = minVal;
			} else if (data[0][i] > maxVal) {
				data[0][i] = maxVal;
			}
		}
	}

	public static void permute(RealMatrix vector, Symmetry symmetry) {
		if (symmetry == Symmetry.ORIGINAL)
			return;

		// Permute using the symmetry operator
		int k = 0;
		double copyOfColumn[] = vector.getColumn(0);
		Iterator<Double> it = new SymmetryIterator(copyOfColumn, symmetry);
		while (it.hasNext()) {
			vector.setEntry(k++, 0, it.next());
		}

		Assert.isTrue(k == copyOfColumn.length);
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
