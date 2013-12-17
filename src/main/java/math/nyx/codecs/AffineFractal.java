package math.nyx.codecs;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Objects;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;

public class AffineFractal extends Fractal {
	private static final long serialVersionUID = -4131592582192920878L;
	private final List<AffineTransform> transforms = new LinkedList<AffineTransform>();

	@Override
	public Signal decode() {
		return decode(1.0f);
	}

	@Override
	public Signal decode(float scale) {
		AffineFractalCodec codec = new AffineFractalCodec();
		return codec.decode(this, scale);
	}

	public void addTransform(AffineTransform transform) {
		transforms.add(transform);
	}

	public List<AffineTransform> getTransforms() {
		return transforms;
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass()).add("transforms", transforms)
	            .toString();
	}
}
