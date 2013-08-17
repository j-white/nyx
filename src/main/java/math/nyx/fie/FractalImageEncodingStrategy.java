package math.nyx.fie;

import math.nyx.core.Collage;
import math.nyx.core.Matrix;

/*
 * TODO: Implement visitor pattern for callback during encode/decode
 * TODO: Allow the user to specify the initial matrix
 */
public interface FractalImageEncodingStrategy {
	public Collage encode(Matrix m);
	public Matrix decode(Collage w, int width, int height, int iterations);
	public Matrix decode(Collage w, int width, int height, int iterations, MatrixDecodingVisitor visitor);
}
