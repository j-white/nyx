package math.nyx.core;

public class Point3D {
	public double x;
	public double y;
	public double z;

	public Point3D() {
		initPoint(0, 0, 0.0d);
	}

	public Point3D(double x, double y, double z) {
		initPoint(x, y, z);
	}

	private void initPoint(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point3D other = (Point3D) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}

	public String toString() {
		return String.format("(%f,%f,%f)", x, y, z);
	}
}
