package math.nyx.core;

import java.io.Serializable;
import java.util.List;

public class AffineMap implements ContractiveMap, Serializable {
	private static final long serialVersionUID = -346333664204636357L;
	private double a, b, c, d, e, f, s, o;
	private Partition domain;
	private Partition range;

	public AffineMap(RMSLinearCombination rmsLinearCombination) {
		initializeMap(rmsLinearCombination.getFrom(), rmsLinearCombination.getTo(), false, 0,
				rmsLinearCombination.getScale(),
				rmsLinearCombination.getOffset());
	}

	public AffineMap(Partition domain, Partition range) {
		initializeMap(domain, range, false, 0, 1, 0);
	}

	public AffineMap(Partition domain, Partition range, double zscale, double zoffset) {
		initializeMap(domain, range, false, 0, zscale, zoffset);
	}

	public AffineMap(Partition domain, boolean flip, double theta) {
		initializeMap(domain, domain, flip, theta, 1, 0);
	}

	private void initializeMap(Partition domain, Partition range,
			boolean flip, double theta, double zscale, double zoffset) {

		this.domain = domain;
		this.range = range;
		s = zscale;
		o = zoffset;

		Point2D d1,d2,r1,r2;
		List<Point2D> dp = domain.getConvexHull();
		List<Point2D> rp = range.getConvexHull();
		d1 = dp.get(0);
		d2 = dp.get(dp.size()-2);
		r1 = rp.get(0);
		r2 = rp.get(rp.size()-2);

		double r_denum = d1.x - d2.x;
		if (r_denum == 0.0) {
			throw new RuntimeException("Invalid points in convex hull.");
		}

		double r = (r1.x - r2.x) / r_denum;
		if (flip) {
			r = -r;
		}

		a = r*Math.cos(theta);
		b = r*Math.sin(theta);
		c = -r*Math.sin(theta);
		d = r*Math.cos(theta);

		e = r1.x - (r*d1.x);
		f = r1.y - (r*d1.y);
	}

	public Point3D applyToPoint(Point3D p0) {
		Point3D p1 = new Point3D();
		p1.x = a*p0.x + c*p0.y + e;
		p1.y = b*p0.x + d*p0.y + f;
		p1.z = s*p0.z + o;
		return p1;
	}

	public Matrix apply(Matrix matrix, double minValue, double maxValue) {
		Matrix m = matrix;

		List<Point2D> hull = range.getConvexHull();
		Point2D topLeftCorner = hull.get(0);
		Point2D bottomRightCorner = hull.get(2);
		
		int rangeWidth = bottomRightCorner.x - topLeftCorner.x + 1;
		int rangeHeight = bottomRightCorner.y - topLeftCorner.y + 1;
		
		Matrix sumOfPoints = new Matrix(rangeWidth, rangeHeight);
		sumOfPoints.fill(0);
		Matrix numberOfPoints = sumOfPoints.copy();
		
		domain.setMatrix(matrix);
		List<Point3D> pointsInDomain = domain.getPoints();
		for (Point3D p1 : pointsInDomain) {
			addEntry(sumOfPoints, numberOfPoints, topLeftCorner, applyToPoint(p1));
		}

		double sumData[][] = sumOfPoints.getDataRef();
		double countData[][] = numberOfPoints.getDataRef();
		double val;
		for (int i = 0; i < rangeWidth; i++) {
			for (int j = 0; j < rangeHeight; j++) {
				if (countData[i][j] > 0) {
					val = sumData[i][j]/countData[i][j];

					if (val < minValue) {
						val = minValue;
					} else if (val > maxValue) {
						val = maxValue;
					}

					m.setEntry(i+topLeftCorner.y, j+topLeftCorner.x, val);
				}
			}
		}

		return m;
	}	

	private void addEntry(Matrix sum, Matrix count, Point2D topLeftCorner, Point3D p) {
		int width, height;
		width = sum.getColumnDimension();
		height = sum.getRowDimension();

		int xlow = (int)Math.floor(p.x);
		if (Math.abs(xlow-p.x) == 0.5) {
			int xhigh = (int)Math.ceil(p.x);
			addEntry(sum, count, topLeftCorner, new Point3D(xlow, p.y, p.z));
			addEntry(sum, count, topLeftCorner, new Point3D(xhigh, p.y, p.z));
			return;
		}

		int ylow = (int)Math.floor(p.x);
		if (Math.abs(ylow-p.y) == 0.5) {
			int yhigh = (int)Math.ceil(p.x);
			addEntry(sum, count, topLeftCorner, new Point3D(p.x, ylow, p.z));
			addEntry(sum, count, topLeftCorner, new Point3D(p.x, yhigh, p.z));
			return;
		}

		int xint = (int)Math.round(p.x) - topLeftCorner.x;
		int yint = (int)Math.round(p.y) - topLeftCorner.y;
		if (xint < 0) {
			xint = 0;
		} else if (xint >= width) {
			xint = width-1;
		}

		if (yint < 0) {
			yint = 0;
		} else if (yint >= height) {
			yint = height-1;
		}

		sum.addToEntry(yint, xint, p.z);
		count.addToEntry(yint, xint, 1);
	}
	
	public void setRange(Partition range) {
		this.range = range;
	}

	public Partition getRange() {
		return range;
	}

	public void setDomain(Partition domain) {
		this.domain = domain;
	}

	public Partition getDomain() {
		return domain;
	}

	public Matrix getXYTransform() {
		return new Matrix(new double[][]{
			{a, b, e},
			{c, d, f},
			{0, 0, 1}
		});
	}

	public String toString() {
		return String.format("Affine transform with a: %f b: %f c: %f d: %f e: %f f: %f s: %f o: %f",
				a, b, c, d, e, f, s, o);
	}
}
