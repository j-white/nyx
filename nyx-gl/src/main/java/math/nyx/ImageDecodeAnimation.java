package math.nyx;

import static javax.media.opengl.GL2.GL_POLYGON;
import static javax.media.opengl.GL2.GL_LINE_LOOP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.media.opengl.GL2;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import org.apache.commons.math.linear.RealMatrix;

import math.nyx.core.Fractal;
import math.nyx.core.FractalDecoderVisitor;
import math.nyx.core.Transform;
import math.nyx.framework.PartitioningStrategy;
import math.nyx.image.ImageMetadata;
import math.nyx.image.ImageSignal;

public class ImageDecodeAnimation extends TimerTask {
	private final int msPerTransform = 60;
	private final ImageSignal sourceSignal;
	private final Fractal fractal;
	private PartitioningStrategy partitioner;
	private Nyx nyx;
	private int scale;
	private ImageAsQuad range;
	private ImageAsQuad domain;
	private FractalDecoder decoder;
	private Thread thread = null;
	private boolean animating = false;
	
	private Transform activeTransform;
	private volatile Object newRangeSignalLock = new Object();
	private volatile ImageSignal newRangeSignal = null;
	private volatile Object newDomainSignalLock = new Object();
	private volatile ImageSignal newDomainSignal = null;

	private ArrayList<Polygon> rangePolys;
	private Map<Integer, Polygon> domainPolys = new HashMap<Integer, Polygon>();

	public ImageDecodeAnimation(ImageSignal sourceSignal, Fractal fractal) {
		this.sourceSignal = sourceSignal;
		this.fractal = fractal;
	}

	public void init(GL2 gl, int scale) {
		this.scale = scale;
		this.partitioner = fractal.getPartitioner(1);
		ImageSignal decodedSignal = (ImageSignal)fractal.decode(scale);

		domain = new ImageAsQuad(gl, sourceSignal, new Point3f(-1.1f, 0, 0));
		range = new ImageAsQuad(gl, decodedSignal, new Point3f(1.1f, 0, 0));

		int numRanges = partitioner.getNumRangePartitions();
		rangePolys = new ArrayList<Polygon>(numRanges);
		for (int i = 0; i < numRanges; i++) {
			rangePolys.add(getPolyForRangeBlock(i));
		}

		// Pre-calculate all of the domain blocks used for the transforms
		for (Transform t : fractal.getTransforms()) {
			getPolyForDomainBlock(t.getDomainBlockIndex());
		}
	}

	private class FractalDecoder implements Runnable, FractalDecoderVisitor {
		private final ImageMetadata scaledMetadata = sourceSignal.getMetadata().scale(scale);

		@Override
		public void run() {
			fractal.decode(scale, this);
		}
	
		@Override
		public void afterTransform(int iteration, Transform transform,
				RealMatrix source, RealMatrix domain,
				RealMatrix decimatedDomain, RealMatrix target) {

			synchronized(newRangeSignalLock) {
				sourceSignal.getMetadata().scale(scale);
				newRangeSignal = new ImageSignal(target, scaledMetadata);
			}
			activeTransform = transform;

			try {
				Thread.sleep(msPerTransform);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}

		@Override
		public void beforeIteration(int n, RealMatrix source, RealMatrix target) {
			synchronized(newDomainSignalLock) {
				newDomainSignal = new ImageSignal(source, scaledMetadata);
			}
			synchronized(newRangeSignalLock) {
				newRangeSignal = new ImageSignal(target, scaledMetadata);
			}
		}

		@Override
		public void afterIteration(int n, RealMatrix source, RealMatrix target) {
			// pass
		}

		@Override
		public void beforeDecode() {
			activeTransform = null;
		}

		@Override
		public void afterDecode() {
			activeTransform = null;
			synchronized(newDomainSignalLock) {
				newDomainSignal = sourceSignal;
			}
			animating = false;
		}
	}

	public void animate() {
		if (animating) {
			return;
		}
		animating = true;
		decoder = new FractalDecoder();
		thread = new Thread(decoder);
		thread.start();
	};

