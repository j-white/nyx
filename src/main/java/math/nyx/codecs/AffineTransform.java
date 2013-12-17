package math.nyx.codecs;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;

import math.nyx.framework.AbstractTransform;

import com.google.common.base.Objects;

public class AffineTransform extends AbstractTransform {
	private static final long serialVersionUID = 4115274904771797361L;
	private final double scale;
	private final double offset;
	
	public AffineTransform(int domainBlockIndex, int rangeBlockIndex, double distance, double scale, double offset) {
		super(domainBlockIndex, rangeBlockIndex, distance);
		this.scale = scale;
		this.offset = offset;
	}

	public double getScale() {
		return scale;
	}

	public double getOffset() {
		return offset;
	}

	public RealMatrix apply(RealMatrix domain) {
		int rangeDimension = domain.getRowDimension();
		SparseRealMatrix K_scale = new OpenMapRealMatrix(rangeDimension, rangeDimension);
		RealMatrix K_offset = new Array2DRowRealMatrix(rangeDimension, 1);

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
		return (K_scale.multiply(domain)).add(K_offset);
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	            .add("scale", scale)
	            .add("offset", offset)
	            .toString();
	}
}
