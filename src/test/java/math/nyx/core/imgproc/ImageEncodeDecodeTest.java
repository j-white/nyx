package math.nyx.core.imgproc;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import math.nyx.core.Fractal;
import math.nyx.core.FractalEncoder;
import math.nyx.core.Signal;
import math.nyx.framework.Transform;
import math.nyx.utils.TestUtils;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SerializationUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class ImageEncodeDecodeTest {
	@Autowired
	private FractalEncoder fractalEncoder;

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
	@Ignore
	public void encodeDecodeLargerImage() {
		int size = 256;
		BufferedImage sourceImage = createImage(size);
		
		Signal sourceSignal = new ImageSignal(sourceImage);
		Fractal fractal = fractalEncoder.encode(sourceSignal);

		// Make sure the image gets encoded "perfectly"
		for (Transform transform : fractal.getTransforms()) {
			assertEquals(0, transform.getDistance(), TestUtils.DELTA);
		}

		decodeAndCompare(fractal, size, 1, sourceImage);
	}
	
	@Test
	public void encodeDecodeScaled() {
		int size = 2;
		BufferedImage sourceImage = createImage(size);
		
		// Encode the image as a signal
		Signal sourceSignal = new ImageSignal(sourceImage);
		//System.out.println("Source signal: " + sourceSignal);
		
		// Now encode the signal as a fractal
		Fractal fractal = fractalEncoder.encode(sourceSignal);
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
		Signal expectedSignal = new ImageSignal(expectedImage);
		//System.out.println("Expected signal: " + expectedSignal);
		
		// Now decode the signal from the fractal using the given scale
		// Square the scale, since we are increasing both the width and height of the image
		Signal decodedSignal = fractal.decode(scale*scale);
		//System.out.println("Decoded signal: " + decodedSignal);
		
		for (int k = 0; k < expectedSignal.getDimension(); k++) {
			assertEquals(expectedSignal.getEntry(k), decodedSignal.getEntry(k), TestUtils.DELTA);
		}
	
		// Convert the signal to and image
		ImageMetadata imageMetadata = new ImageMetadata(size * scale, size * scale, expectedImage.getType(), expectedSignal.getNumChannels());
		ImageSignal decodedImageSignal = new ImageSignal(decodedSignal, imageMetadata);
		BufferedImage decodedImage = decodedImageSignal.getImage();

		TestUtils.assertImageEquals(expectedImage, decodedImage);
	}
}
