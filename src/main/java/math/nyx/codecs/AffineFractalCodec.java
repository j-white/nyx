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
import math.nyx.framework.AveragingDecimationStrategy;
import math.nyx.framework.DecimationStrategy;
import math.nyx.framework.LinearPartitioningStrategy;
import math.nyx.framework.PartitioningStrategy;

public class AffineFractalCodec implements FractalEncoder {
	PartitioningStrategy partitioningStrategy = new LinearPartitioningStrategy();
	DecimationStrategy decimationStrategy = new AveragingDecimationStrategy();

	public AffineFractal encode(Signal signal) {
		RealMatrix x = signal.getVector();
		int signalDimension = signal.getDimension();
		int domainDimension = partitioningStrategy.getDomainDimension(signalDimension);
		int rangeDimension = partitioningStrategy.getRangeDimension(signalDimension);

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
		SparseRealMatrix D = decimationStrategy.getDecimationOperator(rangeDimension, domainDimension);

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
		int domainDimension = Math.round(partitioningStrategy.getDomainDimension(fractal.getSignalDimension())
											* scale);
		int rangeDimension = Math.round(partitioningStrategy.getRangeDimension(fractal.getSignalDimension())
											* scale);
		SparseRealMatrix D = decimationStrategy.getDecimationOperator(rangeDimension, domainDimension);
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
				SparseRealMatrix F_I = partitioningStrategy.getFetchOperator(domainBlockIndex,
																			 domainDimension, signalDimension);
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
				SparseRealMatrix P_J = partitioningStrategy.getPutOperator(rangeBlockIndex,
																		   rangeDimension, signalDimension);
				x_n = x_n.add(P_J.multiply(transformedBlock));
			}
			x = x_n;
		}

		return new Signal(x);
	}

	public AffineTransform getAffineTransform(SignalBlock domainBlock, SignalBlock rangeBlock) {
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

	public PartitioningStrategy getPartitioningStrategy() {
		return partitioningStrategy;
	}

	public DecimationStrategy getDecimationStrategy() {
		return decimationStrategy;
	}
}
