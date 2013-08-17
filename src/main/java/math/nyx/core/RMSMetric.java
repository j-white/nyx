package math.nyx.core;

import java.util.Iterator;

public class RMSMetric {
	public double getDistance(Matrix m1, Matrix m2) {
		return getDistance(new RectMatrixPartition(m1), new RectMatrixPartition(m2));
	}
	
	public double getDistance(Partition p1, Partition p2) {
		Partition p1Scaled = scaleDomainIfNeeded(p1, p2);
		int n = p1Scaled.getNumPoints();

		double sum = 0;
		double one_over_n = ((double)1/(double)n);
		
		Iterator<Double> it = p2.iterator();
		for (Double ai : p1Scaled)  {
			sum += Math.pow(ai - it.next(), 2);
		}

		return Math.sqrt(one_over_n * sum);
	}

	public RMSLinearCombination getDistanceFromBestLinearCombination(Partition from, Partition to) {
		return getDistanceFromBestLinearCombination(from, to, false);
	}

	public RMSLinearCombination getDistanceFromBestLinearCombination(Partition from, Partition to, boolean allowNegativeValues) {
		Partition fromScaled = scaleDomainIfNeeded(from, to);
		int n = fromScaled.getNumPoints();

		double s = 0;
		double o = 0;
		double ai[] = fromScaled.toArray();
		double bi[] = to.toArray();
		double sum_ais = fromScaled.getSumOfPoints();
		double sum_bis = to.getSumOfPoints();
		double sum_squared_ais = fromScaled.getSumOfSquaredPoints();
		double sum_squared_bis = to.getSumOfSquaredPoints();
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
		
		if (s < 0 && allowNegativeValues == false) {
			s = 0;
		}

		double u = (s*sum_squared_ais) - (2*sum_ais_times_bis) + (2*o*sum_ais);
		double v = (n*o) - (2*sum_bis);
		double R = one_over_n * (sum_squared_bis + s*u + o*v);
		return new RMSLinearCombination(from, to, Math.sqrt(Math.abs(R)), s, o);
	}

	private Partition scaleDomainIfNeeded(Partition domain, Partition range) {
		int n = domain.getNumPoints();
		if (n != range.getNumPoints()) {
			float scaleToReduceDomain = (float)range.getWidth()/(float)domain.getWidth();
			return domain.scale(scaleToReduceDomain);
		}
		return domain;
	}
}
