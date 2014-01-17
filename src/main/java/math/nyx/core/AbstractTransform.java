package math.nyx.core;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

public abstract class AbstractTransform implements Transform {
	private static final long serialVersionUID = -7636935260663735189L;
	private final int domainBlockIndex;
	private final int rangeBlockIndex;
	private final Double distance;

	public AbstractTransform(int domainBlockIndex, int rangeBlockIndex, double distance) {
		this.domainBlockIndex = domainBlockIndex;
		this.rangeBlockIndex = rangeBlockIndex;
		this.distance = distance;
	}

	public int getDomainBlockIndex() {
		return domainBlockIndex;
	}

	public int getRangeBlockIndex() {
		return rangeBlockIndex;
	}

	public double getDistance() {
		return distance;
	}

	public int compareTo(Transform o) {
		if (o == null) {
			return distance.compareTo(Double.MAX_VALUE);
		}
		return distance.compareTo(o.getDistance());
	}

	protected ToStringHelper toStringHelper() {
		return Objects.toStringHelper(this).omitNullValues()
				.add("domainBlockIndex", domainBlockIndex)
				.add("rangeBlockIndex", rangeBlockIndex)
				.add("distance", distance);
	}

	@Override
	public final String toString() {
		return toStringHelper().toString();
	}
}
