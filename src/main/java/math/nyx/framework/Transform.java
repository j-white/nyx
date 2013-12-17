package math.nyx.framework;

import java.io.Serializable;

import org.apache.commons.math.linear.RealMatrix;

public interface Transform extends Serializable, Comparable<Transform> {
	public double getDistance();
	public int getDomainBlockIndex();
	public int getRangeBlockIndex();
	public RealMatrix apply(RealMatrix domain);
}
