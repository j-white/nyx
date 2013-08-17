package math.nyx.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.MatrixIndexException;

public class Matrix extends Array2DRowRealMatrix {
	private static final long serialVersionUID = 5346987134854330087L;
	private static final double DEFAULT_DELTA_FOR_EQUALS = 0.00001;

	public Matrix(double[][] d) {
		super(d);
	}

	public Matrix(int width, int height) {
		super(width, height);
	}

	public Matrix(Image img) {
		super(img.getWidth(), img.getHeight());
		
		if (img.getType() != BufferedImage.TYPE_BYTE_GRAY) {
			throw new IllegalArgumentException("Image must be converted to grayscale first.");
		}

		final byte[] pixels = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
		final int width = img.getWidth();
		final int height = img.getHeight();

		double[][] result = new double[height][width];
		for (int i = 0, row = 0, col = 0; i < pixels.length; i++) {
			result[row][col] = pixels[i] & 0xFF;
			col++;
			if (col == width) {
				col = 0;
				row++;
			}
		}

		setSubMatrix(result, 0, 0);
	}

	public Point2D getSize() {
		return new Point2D(getColumnDimension(), getRowDimension());
	}

	public void fill(double value) {
		double[][] data = this.getDataRef();
		for (double[] row: data) {
		    Arrays.fill(row, value);
		}
	}

	public int getNumEntries() {
		return getColumnDimension()*getRowDimension();
	}

	public double getSumOfEntries() {
		double sum = 0;
		double data[][] = getDataRef();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				sum += data[i][j];
			}
		}
		return sum;
	}

	public double getSumOfSquaredEntries() {
		double sum = 0;
		double data[][] = getDataRef();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				sum += Math.pow(data[i][j], 2);
			}
		}
		return sum;
	}

    public Matrix getSubMatrix(final int startRow, final int endRow,
            final int startColumn, final int endColumn)
            		throws MatrixIndexException {
    	return new Matrix(super.getSubMatrix(startRow, endRow, startColumn, endColumn).getData());
    }

    public Matrix copy() {
    	return new Matrix(super.copy().getData());
    }

    public String toString() {
    	int n = getColumnDimension();
    	int m = getRowDimension();
    	double data[][] = getDataRef();

    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < m; i++) {
    		if (i > 0) {
    			sb.append(",\n");
    		}

    		sb.append("{");
    		for (int j = 0; j < n; j++) {
    			if (j > 0) {
    				sb.append(", ");
    			}
    			sb.append(data[i][j]);
    		}
    		sb.append("}");
    	}
    	return sb.toString();
    }

    public boolean equals(Matrix matrix) {
    		return equals(matrix, DEFAULT_DELTA_FOR_EQUALS);
    }

    public boolean equals(Matrix matrix, double delta) {
    	int n = getColumnDimension();
    	int m = getRowDimension();
    	if (n != matrix.getColumnDimension() || m != matrix.getRowDimension()) {
    		return false;
    	}

    	double a[][] = getDataRef();
    	double b[][] = matrix.getDataRef();
    	for (int i = 0; i < m; i++) {
    		for (int j = 0; j < n; j++) {
    			if (Math.abs(a[i][j] - b[i][j]) > delta) {
    				return false;
    			}
    		}
    	}
    	return true;
    }

	public Matrix rotate() {
		Matrix B = copy();
		double a[][] = getDataRef();
		double b[][] = B.getDataRef();

		int n = getColumnDimension();
		int m = getRowDimension();
		int k, l;
		k = l = 0;
		for (int i = m-1; i >= 0; i--) {
			for (int j = 0; j < n; j++) {
				b[k++][l] = a[i][j];
			}
			k=0;
			l++;
		}

		return B;
	}

	public Matrix flipVertically() {
		Matrix B = copy();
		double a[][] = getDataRef();
		double b[][] = B.getDataRef();

		int n = getColumnDimension();
		int m = getRowDimension();
		int k, l;
		k = l = 0;
		for (int i = m-1; i >= 0; i--) {
			for (int j = 0; j < n; j++) {
				b[k][l++] = a[i][j];
			}
			l=0;
			k++;
		}

		return B;
	}

	public double getSubMatrixAverage(final int startRow, final int endRow,
            final int startColumn, final int endColumn) {
		double total = 0;
		double data[][] = getDataRef();
		for (int i = startRow; i <= endRow; i++) {
			for (int j = startColumn; j <= endColumn; j++) {
				total += data[i][j];
			}
		}
		return total / (double)((endRow-startRow+1)*(endColumn-startColumn+1));
	}

	public Matrix scalarMultiply(double d) {
		return new Matrix(super.scalarMultiply(d).getData());
	}

	public Matrix scalarAdd(double d) {
		return new Matrix(super.scalarAdd(d).getData());
	}
	
	public Matrix scale(float ratio) {
		int n = getColumnDimension();
		if (n != getRowDimension() || (n % 2) != 0) {
			throw new IllegalArgumentException("Only square matrices with even dimensions are supported.");
		}

		if (ratio == 1f) {
			return this;
		}

		if (ratio != 0.5f) {
			throw new IllegalArgumentException(String.format("Unsupported ratio %f", ratio));
		}

		int k = n-Math.round(n*ratio);
		Matrix mm = new Matrix(k, k);
		double data[][] = mm.getDataRef();
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < k; j++) {
				data[i][j] = getSubMatrixAverage(i*2, (i*2)+1, j*2, (j*2)+1);
			}
		}

		return mm;
	}
	
	public Matrix subtract(Matrix m) {
		return new Matrix(super.subtract(m).getDataRef());
	}
}
