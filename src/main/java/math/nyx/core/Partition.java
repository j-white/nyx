package math.nyx.core;

import java.util.List;

public abstract class Partition implements Iterable<Double> {
	public abstract void setMatrix(Matrix m);
	public abstract Matrix getMatrix();

	public abstract List<Point3D> getPoints();
	public abstract double[] toArray();
	
	public abstract int getNumPoints();
	public abstract double getSumOfPoints();
	public abstract double getSumOfSquaredPoints();

	public abstract int getWidth();
	public abstract int getHeight();
	
	/**
	 * Generates the convex hull for this 2D partition.
	 * The first point in the list should be the point with
	 * (x,y) = (min x, max y)
	 *  
	 * @return List point 2D points
	 */
	public abstract List<Point2D> getConvexHull();

	public abstract Partition scale(float ratio);

	/**
	 * Retrieves all possible orientations of the partition
	 * that have the same dimensions as the original partition.
	 *
	 * @return	List of oriented partitions
	 */
	public abstract List<Partition> getDifferentOrientations();

	/**
	 * Checks whether or not the two partitions intersect.
	 * 
	 * @param p
	 * @return
	 */
	public abstract boolean intersectsWith(Partition p);
}
