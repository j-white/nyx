package math.nyx.codecs;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;

public class AffineFractal extends Fractal {
	private static final long serialVersionUID = -4131592582192920878L;

	@Override
	public Signal decode() {
		AffineFractalDecoder decoder = new AffineFractalDecoder();
		return decoder.decode(this, 1.0f);
	}

	@Override
	public Signal decode(float scale) {
		AffineFractalDecoder decoder = new AffineFractalDecoder();
		return decoder.decode(this, scale);
	}
}
