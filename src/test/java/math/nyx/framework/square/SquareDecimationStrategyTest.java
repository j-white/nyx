package math.nyx.framework.square;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import math.nyx.framework.square.SquareDecimationStrategy;
import math.nyx.utils.TestUtils;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class SquareDecimationStrategyTest {
	@Autowired
	private SquareDecimationStrategyFactory sdStrategyFactory;

	private SquareDecimationStrategy getDecimator(int rangeDimension, int domainDimension) {
		return sdStrategyFactory.getDecimator(rangeDimension, domainDimension);
	}

	private SparseRealMatrix getDecimationOperator(int rangeDimension, int domainDimension) {
		return getDecimator(rangeDimension, domainDimension).getDecimationOperator();
	}

	@Test
	public void checkOffset() {
		// 3x3 -> 2x2
		SquareDecimationStrategy sdStrategy = getDecimator(4, 9);
		assertEquals(0, sdStrategy.getOffset(0));
		assertEquals(1, sdStrategy.getOffset(1));
		assertEquals(3, sdStrategy.getOffset(2));
		assertEquals(4, sdStrategy.getOffset(3));
		
		// 16x16 -> 8x8
		sdStrategy = getDecimator(64, 256);
		assertEquals(0, sdStrategy.getOffset(0));
		assertEquals(2, sdStrategy.getOffset(1));
		assertEquals(4, sdStrategy.getOffset(2));
		assertEquals(6, sdStrategy.getOffset(3));
		// ...
		assertEquals(32, sdStrategy.getOffset(8));
	}

	@Test
	public void checkIndex() {
		// 3x3 -> 2x2
		SquareDecimationStrategy sdStrategy = getDecimator(4, 9);
		assertEquals(0, sdStrategy.getIndex(0));
		assertEquals(1, sdStrategy.getIndex(1));
		assertEquals(3, sdStrategy.getIndex(2));
		assertEquals(4, sdStrategy.getIndex(3));
		
		// 16x16 -> 8x8
		sdStrategy = getDecimator(64, 256);
		assertEquals(0, sdStrategy.getIndex(0));
		assertEquals(1, sdStrategy.getIndex(1));
		assertEquals(16, sdStrategy.getIndex(2));
		assertEquals(17, sdStrategy.getIndex(3));
	}

	@Test
	public void decimateSmall() throws InterruptedException {
		// Generate
		RealMatrix signal = TestUtils.generateSignal(9);

		// Decimate
		SparseRealMatrix decimator = getDecimationOperator(4, 9);
		RealMatrix decimated = decimator.multiply(signal);

		// Verify
		assertArrayEquals(new double[]{3, 4, 6, 7}, decimated.getColumn(0), TestUtils.DELTA);
	}

	@Test
	public void decimateLarge() {
		double domainVector[] = new double[] {
			51.0, 43.0, 54.0, 59.0, 56.0, 56.0, 52.0, 56.0, 55.0, 55.0, 55.0, 59.0, 60.0, 29.0, 16.0, 33.0,
			43.0, 50.0, 56.0, 57.0, 54.0, 53.0, 54.0, 57.0, 58.0, 55.0, 57.0, 62.0, 55.0, 24.0, 16.0, 31.0,
			51.0, 60.0, 57.0, 51.0, 50.0, 52.0, 53.0, 55.0, 56.0, 58.0, 60.0, 56.0, 47.0, 23.0, 15.0, 25.0,
			52.0, 54.0, 54.0, 51.0, 49.0, 48.0, 53.0, 51.0, 53.0, 58.0, 59.0, 54.0, 44.0, 22.0, 14.0, 19.0,
			51.0, 50.0, 48.0, 49.0, 48.0, 46.0, 48.0, 51.0, 54.0, 56.0, 52.0, 46.0, 41.0, 21.0, 13.0, 14.0,
			54.0, 48.0, 47.0, 46.0, 47.0, 44.0, 46.0, 51.0, 48.0, 49.0, 47.0, 42.0, 31.0, 16.0, 11.0, 10.0,
			48.0, 44.0, 41.0, 38.0, 39.0, 38.0, 40.0, 43.0, 44.0, 40.0, 38.0, 33.0, 25.0, 14.0, 12.0, 8.0,
			27.0, 25.0, 27.0, 28.0, 28.0, 32.0, 32.0, 30.0, 32.0, 30.0, 27.0, 25.0, 22.0, 15.0, 12.0, 9.0,
			16.0, 19.0, 23.0, 23.0, 23.0, 21.0, 21.0, 23.0, 24.0, 26.0, 24.0, 24.0, 22.0, 21.0, 15.0, 10.0,
			8.0, 9.0, 11.0, 12.0, 12.0, 13.0, 13.0, 17.0, 17.0, 21.0, 20.0, 19.0, 20.0, 25.0, 20.0, 14.0,
			10.0, 8.0, 9.0, 11.0, 10.0, 9.0, 9.0, 9.0, 10.0, 12.0, 12.0, 13.0, 16.0, 17.0, 19.0, 20.0,
			20.0, 14.0, 11.0, 10.0, 11.0, 11.0, 10.0, 10.0, 10.0, 9.0, 10.0, 9.0, 11.0, 13.0, 17.0, 19.0,
			39.0, 34.0, 23.0, 15.0, 13.0, 12.0, 11.0, 11.0, 11.0, 11.0, 10.0, 9.0, 10.0, 11.0, 11.0, 13.0,
			35.0, 32.0, 28.0, 24.0, 21.0, 16.0, 13.0, 12.0, 11.0, 11.0, 10.0, 10.0, 12.0, 12.0, 11.0, 10.0,
			21.0, 20.0, 17.0, 15.0, 16.0, 18.0, 17.0, 13.0, 13.0, 12.0, 13.0, 12.0, 12.0, 14.0, 14.0, 13.0,
			14.0, 14.0, 15.0, 14.0, 12.0, 14.0, 13.0, 12.0, 12.0, 12.0, 12.0, 13.0, 12.0, 14.0, 15.0, 13.0
		};

		double decimatedDomainVector[] = new double[] {
			46.75, 56.5, 54.75, 54.75, 55.75, 58.25, 42.0, 24.0,
			54.25, 53.25, 49.75, 53.0, 56.25, 57.25, 34.0, 18.25,
			50.75, 47.5, 46.25, 49.0, 51.75, 46.75, 27.25, 12.0,
			36.0, 33.5, 34.25, 36.25, 36.5, 30.75, 19.0, 10.25,
			13.0, 17.25, 17.25, 18.5, 22.0, 21.75, 22.0, 14.75,
			13.0, 10.25, 10.25, 9.5, 10.25, 11.0, 14.25, 18.75,
			35.0, 22.5, 15.5, 11.75, 11.0, 9.75, 11.25, 11.25,
			17.25, 15.25, 15.0, 13.75, 12.25, 12.5, 13.0, 13.75
		};

		RealMatrix domain = new Array2DRowRealMatrix(domainVector.length, 1);
		domain.setColumn(0, domainVector);
		
		RealMatrix expectedDecimatedDomain = new Array2DRowRealMatrix(decimatedDomainVector.length, 1);
		expectedDecimatedDomain.setColumn(0, decimatedDomainVector);

		// Decimate
		SparseRealMatrix decimator = getDecimationOperator(decimatedDomainVector.length, domainVector.length);
		RealMatrix decimatedDomain = decimator.multiply(domain);
		
		// Verify
		assertArrayEquals(expectedDecimatedDomain.getColumn(0), decimatedDomain.getColumn(0), TestUtils.DELTA);
	}
}
