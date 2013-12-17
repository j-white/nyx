package math.nyx.core;

import java.io.Serializable;

public abstract class Fractal implements Serializable {
	private static final long serialVersionUID = -3691227743645881425L;

	private int signalDimension;

	public abstract Signal decode();

	public abstract Signal decode(float scale);

	public int getSignalDimension() {
		return signalDimension;
	}

	public void setSignalDimension(int signalDimension) {
		this.signalDimension = signalDimension;
	}
}
