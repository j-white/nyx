package math.nyx.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import math.nyx.core.Fractal;

public class Utils {
	public static Fractal loadFractalFromDisk(String imageName) throws IOException {
	    // De-serialize the fractal
	    try(
	    	InputStream file = new FileInputStream(String.format("%s-fractal.nyx", imageName));
	    	InputStream buffer = new BufferedInputStream(file);
	    	ObjectInput input = new ObjectInputStream (buffer);
	    ){
	    	return (Fractal)input.readObject();
	    }
	    catch(ClassNotFoundException ex) {
	    	System.out.println("Cannot perform input. Class not found: " + ex);
	    	ex.printStackTrace();
	    	throw new IOException(ex);
	    }
	}
}
