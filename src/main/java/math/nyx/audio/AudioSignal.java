package math.nyx.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import math.nyx.core.Signal;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

public class AudioSignal extends Signal {
	private static final long serialVersionUID = 2184407289543087741L;

	private final AudioMetadata metadata;

	public AudioSignal(AudioInputStream audioIs) throws IOException {
		this(audioStreamToVector(audioIs), new AudioMetadata(audioIs.getFormat(), audioIs.getFrameLength()));
	}

	public AudioSignal(Signal s, AudioMetadata metadata) {
		super(s.getVector());
		this.metadata = metadata;
	}

	public AudioSignal(RealMatrix v, AudioMetadata metadata) {
		super(v);
		this.metadata = metadata;
	}

	public AudioSignal(InputStream is) throws IOException, UnsupportedAudioFileException {
		this(AudioSystem.getAudioInputStream(is));
	}

	public AudioInputStream getAudioStream() {
		RealMatrix x = getVector();
		int size = x.getRowDimension();
		
		byte[] b = new byte[size];
		for (int i = 0; i < size; i++) {
			b[i] = (byte)x.getEntry(i, 0);
		}

		ByteArrayInputStream bis = new ByteArrayInputStream(b);
		return new AudioInputStream(bis, metadata.getFormat(), metadata.getFrameLength());
	}

	public AudioMetadata getMetadata() {
		return metadata;
	}

	private static RealMatrix audioStreamToVector(AudioInputStream audioIs) throws IOException  {
		int size = audioIs.available();
		byte[] b = new byte[size];
		audioIs.read(b);
		
		RealMatrix x = new Array2DRowRealMatrix(size, 1);
		for (int i = 0; i < size; i++) {
			x.setEntry(i, 0, b[i] & 0xFF);
		}

		return x;
	}

	public static void writeToFile(AudioSignal originalSignal, Signal decodedSignal, int scale, File file) throws IOException {
		AudioFormat audioFormat = originalSignal.getMetadata().getFormat(scale);
		long frameLength = originalSignal.getMetadata().getFrameLength() * scale;
		AudioMetadata audioMetadata = new AudioMetadata(audioFormat, frameLength);
		
		System.out.printf("Size: %d Size scaled: %d Metadata: %s", originalSignal.getDimension(), decodedSignal.getDimension(), audioMetadata);

		AudioSignal decodedAudioSignal = new AudioSignal(decodedSignal, audioMetadata);
		FileOutputStream fos = new FileOutputStream(file);
		try{
			AudioSystem.write(decodedAudioSignal.getAudioStream(), Type.WAVE, fos);
		} finally {
			fos.close();
		}
	}
}
