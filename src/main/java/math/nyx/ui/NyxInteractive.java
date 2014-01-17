package math.nyx.ui;

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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import math.nyx.core.Fractal;
import math.nyx.core.Signal;
import math.nyx.core.Transform;
import math.nyx.framework.PartitioningStrategy;
import math.nyx.framework.square.SquarePartitioningStrategy;
import math.nyx.image.ImageMetadata;
import math.nyx.image.ImageSignal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SparseRealMatrix;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class NyxInteractive extends JFrame {
	private static final long serialVersionUID = 5018791893607216653L;
	private static Log LOG = LogFactory.getLog(NyxInteractive.class);

	private ApplicationContext context;
/*
	@Autowired
	@Qualifier("imageCodec")
	FractalCodec imageCodec;

	private Fractal getFractal(double t) {
		int signalDimension = 4*4*4;
		int numSignalChannels = 1;
		
		Fractal fractal = new Fractal();
		fractal.setSignalDimension(signalDimension);
		fractal.setCodecName(imageCodec.getName());
		
		PartitioningStrategy partitioner = imageCodec.getPartitioningStrategy().getPartitioner(signalDimension, numSignalChannels, 1);
		
		int D = partitioner.getNumDomainPartitions();
		int R = partitioner.getNumRangePartitions();
		System.out.println("Num domains: " + D);
		System.out.println("Num ranges: " + R);

		int xx = 1, yy = 4, zz = 7;
		
		// Four corners
		fractal.addTransform(new AffineTransform(xx, 0, 0.0, 0.5, 30, Symmetry.ORIGINAL));
		fractal.addTransform(new AffineTransform(xx, 3, 0.0, 0.5, 30, Symmetry.ORIGINAL));
		fractal.addTransform(new AffineTransform(xx, 12, 0.0, 0.5, 30, Symmetry.ORIGINAL));
		fractal.addTransform(new AffineTransform(xx, 15, 0.0, 0.5, 30, Symmetry.ORIGINAL));

		// Middle blocks
		fractal.addTransform(new AffineTransform(yy, 5, 0.0, 0.4, 25, Symmetry.FLIP));
		fractal.addTransform(new AffineTransform(yy+1, 10, 0.0, 0.4, 25, Symmetry.FLIP));
		fractal.addTransform(new AffineTransform(yy+2, 6, 0.0, 0.4, 25, Symmetry.FLIP));
		fractal.addTransform(new AffineTransform(yy+3, 9, 0.0, 0.4, 25, Symmetry.FLIP));

		// Top
		fractal.addTransform(new AffineTransform(zz, 1, 0.0, 0.5, 30, Symmetry.ROTATE_90_FLIP));
		fractal.addTransform(new AffineTransform(zz, 2, 0.0, 0.5, 30, Symmetry.ROTATE_90_FLIP));
		
		// Left
		fractal.addTransform(new AffineTransform(zz, 4, 0.0, 0.5, 30, Symmetry.ROTATE_180));
		fractal.addTransform(new AffineTransform(zz, 8, 0.0, 0.5, 30, Symmetry.ROTATE_180));
		
		// Right
		fractal.addTransform(new AffineTransform(zz, 7, 0.0, 0.5, 30, Symmetry.ORIGINAL));
		fractal.addTransform(new AffineTransform(zz, 11, 0.0, 0.5, 30, Symmetry.ORIGINAL));
		
		// Bottom
		fractal.addTransform(new AffineTransform(zz, 13, 0.0, 0.5, 30, Symmetry.ORIGINAL));
		fractal.addTransform(new AffineTransform(zz, 14, 0.0, 0.5, 30, Symmetry.ORIGINAL));

		return fractal;
	}
*/
	public NyxInteractive() {
		super("Nyx: Fractal Image Encoding");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		AutowireCapableBeanFactory nyxFactory = context.getAutowireCapableBeanFactory();
		nyxFactory.autowireBean(this);
	}

	private void main() {
		Fractal fractal = null;
		final String imageName = "lena-gray.png";
		try {
			fractal = Fractal.load(new File(String.format("%s-fractal.nyx", imageName)));
		} catch (IOException ex) {
			LOG.error(String.format("Failed to open input file: %s. Exiting.", ex));
			return;
		}
		//Fractal fractal = getFractal(1);

		// Decode
		Signal decodedSignal = fractal.decode(1024); //fractal.decode(16384);
		
		// Assume the signal is a square gray-scale image
		int imageWidth = (int)Math.sqrt(decodedSignal.getDimension());
		ImageMetadata imageMetadata = new ImageMetadata(imageWidth, imageWidth, BufferedImage.TYPE_BYTE_GRAY, 1);
		ImageSignal decodedImageSignal = new ImageSignal(decodedSignal, imageMetadata);

		InteractiveFractal interactiveTransform = new InteractiveFractal(decodedImageSignal, fractal);
		getContentPane().removeAll();
		getContentPane().add(interactiveTransform);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args) {
		NyxInteractive nyx = new NyxInteractive();
		nyx.main();
	}
}


