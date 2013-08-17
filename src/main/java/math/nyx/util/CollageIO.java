package math.nyx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import math.nyx.core.Collage;

public class CollageIO {
	public static Collage read(File input) throws IOException {
		return read(new FileInputStream(input));
	}

	public static Collage read(InputStream input) throws IOException {
		Collage collage = null;
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(input);
			collage = (Collage)in.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot de-serialize the collage. Missing class.", e);
		} finally {
			in.close();
		}
		return collage;
	}

	public static boolean write(Collage collage, File output) throws IOException {
		FileOutputStream fos = null;
		ObjectOutput out = null;
		try {
			fos = new FileOutputStream(output);
			out = new ObjectOutputStream(fos);   
			out.writeObject(collage);
		} finally {
		  out.close();
		  fos.close();
		}
		return true;
	}
}
