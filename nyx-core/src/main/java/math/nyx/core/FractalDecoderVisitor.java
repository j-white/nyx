package math.nyx.core;

import org.apache.commons.math.linear.RealMatrix;

public interface FractalDecoderVisitor {
	public void afterTransform(int iteration, Transform transform,
			RealMatrix source, RealMatrix domain, RealMatrix decimatedDomain, RealMatrix target);

	public void beforeIteration(int n, RealMatrix source, RealMatrix target);

	public void afterIteration(int n, RealMatrix source, RealMatrix target);

	public void beforeDecode();
	
	public void afterDecode();
}
