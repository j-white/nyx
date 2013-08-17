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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import math.nyx.core.Collage;
import math.nyx.core.ContractiveMap;
import math.nyx.core.Image;
import math.nyx.core.Matrix;
import math.nyx.core.Partition;
import math.nyx.core.Point2D;
import math.nyx.core.RMSMetric;
import math.nyx.fie.FractalImageEncodingStrategy;
import math.nyx.fie.simple.SimpleEncodingStrategy;
import math.nyx.util.CollageIO;
import math.nyx.util.ImageIO;

public class NyxUI extends JFrame {
	private static final long serialVersionUID = 5018791893607216653L;
	private static Log LOG = LogFactory.getLog(NyxUI.class);
	
	public NyxUI() {
		super("Nyx: Fractal Image Encoding");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Collage collage = null;
		try {
			collage = CollageIO.read(new File("lena.nyx"));
		} catch (IOException e) {
			LOG.error(String.format("Failed to open input file: %s. Exiting.", e));
			return;
		}

		FractalImageEncodingStrategy fic = new SimpleEncodingStrategy();
		Matrix collageDecoded = fic.decode(collage, 512, 512, 20);

		Image img = null;
		try {
			Resource imgFile = new ClassPathResource("math/nyx/resources/lena_256.jpg");
			img = ImageIO.read(imgFile.getInputStream());
		} catch (IOException e) {
			LOG.error(String.format("Failed to open image file: %s. Exiting.", e));
			return;
		}

		LOG.info("Converting to grayscale...");
		img = img.toGrayscale();
		
		RMSMetric rmsMetric = new RMSMetric();
		System.out.println("RMS to original image is: "
				+ rmsMetric.getDistance(new Matrix(img), collageDecoded));

		InteractiveTransform interactiveTransform = new InteractiveTransform(collageDecoded, collage);
		getContentPane().add(interactiveTransform);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args) {
		new NyxUI();
	}
}

class VisualMap {
	ContractiveMap map;
	Shape rangeShape;
	Shape domainShape;
	Partition rangePartition;
	Partition domainPartition;
	Image rangeImage;
	Image domainImage;

	public VisualMap(ContractiveMap map, Matrix decodedMatrix) {
		this.map = map;
		this.rangePartition = map.getRange();
		this.domainPartition = map.getDomain();
		
		this.rangeShape = shapeFromPartition(rangePartition);
		this.domainShape = shapeFromPartition(domainPartition);
		
		this.rangePartition.setMatrix(decodedMatrix);
		this.domainPartition.setMatrix(decodedMatrix);
		this.rangeImage = new Image(rangePartition.getMatrix());
		this.domainImage = new Image(domainPartition.getMatrix());
	}

	private Shape shapeFromPartition(Partition partition) {
	   List<Point2D> hull = partition.getConvexHull();
	   GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, hull.size());
	   Point2D firstPoint = hull.get(0);
	   polyline.moveTo (firstPoint.x, firstPoint.y);
	   for (Point2D point : hull) {
		   polyline.lineTo(point.x, point.y);
	   }
	   polyline.lineTo(firstPoint.x, firstPoint.y);
	   return polyline;
	}
}

class InteractiveTransform extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 5381153364389986802L;
	private Matrix decodedMatrix;
	private Image image;
	private Collage transform;
	private List<VisualMap> visualMaps = new ArrayList<VisualMap>();
	private VisualMap selectedVisualMap = null;
	private boolean selectionLocked = false;

	public InteractiveTransform(Matrix matrix, Collage transform) {
		this.decodedMatrix = matrix;
		this.image = new Image(decodedMatrix);
		setTransform(transform);
	    addMouseListener(this);
	    addMouseMotionListener(this);
	}

	public void setTransform(Collage t) {
		this.transform = t;
		List<ContractiveMap> maps = transform.getMaps();
		for (ContractiveMap map : maps) {
			visualMaps.add(new VisualMap(map, decodedMatrix));
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(512, 512);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(image, 0, 0, null);
	
		if (selectedVisualMap != null) {
			g2.setColor(Color.BLUE);
			g2.draw(selectedVisualMap.rangeShape);
			
			g2.setColor(Color.RED);
			g2.draw(selectedVisualMap.domainShape);

			g2.drawImage(selectedVisualMap.rangeImage, 300, 25, null);
			g2.drawImage(selectedVisualMap.domainImage, 300, 75, null);
		}
	}

	private void dumpInfoToStdout() {
		if (selectedVisualMap != null) {
			System.out.println("Range: \n" + selectedVisualMap.rangePartition + "\n");
			System.out.println("Domain: \n" + selectedVisualMap.domainPartition + "\n");
			System.out.println("Map: \n" + selectedVisualMap.map + "\n");
			System.out.println("Range hull: \n" + selectedVisualMap.rangePartition.getConvexHull() + "\n");
			System.out.println("Domain hull: \n" + selectedVisualMap.domainPartition.getConvexHull() + "\n");
		}
	}

	public void mouseClicked(MouseEvent e) {
		selectionLocked = !selectionLocked;
		
		if (selectionLocked) {
			dumpInfoToStdout();
		}
	}

	public void setSelectedVisualMap(VisualMap visualMap) {
		selectedVisualMap = visualMap;
		invalidate();
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		if (selectionLocked) {
			return;
		}

		Point mouseMovedAt = e.getPoint();
		VisualMap matchedVisualMap = null;
		for (VisualMap visualMap : visualMaps) {
			if (visualMap.rangeShape.contains(mouseMovedAt)) {
				matchedVisualMap = visualMap;
				break;
			}
		}

		if (selectedVisualMap != matchedVisualMap) {
			setSelectedVisualMap(matchedVisualMap);
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
