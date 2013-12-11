package math.nyx.core;

import org.apache.commons.math.linear.ArrayRealVector;

public class Vector extends ArrayRealVector {
	private static final long serialVersionUID = -654483284446774948L;

	public Vector() {
	}

	public Vector(int size) {
		super(size);
	}

	public Vector(long size) {
		super((int)size);
	}
}
