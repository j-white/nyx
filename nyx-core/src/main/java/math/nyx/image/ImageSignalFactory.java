package math.nyx.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.io.Files;

import math.nyx.core.Signal;
import math.nyx.core.SignalFactory;

public class ImageSignalFactory implements SignalFactory {
	public static final Set<String> SUPPORTED_EXTENSIONS
		= new HashSet<String>(Arrays.asList("jpg", "png"));

	@Override
	public Signal getSignalFor(File signalFile) throws IOException {
		String fileExtension = Files.getFileExtension(signalFile.getName());
		if (SUPPORTED_EXTENSIONS.contains(fileExtension))
			try (
				FileInputStream fis = new FileInputStream(signalFile);
			) {
				return new ImageSignal(fis);
			}
		return null;
	}
}
