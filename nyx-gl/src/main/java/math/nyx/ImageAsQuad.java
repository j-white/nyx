package math.nyx;

import static javax.media.opengl.GL2GL3.GL_QUADS;

import javax.media.opengl.GL2;
import javax.vecmath.Point3f;

import math.nyx.image.ImageSignal;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

public class ImageAsQuad {
	private final ImageSignal signal;
	private final Point3f world;
	private final Texture texture;
	private float textureTop, textureBottom, textureLeft, textureRight;

	public ImageAsQuad(GL2 gl, ImageSignal signal, Point3f world) {
	   this.signal = signal;
	   this.world = world;
	   this.texture = generateTexture(gl);
	}

	private Texture generateTexture(GL2 gl) {
		Texture texture = AWTTextureIO.newTexture(gl.getGLProfile(), signal.getImage(), false);
        // Texture image flips vertically. Shall use TextureCoords class to retrieve
        // the top, bottom, left and right coordinates, instead of using 0.0f and 1.0f.
        TextureCoords textureCoords = texture.getImageTexCoords();
        textureTop = textureCoords.top();
        textureBottom = textureCoords.bottom();
        textureLeft = textureCoords.left();
        textureRight = textureCoords.right();
        return texture;
	}

	public void draw(GL2 gl) {
		// Enables this texture's target in the current GL context's state.
		texture.enable(gl); // same as gl.glEnable(texture.getTarget());
		// gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
		// GL.GL_REPLACE);
		// Binds this texture to the current GL context.
		texture.bind(gl); // same as gl.glBindTexture(texture.getTarget(),
							// texture.getTextureObject());

		gl.glPushMatrix();
		
		gl.glColor3f(1.0f,1.0f,1.0f);
		
		gl.glTranslatef(world.x, world.y, world.z);

		gl.glBegin(GL_QUADS);

		// Front Face
		gl.glTexCoord2f(textureLeft, textureBottom);
		gl.glVertex3f(-1.0f, -1.0f, 0.0f); // bottom-left of the texture and
											// quad
		gl.glTexCoord2f(textureRight, textureBottom);
		gl.glVertex3f(1.0f, -1.0f, 0.0f); // bottom-right of the texture and
											// quad
		gl.glTexCoord2f(textureRight, textureTop);
		gl.glVertex3f(1.0f, 1.0f, 0.0f); // top-right of the texture and quad
		gl.glTexCoord2f(textureLeft, textureTop);
		gl.glVertex3f(-1.0f, 1.0f, 0.0f); // top-left of the texture and quad

		gl.glEnd();
		
		gl.glPopMatrix();
	}

	public Point3f getWorld() {
		return world;
	}
}
