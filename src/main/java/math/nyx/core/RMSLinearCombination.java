package math.nyx.core;

public class RMSLinearCombination implements Comparable<RMSLinearCombination> {
	private Partition from;
	private Partition to;
	private double rms;
	private double scale;
	private double offset;

	/**
	 * Defined so we can use Double.compareTo.
	 */
	private Double rmsValueAsObject;

	public RMSLinearCombination(double rms) {
		this.rms = rms;
		rmsValueAsObject = new Double(rms);
	}

	public RMSLinearCombination(Partition from, Partition to, double rms, double scale, double offset) {
		this.from = from;
		this.to = to;
		this.rms = rms;
		this.scale = scale;
		this.offset = offset;
		
		rmsValueAsObject = new Double(rms);
	}

	public void setFrom(Partition from) {
		this.from = from;
	}

	public Partition getFrom() {
		return from;
	}

	public Partition getTo() {
		return to;
	}

	public double getRms() {
		return rms;
	}

	public double getScale() {
		return scale;
	}

	public double getOffset() {
		return offset;
	}

	public String toString() {
		return String.format("rms: %f scale: %f offset: %f",
				rms, scale, offset);
	}

	public int compareTo(RMSLinearCombination o) {
		if (o == null) {
			return rmsValueAsObject.compareTo(Double.MAX_VALUE);
		}
		return rmsValueAsObject.compareTo(o.rmsValueAsObject);
	}
}
