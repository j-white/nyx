package math.nyx.affine;

import java.util.Iterator;

import org.springframework.util.Assert;

public class SymmetryIterator implements Iterator<Double> {
	private double data[];
	final int n, N;
	private Symmetry symmetry;
	
	private int a, b;
	private int i, j, k;

	/*
	 * Determines which cell to pull from the vector based on the symmetry
	 */
	public SymmetryIterator(final double data[], Symmetry symmetry) {
		this.data = data;
		this.symmetry = symmetry;
			
		N = data.length;
		n = (int)Math.sqrt(data.length) - 1;
		Assert.isTrue((n+1) * (n+1) == N, "Row dimension must be a square.");

		i = j = k = 0;
	}

	public boolean hasNext() {
		return k < N;
	}

	public void doInc() {
		k++;

		if (j > n) {
			j = 0;
			i++;
		}

		switch (symmetry) {
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
			throw new IllegalArgumentException("Invalid symmetry: " + symmetry);
		}

		j++;
	}

	public Double next() {
		doInc();
		return data[a * (n+1) + b];
	}

	public void remove() {
		// This method is intentionally left blank
	}
}
