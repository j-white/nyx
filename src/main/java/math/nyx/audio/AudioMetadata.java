package math.nyx.audio;

import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import math.nyx.core.SignalMetadata;

import com.google.common.base.Objects;

public class AudioMetadata implements SignalMetadata {
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
		return getFormat(1);
	}

	public AudioFormat getFormat(int scale) {
		return new AudioFormat(new Encoding(formatEncodingAsStr), formatSampleRate, formatSampleSizeInBits * scale, formatChannels,
				formatFrameSize, formatFrameRate, formatIsBigEndian, formatProperties);
	}
	
	public long getFrameLength() {
		return frameLength;
	}

	@Override
	public String toString() {
	    return Objects.toStringHelper(this.getClass())
	    		.add("format", getFormat())
	    		.add("frameLength", getFrameLength())
	            .toString();
	}

	@Override
	public AudioMetadata scale(int scale) {
		return this;
	}
}
