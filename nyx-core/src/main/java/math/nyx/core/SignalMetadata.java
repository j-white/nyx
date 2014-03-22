package math.nyx.core;

import java.io.Serializable;

public interface SignalMetadata extends Serializable {

	/**
	 * Generate new meta-data for the scaled vector.
	 */
	public SignalMetadata scale(int scale);
}
