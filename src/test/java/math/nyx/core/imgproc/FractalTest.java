package math.nyx.core.imgproc;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import math.nyx.core.Fractal;
import math.nyx.core.FractalCodec;
import math.nyx.core.Signal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.SerializationUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class FractalTest {
	@Autowired
	private FractalCodec fractalCodec;

	private BufferedImage createImage(int size) {
		// Create a new image and fill it with red
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = img.createGraphics();
		g2.setColor(Color.RED);
		g2.fillRect(0, 0, size, size);
		return img;
	}

	@Test
	public void doTest() throws IOException {
		int size = 16;
		int scale = 2;

		BufferedImage sourceImage = createImage(size);
		BufferedImage sourceImageScaled = createImage(size * scale);

		// Encode the image as a signal
		Signal sourceSignal = new ImageSignal(sourceImage);

		// Now encode the signal as a fractal
		Fractal fractal = fractalCodec.encode(sourceSignal);

		// Serialize and de-serialize the fractal
		byte[] fractalAsBytes = SerializationUtils.serialize(fractal);
		fractal = (Fractal)SerializationUtils.deserialize(fractalAsBytes);

		// Now decode the signal from the fractal using the given scale
		Signal decodedSignal = fractalCodec.decode(fractal, scale);

		// Convert the signal to and image
		ImageMetadata imageMetadata = new ImageMetadata(size * scale, size * scale, BufferedImage.TYPE_4BYTE_ABGR);
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
