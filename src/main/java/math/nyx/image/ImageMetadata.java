package math.nyx.image;

import math.nyx.core.SignalMetadata;

import com.google.common.base.Objects;

public class ImageMetadata implements SignalMetadata {
	private static final long serialVersionUID = -6662522469914219075L;
	private final int width;
	private final int height;
	private final int type;
	private final int numComponents;

	public ImageMetadata(int width, int height, int type, int numComponents) {
		this.width = width;
		this.height = height;
		this.type = type;
		this.numComponents = numComponents;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getType() {
		return type;
	}

	public int getNumComponents() {
		return numComponents;
	}

	@Override
	public ImageMetadata scale(int scale) {
		int sqrtOfScale = (int)Math.sqrt(scale);
		return new ImageMetadata(width * sqrtOfScale, height * sqrtOfScale, type, numComponents);
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	    		.add("width", getWidth())
	    		.add("heigth", getHeight())
	    		.add("type", getType())
	    		.add("numComponents", getNumComponents())
	            .toString();
	}
}
