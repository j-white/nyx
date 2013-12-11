package math.nyx.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import math.nyx.core.imgproc.Image;

public class ImageIO {
	public static Image read(InputStream input) throws IOException {
		return new Image(javax.imageio.ImageIO.read(input));		
	}
	
	public static boolean write(Image im, String formatName, File output) throws IOException {
		return javax.imageio.ImageIO.write(im, formatName, output);
	}
}
