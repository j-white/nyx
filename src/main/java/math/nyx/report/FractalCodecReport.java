package math.nyx.report;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.linear.RealMatrix;

/**
 * Here's what we want to know when encoding/decoding a signal.
 */
@SuppressWarnings("unused")
public class FractalCodecReport extends TexReport {
	
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

	private Map<Integer, DecodeReport> decodeReports;

	public static class DecodeReport {
		
		private File destFile;
		
		private double psnrOfDecodedFile;

		private int scale;

		private int secondsToDecode;

		/** Size of the decoded signal at the given scale */
		private long sizeOfSignalInBytes;
		
		public File getDestFile() {
			return destFile;
		}

		public int getScale() {
			return scale;
		}

		public long getSignalSizeInBytes() {
			return sizeOfSignalInBytes;
		}
	}

	public List<DecodeReport> getDecodeReports() {
		return new ArrayList<DecodeReport>();
	}
}
