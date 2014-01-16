package math.nyx;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import math.nyx.core.Fractal;
import math.nyx.core.FractalFactory;
import math.nyx.core.Signal;
import math.nyx.core.SignalFactory;
import math.nyx.core.Transform;
import math.nyx.framework.AffineFractalCodec;
import math.nyx.framework.FractalCodec;
import math.nyx.framework.PartitioningStrategy;
import math.nyx.report.FractalCodecReport;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;

public class Nyx {
	private static Logger logger = LogManager.getLogger("Nyx");

	@Autowired
	public SignalFactory signalFactory;

	@Autowired
	private FractalFactory fractalFactory;

	@SuppressWarnings("unused")
	public FractalCodecReport encodeDecode(File signalSource, Set<Integer> scales, File outputDirectory, Boolean forceEncode) {
		logger.info("Encoding \"{}\" into a signal.", signalSource.getAbsolutePath());
		Signal sourceSignal = signalFactory.getSignalFor(signalSource);

		File fractalFile = new File(outputDirectory, String.format("%s-fractal.nyx", signalSource.getName()));
		

		Stopwatch encodingStopwatch = Stopwatch.createStarted();
		Fractal fractal = encode(sourceSignal, fractalFile, !forceEncode);
		encodingStopwatch.stop();

		// Decode as 1x
		logger.info("Decoding {} at 1x.", fractal);
		Stopwatch decodingStopwatch = Stopwatch.createStarted();
		Signal decodedSignal = decode(fractal, 1, signalSource);
		decodingStopwatch.stop();

		// Fractal details
		FractalCodec codec = fractal.getCodec();
		PartitioningStrategy partitioner = fractal.getPartitioner();

		Map<String, Object> stats = new HashMap<String, Object>();
		stats.put("secondsToEncode", encodingStopwatch.elapsed(TimeUnit.SECONDS));
		stats.put("secondsToDecode", decodingStopwatch.elapsed(TimeUnit.SECONDS));
		stats.put("PSNR", sourceSignal.getPSNR(decodedSignal));

		stats.put("signalSourceSizeInBytes", signalSource.length());
		stats.put("signalSizeInBytes", SerializationUtils.serialize(sourceSignal).length);
		stats.put("fractalSizeInBytes", SerializationUtils.serialize(fractal).length);

		stats.put("signalDimension", decodedSignal.getDimension());
		stats.put("rangeDimension", partitioner.getRangeDimension());
		stats.put("numRangePartitions", partitioner.getNumRangePartitions());
		stats.put("domainDimension", partitioner.getDomainDimension());
		stats.put("sizeOfDomainPool", partitioner.getNumDomainPartitions());
		stats.put("numDomainPartitions",  partitioner.getNumDomainPartitions());
		stats.put("numTransforms", fractal.getTransforms().size());

		if (codec instanceof AffineFractalCodec) {
			AffineFractalCodec affineCodec = (AffineFractalCodec)codec;
			
			RealMatrix L = affineCodec.getLMatrix();
			RealMatrix t = affineCodec.getTMatrix();
		}

		RealMatrix D = codec.getDecimationOperator(partitioner);
		
		for (Transform t : fractal.getTransforms()) {
			int d_i = t.getDomainBlockIndex();
			int r_i = t.getRangeBlockIndex();
			double distance = t.getDistance();
			String kernelParameters = Joiner.on(" ").withKeyValueSeparator(":").join(t.getKernelParameters());

			RealMatrix F_d_i = partitioner.getDomainFetchOperator(d_i);
			RealMatrix P_r_i = partitioner.getPutOperator(r_i);
		}

		// Decode at additional scales
		for (int scale : scales) {
			File signalFile = new File(outputDirectory, String.format("decoded-%dx-%s", scale, signalSource.getName()));
			decode(fractal, scale, signalFile);
		}

		return new FractalCodecReport();
	}

	public Fractal encode(Signal signal, File fractalOnDisk, boolean useExisting) {
		Fractal fractal = null;
		if (useExisting) {
			try {
				fractal = Fractal.load(fractalOnDisk);
				logger.info("Using existing fractal from disk: {} .", fractalOnDisk);
			} catch (IOException pass) { }
		}

		FractalCodec codec = fractalFactory.getCodecFor(signal);
		if (fractal == null) {
			logger.info("Encoding fractal for {} .", signal);
			fractal = codec.encode(signal);

			// Persist
		    try {
		    	fractal.write(fractalOnDisk);
			} catch(IOException ex) {
		    	logger.error("Failed to save fractal to disk.", ex);
		    }
		}

		return fractal;
	}

	public Signal decode(Fractal fractal, int scale, File signalOnDisk) {
		logger.info("Decoding {} at {}x.", fractal, scale);
		Signal decodedSignal = fractal.decode(scale);
		decodedSignal.write(signalOnDisk);
		return decodedSignal;
	}
}
