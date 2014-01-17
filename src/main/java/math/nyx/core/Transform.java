package math.nyx.core;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.math.linear.RealMatrix;

public interface Transform extends Serializable, Comparable<Transform> {
	public double getDistance();
	public int getDomainBlockIndex();
	public int getRangeBlockIndex();
	public RealMatrix apply(RealMatrix domain, Signal signal);
	public RealMatrix apply(RealMatrix domain, Signal signal, boolean inPlace);
	public Map<String, Object> getKernelParameters();
}
