package math.nyx;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import math.nyx.core.Fractal;
import math.nyx.core.FractalEncoder;
import math.nyx.core.Signal;
import math.nyx.core.imgproc.ImageMetadata;
import math.nyx.core.imgproc.ImageSignal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 
public class Nyx {
	@Autowired
	private FractalEncoder fractalEncoder;
 
	private ApplicationContext context;

	public static void main(String[] args) {
		Nyx nyx = new Nyx();
		nyx.main();
	}

	public Nyx() {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		AutowireCapableBeanFactory nyxFactory = context.getAutowireCapableBeanFactory();
		nyxFactory.autowireBean(this);
	}
 
	private void main() {
		Fractal fractal = null;

    	// Read the image from disk
		Resource imageFile = new ClassPathResource("math/nyx/resources/tiny2x.png");
		BufferedImage sourceImage = null;
		try {
			sourceImage = ImageIO.read(imageFile.getInputStream());
		} catch (IOException ex) {
			System.out.println("Failed to read image from disk: " + ex);
			ex.printStackTrace();
			System.exit(1);
		}

		// Encode the image into a signal
		ImageSignal sourceSignal = new ImageSignal(sourceImage);
		System.out.println(sourceSignal);

		// Encode the signal into a fractal
		System.out.printf("Encoding signal with dimension %d.\n", sourceSignal.getDimension());
		fractal = fractalEncoder.encode(sourceSignal);

		// Serialize the fractal
	    try (
			OutputStream file = new FileOutputStream("fractal.nyx");
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
	    ){
	    	output.writeObject(fractal);
		}
	    catch(IOException ex){
	    	System.out.println("Cannot perform output: " + ex);
	    	ex.printStackTrace();
	    }
		
	    // De-serialize the fractal
	    try(
	    	InputStream file = new FileInputStream("fractal.nyx");
	    	InputStream buffer = new BufferedInputStream(file);
	    	ObjectInput input = new ObjectInputStream (buffer);
	    ){
	    	fractal = (Fractal)input.readObject();
	    }
	    catch(ClassNotFoundException ex){
	    	System.out.println("Cannot perform input. Class not found: " + ex);
	    	ex.printStackTrace();
	    }
	    catch(IOException ex){
	    	System.out.println("Cannot perform input: " + ex);
	    	ex.printStackTrace();
	    }
	    
	    // Decode at varying scales
	    int scales[] = {2};
	    for (int scale : scales) {
			System.out.printf("Decoding at %dx...\n", scale);
			
			// Decode the fractal into a signal
			Signal decodedSignal = fractal.decode(scale * scale);
			System.out.println(decodedSignal);

			// Decode the signal into an image
			ImageMetadata originalImageMetadata = sourceSignal.getMetadata();
			int originalWidth = originalImageMetadata.getWidth();
			int originalHeight = originalImageMetadata.getHeight();
			int originalType = originalImageMetadata.getType();
			int orginalNumComponents = originalImageMetadata.getNumComponents();

			ImageMetadata imageMetadata = new ImageMetadata(originalWidth * scale, originalHeight * scale,
															originalType, orginalNumComponents);
			ImageSignal decodedImageSignal = new ImageSignal(decodedSignal, imageMetadata);
			File outputfile = new File(String.format("decoded-%dx.png", scale));
			try {
				ImageIO.write(decodedImageSignal.getImage(), "png", outputfile);
			} catch (IOException ex) {
				System.out.println("Failed to write the image to disk: " + ex);
				ex.printStackTrace();
			}
	    }
	}
}
