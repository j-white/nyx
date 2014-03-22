package math.nyx.core;

import math.nyx.audio.AudioSignal;
import math.nyx.framework.FractalCodec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FractalFactory {
	@Autowired
	@Qualifier("imageCodec")
	FractalCodec imageCodec;

	@Autowired
	@Qualifier("audioCodec")
	FractalCodec audioCodec;

	public FractalCodec getEncoderFor(Signal signal) {
		// TODO: Dynamically discover the best type
		if (signal instanceof AudioSignal) {
			return audioCodec;
		} else {
			return imageCodec;
		}
	}

	public FractalCodec getCodecFor(Signal signal) {
		return getEncoderFor(signal);
	}
}
