package math.nyx.audio;

import java.io.Serializable;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public class AudioMetadata implements Serializable {
	private static final long serialVersionUID = 6282613034251062575L;

	private final int formatChannels;
	private final String formatEncodingAsStr;
	private final float formatFrameRate;
	private final int formatFrameSize;
	private final float formatSampleRate;
	private final int formatSampleSizeInBits;
	private final Map<String, Object> formatProperties;
	private final boolean formatIsBigEndian;

	private final long frameLength;

	public AudioMetadata(AudioFormat format, long frameLength) {
		// AudioFormat is not serializable, so we save it's properties instead
		formatChannels = format.getChannels();
		formatEncodingAsStr = format.getEncoding().toString();
		formatFrameRate = format.getFrameRate();
		formatFrameSize = format.getFrameSize();
		formatSampleRate = format.getSampleRate();
		formatSampleSizeInBits = format.getSampleSizeInBits();
		formatProperties = format.properties();
		formatIsBigEndian = format.isBigEndian();

		this.frameLength = frameLength;
	}

	public AudioFormat getFormat() {
		return new AudioFormat(new Encoding(formatEncodingAsStr), formatSampleRate, formatSampleSizeInBits, formatChannels,
				formatFrameSize, formatFrameRate, formatIsBigEndian, formatProperties);
	}

	public long getFrameLength() {
		return frameLength;
	}
}
