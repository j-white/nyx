package math.nyx.core.imgproc;

import java.io.Serializable;

public class ImageMetadata implements Serializable {
	private static final long serialVersionUID = -6662522469914219075L;
	private final int width;
	private final int height;
	private final int type;

	public ImageMetadata(int width, int height, int type) {
		this.width = width;
		this.height = height;
		this.type = type;
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
}