class VisualTransform {
	private final Transform transform;
	private final Signal signal;
	final int imageWidth;
	final Shape rangeShape;
	final Shape domainShape;
	private final PartitioningStrategy partitioner;

	public VisualTransform(Transform transform, Signal signal, int imageWidth) {
		this.transform = transform;
		this.signal = signal;
		this.imageWidth = imageWidth;
		
		//TODO: Dynamically determine the partitioning strategy type
		SquarePartitioningStrategy partitioningStrategy = new SquarePartitioningStrategy();
		this.partitioner = partitioningStrategy.getPartitioner(signal);
		this.rangeShape = getShapeForRangeBlock(transform.getRangeBlockIndex());
		this.domainShape = getShapeForDomainBlock(transform.getDomainBlockIndex());
	}

	private List<Point> getPointsFromIndices(int indices[], int width, int height) {		
		List<Point> points = new ArrayList<Point>();
		for (int idx : indices) {
			int x = idx % width;
			int y = idx / height;
			points.add(new Point(x, y));
		}
		return points;
	}

	private Shape getShapeFromIndices(int indices[]) {
		List<Point> points = getPointsFromIndices(indices, imageWidth, imageWidth);
		List<Point> hull = calcVHull(points);
		
		GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);
		Point firstPoint = hull.get(0);
		polyline.moveTo(firstPoint.x, firstPoint.y);
		for (Point point : hull) {
			polyline.lineTo(point.x, point.y);
		}
		polyline.lineTo(firstPoint.x, firstPoint.y);
		   
		return polyline;
	}

	private Shape getShapeForRangeBlock(int rangeBlockIndex) {
		return getShapeFromIndices(partitioner.getRangeIndices(rangeBlockIndex));
	}
	
	private Shape getShapeForDomainBlock(int domainBlockIndex) {
		return getShapeFromIndices(partitioner.getDomainIndices(domainBlockIndex));
	}

	public Transform getTransform() {
		return transform;
	}

	public RealMatrix getDomainBlock() {
		SparseRealMatrix F_I = partitioner.getDomainFetchOperator(transform.getDomainBlockIndex());
		return F_I.multiply(signal.getVector());
	}

	public RealMatrix getRangeBlock() {
		SparseRealMatrix F_I = partitioner.getRangeFetchOperator(transform.getRangeBlockIndex());
		return F_I.multiply(signal.getVector());
	}

	private List<Point> calcVHull(List<Point> listPoints){
        //Calculate convex vector of points
		List<Point> hullPoints = new ArrayList<>();
        Point lp = new Point(Integer.MAX_VALUE,0);
        Point rp = new Point(0,0);
        Point np = new Point();
        
        //Calculate leftmost and rightmost points - lp and rp
        for(int i=0;i<listPoints.size();i++){
            if(listPoints.get(i).x<=lp.x){
                lp.move(listPoints.get(i).x, listPoints.get(i).y);
            }
            if(listPoints.get(i).x>=rp.x){
                rp.move(listPoints.get(i).x, listPoints.get(i).y);
            }
        }
        
        //So calculate determinant and find the point for which every det is positive
        hullPoints.add(new Point(lp));
        np.setLocation(lp);
        
        do {
            for(int i=0;i<listPoints.size();i++){
                boolean nextPoint = true;
                if(np.x==listPoints.get(i).x && np.y==listPoints.get(i).y){
                    continue;
                }
                for(int j=0;j<listPoints.size();j++){
                    if(i==j || (np.x==listPoints.get(j).x && np.y==listPoints.get(j).y)){
                        continue;
                    }
                    
                    long det = (long) np.x*((long) listPoints.get(i).y-(long) listPoints.get(j).y)+ (long) listPoints.get(i).x*((long) listPoints.get(j).y- (long) np.y)+ (long) listPoints.get(j).x*((long) np.y- (long) listPoints.get(i).y);
                    if(det<0){
                        nextPoint = false;
                        break;
                    }
                }
                if(nextPoint){
                    np.setLocation(listPoints.get(i));
                    hullPoints.add(new Point(np.x,np.y));
                    //System.out.println(np.x + ";" + np.y);
                    break;
                }
            }
        //This condition does not work well. It will sometimes never end
        //if the left most point is not on the convex hull
        } while (!(np.x==lp.x && np.y==lp.y));
        
        return hullPoints;
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
			visualTransforms.add(new VisualTransform(transform, signal, image.getWidth()));
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
			System.out.println("Transform: \n" + selectedVisualTransform.getTransform() + "\n");
			System.out.println("Domain: \n" + selectedVisualTransform.getDomainBlock() + "\n");
			System.out.println("Range: \n" + selectedVisualTransform.getRangeBlock() + "\n");
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

