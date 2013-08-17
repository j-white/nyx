package math.nyx.fie.simple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import math.nyx.core.AffineMap;
import math.nyx.core.Collage;
import math.nyx.core.Matrix;
import math.nyx.core.Partition;
import math.nyx.core.RMSLinearCombination;
import math.nyx.core.RMSMetric;
import math.nyx.fie.FractalImageEncodingStrategy;
import math.nyx.fie.MatrixDecodingVisitor;
import math.nyx.fie.MatrixPartitioner;
import math.nyx.fie.OptimizedPartition;

public class SimpleEncodingStrategy implements FractalImageEncodingStrategy {
	
	private MatrixPartitioner matrixPartitioner = new FixedSizeMatrixPartitioner();

	private int numberOfIterations = DEFAULT_ITERATIONS;
	private double initialValue = DEFAULT_INITIAL_VALUE;
	private double rmsThreshold = DEFAULT_RMS_THRESHOLD;

	public static final int DEFAULT_ITERATIONS = 10;
	public static final double DEFAULT_RMS_THRESHOLD = 0.1;
	public static final double DEFAULT_INITIAL_VALUE = 0.0;

	private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static Log LOG = LogFactory.getLog(SimpleEncodingStrategy.class);
	
	public SimpleEncodingStrategy() {
		// This method is intentionally left blank
	}

	public SimpleEncodingStrategy(double rmsThreshold, int numberOfIterations) {
		setRmsThreshold(rmsThreshold);
		this.numberOfIterations = numberOfIterations;
	}

	public void setRmsThreshold(double rmsThreshold) {
		this.rmsThreshold = rmsThreshold;
	}

	public double getRmsThreshold() {
		return rmsThreshold;
	}

	public void setNumberOfIterations(int numberOfIterations) {
		this.numberOfIterations = numberOfIterations;
	}

	public int getNumberOfIterations() {
		return numberOfIterations;
	}

	public void setMatrixPartitioner(MatrixPartitioner matrixPartitioner) {
		this.matrixPartitioner = matrixPartitioner;
	}

	public MatrixPartitioner getMatrixPartitioner() {
		return matrixPartitioner;
	}

	private static class RangeAndDomainMatcher implements Callable<RMSLinearCombination> {
		private Partition range;
		private List<OptimizedPartition> optimizedDomains;
		private List<Partition> domains;
		private RMSMetric rmsMetric = new RMSMetric();
		private RMSLinearCombination rmsThreshold;

		public RangeAndDomainMatcher(Partition range, List<Partition> domains, List<OptimizedPartition> optimizedDomains, double rmsThreshold) {
			this.range = range;
			this.domains = domains;
			this.optimizedDomains = optimizedDomains;
			this.rmsThreshold = new RMSLinearCombination(rmsThreshold);
		}

		public RMSLinearCombination call() throws Exception {
			RMSLinearCombination bestRmsResult = null;
			float rangeWidth = (float)range.getWidth();
			int numberOfDomains = optimizedDomains.size();

			for (int domainIndex = 0; domainIndex < numberOfDomains; domainIndex++) {
				Partition optimizedDomain = optimizedDomains.get(domainIndex);

				// Determine scale and perform scale
				float domainWidth = (float)optimizedDomain.getWidth();
				float scaleToReduceDomain = rangeWidth/domainWidth;
				Partition scaledDomain = optimizedDomain.scale(scaleToReduceDomain);

				// Check the distance
				RMSLinearCombination rmsResult = rmsMetric.getDistanceFromBestLinearCombination(scaledDomain, range);
				if (rmsResult.compareTo(bestRmsResult) < 0) {
					rmsResult.setFrom(domains.get(domainIndex));
					bestRmsResult = rmsResult;
					if (bestRmsResult.compareTo(rmsThreshold) <= 0) {
						return bestRmsResult;
					}
				}
			}

			if (bestRmsResult == null) {
				throw new IllegalStateException("No partitions with finite metric.");
			}

			return bestRmsResult;
		}
	}

