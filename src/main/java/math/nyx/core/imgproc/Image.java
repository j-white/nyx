package math.nyx.core.imgproc;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;

import math.nyx.core.Matrix;

public class Image extends BufferedImage {
	public Image(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	public Image(BufferedImage img) {
		super(img.getWidth(), img.getHeight(), img.getType());
		setData(img.getData());
	}

	public Image(Matrix m) {
		super(m.getColumnDimension(), m.getRowDimension(), BufferedImage.TYPE_BYTE_GRAY);

		final byte[] pixels = ((DataBufferByte)getRaster().getDataBuffer()).getData();
		double[][] data = m.getData();
		final int width = getWidth();

		for (int i = 0, row = 0, col = 0; i < pixels.length; i++) {
			pixels[i] = (byte)data[row][col];
			col++;
			if (col == width) {
				col = 0;
				row++;
			}
		}
	}

	public Image toGrayscale() {
		if (this.getType() == BufferedImage.TYPE_BYTE_GRAY) {
			return this;
		}

		Image gray = new Image(getWidth(), getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        ColorConvertOp op = new ColorConvertOp(getColorModel().getColorSpace(),
        		gray.getColorModel().getColorSpace(), null);
        op.filter(this, gray);
        return gray;
	}

	public Dimension getDimension() {
		return new Dimension(getWidth(), getHeight());
	}
}
