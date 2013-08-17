package math.nyx.util;

import math.nyx.core.Matrix;
import math.nyx.fie.MatrixPartitioner;
import math.nyx.fie.simple.SimpleEncodingStrategy;

public class MatrixGenerator {
	public static Matrix generateSquareMatrix(int n) {
		double data[][] = new double[n][n];
		int k = 1;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				data[i][j] = k++;
			}
		}
		return new Matrix(data);
	}

	public static Matrix generateSquareMatrixScaled(int n) {
		double data[][] = new double[n][n];
		double k = (double)(4*n+6)/(double)(4);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				data[i][j] = k+(4*n*i)+2*j;
			}
		}
		return new Matrix(data);
	}

	public static interface MatrixGeneratorVisitor {
		public void generatedDomain(Matrix domain);
		public void generatedRange(Matrix range);
	}

	public static Matrix generateInterestingMatrix(int n) {
		return generateInterestingMatrix(n, null);
	}

	public static Matrix generateInterestingMatrix(int n, MatrixGeneratorVisitor v) {
		double inc = 0.25d;
		double fill = 1.0d;
		Matrix m = new Matrix(n, n);
		m.fill(fill);

		// Determine the length for the domain and range
		SimpleEncodingStrategy fic = new SimpleEncodingStrategy();
		MatrixPartitioner partitioner = fic.getMatrixPartitioner();
		int domainLength = partitioner.getDomainSize(m).x;
		int rangeLength = partitioner.getRangeSize(m).x;

		// Both the domain and range should fit perfectly
		assert(n % domainLength != 0);
		assert(n % rangeLength != 0);

		// Now start off with a constant domain
		Matrix domain = new Matrix(domainLength, domainLength);
		domain.fill(fill);
		if (v != null) {
			v.generatedDomain(domain.copy());
		}
		
		for (int i = 0; i < n; i+=domainLength) {
			for (int j = 0; j < n; j+=domainLength) {
				if (i == 0 && j == 0) {
					// Don't set the first domain, let it stay constant
					continue;
				}
				Matrix scaledDomain = domain.scale(0.5f);
				scaledDomain = scaledDomain.scalarAdd(inc);
				double scaledData[][] = scaledDomain.getDataRef();
				if (v != null) {
					v.generatedRange(scaledDomain.copy());
				}
				domain.setSubMatrix(scaledData, 0, 0);

				scaledDomain = scaledDomain.scalarAdd(inc);
				scaledData = scaledDomain.getDataRef();
				if (v != null) {
					v.generatedRange(scaledDomain.copy());
				}
				domain.setSubMatrix(scaledData, 0, scaledData.length);

				scaledDomain = scaledDomain.scalarAdd(i*inc);
				scaledData = scaledDomain.getDataRef();
				if (v != null) {
					v.generatedRange(scaledDomain.copy());
				}
				domain.setSubMatrix(scaledData, scaledData.length, 0);
				
				scaledDomain = scaledDomain.scalarAdd(inc);
				scaledData = scaledDomain.getDataRef();
				if (v != null) {
					v.generatedRange(scaledDomain.copy());
				}
				domain.setSubMatrix(scaledData, scaledData.length, scaledData.length);

				if (v != null) {
					v.generatedDomain(domain.copy());
				}
				m.setSubMatrix(domain.getDataRef(), i, j);

				if (i*j % 2 == 0) {
					domain = domain.flipVertically();
				}
				domain = domain.rotate();
			}
		}
		
		return m;
	}
}
