package math.nyx.codecs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.springframework.util.Assert;

import math.nyx.core.SignalBlock;
import math.nyx.core.FractalEncoder;
import math.nyx.core.Signal;

public class AffineFractalCodec implements FractalEncoder {
	private final double ln_of_2 = Math.log(2);

	public AffineFractal encode(Signal signal) {
		RealMatrix x = signal.getVector();
		int signalDimension = signal.getDimension();
		int domainDimension = getDomainDimension(signalDimension);
		int rangeDimension = getRangeDimension(signalDimension);

		Assert.isTrue(signalDimension % domainDimension == 0, "Domain dimension should divide the signal dimension");
		Assert.isTrue(domainDimension % rangeDimension == 0, "Range dimension should divide the domain dimension");

		int numberOfRanges = Math.round((float)signalDimension / rangeDimension);

		// Break the signal into non-overlapping range blocks
		List<SignalBlock> rangeBlocks = new ArrayList<SignalBlock>();
		for (int i = 0; i < numberOfRanges; i++) {
			// Build a sparse matrix with 1s on the diagonal offset at i*rangeDimension
			SparseRealMatrix F_I = new OpenMapRealMatrix(rangeDimension, signalDimension);
			for (int j = 0; j < rangeDimension; j++) {
				F_I.setEntry(j, i*rangeDimension + j, 1);
			}

			// Multiply the sparse matrix with the vector to retrieve the range block
			RealMatrix rangeBlock = F_I.multiply(x);		

			// Store the results
			rangeBlocks.add(new SignalBlock(i, rangeBlock));
		}

		// Build the decimation operator D
		SparseRealMatrix D = getDecimationOperator(rangeDimension, domainDimension);

		// Break the signal into overlapping domain blocks
		List<SignalBlock> decimatedDomainBlocks = new ArrayList<SignalBlock>();
		for (int i = 0; i <= (signalDimension - domainDimension); i++) {
			// Build a sparse matrix with 1s on the diagonal offset at i
			SparseRealMatrix F_I = new OpenMapRealMatrix(domainDimension, signalDimension);
			for (int j = 0; j < domainDimension; j++) {
				F_I.setEntry(j, i + j, 1);
			}

			// Multiply the sparse matrix with the vector to retrieve the domain block
			RealMatrix domainBlock = F_I.multiply(x);		

			// Decimate the domain block
			RealMatrix decimatedDomainBlock = D.multiply(domainBlock);

			// Store the results
			decimatedDomainBlocks.add(new SignalBlock(i, decimatedDomainBlock));
		}

		// Construct the fractal used to store our results
		AffineFractal fractal = new AffineFractal();
		fractal.setSignalDimension(signal.getDimension());

		// Now match the domain and range partitions while minimizing the distance
		// and store the results in the fractal
		int k = 0;
		for (SignalBlock rangeBlock : rangeBlocks) {
			System.out.printf("Encoding range block %d of %d\n", ++k, rangeBlocks.size());
			AffineTransform bestTransform = null;
			for (SignalBlock domainBlock : decimatedDomainBlocks) {
				AffineTransform transform = getAffineTransform(domainBlock, rangeBlock);
				if (transform.compareTo(bestTransform) < 0) {
					bestTransform = transform;
				}
			}

			Assert.isTrue(bestTransform != null, "No domain blocks found.");
			fractal.addTransform(bestTransform);
			//System.out.println(bestTransform);
		}

		return fractal;
	}

