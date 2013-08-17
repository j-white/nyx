package math.nyx.core;

import java.io.Serializable;

public class Point2D implements Serializable {
	private static final long serialVersionUID = -198474910528354923L;
	public int x;
	public int y;

	public Point2D() {
		initPoint(0, 0);
	}

	public Point2D(int x, int y) {
		initPoint(x, y);
	}

	private void initPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point2D other = (Point2D) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public String toString() {
		return String.format("(%d,%d)", x, y);
	}
}
