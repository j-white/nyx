package math.nyx.framework;

import org.apache.commons.math.linear.RealMatrix;

import com.google.common.base.Objects;

public class SignalBlock {
	private final int index;
	private final RealMatrix block;

	private double minVal = Double.POSITIVE_INFINITY;
	private double maxVal = Double.NEGATIVE_INFINITY;
	private double minAbsVal = Double.POSITIVE_INFINITY;
	private double maxAbsVal = Double.NEGATIVE_INFINITY;
	private double sumOfPoints = Double.NEGATIVE_INFINITY;
	private double sumOfAbsPoints = Double.NEGATIVE_INFINITY;
	private double sumOfSquaredPoints = Double.NEGATIVE_INFINITY;

	public SignalBlock(int index, RealMatrix block) {
		this.index = index;
		this.block = block;
		generateFacts();
	}

	public int getIndex() {
		return index;
	}

	public RealMatrix getBlock() {
		return block;
	}

	private void generateFacts() {
		double[] xis = block.getColumn(0);
		double sum = 0;
		double sumOfAbs = 0;
		double sumOfSquares = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double minAbs = Double.MAX_VALUE;
		double maxAbs = Double.MIN_VALUE;
		
		for (double xi  : xis) {
			double abs = Math.abs(xi);
			sum += xi;
			sumOfAbs += abs;
			sumOfSquares += xi * xi;
			if (xi < min) min = xi;
			if (xi > max) max = xi;
			if (abs < minAbs) minAbs = abs;
			if (abs > maxAbs) maxAbs = abs;
		}

		sumOfPoints = sum;
		sumOfAbsPoints = sumOfAbs;
		sumOfSquaredPoints = sumOfSquares;
		minVal = min;
		maxVal = max;
		minAbsVal = minAbs;
		maxAbsVal = maxAbs;
	}
	
	public double getSumOfPoints() {
		return sumOfPoints;
	}

	public double getSumOfAbsPoints() {
		return sumOfAbsPoints;
	}

	public double getSumOfSquaredPoints() {
		return sumOfSquaredPoints;
	}

	public double getMinVal() {
		return minVal;
	}

	public double getMaxVal() {
		return maxVal;
	}

	public double getMinAbsVal() {
		return minAbsVal;
	}

	public double getMaxAbsVal() {
		return maxAbsVal;
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass()).add("index", index)
	            .add("block", block)
	            .toString();
	}
}
