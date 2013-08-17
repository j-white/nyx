package math.nyx.fie;

import math.nyx.core.Matrix;

public interface MatrixDecodingVisitor {
	public void decodedIteration(Matrix matrix, int currentIteration, int totalIterations);
}
