package math.nyx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;
import math.nyx.core.Transform;
import math.nyx.image.ImageMetadata;
import math.nyx.image.ImageSignal;
import math.nyx.utils.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NyxInteractive extends JFrame {
	private static final long serialVersionUID = 5018791893607216653L;
	private static Log LOG = LogFactory.getLog(NyxInteractive.class);
	
	public NyxInteractive() {
		super("Nyx: Fractal Image Encoding");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final String imageName = "lena-gray.png";
		Fractal fractal = null;
		try {
			fractal = Utils.loadFractalFromDisk(imageName);
		} catch (IOException ex) {
			LOG.error(String.format("Failed to open input file: %s. Exiting.", ex));
			return;
		}

		// Decode
		Signal decodedSignal = fractal.decode();
		
		// Assume the signal is a square gray-scale image
		int imageWidth = (int)Math.sqrt(decodedSignal.getDimension());
		ImageMetadata imageMetadata = new ImageMetadata(imageWidth, imageWidth, BufferedImage.TYPE_BYTE_GRAY, 1);
		ImageSignal decodedImageSignal = new ImageSignal(decodedSignal, imageMetadata);

		InteractiveFractal interactiveTransform = new InteractiveFractal(decodedImageSignal, fractal);
		getContentPane().add(interactiveTransform);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args) {
		new NyxInteractive();
	}
}


class VisualTransform {
	final Transform transform;
	final Signal signal;
	final Shape rangeShape;
	final Shape domainShape;

	public VisualTransform(Transform transform, Signal signal) {
		this.transform = transform;
		this.signal = signal;
		this.rangeShape = getShapeForRangeBlock(transform.getRangeBlockIndex());
		this.domainShape = getShapeForDomainBlock(transform.getDomainBlockIndex());
	}

	private Shape getShapeForRangeBlock(int rangeBlockIndex) {
		GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 0);
		return polyline;
	}
	
	private Shape getShapeForDomainBlock(int domainBlockIndex) {
		GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 0);
		/*
		 * 	   Point2D firstPoint = hull.get(0);
	   polyline.moveTo (firstPoint.x, firstPoint.y);
	   for (Point2D point : hull) {
		   polyline.lineTo(point.x, point.y);
	   }
	   polyline.lineTo(firstPoint.x, firstPoint.y);
		 */
		return polyline;
	}
}


class InteractiveFractal extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 5381153364389986802L;
	private final ImageSignal signal;
	private final BufferedImage image;
	private List<VisualTransform> visualTransforms = new ArrayList<VisualTransform>();
	private VisualTransform selectedVisualTransform = null;
	private boolean selectionLocked = false;

	public InteractiveFractal(ImageSignal signal, Fractal fractal) {
		this.signal = signal;
		this.image = signal.getImage();
		setFractal(fractal);
	    addMouseListener(this);
	    addMouseMotionListener(this);
	}

	private void setFractal(Fractal fractal) {
		visualTransforms.clear();
		for (Transform transform : fractal.getTransforms()) {
			visualTransforms.add(new VisualTransform(transform, signal));
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(image.getWidth(), image.getHeight());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(image, 0, 0, null);
	

		if (selectedVisualTransform != null) {
			g2.setColor(Color.BLUE);
			g2.draw(selectedVisualTransform.rangeShape);
			
			g2.setColor(Color.RED);
			g2.draw(selectedVisualTransform.domainShape);
		}
	}

	private void dumpInfoToStdout() {
		if (selectedVisualTransform != null) {
			/*
			System.out.println("Range: \n" + selectedVisualMap.rangePartition + "\n");
			System.out.println("Domain: \n" + selectedVisualMap.domainPartition + "\n");
			System.out.println("Map: \n" + selectedVisualMap.map + "\n");
			System.out.println("Range hull: \n" + selectedVisualMap.rangePartition.getConvexHull() + "\n");
			System.out.println("Domain hull: \n" + selectedVisualMap.domainPartition.getConvexHull() + "\n");
			*/
		}
	}

	public void mouseClicked(MouseEvent e) {
		selectionLocked = !selectionLocked;
		
		if (selectionLocked) {
			dumpInfoToStdout();
		}
	}

	public void setSelectedVisualTransform(VisualTransform visualTransform) {
		selectedVisualTransform = visualTransform;
		invalidate();
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		if (selectionLocked) {
			return;
		}

		Point mouseMovedAt = e.getPoint();
		VisualTransform matchedVisualTransform = null;
		for (VisualTransform visualTransform : visualTransforms) {
			if (visualTransform.rangeShape.contains(mouseMovedAt)) {
				matchedVisualTransform = visualTransform;
				break;
			}
		}

		if (selectedVisualTransform != matchedVisualTransform) {
			setSelectedVisualTransform(matchedVisualTransform);
		}
	}

	public void mouseDragged(MouseEvent e) {
		// This method is intentionally left blank
	}

	public void mousePressed(MouseEvent e) {
		// This method is intentionally left blank
	}

	public void mouseReleased(MouseEvent e) {
		// This method is intentionally left blank
	}

	public void mouseEntered(MouseEvent e) {
		// This method is intentionally left blank
	}

	public void mouseExited(MouseEvent e) {
		// This method is intentionally left blank
	}
}

