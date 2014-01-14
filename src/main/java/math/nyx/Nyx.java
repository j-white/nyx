package math.nyx;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import math.nyx.core.Fractal;
import math.nyx.core.FractalEncoder;
import math.nyx.core.FractalFactory;
import math.nyx.core.Signal;
import math.nyx.image.ImageSignal;
import math.nyx.utils.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 
public class Nyx {
	@Autowired
	private FractalFactory fractalFactory;
 
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
		final String fileName = "lena-gray.png";
		Fractal fractal = null;

		// Retrieve the signal from the file	
		Signal sourceSignal = null;
		Resource fileAsResource = new ClassPathResource("math/nyx/resources/" + fileName);
		try {
			sourceSignal = new ImageSignal(fileAsResource.getInputStream());
			//sourceSignal = new AudioSignal(fileAsResource.getInputStream());
		} catch (IOException ex) {
			System.err.println("Failed to read signal from disk.");
			ex.printStackTrace();
			System.exit(1);
		}

		// Try and load the fractal from disk before encoding
		try {
			fractal = Utils.loadFractalFromDisk(fileName);
		} catch (IOException pass) {
			// Encode the signal into a fractal
			FractalEncoder fractalEncoder = fractalFactory.getEncoderFor(sourceSignal);
			
			System.out.printf("Encoding signal of dimension %d with %s.\n",
					sourceSignal.getDimension(), fractalEncoder);
			fractal = fractalEncoder.encode(sourceSignal);

			// Serialize the fractal
		    try (
				OutputStream file = new FileOutputStream(String.format("%s-fractal.nyx", fileName));
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);
		    ){
		    	output.writeObject(fractal);
			}
		    catch(IOException ex){
		    	System.err.println("Failed to save fractal to disk.");
		    	ex.printStackTrace();
		    	System.exit(1);
		    }
		}
		
		//System.out.println("Encoded fractal: " + fractal);

	    // Decode at varying scales
	    int scales[] = {1, 2, 3, 4, 5, 6, 7, 8, 16};
	    for (int scale : scales) {
			System.out.printf("\nDecoding at %dx ...\n", scale);
			
			// Decode the fractal to a signal
			// FIXME: Should be not need to change based on the signal type
			Signal decodedSignal = fractal.decode(scale * scale);

			// Write the signal o disk
			File outputfile = new File(String.format("decoded-%dx-%s", scale, fileName));
			try {
				ImageSignal.writeToFile((ImageSignal)sourceSignal, decodedSignal, scale, outputfile);
				//AudioSignal.writeToFile((AudioSignal)sourceSignal, decodedSignal, scale, outputfile);
			} catch (IOException ex) {
				System.out.println("Failed to write the signal to disk.");
				ex.printStackTrace();
				System.exit(1);
			}
	    }
	}
}
