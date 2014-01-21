package math.nyx.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;

/**
 * Representation of an image as a vector.
 * 
 * Provides routines for converting to and from this representation.
 *
 * @author jwhite
 */
public class ImageSignal extends Signal {
	private static final long serialVersionUID = -1875089091108123392L;
	private static Logger logger = LogManager.getLogger("Nyx");

	public static final String TYPE = "Image";

	private final ImageMetadata metadata;

	public static final double MIN_VAL = 0;

	private double minVal = MIN_VAL;

	public static final double MAX_VAL = 255;

	private double maxVal = MAX_VAL;

	public ImageSignal(Fractal fractal, RealMatrix decodedVector, ImageMetadata metadata) {
		super(fractal, decodedVector);
		this.metadata = metadata;
	}

	public ImageSignal(BufferedImage img) {
		this(bufferedImageToVector(img), new ImageMetadata(img.getWidth(), img.getHeight(),
														   img.getType(), img.getColorModel().getNumComponents()));
	}

	public ImageSignal(int size) {
		this(new Array2DRowRealMatrix(size, 1));
	}

	public ImageSignal(RealMatrix v) {
		super(v);
		metadata = new ImageMetadata(1, v.getRowDimension(), BufferedImage.TYPE_BYTE_GRAY, 1);
	}

	public ImageSignal(Signal s, ImageMetadata metadata) {
		super(s.getVector());
		this.metadata = metadata;
	}

	public ImageSignal(RealMatrix v, ImageMetadata metadata) {
		super(v);
		this.metadata = metadata;
	}

	public ImageSignal(InputStream is) throws IOException {
		this(ImageIO.read(is));
	}

	public String getType() {
		return TYPE;
	}

	@Override
	public int getNumChannels() {
		return metadata.getNumComponents();
	}

	public ImageMetadata getMetadata() {
		return metadata;
	}

	public BufferedImage getImage() {
		BufferedImage img = metadata.getBufferedImage();
		DataBuffer dataBuffer = img.getRaster().getDataBuffer();
		double[] data = getVector().getColumn(0);

		int numChannels = getNumChannels();
		int numEntriesPerChannel = Math.round((float)dataBuffer.getSize() / numChannels);
		logger.debug("Converting {}, with {} entries per channel to buffered image.", this, numEntriesPerChannel);

		switch(dataBuffer.getDataType()) {
		case DataBuffer.TYPE_BYTE:
			{
				final byte[] pixels = ((DataBufferByte)dataBuffer).getData();
				for (int i = 0; i < numChannels; i++) {
					for (int j = 0; j < numEntriesPerChannel; j++) {
						int u = numEntriesPerChannel*i + j;
						int v = i + j*numChannels;
						pixels[v] = (byte)data[u];
					}
				}
			}
			break;
		case DataBuffer.TYPE_USHORT:
			{
				final short[] pixels = ((DataBufferUShort)dataBuffer).getData();
				for (int i = 0; i < pixels.length; i++) {
					pixels[i] = (short)data[i];
				}
			}
			break;
		case DataBuffer.TYPE_INT:
			{
				final int[] pixels = ((DataBufferInt)dataBuffer).getData();
				for (int i = 0; i < pixels.length; i++) {
					pixels[i] = (int)data[i];
				}
			}
			break;
		default:
			throw new RuntimeException("Unsupported image buffer type " + dataBuffer.getDataType());
		}

		return img;
	}

	private static RealMatrix bufferedImageToVector(BufferedImage img)  {
		DataBuffer dataBuffer = img.getRaster().getDataBuffer();
		final int numChannels = img.getColorModel().getNumComponents();
		final int bufferSize = dataBuffer.getSize();
		RealMatrix x = new Array2DRowRealMatrix(bufferSize, 1);

		int k = 0;
		switch(dataBuffer.getDataType()) {
		case DataBuffer.TYPE_BYTE:
			{
				final byte[] pixels = ((DataBufferByte)dataBuffer).getData();
				for (int i = 0; i < numChannels; i++) {
					for (int j = i; j < pixels.length; j += numChannels) {
						int u = j;
						int v = k++;
						//System.out.println("u: " + u + " v: " + v);
						x.setEntry(v, 0, pixels[u] & 0xFF);
					}
				}
			}
			break;
		case DataBuffer.TYPE_USHORT:
			{
				final short[] pixels = ((DataBufferUShort)dataBuffer).getData();
				for (int j = 0; j < bufferSize; j++) {
					x.setEntry(k++, 0, pixels[j]);
				}
			}
			break;
			case DataBuffer.TYPE_INT:
			{
				final int[] pixels = ((DataBufferInt)dataBuffer).getData();
				for (int j = 0; j < bufferSize; j++) {
					x.setEntry(k++, 0, pixels[j]);
				}
			}
			break;
		default:
			throw new RuntimeException("Unsupported image buffer type " + dataBuffer.getDataType());
		}
		
		return x;
	}

	@Override
	public void write(File file) throws IOException {
		ImageIO.write(getImage(), "png", file);
	}

	public void setMinVal(double minVal) {
		this.minVal = minVal;
	}

	@Override
	public double getMinVal() {
		return minVal;
	}

	public void setMaxVal(double maxVal) {
		this.maxVal = maxVal;
	}

	@Override
	public double getMaxVal() {
		return maxVal;
	}

	@Override
	public int getScaledDimension(int scale) {
		return getDimension() * scale * scale;
	}
}
