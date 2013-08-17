package math.nyx.fie;

import java.util.Iterator;
import java.util.List;

import math.nyx.core.Matrix;
import math.nyx.core.Partition;
import math.nyx.core.Point2D;
import math.nyx.core.Point3D;

public class OptimizedPartition extends Partition {
	private Partition partition;
	private int numPoints;
	private double sumOfPoints;
	private double sumOfSquaredPoints;
	private double[] arrayOfPoints;
	private OptimizedPartition optimizedScaledPartition;
	private boolean calculateScaledPartition = true;
	private static final float RATIO_TO_OPTIMIZED = 0.5f;

	public OptimizedPartition(Partition partition) {
		this.partition = partition;
		optimize();
	}

	private OptimizedPartition(Partition partition, boolean calculateScaledPartition) {
		this.partition = partition;
		this.calculateScaledPartition = calculateScaledPartition;
		optimize();
	}

	private void optimize() {
		if (calculateScaledPartition) {
			optimizedScaledPartition = new OptimizedPartition(partition.scale(RATIO_TO_OPTIMIZED), false);
		} else {
			numPoints = partition.getNumPoints();
			sumOfPoints = partition.getSumOfPoints();
			sumOfSquaredPoints = partition.getSumOfSquaredPoints();
			arrayOfPoints = partition.toArray();
		}
	}

	//
	// Optimized routines
	//
	@Override
	public double[] toArray() {
		return arrayOfPoints;
	}

	@Override
	public int getNumPoints() {
		return numPoints;
	}

	@Override
	public double getSumOfPoints() {
		return sumOfPoints;
	}

	@Override
	public double getSumOfSquaredPoints() {
		return sumOfSquaredPoints;
	}

	@Override
	public Partition scale(float ratio) {
		if (ratio == RATIO_TO_OPTIMIZED) {
			return optimizedScaledPartition;
		}
		return partition.scale(ratio);
	}

	@Override
	public void setMatrix(Matrix m) {
		partition.setMatrix(m);
		// Re-optimize after updating the matrix
		optimize();
	}

	//
	// Standard routines
	//
	public Iterator<Double> iterator() {
		return partition.iterator();
	}

	@Override
	public Matrix getMatrix() {
		return partition.getMatrix();
	}

	@Override
	public List<Point3D> getPoints() {
		return partition.getPoints();
	}

	@Override
	public int getWidth() {
		return partition.getWidth();
	}

	@Override
	public int getHeight() {
		return partition.getHeight();
	}

	@Override
	public List<Point2D> getConvexHull() {
		return partition.getConvexHull();
	}

	@Override
	public List<Partition> getDifferentOrientations() {
		return partition.getDifferentOrientations();
	}

	@Override
	public boolean intersectsWith(Partition p) {
		return partition.intersectsWith(p);
	}
}
