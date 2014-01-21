package math.nyx.core;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.math.linear.Array2DColumnRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

public interface Transform extends Serializable, Comparable<Transform> {
	public double getDistance();
	public int getDomainBlockIndex();
	public int getRangeBlockIndex();
	public void apply(RealMatrix domain, Signal signal);
	public void apply(Array2DColumnRealMatrix domain, Signal signal);
	public Map<String, Object> getKernelParameters();
}
