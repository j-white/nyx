package math.nyx.core;

import java.util.Iterator;

import math.nyx.core.RectMatrixPartition.Orientation;

public class RectMatrixPartitionIterator implements Iterator<Double> {
	double data[][];
	int a, b, c, d, i, j, n;
	int startRow, endRow, startColumn, endColumn;
	Orientation orientation;
	
	/*
	 * Iterates over the target region (i,j)
	 * Determines which cell to pull (a,b) from the matrix based on the orientation
	 */
	public RectMatrixPartitionIterator(final double data[][], final int startRow, final int endRow,
            final int startColumn, final int endColumn, Orientation orientation) {
		this.data = data;
		this.startRow = startRow;
		this.endRow = endRow;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
		this.orientation = orientation;
		n = endColumn-startColumn;
		if ((endRow-startRow) != n) {
			throw new RuntimeException("Only square partitions are supported!");
		}

		i = 0;
		j = 0;
	}

	public boolean hasNext() {
		return !(i == n && j == (n+1));
	}

	public void doInc() {
		if (j > n) {
			j = 0;
			i++;
		}

		c = i;
		d = j;

		switch (orientation) {
		case ORIGINAL:
			a = i;
			b = j;
			break;
		case ROTATE_90:
			a = n-j;
			b = i;
			break;
		case ROTATE_180:
			a = n-i;
			b = n-j;
			break;
		case ROTATE_270:
			a = j;
			b = n-i;
			break;
		case FLIP:
			a = n-i;
			b = j;
			break;
		case ROTATE_90_FLIP:
			a = n-j;
			b = n-i;
			break;
		case ROTATE_180_FLIP:
			a = i;
			b = n-j;
			break;
		case ROTATE_270_FLIP:
			a = j;
			b = i;
			break;
		default:
			throw new RuntimeException("Invalid orientation: " + orientation);
		}

		j++;
	}

	public Point3D nextPoint() {
		doInc();
		// x-axis for the columns
		// y-axis for the rows 
		return new Point3D(d+startColumn, c+startRow, data[a+startRow][b+startColumn]);
	}

	public Double next() {
		doInc();
		return data[a+startRow][b+startColumn];
	}

	public void remove() {
		// This method is intentionally left blank
	}
}
