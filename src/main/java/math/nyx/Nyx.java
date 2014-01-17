package math.nyx;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import math.nyx.core.Fractal;
import math.nyx.core.FractalFactory;
import math.nyx.core.Signal;
import math.nyx.core.SignalFactory;
import math.nyx.framework.FractalCodec;
import math.nyx.report.FractalCodecReport;
import math.nyx.report.FractalCodecReport.DecodeReport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Stopwatch;

/**
 * Contains methods used to encode and decode audio and image files
 * using a fractal based approach and generate LaTeX reports for analysis.
 *
 * @author jwhite
 */
public class Nyx {
	private static Logger logger = LogManager.getLogger("Nyx");

	@Autowired
	public SignalFactory signalFactory;

	@Autowired
	private FractalFactory fractalFactory;

	public FractalCodecReport encodeDecode(File sourceFile, Set<Integer> scales, File outputDirectory, Boolean forceEncode) throws IOException {
		logger.info("Encoding {} into a signal.", sourceFile.getAbsolutePath());
		Signal sourceSignal = signalFactory.getSignalFor(sourceFile);
		logger.info("Succesfully encoded file to {}.", sourceSignal);

		File fractalFile = new File(outputDirectory, String.format("%s-fractal.nyx", sourceFile.getName()));

		// Encode the fractal
		Stopwatch encodingStopwatch = Stopwatch.createStarted();
		Fractal fractal = encode(sourceSignal, fractalFile, !forceEncode);
		encodingStopwatch.stop(); 

		// Save everything we have so far in the report
		FractalCodecReport report = new FractalCodecReport();
		report.setSourceFile(sourceFile);
		report.setSourceSignal(sourceSignal);
		report.setFractal(fractal);
		report.setSecondsToEncode(encodingStopwatch.elapsed(TimeUnit.SECONDS));

		// Decode at the various scales
		for (int scale : scales) {
			File destFile = new File(outputDirectory, String.format("decoded-%dx-%s", scale, sourceFile.getName()));
			logger.info("Decoding fractal at {}x.", scale);

			Stopwatch decodingStopwatch = Stopwatch.createStarted();
			Signal decodedSignal = decode(fractal, scale, destFile);
			decodingStopwatch.stop();

			// Save the decode details in a decode report
			DecodeReport decodeReport = new DecodeReport();
			decodeReport.setDestFile(destFile);
			decodeReport.setDecodedSignal(decodedSignal);
			decodeReport.setScale(scale);
			decodeReport.setSecondsToDecode(decodingStopwatch.elapsed(TimeUnit.SECONDS));

			// Append the decode report the "main" report
			report.addDecodeReport(decodeReport);
		}

		return report;
	}

	public Fractal encode(Signal signal) {
		FractalCodec codec = fractalFactory.getCodecFor(signal);
		return codec.encode(signal);
	}

	public Fractal encode(Signal signal, File fractalOnDisk) throws IOException {
		return encode(signal, fractalOnDisk, false);
	}

	public Fractal encode(Signal signal, File fractalOnDisk, boolean useExisting) throws IOException {
		Fractal fractal = null;
		if (useExisting) {
			try {
				fractal = Fractal.load(fractalOnDisk);
				logger.info("Using existing fractal from disk: {}.", fractalOnDisk);
			} catch (IOException pass) { }
		}

		FractalCodec codec = fractalFactory.getCodecFor(signal);
		if (fractal == null) {
			logger.info("Encoding fractal for {}.", signal);
			fractal = codec.encode(signal);

			// Persist
		    fractal.write(fractalOnDisk);
		}

		return fractal;
	}

	public Signal decode(Fractal fractal, int scale, File signalOnDisk) throws IOException {
		Signal decodedSignal = fractal.decode(scale);
		logger.info("Writing decoded signal at {}x to {}", scale, signalOnDisk);
		decodedSignal.write(signalOnDisk);
		return decodedSignal;
	}
}
