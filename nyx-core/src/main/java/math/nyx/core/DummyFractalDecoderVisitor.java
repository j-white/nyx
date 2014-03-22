package math.nyx.core;

import org.apache.commons.math.linear.RealMatrix;

public class DummyFractalDecoderVisitor implements FractalDecoderVisitor {
	@Override
	public void afterTransform(int iteration, Transform transform,
			RealMatrix source, RealMatrix domain, RealMatrix decimatedDomain, RealMatrix target) {
		// Do nothing
	}

	@Override
	public void beforeIteration(int n, RealMatrix source, RealMatrix target) {
		// Do nothing
	}

	@Override
	public void afterIteration(int n, RealMatrix source, RealMatrix target) {
		// Do nothing
	}

	@Override
	public void beforeDecode() {
		// Do nothing
	}

	@Override
	public void afterDecode() {
		// Do nothing
	}
}
