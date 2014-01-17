package math.nyx.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.springframework.util.SerializationUtils;

import com.google.common.base.Objects;

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
		int unpaddedSignalDimension = decodedVector.getRowDimension() - fractal.getSignalPad();
		this.x = decodedVector.getSubMatrix(0, unpaddedSignalDimension-1, 0, 0);
		this.numChannels = fractal.getNumSignalChannels();
	}

	public abstract SignalMetadata getMetadata();

	public abstract double getMinVal();

	public abstract double getMaxVal();

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

	public double getEntry(int row) {
		return x.getEntry(row, 0);
	}

	public int getNumChannels() {
		return numChannels;
	}

	public long getSizeInBytes() {
		return SerializationUtils.serialize(this).length;
	}

	public double getPSNR(Signal signal) {
		return 0.0;
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

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	    		.add("dimension", getDimension())
	    		.add("unpaddedDimension", getUnpaddedDimension())
	    		.add("numChannels", getNumChannels())
	            .toString();
	}
}
