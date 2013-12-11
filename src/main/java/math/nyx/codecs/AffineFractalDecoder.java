package math.nyx.codecs;

import math.nyx.core.Signal;

public class AffineFractalDecoder {
	public Signal decode(AffineFractal fractal, float scale) {
		int signalDimension = Math.round(fractal.getSignalDimension() * scale);
		Signal signal = new Signal(signalDimension);
		return signal;
	}
}
