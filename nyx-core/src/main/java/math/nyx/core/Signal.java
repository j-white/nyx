package math.nyx.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ServiceLoader;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.springframework.util.SerializationUtils;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public abstract class Signal implements Serializable {
	private static final long serialVersionUID = -3505516831802019801L;

	private RealMatrix x;
	private final int numChannels;
	private int pad = 0;

	public Signal(int dimension) {
		this(new Array2DRowRealMatrix(dimension, 1));
	}

	public Signal(RealMatrix x) {
		this.x = x;
		this.numChannels = 1;
	}

	public Signal(Fractal fractal, RealMatrix decodedVector) {
		// Extract the unpadded signal from the decoded vector
		int unpaddedSignalDimension = decodedVector.getRowDimension() - fractal.getSignal().getPad();
		this.x = decodedVector.getSubMatrix(0, unpaddedSignalDimension-1, 0, 0);
		this.numChannels = fractal.getSignal().getNumChannels();
	}

	/** Used to identify the type of the signal in a report. */
	public abstract String getType();

	public abstract SignalMetadata getMetadata();

	public abstract double getMinVal();

	public abstract double getMaxVal();
	
	public abstract int getScaledDimension(int scale);

	public abstract void write(File file) throws IOException;

	public void pad(int targetDimension) {
		int targetPad = targetDimension - getDimension();
		if (targetPad < 0) {
			throw new IllegalArgumentException("Target dimension cannot be smaller than the current dimension.");
		} else if(targetPad == 0) {
			return;
		}

		// Copy the existing vector over to the padded vector
		RealMatrix paddedVector = new Array2DRowRealMatrix(targetDimension, 1);
		paddedVector.setSubMatrix(x.getData(), 0, 0);
		
		// Save the results
		pad = targetPad;
		x = paddedVector;
	}

	public RealMatrix getVector() {
		return x;
	}

	public double getEntry(final int row) {
		return x.getEntry(row, 0);
	}

	public RealMatrix getUnpaddedVector() {
		return getVector().getSubMatrix(0, getUnpaddedDimension()-1, 0, 0);
	}

	public int getDimension() {
		return x.getRowDimension();
	}

	public int getPad() {
		return pad;
	}

	public int getUnpaddedDimension() {
		return getDimension() - getPad();
	}

	public int getNumChannels() {
		return numChannels;
	}

	public long getSizeInBytes() {
		return SerializationUtils.serialize(this).length;
	}

	public double getPSNR(Signal signal) {
		int n = getUnpaddedDimension();
		if (n != signal.getUnpaddedDimension()) {
			throw new IllegalArgumentException("Cannot compare two signals of different lengths.");
		}

		// Calculate the value of the rms metric (2-norm)
		double rms = 0;
		for (int i = 0; i < n; i++) {
			rms += Math.pow(getEntry(i) - signal.getEntry(i), 2);
		}
		rms = Math.sqrt(rms);

		if (rms == 0) return 0;

		return 20 * Math.log10(getMaxVal() / rms);
	}

	@Override
    public boolean equals(Object obj) {
		if (obj == null)
            return false;
		if (obj == this)
            return true;
		if (!(obj instanceof Signal))
            return false;
		
		Signal rhs = (Signal) obj;
		if (numChannels != rhs.numChannels) {
			return false;
		} else if (getUnpaddedDimension() != rhs.getUnpaddedDimension()) {
			return false;
		} else {
			return getUnpaddedVector().equals(rhs.getUnpaddedVector());
		}
    }

	public ToStringHelper toStringHelper() {
		return Objects.toStringHelper(this.getClass())
	    		.add("dimension", getDimension())
	    		.add("unpaddedDimension", getUnpaddedDimension())
	    		.add("numChannels", getNumChannels())
	    		.add("metadata", getMetadata());
	}

	@Override
	public final String toString() {
		return toStringHelper().toString();
	}

	private static ServiceLoader<SignalFactory> signalFactoryLoader = ServiceLoader.load(SignalFactory.class);

	public static Signal getSignalFor(File signalFile) throws IOException, InvalidSignalException {
	     for (SignalFactory signalFactory : signalFactoryLoader) {
	         Signal signal = signalFactory.getSignalFor(signalFile);
	         if (signal != null)
	             return signal;
	     }
	     throw new InvalidSignalException("No handlers availabe.");
	}
}
