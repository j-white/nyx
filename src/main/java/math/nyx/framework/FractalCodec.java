package math.nyx.framework;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.springframework.util.Assert;

import math.nyx.core.Fractal;
import math.nyx.core.SignalBlock;
import math.nyx.core.FractalEncoder;
import math.nyx.core.Signal;
import math.nyx.framework.DecimationStrategy;
import math.nyx.framework.Kernel;
import math.nyx.framework.PartitioningStrategy;
import math.nyx.framework.Transform;

public class FractalCodec implements FractalEncoder {
	private Kernel kernel;
	private PartitioningStrategy partitioningStrategy;
	private DecimationStrategy decimationStrategy;
	private String name;

	public Fractal encode(Signal signal) {
		RealMatrix x = signal.getVector();
		PartitioningStrategy partitioner = partitioningStrategy.getPartitioner(signal);

		// Break the signal into non-overlapping range blocks
		int numRangePartitions = partitioner.getNumRangePartitions();
		System.out.printf("Partition range with %d partitions of dimension %d.\n", 
				numRangePartitions, partitioner.getRangeDimension());
		List<SignalBlock> rangeBlocks = new ArrayList<SignalBlock>();
		for (int i = 0; i < numRangePartitions; i++) {
			// Fetch the range block at index i
			SparseRealMatrix F_I = partitioner.getRangeFetchOperator(i);
			RealMatrix rangeBlock = F_I.multiply(x);			

			// Store the results
			rangeBlocks.add(new SignalBlock(i, rangeBlock));
		}

		// Build the decimation operator D
		SparseRealMatrix D = getDecimationOperator(partitioner);

		// Break the signal into overlapping domain blocks
		int numDomainPartitions = partitioner.getNumDomainPartitions();
		List<SignalBlock> decimatedDomainBlocks = new ArrayList<SignalBlock>();
		for (int i = 0; i < numDomainPartitions; i++) {
			// Fetch the domain block at index i
			SparseRealMatrix F_I = partitioner.getDomainFetchOperator(i);
			RealMatrix domainBlock = F_I.multiply(x);		

			// Decimate it
			RealMatrix decimatedDomainBlock = D.multiply(domainBlock);

			// Store the results
			decimatedDomainBlocks.add(new SignalBlock(i, decimatedDomainBlock));
		}

		// Construct the fractal used to store our results
		Fractal fractal = new Fractal();
		fractal.setCodecName(name);
		fractal.setSignalDimension(signal.getDimension());
		fractal.setNumSignalChannels(signal.getNumChannels());

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

				// If the distance is identically zero, don't try and find a "better" transform
				if (transform.getDistance() == 0.0) {
					break;
				}
			}

			Assert.isTrue(bestTransform != null, "No domain blocks found.");
			fractal.addTransform(bestTransform);
		}

		return fractal;
	}

	public Signal decode(Fractal fractal, int scale) {
		PartitioningStrategy partitioner = partitioningStrategy.getPartitioner(fractal, scale);
		int scaledSignalDimension = partitioner.getScaledSignalDimension();

		SparseRealMatrix D = getDecimationOperator(partitioner);

		// Iterated system
		int numberOfIterations = 12;
		RealMatrix x = new Array2DRowRealMatrix(scaledSignalDimension, 1);
		for (int n = 1; n <= numberOfIterations; n++) {
			System.out.printf("Decoding: Iteration %d of %d.\n", n, numberOfIterations);
			RealMatrix x_n = new Array2DRowRealMatrix(scaledSignalDimension, 1);
			for (Transform transform : fractal.getTransforms()) {
				int rangeBlockIndex = transform.getRangeBlockIndex();

				// Fetch
				SparseRealMatrix F_I = partitioner.getDomainFetchOperator(transform.getDomainBlockIndex());
				RealMatrix domainBlock = F_I.multiply(x);

				// Decimate
				RealMatrix decimatedDomainBlock = D.multiply(domainBlock);

				// Apply the transform
				RealMatrix transformedBlock = transform.apply(decimatedDomainBlock);
				if (n == numberOfIterations) {
					System.out.printf("Range block index %d: %s\n", rangeBlockIndex, transformedBlock);
				}
				
				// Put
				SparseRealMatrix P_J = partitioner.getPutOperator(rangeBlockIndex);
				x_n = x_n.add(P_J.multiply(transformedBlock));
			}
			x = x_n;
		}

		return new Signal(fractal, x);
	}

	private SparseRealMatrix getDecimationOperator(PartitioningStrategy partitioner) {
		return decimationStrategy.getDecimationOperator(partitioner.getRangeDimension(),
				partitioner.getDomainDimension());
	}

	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}

	public Kernel getKernel() {
		return kernel;
	}
	
	public void setPartitioningStrategy(PartitioningStrategy partitioningStrategy) {
		this.partitioningStrategy = partitioningStrategy;
	}

	public PartitioningStrategy getPartitioningStrategy() {
		return partitioningStrategy;
	}

	public void setDecimationStrategy(DecimationStrategy decimationStrategy) {
		this.decimationStrategy = decimationStrategy;
	}

	public DecimationStrategy getDecimationStrategy() {
		return decimationStrategy;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
