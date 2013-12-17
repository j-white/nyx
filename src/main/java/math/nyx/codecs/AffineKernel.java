package math.nyx.codecs;

import org.apache.commons.math.linear.RealMatrix;
import org.springframework.util.Assert;

import math.nyx.core.SignalBlock;
import math.nyx.framework.Kernel;

public class AffineKernel implements Kernel {
	@Override
	public AffineTransform encode(SignalBlock domainBlock, SignalBlock rangeBlock) {
		RealMatrix domain = domainBlock.getBlock();
		RealMatrix range = rangeBlock.getBlock();

		Assert.isTrue(domain.getColumnDimension() == 1, "Domain must be a column vector.");
		Assert.isTrue(range.getColumnDimension() == 1, "Range must be a column vector.");
		Assert.isTrue(domain.getRowDimension() == range.getRowDimension(), "Domain and range must have the same dimension.");

		int n = domain.getRowDimension();
		double s = 0;
		double o = 0;
		double ai[] = domain.getColumn(0);
		double bi[] = range.getColumn(0);
		double sum_ais = domainBlock.getSumOfPoints();
		double sum_bis = rangeBlock.getSumOfPoints();
		double sum_squared_ais = domainBlock.getSumOfSquaredPoints();
		double sum_squared_bis = rangeBlock.getSumOfSquaredPoints();
		double sum_ais_squared = Math.pow(sum_ais,2);
		double one_over_n = ((double)1/(double)n);

		double sum_ais_times_bis = 0;

		for (int i = 0; i < n; i++) {
			sum_ais_times_bis += ai[i] * bi[i];
		}

		double s_denum = (n*sum_squared_ais) - sum_ais_squared;
		if (s_denum == 0) {
			s = 0;
			o = one_over_n * sum_bis;
		} else {
			s = ((n*sum_ais_times_bis) - (sum_ais*sum_bis))/s_denum;
			o = one_over_n * (sum_bis - s*sum_ais);
		}

		/*
		boolean allowNegativeValues = true;
		if (s < 0 && allowNegativeValues == false) {
			s = 0;
		}
		*/

		double u = (s*sum_squared_ais) - (2*sum_ais_times_bis) + (2*o*sum_ais);
		double v = (n*o) - (2*sum_bis);
		double R = one_over_n * (sum_squared_bis + s*u + o*v);
		return new AffineTransform(domainBlock.getIndex(), rangeBlock.getIndex(),
				Math.sqrt(Math.abs(R)), s, o);
	}
}
