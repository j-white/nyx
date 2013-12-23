package math.nyx.image;

import java.io.Serializable;

public class ImageMetadata implements Serializable {
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
}
