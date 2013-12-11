package math.nyx.core;

import java.io.Serializable;

public class Signal implements Serializable {
	private static final long serialVersionUID = -3505516831802019801L;

	private final Vector v;

	public Signal(long dimension) {
		this(new Vector(dimension));
	}

	public Signal(Vector v) {
		this.v = v;
	}

	public Vector getVector() {
		return v;
	}

	public long getDimension() {
		return v.getDimension();
	}
}
