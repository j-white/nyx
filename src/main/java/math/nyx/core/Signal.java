package math.nyx.core;

import java.io.Serializable;

public interface Signal extends Serializable {
	public Vector getVector();
	public long getDimension();
}
