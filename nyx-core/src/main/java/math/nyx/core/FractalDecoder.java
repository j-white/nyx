package math.nyx.core;

public interface FractalDecoder {
	public Signal decode(Fractal fractal, int scale);
	public Signal decode(Fractal fractal, int scale, FractalDecoderVisitor visitor);
}
