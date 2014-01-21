package math.nyx.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math.linear.Array2DColumnRealMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import com.google.common.base.Objects;

import math.nyx.core.Fractal;
import math.nyx.core.FractalDecoder;
import math.nyx.core.FractalEncoder;
import math.nyx.core.Signal;
import math.nyx.core.Transform;
import math.nyx.framework.Kernel;
import math.nyx.framework.PartitioningStrategy;

public class FractalCodec implements FractalEncoder, FractalDecoder {
	private static Logger logger = LogManager.getLogger("Nyx");

	private Kernel kernel;

	private PartitioningStrategyFactory partitioningStrategyFactory;

	private DecimationStrategyFactory decimationStrategyFactory;

	private String name;

	public static final int DECODE_ITERATIONS = 12;

	private int decodeIterations = DECODE_ITERATIONS;

	public static final boolean OPTIMIZED = true;

	private boolean optimized = OPTIMIZED;

	public Fractal encode(Signal signal) {
		// Fetch the partitioner, this will pad the signal if necessary
		PartitioningStrategy partitioner = partitioningStrategyFactory.getPartitioner(signal);

		// Now fetch the underlying vector
		RealMatrix x = signal.getVector();

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

		// If we have any range blocks, we should have at least 1 domain block
		if (!rangeBlocks.isEmpty()) {
			Assert.notEmpty(decimatedDomainBlocks);
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
	    	logger.info("Successfully encoded {}/{} ({}) range blocks.", ++k, rangeBlocks.size(), percentageComplete);
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
				if (transform.compareTo(bestTransform) < 0)
					bestTransform = transform;

				// If the distance is <= the kernel's threshold, don't try to find a "better" transform
				if (transform.getDistance() <= kernel.getThreshold())
					break;
			}

			if (bestTransform != null)
				return bestTransform;

			throw new Exception("No transform found.");
		}
	}

	public Signal decode(Fractal fractal, int scale) {
		return decode(fractal, scale, decodeIterations);
	}

	// Courtesy of http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public Signal decode(Fractal fractal, int scale, int numberOfIterations) {
		if (optimized) {
			return decodeOptimized(fractal, scale, numberOfIterations);
		} else {
			return decodeSimple(fractal, scale, numberOfIterations);
		}
	}

	public Signal decodeSimple(Fractal fractal, int scale, int numberOfIterations) {
		PartitioningStrategy partitioner = partitioningStrategyFactory.getPartitioner(fractal.getSignal(), scale);
		int scaledSignalDimension = partitioner.getScaledSignalDimension();
		RealMatrix x = new Array2DRowRealMatrix(scaledSignalDimension, 1);

		int numTransforms = fractal.getTransforms().size();
		Signal signal = fractal.getSignal();
		SparseRealMatrix D = getDecimationOperator(partitioner);
		
		for (int n = 1; n <= numberOfIterations; n++) {
			logger.info("Decoding: Iteration {} of {}", n, numberOfIterations);
			RealMatrix x_n = new Array2DRowRealMatrix(scaledSignalDimension, 1);

			int k = 1;
			for (Transform transform : fractal.getTransforms()) {
				if (logger.isDebugEnabled()) {
					String percentageComplete = String.format("%.2f%%", ((float)k/numTransforms) * 100);
					logger.debug("Applying transform {}/{} ({}): {}", ++k, numTransforms,
							percentageComplete, transform);
				}

				// Fetch
				SparseRealMatrix F_I = partitioner.getDomainFetchOperator(transform.getDomainBlockIndex());
				RealMatrix domainBlock = F_I.multiply(x);

				// Decimate
				RealMatrix decimatedDomainBlock = D.multiply(domainBlock);

				// Apply the transform
				transform.apply(decimatedDomainBlock, signal);
				
				// Put
				int rangeBlockIndex = transform.getRangeBlockIndex();
				SparseRealMatrix P_J = partitioner.getPutOperator(rangeBlockIndex);
				x_n = x_n.add(P_J.multiply(decimatedDomainBlock));
			}

			x = x_n;
		}

		return fractal.getSignalFromDecodedVector(x, scale);
	}

	public Signal decodeOptimized(Fractal fractal, int scale, int numberOfIterations) {
		PartitioningStrategy partitioner = partitioningStrategyFactory.getPartitioner(fractal.getSignal(), scale);
		int scaledSignalDimension = partitioner.getScaledSignalDimension();

		logger.info("Allocating memory for two column vectors of dimension {} (total of {}) and associated operators.",
						scaledSignalDimension, humanReadableByteCount(2*(long)scaledSignalDimension*8, true));

		Array2DColumnRealMatrix domain = new Array2DColumnRealMatrix(partitioner.getDomainDimension(), 1);
		double domainRef[][] = domain.getDataRef();

		Array2DColumnRealMatrix x = new Array2DColumnRealMatrix(scaledSignalDimension, 1);
		double xRef[][] = x.getDataRef();

		Array2DColumnRealMatrix x_n = new Array2DColumnRealMatrix(scaledSignalDimension, 1);
		double xnRef[][] = x_n.getDataRef();

		int domainIndices[] = new int[partitioner.getDomainDimension()];
		int rangeIndices[] = new int[partitioner.getRangeDimension()];

		DecimationStrategy decimator = getDecimator(partitioner);
		final int decimationRatio = decimator.getDecimationRatio();
		int decimationIndices[][] = decimator.getIndices();

		Array2DColumnRealMatrix decimatedDomain = new Array2DColumnRealMatrix(partitioner.getRangeDimension(), 1);
		double decimatedDomainRef[][] = decimatedDomain.getDataRef();

		int numTransforms = fractal.getTransforms().size();
		Signal signal = fractal.getSignal();

		logger.info("Done allocating memory.");

		for (int n = 1; n <= numberOfIterations; n++) {
			logger.info("Decoding: Iteration {} of {}", n, numberOfIterations);

			logger.trace("Resetting vector to 0.");
			for (int i = 0; i < scaledSignalDimension; i++) {
				xnRef[0][i] = 0;
			}

			int k = 1;
			for (Transform transform : fractal.getTransforms()) {
				if (logger.isDebugEnabled()) {
					String percentageComplete = String.format("%.2f%%", ((float)k/numTransforms) * 100);
					logger.debug("Applying transform {}/{} ({}): {}", ++k, numTransforms,
							percentageComplete, transform);
				}

				logger.trace("Fetching");
				partitioner.getDomainIndices(transform.getDomainBlockIndex(), domainIndices);
				for (int i = 0; i < domainIndices.length; i++) {
					domainRef[0][i] = xRef[0][domainIndices[i]];
				}

				logger.trace("Decimating");
				for (int i = 0; i < decimationIndices.length; i++) {
					decimatedDomainRef[0][i] = 0;
					for (int j = 0; j < decimationIndices[i].length; j++) {
						decimatedDomainRef[0][i] += domainRef[0][decimationIndices[i][j]];
					}
					decimatedDomainRef[0][i] *= 1.0f/decimationRatio;
				}

				logger.trace("Transforming");
				transform.apply(decimatedDomain, signal);

				logger.trace("Putting");
				int rangeBlockIndex = transform.getRangeBlockIndex();
				partitioner.getRangeIndices(rangeBlockIndex, rangeIndices);
				for (int i = 0; i < rangeIndices.length; i++) {
					x_n.addToEntry(rangeIndices[i], 0, decimatedDomainRef[0][i]);
				}
			}

			// Swap x and x_n
			Array2DColumnRealMatrix tmp = x;
			x = x_n;
			x_n = tmp;

			xRef = x.getDataRef();
			xnRef = x_n.getDataRef();
		}

		return fractal.getSignalFromDecodedVector(x, scale);
	}

	public DecimationStrategy getDecimator(PartitioningStrategy partitioner) {
		return decimationStrategyFactory.getDecimator(partitioner.getRangeDimension(),
				partitioner.getDomainDimension());
	}

	public SparseRealMatrix getDecimationOperator(PartitioningStrategy partitioner) {
		return getDecimator(partitioner).getDecimationOperator();
	}

	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}

	public Kernel getKernel() {
		return kernel;
	}

	public void setPartitioningStrategyFactory(PartitioningStrategyFactory partitioningStrategyFactory) {
		this.partitioningStrategyFactory = partitioningStrategyFactory;
	}

	public PartitioningStrategyFactory getPartitioningStrategyFactory() {
		return partitioningStrategyFactory;
	}

	public void setDecimationStrategyFactory(DecimationStrategyFactory decimationStrategyFactory) {
		this.decimationStrategyFactory = decimationStrategyFactory;
	}

	public DecimationStrategyFactory getDecimationStrategyFactory() {
		return decimationStrategyFactory;
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

	public void setOptimized(boolean optimized) {
		this.optimized = optimized;
	}

	public boolean getOptimized() {
		return optimized;
	}

	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	    		.add("name", getName())
	            .toString();
	}
}
