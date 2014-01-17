package math.nyx.report;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;
import math.nyx.core.Transform;
import math.nyx.report.FractalCodecReport.DecodeReport;

import org.apache.commons.math.linear.RealMatrix;

/**
 * Here's what we want to know when encoding/decoding a signal.
 */
@SuppressWarnings("unused")
public class FractalCodecReport extends TexReport {
	public static final String TEMPLATE = "fractalCodecReport.tex.tpl";

	@Override
	public String getTemplate() {
		return TEMPLATE;
	}

	/* Source and signal summary */

	private File sourceFile;

	private Signal sourceSignal;

	public void setSourceFile(File sourceFile) {
		this.sourceFile = sourceFile;
	}

	public void setSourceSignal(Signal sourceSignal) {
		this.sourceSignal = sourceSignal;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public Signal getSourceSignal() {
		return sourceSignal;
	}

	public String getSignalType() {
		return sourceSignal.getType();
	}

	public int getSignalDimension() {
		return sourceSignal.getDimension();
	}

	public long getSizeOfSignalInBytes() {
		return sourceSignal.getSizeInBytes();
	}

	/* Encoding summary */

	private Fractal fractal;

	private long secondsToEncode;

	public void setFractal(Fractal fractal) {
		this.fractal = fractal;
	}

	public void setSecondsToEncode(long secondsToEncode) {
		this.secondsToEncode = secondsToEncode;
	}

	public Fractal getFractal() {
		return fractal;
	}

	public long getSecondsToEncode() {
		return secondsToEncode;
	}

	public long getSizeOfFractalInBytes() {
		return fractal.getSizeInBytes();
	}

	public String getCodecName() {
		return fractal.getCodecName();
	}

	public int getRangeDimension() {
		return fractal.getPartitioner().getRangeDimension();
	}

	public int getDomainDimension() {
		return fractal.getPartitioner().getDomainDimension();
	}

	public int getNumRangePartitions() {
		return fractal.getPartitioner().getNumRangePartitions();
	}

	public int getNumDomainPartitions() {
		return fractal.getPartitioner().getNumDomainPartitions();
	}

	public int getNumTransforms() {
		return fractal.getTransforms().size();
	}

	/* Operators */

	public RealMatrix getDecimationOperator() {
		return fractal.getCodec().getDecimationOperator(fractal.getPartitioner());
	}

	public RealMatrix getDomainFetchOperator(int domainBlockIndex) {
		return fractal.getPartitioner().getDomainFetchOperator(domainBlockIndex);
	}

	public RealMatrix getRangeFetchOperator(int rangeBlockIndex) {
		return fractal.getPartitioner().getRangeFetchOperator(rangeBlockIndex);
	}

	public RealMatrix getPutOperator(int rangeBlockIndex) {
		return fractal.getPartitioner().getPutOperator(rangeBlockIndex);
	}

	public List<Transform> getTransforms() {
		return fractal.getTransforms();
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

	public void addDecodeReport(DecodeReport decodeReport) {
		decodeReports.add(decodeReport);
	}

	public void setDecodeReports(List<DecodeReport> decodeReports) {
		this.decodeReports = decodeReports;
	}

	public List<DecodeReport> getDecodeReports() {
		return decodeReports;
	}
}
