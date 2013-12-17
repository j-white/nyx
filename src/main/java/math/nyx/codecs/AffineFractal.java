package math.nyx.codecs;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;

public class AffineFractal extends Fractal {
	private static final long serialVersionUID = -4131592582192920878L;

	@Override
	public Signal decode() {
		return decode(1.0f);
	}

	@Override
	public Signal decode(float scale) {
		AffineFractalCodec codec = new AffineFractalCodec();
		return codec.decode(this, scale);
	}
}