	public Collage encode(Matrix m) {
		if (m.getColumnDimension() < 4 || m.getRowDimension() < 4) {
			throw new IllegalArgumentException("Invalid size.");
		}

		if (!m.isSquare()) {
			throw new IllegalArgumentException("Cannot encode non-square images.");
		}

		// For the range, break the image into non-overlapping partitions
		List<Partition> ranges = matrixPartitioner.partitionRange(m);
		int numberOfPartitionsInRange = ranges.size();
		
		// Assuming that all of the range partitions are the same size
		Partition firstPartitionInRange = ranges.iterator().next();
		LOG.info(String.format("Split image range into %d partitions with %d points each",
				numberOfPartitionsInRange, firstPartitionInRange.getNumPoints()));

		// For the domain, break the image into overlapping partitions
		List<Partition> domains = matrixPartitioner.partitionDomain(m);
		int numberOfPartitionsInDomain = domains.size();

		// Assuming that all of the domain partitions are the same size
		Partition firstPartitionInDomain = domains.iterator().next();
		LOG.info(String.format("Split image domain into %d partitions with %d points each",
				numberOfPartitionsInDomain, firstPartitionInDomain.getNumPoints()));

		// Create a new collage, used to store the collection of maps
		Collage collage = new Collage();

		LOG.info("Calculating re-scaled domains and sums.");
		// TODO: Use thread pool to compute these
		List<OptimizedPartition> optimizedDomains = new ArrayList<OptimizedPartition>();
		for (Partition domain : domains) {
			optimizedDomains.add(new OptimizedPartition(domain));
		}

		Set<Callable<RMSLinearCombination>> callables = new HashSet<Callable<RMSLinearCombination>>();
		for (int rangeIndex = 0; rangeIndex < numberOfPartitionsInRange; rangeIndex++) {
			Partition range = ranges.get(rangeIndex);
			RangeAndDomainMatcher rangeAndDomainMatcher = new RangeAndDomainMatcher(range,
					domains, optimizedDomains, rmsThreshold);
			callables.add(rangeAndDomainMatcher);
		}

		LOG.info("Comparing ranges to domains.");
		List<Future<RMSLinearCombination>> futures;
		try {
			futures = threadPool.invokeAll(callables);
			for (Future<RMSLinearCombination> future : futures) {
				collage.addMap(new AffineMap(future.get()));
			}
		} catch (InterruptedException e) {
			Thread.interrupted();
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		LOG.info(String.format("Succesfully encoded the image with %d maps", collage.getMaps().size()));
		return collage;
	}

	private void verifyMatrixSize(int width, int height) {
		if (width < 4 || height < 4) {
			throw new IllegalArgumentException("Invalid size.");
		}
	}

	private Matrix createInitialMatrix(int width, int height) {
		Matrix m = new Matrix(width, height);
		m.fill(initialValue);
		return m;
	}

	public Matrix decode(Collage w, int width, int height, int iterations) {
		return decode(w, width, height, iterations, new MatrixDecodingVisitor() {
			public void decodedIteration(Matrix matrix, int currentIteration,
					int totalIterations) {
				// Do nothing
			}
		});
	}

	public Matrix decode(Collage w, int width, int height, int iterations, MatrixDecodingVisitor visitor) {
		verifyMatrixSize(width, height);
		Matrix m = createInitialMatrix(width, height);
		visitor.decodedIteration(m, 0, iterations);

		LOG.info(String.format("Decoding the image from %d maps", w.getMaps().size()));
		for (int i = 1; i <= iterations; i++) {
			LOG.debug(String.format("\tPass %d of %d", i, iterations));
			m = w.apply(m);
			visitor.decodedIteration(m, i, iterations);
		}
		LOG.info("Done decoding.");

		return m;
	}
}

