package math.nyx.core;

public interface ContractiveMap {
	public void setRange(Partition range);
	public Partition getRange();

	public void setDomain(Partition domain);
	public Partition getDomain();

	public Matrix apply(Matrix matrix, double minValue, double maxValue);
}
