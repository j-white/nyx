package math.nyx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import math.nyx.core.Collage;
import math.nyx.core.Image;
import math.nyx.core.Matrix;
import math.nyx.fie.FractalImageEncodingStrategy;
import math.nyx.fie.MatrixDecodingVisitor;
import math.nyx.fie.simple.SimpleEncodingStrategy;
import math.nyx.util.AnimatedGifEncoder;
import math.nyx.util.CollageIO;
import math.nyx.util.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class NyxConsole {
	private static Log LOG = LogFactory.getLog(NyxConsole.class);
	private static FractalImageEncodingStrategy fic = new SimpleEncodingStrategy();

	private static List<Matrix> decodingIterations = new ArrayList<Matrix>();

	public static void main(String[] args) {
		int numberOfIterations = 20;
		/*
		Image img = null;
		try {
			Resource imgFile = new ClassPathResource("math/nyx/resources/ocean.png");
			img = ImageIO.read(imgFile.getInputStream());
		} catch (IOException e) {
			LOG.error(String.format("Failed to open image file: %s. Exiting.", e));
			return;
		}

		LOG.info("Converting to grayscale...");
		img = img.toGrayscale();
    	
		try {
			File outputfile = new File("lena_original.png");
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			LOG.error(String.format("Failed to save image file: %s. Exiting.", e));
			return;
		}

		LOG.info("Encoding...");
		Matrix m = new Matrix(img);
		Collage collage = fic.encode(m);

		LOG.info("Exporting collage...");
		try {
			File outputfile = new File("lena.nyx");
			CollageIO.write(collage, outputfile);
		} catch (IOException e) {
			LOG.error(String.format("Failed to save collage: %s. Exiting.", e));
			return;
		}*/

		LOG.info("Re-importing collage...");
		Collage collage = null;
		try {
			collage = CollageIO.read(new File("lena.nyx"));
		} catch (IOException e) {
			LOG.error(String.format("Failed to open input file: %s. Exiting.", e));
			return;
		}

		/*LOG.info("Decoding...");
		fic.decode(collage, 512, 512, numberOfIterations, new MatrixDecodingVisitor() {
			public void decodedIteration(Matrix matrix, int currentIteration,
					int totalIterations) {
				decodingIterations.add(matrix);
			}
		});

		LOG.info("Exporting to GIF...");
		AnimatedGifEncoder gif = new AnimatedGifEncoder();
		gif.start("decoding_lena.gif");
		gif.setDelay(250);   // 4 frame per sec

		int i = 0;
		for (Matrix mm : decodingIterations) {
			Image imgDec = new Image(mm);
			
		    try {
		    	File outputfile = new File("decoding_lena_" + i++ + ".png");
				ImageIO.write(imgDec, "png", outputfile);
			} catch (IOException e) {
				LOG.error(String.format("Failed to save image file: %s. Exiting.", e));
				return;
			}

		    gif.addFrame(imgDec);
		}
		gif.finish();
		*/

		Matrix mm = fic.decode(collage, 512, 512, numberOfIterations);
		Image imgDec = new Image(mm);
	    try {
	    	File outputfile = new File("decoding_lena.png");
			ImageIO.write(imgDec, "png", outputfile);
		} catch (IOException e) {
			LOG.error(String.format("Failed to save image file: %s. Exiting.", e));
			return;
		}
		
		LOG.info("Done. Exiting.");
	}
}
