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
import math.nyx.framework.Kernel;
import math.nyx.framework.LinearPartitioningStrategy;
import math.nyx.framework.PartitioningStrategy;
import math.nyx.framework.Transform;

public class AffineFractalCodec implements FractalEncoder {
	PartitioningStrategy partitioningStrategy = new LinearPartitioningStrategy();
	DecimationStrategy decimationStrategy = new AveragingDecimationStrategy();
	Kernel kernel = new AffineKernel();

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
			Transform bestTransform = null;
			for (SignalBlock domainBlock : decimatedDomainBlocks) {
				Transform transform = kernel.encode(domainBlock, rangeBlock);
				if (transform.compareTo(bestTransform) < 0) {
					bestTransform = transform;
				}
			}

			Assert.isTrue(bestTransform != null, "No domain blocks found.");
			fractal.addTransform(bestTransform);
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
		List<Transform> transforms = fractal.getTransforms();

		// Iterated system
		int numberOfIterations = 12;
		RealMatrix x = new Array2DRowRealMatrix(signalDimension, 1);
		for (int n = 1; n <= numberOfIterations; n++) {
			System.out.printf("Decoding: Iteration %d of %d.\n", n, numberOfIterations);
			RealMatrix x_n = new Array2DRowRealMatrix(signalDimension, 1);
			for (Transform transform : transforms) {
				int domainBlockIndex =  Math.round(transform.getDomainBlockIndex() * scale);
				int rangeBlockIndex = transform.getRangeBlockIndex();

				// Fetch
				SparseRealMatrix F_I = partitioningStrategy.getFetchOperator(domainBlockIndex,
																			 domainDimension, signalDimension);
				RealMatrix domainBlock = F_I.multiply(x);

				// Decimate
				RealMatrix decimatedDomainBlock = D.multiply(domainBlock);

				// Apply the transform
				RealMatrix transformedBlock = transform.apply(decimatedDomainBlock);

				// Put
				SparseRealMatrix P_J = partitioningStrategy.getPutOperator(rangeBlockIndex,
																		   rangeDimension, signalDimension);
				x_n = x_n.add(P_J.multiply(transformedBlock));
			}
			x = x_n;
		}

		return new Signal(x);
	}

	public PartitioningStrategy getPartitioningStrategy() {
		return partitioningStrategy;
	}

	public DecimationStrategy getDecimationStrategy() {
		return decimationStrategy;
	}
}
