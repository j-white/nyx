package math.nyx.core.imgproc;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.SerializationUtils;

public class ImageSignalTest {
	/**
	 * Test encode/serialize/de-serialize/decode
	 *
	 * @param sourceImage
	 */
	private void testImageSignalConversion(BufferedImage sourceImage) {
		// Encode the image into a signal
		ImageSignal signal = new ImageSignal(sourceImage);
		
		// Verify the number of channels
		assertEquals(signal.getNumChannels(), sourceImage.getColorModel().getNumComponents());

		// Serialize and de-serialize the signal
		byte[] signalAsBytes = SerializationUtils.serialize(signal);
		signal = (ImageSignal)SerializationUtils.deserialize(signalAsBytes);

		// Now decode the image from the signal
		BufferedImage decodedImage = signal.getImage();

		// Verify the image meta-data
		assertEquals(sourceImage.getWidth(), decodedImage.getWidth());
		assertEquals(sourceImage.getHeight(), decodedImage.getHeight());
		assertEquals(sourceImage.getTransparency(), decodedImage.getTransparency());
		assertEquals(sourceImage.getType(), decodedImage.getType());

		// Now compare the images pixel by pixel
		for (int i = 0; i < sourceImage.getWidth(); i++) {
			for (int j = 0; j < sourceImage.getHeight(); j++) {
				assertEquals(String.format("Pixel mismatch at (%d,%d)", i, j),
						sourceImage.getRGB(i, j), decodedImage.getRGB(i, j));
			}
		}
	}

	@Test
	public void test4ByteABGRImage() {
		int w = 32;
		int h = 16;

		// Create a new image and fill it with red
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2 = img.createGraphics();
		g2.setColor(Color.RED);
		g2.fillRect(0, 0, w, h);

		testImageSignalConversion(img);
	}

	@Test
	public void testIntARGBImage() {
		int w = 16;
		int h = 3;

		// Create a new image and fill it with blue
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		g2.setColor(Color.BLUE);
		g2.fillRect(0, 0, w, h);

		testImageSignalConversion(img);
	}

	@Test
	public void testUshortGrayImage() {
		int w = 1;
		int h = 1;

		// Create a new image and fill it with gray
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY);
		Graphics2D g2 = img.createGraphics();
		g2.setColor(Color.GRAY);
		g2.fillRect(0, 0, w, h);

		testImageSignalConversion(img);
	}

	@Test
	public void testPNG() throws IOException {
		Resource imgFile = new ClassPathResource("math/nyx/resources/ocean.png");
		BufferedImage img = ImageIO.read(imgFile.getInputStream());
		testImageSignalConversion(img);
	}

	@Test
	public void testJPG() throws IOException {
		Resource imgFile = new ClassPathResource("math/nyx/resources/lena_128.jpg");
		BufferedImage img = ImageIO.read(imgFile.getInputStream());

		testImageSignalConversion(img);
	}
}
