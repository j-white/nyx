package math.nyx.image;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;
import math.nyx.framework.FractalCodec;
import math.nyx.image.ImageSignal;
import math.nyx.utils.TestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SerializationUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml",
								   "classpath:applicationContext-test.xml"}) 
public class ImageEncodeDecodeTest {
	@Autowired
	@Qualifier("affineSquareCodec")
	private FractalCodec fractalCodec;

	private BufferedImage createImage(int size) {
		if (size % 2 != 0 || size < 2) {
			throw new IllegalArgumentException("Size must be an even positive integer.");
		}
		int width = size / 2;

		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2 = img.createGraphics();
		g2.setColor(Color.RED);
		g2.fillRect(0, 0, width, width);
		
		g2.setColor(Color.BLUE);
		g2.fillRect(width, 0, width, width);
		
		g2.setColor(Color.RED);
		g2.fillRect(0, width, width, width);
		
		g2.setColor(Color.GREEN);
		g2.fillRect(width, width, width, width);
		return img;
	}

	@Test
	public void encodeDecodeScaled() {
		int size = 2;
		BufferedImage sourceImage = createImage(size);
		
		// Encode the image as a signal
		Signal sourceSignal = new ImageSignal(sourceImage);
		//System.out.println("Source signal: " + sourceSignal);
		
		// Now encode the signal as a fractal
		Fractal fractal = fractalCodec.encode(sourceSignal);
		//System.out.println("Fractal: "  + fractal);

		// Serialize and de-serialize the fractal
		byte[] fractalAsBytes = SerializationUtils.serialize(fractal);
		fractal = (Fractal)SerializationUtils.deserialize(fractalAsBytes);

		int scales[] = {1, 2, 3, 4, 5, 6, 7, 8};
		for (int scale : scales) {
			System.out.println("\nTesting with scale: " + scale);
			BufferedImage sourceImageScaled = createImage(size * scale);
			decodeAndCompare(fractal, size, scale, sourceImageScaled);
		}
	}

	private void decodeAndCompare(Fractal fractal, int size, int scale, BufferedImage expectedImage) {
		// Decode the signal from the fractal at the given scale
		ImageSignal decodedSignal = (ImageSignal)fractalCodec.decode(fractal, scale);

		// Create a signal from the expected image
		ImageSignal expectedSignal = new ImageSignal(expectedImage);

		// Compare the expected with the decoded signals
		assertEquals(expectedSignal.getDimension(), decodedSignal.getDimension());
		for (int k = 0; k < expectedSignal.getDimension(); k++) {
			String msg = String.format("Error decoding message at scale %d", scale);
			assertEquals(msg, expectedSignal.getEntry(k), decodedSignal.getEntry(k), TestUtils.DELTA);
		}

		// And compare the expected with the decoded images
		TestUtils.assertImageEquals(expectedImage, decodedSignal.getImage());
	}
}
