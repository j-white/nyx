package math.nyx.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import math.nyx.image.ImageSignal;

public class SignalFactory {
	public Signal getSignalFor(File signalFile) throws IOException {
		return new ImageSignal(new FileInputStream(signalFile));
	}
}
