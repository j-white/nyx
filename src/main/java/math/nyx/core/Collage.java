package math.nyx.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Collection of contractive maps.
 *
 * @author jwhite
 */
public class Collage implements Serializable {
	private static final long serialVersionUID = -4306510899219622749L;
	private List<ContractiveMap> maps = new ArrayList<ContractiveMap>();
	private double minValue = -Double.MAX_VALUE;
	private double maxValue = Double.MAX_VALUE;

	public Collage() {
		// This method is intentionally left blank
	}

	public Collage(ContractiveMap w) {
		maps.add(w);
	}

	public Collage(ContractiveMap w[]) {
		for (ContractiveMap wi : w) {
			addMap(wi);
		}
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void addMap(ContractiveMap wi) {
		maps.add(wi);
	}

	public List<ContractiveMap> getMaps() {
		return maps;
	}

	public Matrix apply(Matrix matrix) {
		Matrix m = matrix.copy();
		for (ContractiveMap map : maps) {
			m = map.apply(m, minValue, maxValue);
		}
		return m;
	}
}
