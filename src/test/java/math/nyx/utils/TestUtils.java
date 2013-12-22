package math.nyx.utils;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

public class TestUtils {
	public static final double DELTA = 0.00001;

	public static RealMatrix generateSignal(int signalDimension) {
		// Generate a signal with with x[0] = 1 and x[k] = x[k-1] + 1
		RealMatrix signal = new Array2DRowRealMatrix(signalDimension, 1);
		for (int i = 0; i < signalDimension; i++) {
			signal.setEntry(i, 0, i+1);
		}
		return signal;
	}

	public static void assertImageEquals(BufferedImage expected, BufferedImage actual) {
		// Compare the images pixel by pixel
		for (int i = 0; i < expected.getWidth(); i++) {
			for (int j = 0; j < expected.getHeight(); j++) {
				int expectedPixel = expected.getRGB(i, j);
				int actualPixel = actual.getRGB(i, j);
				
				if (expectedPixel != actualPixel) {
					try {
						File expectedFile = new File("expected.png");
						ImageIO.write(expected, "png", expectedFile);
						
						File actualFile = new File("actual.png");
						ImageIO.write(actual, "png", actualFile);
						
						System.out.println("Images succesfully dumped for analysis.");
					} catch(IOException e) {
						System.out.println("Error occured dumping images.");
					}
				}

				assertEquals(String.format("Pixel mismatch at (%d,%d)", i, j),
						expectedPixel, actualPixel);
			}
		}
	}
}
