package math.nyx.core.imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;

import math.nyx.core.Signal;
import math.nyx.core.Vector;

/**
 * Representation of an image as a vector.
 * 
 * Provides routines for converting to and from this representation.
 *
 * @author jwhite
 */
public class ImageSignal implements Signal {
	private static final long serialVersionUID = -1875089091108123392L;
	private final Vector v;
	private final ImageMetadata metadata;

	public ImageSignal(BufferedImage img) {
		this(bufferedImageToVector(img), new ImageMetadata(img.getWidth(), img.getHeight(), img.getType()));
	}

	public ImageSignal(Signal s, ImageMetadata metadata) {
		this(s.getVector(), metadata);
	}

	public ImageSignal(Vector v, ImageMetadata metadata) {
		this.v = v;
		this.metadata = metadata;
	}

	public Vector getVector() {
		return v;
	}

	public ImageMetadata getMetadata() {
		return metadata;
	}

	public long getDimension() {
		return v.getDimension();
	}

	public BufferedImage getImage() {
		BufferedImage img = new BufferedImage(metadata.getWidth(), metadata.getHeight(), metadata.getType());
		DataBuffer dataBuffer = img.getRaster().getDataBuffer();
		double[] data = v.getData();

		switch(dataBuffer.getDataType()) {
		case DataBuffer.TYPE_BYTE:
			final byte[] bytePixels = ((DataBufferByte)dataBuffer).getData();
			for (int i = 0; i < bytePixels.length; i++) {
				bytePixels[i] = (byte)data[i];
			}
			break;
		case DataBuffer.TYPE_USHORT:
			final short[] shortPixels = ((DataBufferUShort)dataBuffer).getData();
			for (int i = 0; i < shortPixels.length; i++) {
				shortPixels[i] = (short)data[i];
			}
			break;
		case DataBuffer.TYPE_INT:
			final int[] intPixels = ((DataBufferInt)dataBuffer).getData();
			for (int i = 0; i < intPixels.length; i++) {
				intPixels[i] = (int)data[i];
			}
			break;
		default:
			throw new RuntimeException("Unsupported image buffer type " + dataBuffer.getDataType());
		}

		return img;
	}

	private static Vector bufferedImageToVector(BufferedImage img)  {
		DataBuffer dataBuffer = img.getRaster().getDataBuffer();
		final int numBanks = dataBuffer.getNumBanks();
		final int bufferSize = dataBuffer.getSize();
		Vector v = new Vector(numBanks * bufferSize);

		int k = 0;
		switch(dataBuffer.getDataType()) {
		case DataBuffer.TYPE_BYTE:
			for (int i = 0; i < numBanks; i++) {
				final byte[] pixels = ((DataBufferByte)dataBuffer).getData(i);
				for (int j = 0; j < bufferSize; j++) {
					v.setEntry(k++, pixels[j]);
				}
			}
			break;
		case DataBuffer.TYPE_USHORT:
			for (int i = 0; i < numBanks; i++) {
				final short[] pixels = ((DataBufferUShort)dataBuffer).getData(i);
				for (int j = 0; j < bufferSize; j++) {
					v.setEntry(k++, pixels[j]);
				}
			}
			break;
		case DataBuffer.TYPE_INT:
			for (int i = 0; i < numBanks; i++) {
				final int[] pixels = ((DataBufferInt)dataBuffer).getData(i);
				for (int j = 0; j < bufferSize; j++) {
					v.setEntry(k++, pixels[j]);
				}
			}
			break;
		default:
			throw new RuntimeException("Unsupported image buffer type " + dataBuffer.getDataType());
		}

		return v;
	}
}
