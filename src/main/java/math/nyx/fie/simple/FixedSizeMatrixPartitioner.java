package math.nyx.fie.simple;

import java.util.ArrayList;
import java.util.List;

import math.nyx.core.Matrix;
import math.nyx.core.Partition;
import math.nyx.core.Point2D;
import math.nyx.core.RectMatrixPartition;
import math.nyx.fie.MatrixPartitioner;

public class FixedSizeMatrixPartitioner implements MatrixPartitioner {
	public List<Partition> partitionDomain(Matrix m) {
		int n = getDomainLength(m.getColumnDimension());
		return partitionMatrix(m, n, n, true, true);
	}

	public List<Partition> partitionRange(Matrix m) {
		int n = getRangeLength(m.getColumnDimension());
		return partitionMatrix(m, n, n, false, false);
	}

	private List<Partition> partitionMatrix(Matrix m, int width, int height, boolean overlapping, boolean differentOrientations) {
		int matrixWidth =  m.getColumnDimension();
		int matrixHeight = m.getRowDimension();	

		if (!overlapping) {
			if ((matrixWidth % width != 0) || (matrixHeight % height != 0)) {
				throw new IllegalArgumentException(String.format("Invalid partition size (%d,%d) for matrix size (%d,%d)",
						width, height, matrixWidth, matrixHeight));
			}
		}

		int xinc = 1;
		int yinc = 1;
		if (!overlapping) {
			xinc = width;
			yinc = height;
		}

		List<Partition> partitions = new ArrayList<Partition>();
		for (int i = 0; (i+width-1) < matrixWidth; i+=xinc) {
			for (int j = 0; (j+height-1) < matrixHeight; j+=yinc) {
				RectMatrixPartition rmp = new RectMatrixPartition(m, i, i+width-1, j, j+height-1);
				if (!differentOrientations) {
					partitions.add(rmp);
				} else {
					partitions.addAll(rmp.getDifferentOrientations());
				}
			}
		}
		return partitions;
	}

	public int getDomainLength(int n) {
		if (n < 32) {
			return 4;
		} else if (n <= 256) {
			return 16;
		} else {
			return 32;
		}
	}

	public int getRangeLength(int n) {
		if (n < 32) {
			return 2;
		} else if (n <= 256) {
			return 8;
		} else {
			return 16;
		}
	}

	public Point2D getDomainSize(Matrix m) {
		int n = getDomainLength(m.getColumnDimension());
		return new Point2D(n, n);
	}

	public Point2D getRangeSize(Matrix m) {
		int n = getRangeLength(m.getColumnDimension());
		return new Point2D(n, n);
	}
}
