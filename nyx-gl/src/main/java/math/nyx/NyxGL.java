package math.nyx;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import math.nyx.image.ImageSignal;

import com.jogamp.opengl.util.FPSAnimator;

import static javax.media.opengl.GL.*; // GL constants
import static javax.media.opengl.GL2.*; // GL2 constants
import static java.awt.event.KeyEvent.VK_W;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_Z;
import static java.awt.event.KeyEvent.VK_X;
import static java.awt.event.KeyEvent.VK_Y;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_J;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_K;

public class NyxGL extends GLCanvas implements GLEventListener, KeyListener {
	private static final long serialVersionUID = 8290398504918746872L;

	private static String TITLE = "NyxGL";
	private static final int CANVAS_WIDTH = 1024; // width of the drawable
	private static final int CANVAS_HEIGHT = 768; // height of the drawable
	private static final int FPS = 30; // animator's target frames per second

	private float translateX = 0.0f;
	private float translateY = 0.0f;
	private float translateZ = -5.0f;
	private float rotateX = 0.0f;
	private float rotateY = 0.0f;
	private float rotateZ = 0.0f;

	@Autowired
	private Nyx nyx;
	
	private ImageDecodeAnimation decodeAnimation;

	/** The entry main() method to setup the top-level container and animator */
	public static void main(String[] args) {
		// Run the GUI codes in the event-dispatching thread for thread safety
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Create the OpenGL rendering canvas
				GLCanvas canvas = new GLCanvas(); // heavy-weight GLCanvas
				canvas.setPreferredSize(new Dimension(CANVAS_WIDTH,
						CANVAS_HEIGHT));
				NyxGL renderer = new NyxGL();
				
				// Spring-ify
				@SuppressWarnings("resource")
				ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
				AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
				beanFactory.autowireBean(renderer);
				
				canvas.addGLEventListener(renderer);

				canvas.addKeyListener(renderer);
				canvas.setFocusable(true); // To receive key event
				canvas.requestFocus();

				// Create a animator that drives canvas' display() at the
				// specified FPS.
				final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

				// Create the top-level container
				final JFrame frame = new JFrame(); // Swing's JFrame or AWT's
													// Frame
				frame.getContentPane().add(canvas);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						// Use a dedicate thread to run the stop() to ensure
						// that the
						// animator stops before program exits.
						new Thread() {
							@Override
							public void run() {
								if (animator.isStarted())
									animator.stop();
								System.exit(0);
							}
						}.start();
					}
				});
				frame.setTitle(TITLE);
				frame.pack();
				frame.setVisible(true);
				animator.start(); // start the animation loop
			}
		});
	}

	// Setup OpenGL Graphics Renderer

	private GLU glu; // for the GL Utility

	/** Constructor to setup the GUI for this Component */
	public NyxGL() {
		this.addGLEventListener(this);
	}

	// ------ Implement methods declared in GLEventListener ------

	/**
	 * Called back immediately after the OpenGL context is initialized. Can be
	 * used to perform one-time initialization. Run only once.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL graphics context
		glu = new GLU(); // get GL Utilities
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
		gl.glClearDepth(1.0f); // set clear depth value to farthest
		gl.glEnable(GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL_LEQUAL); // the type of depth test to do
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best
																// perspective
																// correction
		gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out
									// lighting
        // Use linear filter for texture if image is larger than the original texture
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // Use linear filter for texture if image is smaller than the original texture
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        
        // Enable blending
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        setupFractal(gl);
	}

	public void setupFractal(GL2 gl) {
		ImageSignal sourceSignal = null;
		try {
			sourceSignal = new ImageSignal(new ClassPathResource("images/flames-48x48-gray.jpg").getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		decodeAnimation = new ImageDecodeAnimation(sourceSignal, nyx.encode(sourceSignal));
		decodeAnimation.setNyx(nyx);
		decodeAnimation.init(gl, 16);
		decodeAnimation.animate(gl);
	}

	/**
	 * Call-back handler for window re-size event. Also called when the drawable
	 * is first set to visible.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context

		if (height == 0)
			height = 1; // prevent divide by zero
		float aspect = (float) width / height;

		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GL_PROJECTION); // choose projection matrix
		gl.glLoadIdentity(); // reset projection matrix
		glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear,
														// zFar

		// Enable the model-view transform
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity(); // reset
	}

	/**
	 * Called back by the animator to perform rendering.
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color
																// and depth
																// buffers
		gl.glLoadIdentity(); // reset the model-view matrix
		gl.glTranslatef(translateX, translateY, translateZ);
		gl.glRotatef(rotateX, 1.0f, 0.0f, 0.0f); // rotate about the x-axis
		gl.glRotatef(rotateY, 0.0f, 1.0f, 0.0f); // rotate about the y-axis
		gl.glRotatef(rotateZ, 0.0f, 0.0f, 1.0f); // rotate about the z-axis

		decodeAnimation.draw(gl);
	}

	/**
	 * Called back before the OpenGL context is destroyed. Release resource such
	 * as buffers.
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case VK_W:
				translateZ++;
				break;
			case VK_S:
				translateZ--;
				break;
			case VK_A:
				translateX++;
				break;
			case VK_D:
				translateX--;
				break;
			case VK_Z:
				translateY++;
				break;
			case VK_X:
				translateY--;
				break;
			case VK_Y:
				rotateX++;
				break;
			case VK_H:
				rotateX--;
				break;
			case VK_U:
				rotateY++;
				break;
			case VK_J:
				rotateY--;
				break;
			case VK_I:
				rotateZ++;
				break;
			case VK_K:
				rotateZ--;
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
