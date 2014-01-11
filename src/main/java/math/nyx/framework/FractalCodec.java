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

import math.nyx.core.Fractal;
import math.nyx.core.FractalDecoder;
import math.nyx.core.FractalEncoder;
import math.nyx.core.Signal;
import math.nyx.core.Transform;
import math.nyx.framework.DecimationStrategy;
import math.nyx.framework.Kernel;
import math.nyx.framework.PartitioningStrategy;

public class FractalCodec implements FractalEncoder, FractalDecoder {
	private Kernel kernel;
	private PartitioningStrategy partitioningStrategy;
	private DecimationStrategy decimationStrategy;
	private String name;

	public Fractal encode(Signal signal) {
		// Pad the signal to a size that is supported by the partitioning strategy
		int paddedDimension = partitioningStrategy.getPaddedDimension(signal);
		signal.pad(paddedDimension);
		
		// Now fetch the underlying vector and the partitioner
		RealMatrix x = signal.getVector();
		PartitioningStrategy partitioner = partitioningStrategy.getPartitioner(signal);

		// Break the signal into non-overlapping range blocks
		int numRangePartitions = partitioner.getNumRangePartitions();
		System.out.printf("Partitioning range with %d partitions of dimension %d.\n", 
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
		System.out.printf("Partitioning domain with %d partitions of dimension %d.\n", 
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
		Fractal fractal = new Fractal();
		fractal.setCodecName(name);
		fractal.setSignalDimension(signal.getDimension());
		fractal.setSignalPad(signal.getPad());
		fractal.setNumSignalChannels(signal.getNumChannels());

		// Now match the domain and range partitions while minimizing the distance
		// and store the results in the fractal
		System.out.println("Encoding range blocks...");
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
	    	System.out.printf("Successfully encoded %d/%d (%.2f%%) range blocks\n", ++k, rangeBlocks.size(), ((float)k/rangeBlocks.size()) * 100);
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
		return decode(fractal, scale, 12);
	}

	private double calcPsnr(RealMatrix a, RealMatrix b, int signalMax) {
		double rms = 0.0;
		double A[][] = a.getData();
		double B[][] = b.getData();
		
		int N  = a.getRowDimension();
		int M = a.getColumnDimension();

		for (int i = 0; i < N; i++) {
			for (int j = 0; j < M; j++) {
				rms += Math.sqrt(Math.pow(A[i][j],  2) + Math.pow(B[i][j],  2));
			}
		}

		return 20 * Math.log10(signalMax / rms);
	}

	public Signal decode(Fractal fractal, int scale, int numberOfIterations) {
		PartitioningStrategy partitioner = partitioningStrategy.getPartitioner(fractal, scale);
		int scaledSignalDimension = partitioner.getScaledSignalDimension();

		SparseRealMatrix D = getDecimationOperator(partitioner);
		boolean optimized = true;
		RealMatrix domainBlock = null;
		double domainBlockRef[][] = null;

		// Iterated system
		//double psnr_in_db = 0.0f;
		Array2DRowRealMatrix x = new Array2DRowRealMatrix(scaledSignalDimension, 1);
		double xRef[][] = x.getDataRef();
		
		for (int n = 1; n <= numberOfIterations; n++) {
			System.out.printf("Decoding: Iteration %d of %d.\n", n, numberOfIterations);
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

					int domainIndices[] = partitioner.getDomainIndices(transform.getDomainBlockIndex());
					for (int i = 0; i < domainIndices.length; i++) {
						domainBlockRef[i][0] = xRef[domainIndices[i]][0];
					}
				}

				// Decimate
				RealMatrix decimatedDomainBlock = D.multiply(domainBlock);

				// Apply the transform
				RealMatrix transformedBlock = transform.apply(decimatedDomainBlock);

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
					int rangeIndices[] = partitioner.getRangeIndices(rangeBlockIndex);
					for (int i = 0; i < rangeIndices.length; i++) {
						x_n.addToEntry(rangeIndices[i], 0, transformedBlock.getEntry(i, 0));
					}
				}
			}
			
			//psnr_in_db  = calcPsnr(x, x_n, 255);
			x = x_n;
			xRef = x.getDataRef();
		}
		
		/*int id = 32 * 28;
		SparseRealMatrix F_I = partitioner.getRangeFetchOperator(id);
		System.out.println("Range block " + id + " decoded: " + F_I.multiply(x));
		System.exit(1);*/
		
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
