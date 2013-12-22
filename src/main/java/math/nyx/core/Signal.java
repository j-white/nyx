package math.nyx.core;

import java.io.Serializable;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

import com.google.common.base.Objects;

public class Signal implements Serializable {
	private static final long serialVersionUID = -3505516831802019801L;

	private final RealMatrix x;
	private final int numChannels;

	public Signal(int dimension) {
		this(new Array2DRowRealMatrix(dimension, 1));
	}

	public Signal(RealMatrix x) {
		this.x = x;
		this.numChannels = 1;
	}

	public Signal(Fractal fractal, RealMatrix decodedVector) {
		this.x = decodedVector;
		this.numChannels = fractal.getNumSignalChannels();
	}

	public RealMatrix getVector() {
		return x;
	}

	public int getDimension() {
		return x.getRowDimension();
	}

	public double getEntry(int row) {
		return x.getEntry(row, 0);
	}

	public int getNumChannels() {
		return numChannels;
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	    		.add("dimension", getDimension())
	    		.add("numChannels", getNumChannels())
	            .add("vector", getVector())
	            .toString();
	}
}
