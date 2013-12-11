package math.nyx.core;

public interface FractalCodec {
	public Fractal encode(Signal signal);

	public Signal decode(Fractal fractal);

	public Signal decode(Fractal fractal, float scale);
}
