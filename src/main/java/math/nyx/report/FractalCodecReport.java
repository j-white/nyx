package math.nyx.report;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;
import math.nyx.report.FractalCodecReport.DecodeReport;

import org.apache.commons.math.linear.RealMatrix;

/**
 * Here's what we want to know when encoding/decoding a signal.
 */
@SuppressWarnings("unused")
public class FractalCodecReport extends TexReport {
	public static final String TEMPLATE = "fractalCodecReport.tex.tpl";

	/* Source and signal summary */

	private URL sourceUrl;

	private String signalType;

	private int signalDimension;
	
	private long sizeOfSignalInBytes;

	/* Encoding summary */

	private String codecType;

	private int secondsToEncode;

	private long sizeOfFractalInBytes;
	
	private int rangeDimension;
	
	private int numRangePartitions;
	
	private int domainDimension;
	
	private int numDomainPartitions;

	private int numTransforms;
	
	/* Operators */
	
	private Map<Integer, Integer> rangeToDomainMap;
	
	private List<RealMatrix> rangeFetchOperators;
	
	private Map<Integer, RealMatrix> domainFetchOperators;

	private RealMatrix decimationOperator;
	
	private Map<Integer, Kernel> kernels;
	
	private List<RealMatrix> putOperators;

	private static class Kernel {
		String type;
		
		Map<String, Double> parameters;
	}
	
	/* Decoded summary */

	private List<DecodeReport> decodeReports = new ArrayList<DecodeReport>();

	public static class DecodeReport {
		
		private File destFile;

		private int scale;

		private long secondsToDecode;

		private Signal decodedSignal; 

		public void setDestFile(File destFile) {
			this.destFile = destFile;
		}
	
		public File getDestFile() {
			return destFile;
		}

		public void setScale(int scale) {
			this.scale = scale;
		}

		public int getScale() {
			return scale;
		}

		public void setDecodedSignal(Signal decodedSignal) {
			this.decodedSignal = decodedSignal;
		}

		public Signal getDecodedSignal() {
			return this.decodedSignal;
		}

		public void setSecondsToDecode(long elapsed) {
			this.secondsToDecode = elapsed;
		}

		public long getSecondsToDecode() {
			return secondsToDecode;
		}
	}

	private void wow() {
		/*
		// Fractal details
		FractalCodec codec = fractal.getCodec();
		PartitioningStrategy partitioner = fractal.getPartitioner();

		Map<String, Object> stats = new HashMap<String, Object>();
		stats.put("secondsToEncode", );
		stats.put("secondsToDecode", decodingStopwatch.elapsed(TimeUnit.SECONDS));
		stats.put("PSNR", sourceSignal.getPSNR(decodedSignal));

		stats.put("signalSourceSizeInBytes", sourceFile.length());
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
			
			decode(fractal, scale, signalFile);
		}
		*/
	}

	public List<DecodeReport> getDecodeReports() {
		return decodeReports;
	}

	public void addDecodeReport(DecodeReport decodeReport) {
		decodeReports.add(decodeReport);
	}

	public void setSourceFile(File sourceFile) {
		// TODO Auto-generated method stub
		
	}
	public void setFractal(Fractal fractal) {
		// TODO Auto-generated method stub
		
	}
	public void setSecondsToEncode(long elapsed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTemplate() {
		return TEMPLATE;
	}
}
