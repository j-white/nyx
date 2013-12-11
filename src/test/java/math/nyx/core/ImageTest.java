package math.nyx.core;

import static math.nyx.util.TestUtils.assertMatrixEquals;
import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import math.nyx.core.imgproc.Image;
import math.nyx.fie.simple.FixedSizeMatrixPartitioner;
import math.nyx.util.MatrixGenerator;
import math.nyx.util.TestUtils;

import org.junit.Test;

public class ImageTest {
	@Test
	public void imageFromAndToMatrix() {
		double c = 127;
		int n = 32;
		Matrix m = new Matrix(n, n);
		m.fill(c);
		Image img = new Image(m);
		assertEquals(BufferedImage.TYPE_BYTE_GRAY, img.getType());

		Matrix mm = new Matrix(img);
		double data[][] = mm.getDataRef();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				assertEquals(c, data[i][j], TestUtils.DELTA);
			}
		}
	}

	@Test
	public void matrixToImageConversion() throws IOException {
		int n = 64;
		Matrix m = MatrixGenerator.generateInterestingMatrix(n);
		FixedSizeMatrixPartitioner fixedSizeMatrixPartitioner = new FixedSizeMatrixPartitioner();
		List<Partition> ranges = fixedSizeMatrixPartitioner.partitionRange(m);
		
		Matrix m1 = new Matrix(n, n);
		Matrix m2 = new Matrix(n, n);
		for (Partition range : ranges) {
			Point2D topLeftCorner = range.getConvexHull().get(0);
			m1.setSubMatrix(range.getMatrix().getData(), topLeftCorner.y, topLeftCorner.x);

			Image rangeAsImage = new Image(range.getMatrix());
			m2.setSubMatrix(new Matrix(rangeAsImage).getData(), topLeftCorner.y, topLeftCorner.x);
		}

		assertMatrixEquals(m, m1, TestUtils.DELTA);
		assertMatrixEquals(m, m2, 1.0);
	}
}