	public Signal decode(AffineFractal fractal, float scale) {
		int signalDimension = Math.round(fractal.getSignalDimension() * scale);
		int domainDimension = Math.round(getDomainDimension(fractal.getSignalDimension()) * scale);
		int rangeDimension = Math.round(getRangeDimension(fractal.getSignalDimension()) * scale);

		SparseRealMatrix D = getDecimationOperator(rangeDimension, domainDimension);
		List<AffineTransform> transforms = fractal.getTransforms();

		// Kernel matrices, entries will be reset every iteration
		SparseRealMatrix K_scale = new OpenMapRealMatrix(rangeDimension, rangeDimension);
		RealMatrix K_offset = new Array2DRowRealMatrix(rangeDimension, 1);

		// Iterated system
		int numberOfIterations = 12;
		RealMatrix x = new Array2DRowRealMatrix(signalDimension, 1);
		for (int n = 1; n <= numberOfIterations; n++) {
			System.out.printf("Decoding: Iteration %d of %d.\n", n, numberOfIterations);
			RealMatrix x_n = new Array2DRowRealMatrix(signalDimension, 1);
			for (AffineTransform transform : transforms) {
				int domainBlockIndex =  Math.round(transform.getDomainBlockIndex() * scale);
				int rangeBlockIndex = transform.getRangeBlockIndex();

				// Fetch
				SparseRealMatrix F_I = getFetchOperator(domainBlockIndex, domainDimension, signalDimension);
				RealMatrix domainBlock = F_I.multiply(x);

				// Decimate
				RealMatrix decimatedDomainBlock = D.multiply(domainBlock);

				// Build the transform
				for (int i = 0; i < rangeDimension; i++) {
					K_offset.setEntry(i, 0, transform.getOffset());

					for (int j = 0; j < rangeDimension; j++) {
						if (i == j) {
							K_scale.setEntry(i, j, transform.getScale());
						}
					}
				}		

				// Apply the transform
				RealMatrix transformedBlock = (K_scale.multiply(decimatedDomainBlock)).add(K_offset);

				// Put
				SparseRealMatrix P_J = getPutOperator(rangeBlockIndex, rangeDimension, signalDimension);
				x_n = x_n.add(P_J.multiply(transformedBlock));
			}
			x = x_n;
		}

		return new Signal(x);
	}

	public boolean isPowerOfTwo(int x) {
		double a = Math.log(x) / ln_of_2;
		return (a - Math.floor(a)) == 0;
	}

	private void checkSignalDimesion(int signalDimension) {
		if (signalDimension < 1 || signalDimension % 2 != 0) {
			throw new IllegalArgumentException("Signal dimension must be an even positive integer.");
		}
	}

	public int getDomainDimension(int signalDimension) {
		checkSignalDimesion(signalDimension);

		int N = Math.round((float)signalDimension / 4);
		int M = Math.min((int)Math.floor(Math.log(signalDimension) / Math.log(1.1)), N);

		// If M is odd, make it even
		if ((M+1) % 2 == 0) {
			M--;
		}

		// Find the largest positive even integer bounded by M that divides the signal dimension
		for (int k = M; k > 0; k -= 2) {
			if (signalDimension % k == 0) {
				return k;
			}
		}

		return 2;
	}

	public int getRangeDimension(int signalDimension) {
		checkSignalDimesion(signalDimension);
		return Math.round((float)getDomainDimension(signalDimension)/2);
	}

	public SparseRealMatrix getFetchOperator(int domainBlockIndex, int domainDimension, int signalDimension) {
		SparseRealMatrix F_I = new OpenMapRealMatrix(domainDimension, signalDimension);
		for (int j = 0; j < domainDimension; j++) {
			F_I.setEntry(j, domainBlockIndex + j, 1);
		}
		return F_I;
	}

	public SparseRealMatrix getDecimationOperator(int rangeDimension, int domainDimension) {
		final int decimationRatio = Math.round((float)domainDimension / rangeDimension);

		SparseRealMatrix D = new OpenMapRealMatrix(rangeDimension, domainDimension);
		for (int i = 0; i < rangeDimension; i++) {
			for (int j = 0; j < decimationRatio; j++) {
				D.setEntry(i, i*decimationRatio + j, 1.0f/decimationRatio);
			}
		}
		return D;
	}

	public SparseRealMatrix getPutOperator(int rangeBlockIndex, int rangeDimension, int signalDimension) {
		SparseRealMatrix P_J = new OpenMapRealMatrix(signalDimension, rangeDimension);
		for (int j = 0; j < rangeDimension; j++) {
			P_J.setEntry(rangeDimension*rangeBlockIndex + j, j, 1);
		}
		return P_J;
	}

	public AffineTransform getAffineTransform(SignalBlock domainBlock, SignalBlock rangeBlock) {
		boolean allowNegativeValues = true;
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
		
		if (s < 0 && allowNegativeValues == false) {
			s = 0;
		}

		double u = (s*sum_squared_ais) - (2*sum_ais_times_bis) + (2*o*sum_ais);
		double v = (n*o) - (2*sum_bis);
		double R = one_over_n * (sum_squared_bis + s*u + o*v);
		return new AffineTransform(domainBlock.getIndex(), rangeBlock.getIndex(),
				Math.sqrt(Math.abs(R)), s, o);
	}
}
