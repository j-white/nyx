package math.nyx.core;

import org.apache.commons.math.linear.RealMatrix;

import com.google.common.base.Objects;

public class SignalBlock {
	private final int index;
	private final RealMatrix block;

	private double sumOfPoints = Double.NEGATIVE_INFINITY;
	private double sumOfSquaredPoints = Double.NEGATIVE_INFINITY;

	public SignalBlock(int index, RealMatrix block) {
		this.index = index;
		this.block = block;
	}

	public int getIndex() {
		return index;
	}

	public RealMatrix getBlock() {
		return block;
	}

	public double getSumOfPoints() {
		if (sumOfPoints == Double.NEGATIVE_INFINITY) {
			double sum = 0;
			double[] xis = block.getColumn(0);
			for (double xi  : xis) {
				sum += xi;
			}
			sumOfPoints = sum;
		}
		return sumOfPoints;
	}

	public double getSumOfSquaredPoints() {
		if (sumOfSquaredPoints == Double.NEGATIVE_INFINITY) {
			double sum = 0;
			double[] xis = block.getColumn(0);
			for (double xi  : xis) {
				sum += xi * xi;
			}
			sumOfSquaredPoints = sum;
		}
		return sumOfSquaredPoints;
	}


	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass()).add("index", index)
	            .add("block", block)
	            .toString();
	}
}