	@Override
	public void run() {
		// timer, pass
	}

	public void draw(GL2 gl) {
		synchronized(newRangeSignalLock) {
			if (newRangeSignal != null) {
				range.updateTexture(gl, newRangeSignal);
				newRangeSignal = null;
			}
		}
		synchronized(newDomainSignalLock) {
			if (newDomainSignal != null) {
				domain.updateTexture(gl, newDomainSignal);
				newDomainSignal = null;
			}
		}
		domain.draw(gl);
		range.draw(gl);
		
		if (animating && activeTransform != null) {
			int di = activeTransform.getDomainBlockIndex();
			int ri = activeTransform.getRangeBlockIndex();
			domainPolys.get(di).draw(gl);
			rangePolys.get(ri).draw(gl);
		}
	}

	public static class Polygon {
		private final Point3f world;
		private final List<Point2f> points;
		public Polygon(List<Point2f> points, Point3f world) {
			this.world = world;
			this.points = points;
		}
		
		public void draw(GL2 gl) {
			gl.glPushMatrix();
			gl.glTranslatef(world.x - 1, world.y - 1, world.z);

			// Draw the polygon
			gl.glColor4f(0.0f, 0.0f, 1.0f, 0.6f); //blue color w/ alpha
			gl.glBegin(GL_POLYGON);
				for (Point2f p : points) {
					gl.glVertex3f(p.x, p.y, 0.1f);
				}
			gl.glEnd();
			
			// Draw the edge
			gl.glColor3f(0.0f, 0.0f, 1.0f); //blue color
			gl.glBegin(GL_LINE_LOOP);
				for (Point2f p : points) {
					gl.glVertex3f(p.x, p.y, 0.1f);
				}
			gl.glEnd();

			gl.glPopMatrix();
		}
	}

	public Polygon getPolyForDomainBlock(int domainBlockIndex) {
		Polygon poly = null;
		if (domainPolys.containsKey(domainBlockIndex)) {
			poly = domainPolys.get(domainBlockIndex);
		} else {
			poly = getPolyFromIndices(partitioner.getDomainIndices(domainBlockIndex), domain.getWorld());
			domainPolys.put(domainBlockIndex, poly);
		}
		return poly;
	}

	private Polygon getPolyForRangeBlock(int rangeBlockIndex) {
		return getPolyFromIndices(partitioner.getRangeIndices(rangeBlockIndex), range.getWorld());
	}

	private Polygon getPolyFromIndices(int indices[], Point3f world) {
		int imageWidth = sourceSignal.getMetadata().getWidth();
		int imageHeight = sourceSignal.getMetadata().getHeight();
		
		ArrayList<Point2f> points = getPointsFromIndices(indices, imageWidth, imageHeight);
		List<Point2f> hull = FastConvexHull.execute(points);
		
		List<Point2f> imagePoints = new LinkedList<Point2f>();
		for (Point2f p : hull) {
			imagePoints.add(new Point2f((p.x + 0.5f) / imageWidth * 2, 2 - (p.y + 0.5f) / imageHeight * 2));
		}

		return new Polygon(imagePoints, world);
	}

	private ArrayList<Point2f> getPointsFromIndices(int indices[], int width, int height) {		
		ArrayList<Point2f> points = new ArrayList<Point2f>();
		for (int idx : indices) {
			// Determine the row (X) and column (Y) of the corresponding matrix cell for the given index
			int x = idx % width;
			int y = idx / height;

			// The cell represents a region whose center is at:
			float c = 0.5f;
			x += c;
			y += c;
			
			// Bottom left
			points.add(new Point2f(x - c, y - c));
			// Bottom right
			points.add(new Point2f(x + c, y - c));
			// Top right
			points.add(new Point2f(x + c, y + c));
			// Top left
			points.add(new Point2f(x - c, y + c));
		}
		return points;
	}

	public void setNyx(Nyx nyx) {
		this.nyx = nyx;
	}

	public Nyx getNyx() {
		return nyx;
	}
}
