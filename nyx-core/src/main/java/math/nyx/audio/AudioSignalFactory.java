package math.nyx.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.common.io.Files;

import math.nyx.core.InvalidSignalException;
import math.nyx.core.Signal;
import math.nyx.core.SignalFactory;

public class AudioSignalFactory implements SignalFactory {
	public static final Set<String> SUPPORTED_EXTENSIONS
		= new HashSet<String>(Arrays.asList("wav"));

	@Override
	public Signal getSignalFor(File signalFile) throws IOException, InvalidSignalException {
		String fileExtension = Files.getFileExtension(signalFile.getName());
		if (SUPPORTED_EXTENSIONS.contains(fileExtension)) {
			try (
					FileInputStream fis = new FileInputStream(signalFile);
					BufferedInputStream bis = new BufferedInputStream(fis);
			)
			{
				return new AudioSignal(bis);
			} catch (UnsupportedAudioFileException e) {
				throw new InvalidSignalException(e);
			}
		}
		return null;
	}
}
