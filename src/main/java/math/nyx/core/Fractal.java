package math.nyx.core;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import math.nyx.framework.Transform;

import com.google.common.base.Objects;

public abstract class Fractal implements Serializable {
	private static final long serialVersionUID = -3691227743645881425L;

	private int signalDimension;

	private final List<Transform> transforms = new LinkedList<Transform>();

	public abstract Signal decode();

	public abstract Signal decode(float scale);

	public int getSignalDimension() {
		return signalDimension;
	}

	public void setSignalDimension(int signalDimension) {
		this.signalDimension = signalDimension;
	}

	public void addTransform(Transform transform) {
		transforms.add(transform);
	}

	public List<Transform> getTransforms() {
		return transforms;
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass()).add("transforms", transforms)
	            .toString();
	}
}
