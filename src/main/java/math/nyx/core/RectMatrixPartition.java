package math.nyx.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RectMatrixPartition extends Partition implements Serializable {
	private static final long serialVersionUID = -3936530267167350541L;
	private int startRow, endRow, startColumn, endColumn;
	private Point2D originalMatrixSize;
	private Orientation orientation = Orientation.ORIGINAL;

	private transient Matrix m;
	private transient double[][] data;
	private transient double sumOfPoints = Double.NEGATIVE_INFINITY;
	private transient double sumOfSquaredPoints = Double.NEGATIVE_INFINITY;
	private transient RectMatrixPartition scaledPartition = null;
	private transient float scaledPartitionRatio = 0;
	private transient double[] arrayOfPoints = null;

	public static enum Orientation {
        ORIGINAL,         /* 1 2
                            3 4 */ 
        ROTATE_90,       /* 3 1
                            4 2 */
        ROTATE_180,      /* 4 3
                            2 1 */
        ROTATE_270,      /* 2 4
                            1 3 */
        FLIP,            /* 3 4
                            1 2 */
        ROTATE_90_FLIP,  /* 4 2
                            3 1 */
        ROTATE_180_FLIP, /* 2 1
                            4 3 */
        ROTATE_270_FLIP  /* 1 3
                            2 4 */
    }

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}
	
	public Orientation getOrientation() {
		return orientation;
	}
	
	public RectMatrixPartition(final Matrix m) {
		initPartition(m, 0, m.getRowDimension()-1, 0, m.getColumnDimension()-1, Orientation.ORIGINAL);
	}

	public RectMatrixPartition(final Matrix m, final Orientation orientation) {
		initPartition(m, 0, m.getRowDimension()-1, 0, m.getColumnDimension()-1, orientation);
	}

	public RectMatrixPartition(final Matrix m, final int startRow, final int endRow,
			final int startColumn, final int endColumn) {
		initPartition(m, startRow, endRow, startColumn, endColumn, Orientation.ORIGINAL);
	}

	public RectMatrixPartition(final Matrix m, final int startRow, final int endRow,
			final int startColumn, final int endColumn, final Orientation orientation) {
		initPartition(m, startRow, endRow, startColumn, endColumn, orientation);
	}

	private void initPartition(final Matrix m, final int startRow, final int endRow,
			final int startColumn, final int endColumn, final Orientation orientation) {
		originalMatrixSize = m.getSize();
		setMatrix(m, false);

		this.startRow = startRow;
		this.endRow = endRow;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
		this.orientation = orientation;
	}

	private void setMatrix(Matrix matrix, boolean rescaleIfNeeded) {
		this.m = matrix;
		this.data = m.getDataRef();
		sumOfPoints = Double.NEGATIVE_INFINITY;
		sumOfSquaredPoints = Double.NEGATIVE_INFINITY;
		arrayOfPoints = null;

		if (rescaleIfNeeded && originalMatrixSize != m.getSize()) {
			recalculateRowsAndColumns();
		}
	}

	public void setMatrix(Matrix matrix) {
		setMatrix(matrix, true);
	}

	private void recalculateRowsAndColumns() {
		float oldWidth = originalMatrixSize.x;
		float oldHeight = originalMatrixSize.y;

		float newWidth = m.getColumnDimension();
		float newHeight = m.getRowDimension();
		
		if ( Math.abs(oldWidth - newWidth) < 0.1 || Math.abs(oldHeight - newHeight) < 0.1) {
			return;
		}
		
		float xScale = newWidth/oldWidth;
		startColumn =  (int)Math.floor(startColumn * xScale);
		endColumn = (int)Math.floor(endColumn * xScale);
		
		float yScale = newHeight/oldHeight;
		startRow =  (int)Math.floor(startRow * yScale);
		endRow = (int)Math.floor(endRow * yScale);
		
		originalMatrixSize = m.getSize();
	}

	public Matrix getMatrix() {
		Matrix mm = new Matrix((endRow-startRow)+1, (endColumn-startColumn)+1);
		double data[][] = mm.getDataRef();
		RectMatrixPartitionIterator it = iterator();
		while (it.hasNext()) {
			Point3D p = it.nextPoint();
			int row = (int)Math.round(p.y);
			int column = (int)Math.round(p.x);
			// Subtract the startRow and startColumn to offset the new values at 0
			data[row-startRow][column-startColumn] = p.z;
		}
		return mm;
	}

	public RectMatrixPartitionIterator iterator() {
        return new RectMatrixPartitionIterator(data, startRow, endRow,
        		startColumn, endColumn, orientation);
	}

	@Override
	public int getNumPoints() {
		return (endRow-startRow+1) * (endColumn-startColumn+1);
	}

	@Override
	public synchronized double getSumOfPoints() {
		/*if (sumOfPoints != Double.NEGATIVE_INFINITY) {
			return sumOfPoints;
		}*/

		double sumOfPoints = 0;
		for (double ai : this) {
			sumOfPoints += ai;
		}
		return sumOfPoints;
	}

	@Override
	public synchronized double getSumOfSquaredPoints() {
		if (sumOfSquaredPoints != Double.NEGATIVE_INFINITY) {
			return sumOfSquaredPoints;
		}

		sumOfSquaredPoints = 0;
		for (double ai : this) {
			sumOfSquaredPoints += ai*ai;
		}
		return sumOfSquaredPoints;
	}

	@Override
	public RectMatrixPartition scale(float ratio) {
		return new RectMatrixPartition(getMatrix().scale(ratio));
	}

	/**
	 * 0  1
	 * 3  2
	 */
	@Override
	public List<Point2D> getConvexHull() {
		List<Point2D> hull = new ArrayList<Point2D>();
		hull.add(new Point2D(startColumn, startRow));
		hull.add(new Point2D(endColumn, startRow));
		hull.add(new Point2D(endColumn, endRow));
		hull.add(new Point2D(startColumn, endRow));
		return hull;
	}

	@Override
	public List<Point3D> getPoints() {
		List<Point3D> listOfPoints = new ArrayList<Point3D>();
		RectMatrixPartitionIterator it = iterator();
		while (it.hasNext()) {
			listOfPoints.add(it.nextPoint());
		}
		return listOfPoints;
	}

	public String toString() {
		return getMatrix().toString();
	}

	@Override
	public int getWidth() {
		return endColumn-startColumn+1;
	}

	@Override
	public int getHeight() {
		return endRow-startRow+1;
	}

	@Override
	public List<Partition> getDifferentOrientations() {
		List<Partition> orientations = new ArrayList<Partition>();
		
		for (Orientation o : Orientation.values()) {
			RectMatrixPartition p = new RectMatrixPartition(m,
					startRow, endRow, startColumn, endColumn, o);
			p.sumOfPoints = getSumOfPoints();
			p.sumOfSquaredPoints = getSumOfSquaredPoints();
			orientations.add(p);
		}

		return orientations;
	}

	@Override
	public boolean intersectsWith(Partition p) {
		if (getClass() != p.getClass()) {
			throw new RuntimeException("Not yet implemented!");
		}

		RectMatrixPartition other = (RectMatrixPartition) p;
		return (startRow < other.endRow && endRow > other.startRow && startColumn < other.endColumn && endColumn > other.startColumn);
	}

	@Override
	public double[] toArray() {
		List<Point3D> points = getPoints();
		double[] arrayOfPoints = new double[points.size()];
		for (int i = 0; i < points.size(); i++) {
			Point3D point = points.get(i);
			arrayOfPoints[i] = point.z;
		}
		return arrayOfPoints;
	}
}
