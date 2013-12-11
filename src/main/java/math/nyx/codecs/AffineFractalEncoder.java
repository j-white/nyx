package math.nyx.codecs;

import math.nyx.core.Fractal;
import math.nyx.core.FractalEncoder;
import math.nyx.core.Signal;

public class AffineFractalEncoder implements FractalEncoder {
	public Fractal encode(Signal signal) {
		AffineFractal fractal = new AffineFractal();
		fractal.setSignalDimension(signal.getDimension());
		return fractal;
	}
}
