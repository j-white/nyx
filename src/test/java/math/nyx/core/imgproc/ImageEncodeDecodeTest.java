package math.nyx.core.imgproc;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import math.nyx.core.Fractal;
import math.nyx.core.FractalEncoder;
import math.nyx.core.Signal;

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
		// Create a new image and fill it with red
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = img.createGraphics();
		g2.setColor(Color.RED);
		g2.fillRect(0, 0, size, size);
		g2.setColor(Color.BLACK);
		g2.drawLine(0, 0, size, 0);
		g2.setColor(Color.YELLOW);
		g2.drawLine(0, size, size, size);
		return img;
	}

	@Test
	public void encodeDecodeScaled() throws IOException {
		int size = 16;
		int scales[] = {1};
		for (int scale : scales) {
			System.out.println("\nTesting with scale: " + scale);
			BufferedImage sourceImage = createImage(size);
			BufferedImage sourceImageScaled = createImage(size * scale);
			testEncodeDecodeScaled(sourceImage, sourceImageScaled, size, scale);
		}
	}

	private void testEncodeDecodeScaled(BufferedImage sourceImage, BufferedImage sourceImageScaled, int size, int scale) {
		// Encode the image as a signal
		Signal sourceSignal = new ImageSignal(sourceImage);

		// Now encode the signal as a fractal
		Fractal fractal = fractalEncoder.encode(sourceSignal);

		// Serialize and de-serialize the fractal
		byte[] fractalAsBytes = SerializationUtils.serialize(fractal);
		fractal = (Fractal)SerializationUtils.deserialize(fractalAsBytes);

		// Now decode the signal from the fractal using the given scale
		// Square the scale, since we are increasing both the width and height of the image
		Signal decodedSignal = fractal.decode(scale*scale);

		// Convert the signal to and image
		ImageMetadata imageMetadata = new ImageMetadata(size * scale, size * scale, BufferedImage.TYPE_4BYTE_ABGR, 4);
		ImageSignal decodedImageSignal = new ImageSignal(decodedSignal, imageMetadata);
		BufferedImage decodedImage = decodedImageSignal.getImage();

		// Compare the decoded image and the scaled image pixel by pixel
		for (int i = 0; i < sourceImageScaled.getWidth(); i++) {
			for (int j = 0; j < sourceImageScaled.getHeight(); j++) {
				assertEquals(String.format("Pixel mismatch at (%d,%d)", i, j),
						sourceImageScaled.getRGB(i, j), decodedImage.getRGB(i, j));
			}
		}
	}
}
