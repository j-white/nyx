package math.nyx.codecs;

import java.io.Serializable;

import com.google.common.base.Objects;

public class AffineTransform implements Comparable<AffineTransform>, Serializable {
	private static final long serialVersionUID = 4115274904771797361L;
	private final int domainBlockIndex;
	private final int rangeBlockIndex;
	private final Double distance;
	private final double scale;
	private final double offset;
	
	public AffineTransform(int domainBlockIndex, int rangeBlockIndex, double distance, double scale, double offset) {
		this.domainBlockIndex = domainBlockIndex;
		this.rangeBlockIndex = rangeBlockIndex;
		this.distance = distance;
		this.scale = scale;
		this.offset = offset;
	}

	public int getDomainBlockIndex() {
		return domainBlockIndex;
	}

	public int getRangeBlockIndex() {
		return rangeBlockIndex;
	}

	public double getScale() {
		return scale;
	}

	public double getOffset() {
		return offset;
	}

	public double getDistance() {
		return distance;
	}

	public int compareTo(AffineTransform o) {
		if (o == null) {
			return distance.compareTo(Double.MAX_VALUE);
		}
		return distance.compareTo(o.distance);
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass()).add("domainBlockIndex", domainBlockIndex)
	            .add("rangeBlockIndex", rangeBlockIndex)
	            .add("distance", distance)
	            .add("scale", scale)
	            .add("offset", offset)
	            .toString();
	}
}
