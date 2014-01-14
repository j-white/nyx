package math.nyx.framework;

import math.nyx.core.Transform;

public interface Kernel {
	public Transform encode(SignalBlock domainBlock, SignalBlock rangeBlock);
	public Transform encode(SignalBlock domainBlock, SignalBlock rangeBlock, boolean permute);
}
