package math.nyx.fie;

import java.util.List;

import math.nyx.core.Matrix;
import math.nyx.core.Partition;
import math.nyx.core.Point2D;

/**
 * Given a matrix return a set of partitions.
 *
 * @author jwhite
 *
 */
public interface MatrixPartitioner {
	public Point2D getDomainSize(Matrix m);
	public Point2D getRangeSize(Matrix m);
	public List<Partition> partitionDomain(Matrix m);
	public List<Partition> partitionRange(Matrix m);
}
