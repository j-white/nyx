package math.nyx.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Objects;

import math.nyx.core.Fractal;
import math.nyx.core.FractalDecoder;
import math.nyx.core.FractalEncoder;
import math.nyx.core.Signal;
import math.nyx.core.Transform;
import math.nyx.framework.DecimationStrategy;
import math.nyx.framework.Kernel;
import math.nyx.framework.PartitioningStrategy;

public class FractalCodec implements FractalEncoder, FractalDecoder {
	private static Logger logger = LogManager.getLogger("Nyx");

	private Kernel kernel;
	private PartitioningStrategy partitioningStrategy;
	private DecimationStrategy decimationStrategy;
	private String name;
	
	public static final int DECODE_ITERATIONS = 12;

	private int decodeIterations = DECODE_ITERATIONS;

	public Fractal encode(Signal signal) {
		// Pad the signal to a size that is supported by the partitioning strategy
		int paddedDimension = partitioningStrategy.getPaddedDimension(signal);
		signal.pad(paddedDimension);

		// Now fetch the underlying vector and the partitioner
		RealMatrix x = signal.getVector();
		PartitioningStrategy partitioner = partitioningStrategy.getPartitioner(signal);

		// Break the signal into non-overlapping range blocks
		int numRangePartitions = partitioner.getNumRangePartitions();
		logger.info("Partitioning range with {} partitions of dimension {}.", 
				numRangePartitions, partitioner.getRangeDimension());
		List<SignalBlock> rangeBlocks = new ArrayList<SignalBlock>();
		for (int i = 0; i < numRangePartitions; i++) {
			// Fetch the range block at index i
			SparseRealMatrix F_I = partitioner.getRangeFetchOperator(i);
			RealMatrix rangeBlock = F_I.multiply(x);	
			//System.out.println("Range block: " + rangeBlock);

			// Store the results
			rangeBlocks.add(new SignalBlock(i, rangeBlock));
		}

		// Build the decimation operator D
		SparseRealMatrix D = getDecimationOperator(partitioner);

		// Break the signal into overlapping domain blocks
		int numDomainPartitions = partitioner.getNumDomainPartitions();
		logger.info("Partitioning domain with {} partitions of dimension {}.", 
				numDomainPartitions, partitioner.getDomainDimension());
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
		Fractal fractal = new Fractal(signal);
		fractal.setCodecName(name);

		// Now match the domain and range partitions while minimizing the distance
		// and store the results in the fractal
		logger.info("Encoding range blocks...");
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	    List<Future<Transform>> futures = new ArrayList<Future<Transform>>();
	    for (SignalBlock rangeBlock : rangeBlocks) {
	    	Callable<Transform> worker = new Worker(rangeBlock, kernel, decimatedDomainBlocks);
	    	Future<Transform> future = executor.submit(worker);
	    	futures.add(future);
	    }
	    
	    int k = 0;
	    for (Future<Transform> future : futures) {
	    	try {
				fractal.addTransform(future.get());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
	    	String percentageComplete = String.format("%.2f%%", ((float)k/rangeBlocks.size()) * 100);
	    	logger.debug("Successfully encoded {}/{} ({}) range blocks.", ++k, rangeBlocks.size(), percentageComplete);
	    }

	    executor.shutdown();
	    
		return fractal;
	}

	private static class Worker implements Callable<Transform> {
		private final SignalBlock rangeBlock;
		private final Kernel kernel;
		private final List<SignalBlock> decimatedDomainBlocks;

		public Worker(SignalBlock rangeBlock, Kernel kernel, List<SignalBlock> decimatedDomainBlocks) {
			this.rangeBlock = rangeBlock;
			this.kernel = kernel;
			this.decimatedDomainBlocks = decimatedDomainBlocks;
		}

		@Override
		public Transform call() throws Exception {
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
			
			/*if (rangeBlock.getIndex() == 864) {
				System.out.printf("Range at %d encoded with: %s\nBlock: %s", rangeBlock.getIndex(), bestTransform, rangeBlock);
			}*/
			return bestTransform;
		}
	}
	
	public Signal decode(Fractal fractal, int scale) {
		return decode(fractal, scale, decodeIterations);
	}

	public Signal decode(Fractal fractal, int scale, int numberOfIterations) {
		PartitioningStrategy partitioner = partitioningStrategy.getPartitioner(fractal, scale);
		int scaledSignalDimension = partitioner.getScaledSignalDimension();

		SparseRealMatrix D = getDecimationOperator(partitioner);
		boolean optimized = true;
		RealMatrix domainBlock = null;

		// Iterated system
		Array2DRowRealMatrix x = new Array2DRowRealMatrix(scaledSignalDimension, 1);
		
		// For optimized routine
		double domainBlockRef[][] = null;
		double xRef[][] = x.getDataRef();
		int domainIndices[] = new int[partitioner.getDomainDimension()];
		int rangeIndices[] = new int[partitioner.getRangeDimension()];

		for (int n = 1; n <= numberOfIterations; n++) {
			logger.info("Decoding: Iteration {} of {}", n, numberOfIterations);
			Array2DRowRealMatrix x_n = new Array2DRowRealMatrix(scaledSignalDimension, 1);
			for (Transform transform : fractal.getTransforms()) {
				int rangeBlockIndex = transform.getRangeBlockIndex();

				// Fetch
				if (!optimized) {
					SparseRealMatrix F_I = partitioner.getDomainFetchOperator(transform.getDomainBlockIndex());
					domainBlock = F_I.multiply(x);
				} else {
					if (domainBlock == null) {
						Array2DRowRealMatrix block = new Array2DRowRealMatrix(partitioner.getDomainDimension(), 1);
						domainBlock = block;
						domainBlockRef = block.getDataRef();
					}

					partitioner.getDomainIndices(transform.getDomainBlockIndex(), domainIndices);
					for (int i = 0; i < domainIndices.length; i++) {
						domainBlockRef[i][0] = xRef[domainIndices[i]][0];
					}
				}

				// Decimate
				RealMatrix decimatedDomainBlock = D.multiply(domainBlock);

				// Apply the transform
				RealMatrix transformedBlock = transform.apply(decimatedDomainBlock, fractal.getSignalMinVal(), fractal.getSignalMaxVal());

				/*
				if (n == numberOfIterations) {
					System.out.printf("Range block index %d: %s\n", rangeBlockIndex, transformedBlock);
				}
				*/
				
				// Put
				if (!optimized) {
					SparseRealMatrix P_J = partitioner.getPutOperator(rangeBlockIndex);
					x_n = (Array2DRowRealMatrix) x_n.add(P_J.multiply(transformedBlock));
				} else {
					partitioner.getRangeIndices(rangeBlockIndex, rangeIndices);
					for (int i = 0; i < rangeIndices.length; i++) {
						x_n.addToEntry(rangeIndices[i], 0, transformedBlock.getEntry(i, 0));
					}
				}
			}

			x = x_n;
			xRef = x.getDataRef();
		}

		return fractal.getSignalFromDecodedVector(x, scale);
	}

	public SparseRealMatrix getDecimationOperator(PartitioningStrategy partitioner) {
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

	public void setDecodeIterations(int decodeIterations) {
		this.decodeIterations = decodeIterations;
	}

	public int getDecodeIterations() {
		return decodeIterations;
	}

	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	    		.add("name", getName())
	            .toString();
	}
}
