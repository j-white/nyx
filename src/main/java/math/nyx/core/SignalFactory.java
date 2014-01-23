package math.nyx.core;

import java.io.File;
import java.io.IOException;

public interface SignalFactory {
	public Signal getSignalFor(File signalFile) throws IOException, InvalidSignalException;
}
