package math.nyx.framework;

import math.nyx.core.SignalBlock;

public interface Kernel {
	public Transform encode(SignalBlock domainBlock, SignalBlock rangeBlock);
}
