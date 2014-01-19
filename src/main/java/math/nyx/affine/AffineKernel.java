package math.nyx.affine;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.springframework.util.Assert;

import math.nyx.framework.Kernel;
import math.nyx.framework.SignalBlock;

public class AffineKernel implements Kernel {
	public static final double THRESHOLD = 0.001;

	private double threshold = THRESHOLD;

	public static final boolean PERMUTE = false;

	private boolean permute = PERMUTE;

	public static final boolean ALLOW_NEGATIVE_SCALES = false;

	private boolean allowNegativeScales = ALLOW_NEGATIVE_SCALES;

	@Override
	public AffineTransform encode(SignalBlock domainBlock, SignalBlock rangeBlock) {
		return encode(domainBlock, rangeBlock, permute);
	}

	public AffineTransform encode(SignalBlock domainBlock, SignalBlock rangeBlock, Symmetry symmetry) {
		RealMatrix domain = new Array2DRowRealMatrix(domainBlock.getBlock().getData(), false);
		AffineTransform.permute(domain, symmetry);
		RealMatrix range = rangeBlock.getBlock();

		Assert.isTrue(domain.getColumnDimension() == 1,
				"Domain must be a column vector.");
		Assert.isTrue(range.getColumnDimension() == 1,
				"Range must be a column vector.");
		Assert.isTrue(domain.getRowDimension() == range.getRowDimension(),
				"Domain and range must have the same dimension.");

		int n = domain.getRowDimension();
		double s = 0;
		double o = 0;
		double sum_ais = domainBlock.getSumOfPoints();
		double sum_bis = rangeBlock.getSumOfPoints();
		double sum_squared_ais = domainBlock.getSumOfSquaredPoints();
		double sum_squared_bis = rangeBlock.getSumOfSquaredPoints();
		double sum_ais_squared = Math.pow(sum_ais,2);
		double one_over_n = ((double)1/(double)n);

		double sum_ais_times_bis = 0;

		for (int i = 0; i < n; i++) {
			sum_ais_times_bis += domain.getEntry(i, 0) * range.getEntry(i, 0);
		}

		double s_denum = (n*sum_squared_ais) - sum_ais_squared;
		if (s_denum == 0) {
			s = 0;
			o = one_over_n * sum_bis;
		} else {
			s = ((n*sum_ais_times_bis) - (sum_ais*sum_bis))/s_denum;
			o = one_over_n * (sum_bis - s*sum_ais);
		}

		if (s < 0 && allowNegativeScales == false) {
			s = 0;
		}

		double u = (s*sum_squared_ais) - (2*sum_ais_times_bis) + (2*o*sum_ais);
		double v = (n*o) - (2*sum_bis);
		double R = one_over_n * (sum_squared_bis + s*u + o*v);
		return new AffineTransform(domainBlock.getIndex(), rangeBlock.getIndex(),
				Math.sqrt(Math.abs(R)), s, o, symmetry);
	}

	public AffineTransform encode(SignalBlock domainBlock, SignalBlock rangeBlock, boolean permute) {
		if (permute) {
			// Iterate over all of the known symmetries to find the one with the least distance
			AffineTransform bestTransform = null;
			for (Symmetry symmetry : Symmetry.values()) {
				AffineTransform transform = encode(domainBlock, rangeBlock, symmetry);
				if (transform.compareTo(bestTransform) < 0) {
					bestTransform = transform;
				}

				// If the distance is <= the threshold, don't try to find a "better" transform
				if (transform.getDistance() <= threshold) {
					break;
				}
			}
			return bestTransform;
		} else {
			return encode(domainBlock, rangeBlock, Symmetry.ORIGINAL);
		}
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public double getThreshold() {
		return threshold;
	}

	public void setPermute(boolean permute) {
		this.permute = permute;
	}

	public boolean getPermute() { 
		return permute;
	}

	public void setAllowNegativeScales(boolean allowNegativeScales) {
		this.allowNegativeScales = allowNegativeScales;
	}

	public boolean getAllowNegativeScales() { 
		return allowNegativeScales;
	}
}
