package math.nyx.core;

public interface Metric {
	public double getDistance(Matrix m1, Matrix m2);
	
	public double getDistance(Partition p1, Partition p2);
}
