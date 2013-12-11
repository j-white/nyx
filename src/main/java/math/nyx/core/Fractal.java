package math.nyx.core;

import java.io.Serializable;

public abstract class Fractal implements Serializable {
	private static final long serialVersionUID = -3691227743645881425L;

	private long signalDimension;

	public abstract Signal decode();

	public abstract Signal decode(float scale);

	public long getSignalDimension() {
		return signalDimension;
	}

	public void setSignalDimension(long signalDimension) {
		this.signalDimension = signalDimension;
	}
}
