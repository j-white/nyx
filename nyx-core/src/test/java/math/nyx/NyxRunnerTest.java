package math.nyx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import math.nyx.affine.Symmetry;
import math.nyx.audio.AudioSignal;
import math.nyx.core.Transform;
import math.nyx.image.ImageSignal;
import math.nyx.report.FractalCodecReport;
import math.nyx.report.FractalCodecReport.DecodeReport;
import math.nyx.utils.TestUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.io.ByteStreams;

/**
 * Test encoding, decoding and report generation via the main method.
 * 
 * @author jwhite
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class NyxRunnerTest {
	@Autowired
	ApplicationContext context;

	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

	private NyxRunner nyxRunner = new NyxRunner();

	@Before
	public void setUp() {
		context.getAutowireCapableBeanFactory().autowireBean(nyxRunner);
	}

	/**
	 * Verify the report generated by encoding and decoding a 32 x 32 image
	 */
	@Test
	public void imageReportTest() throws IOException {
		// Copy the resource from the class-path to a temporary file
		File sourceFile = getFileFor("ocean-32x32-gray.png");

		// Generate the report via the main function
		FractalCodecReport report = getReportFor(sourceFile);
	
		// Verify basic properties
		assertEquals(sourceFile, report.getSourceFile());
		assertEquals(ImageSignal.TYPE, report.getSignalType());
		assertEquals(32*32, report.getSignalDimension());

		// We should have two decode reports with scales 1 and 4
		List<DecodeReport> decodeReports = report.getDecodeReports();
		assertEquals(2, decodeReports.size());

		assertEquals(1, decodeReports.get(0).getScale());
		assertEquals(4, decodeReports.get(1).getScale());

		// The number of range partitions should be greater than 0
		assertTrue(report.getNumRangePartitions() > 0);

		// The number of transforms should equal the number of range partitions
		assertEquals(report.getNumRangePartitions(), report.getNumTransforms());

		// The transforms should use at least two different symmetries
		Set<Symmetry> symmetriesUsed = new HashSet<Symmetry>();
		for (Transform t : report.getTransforms()) {
			symmetriesUsed.add((Symmetry)t.getKernelParameters().get("symmetry"));
		}
		assertTrue(symmetriesUsed.size() > 1);

		// Grab the first decode report
		DecodeReport decodeReport =  decodeReports.get(0);

		// The size of the decoded signal should be non-zero
		assertNotEquals(0, decodeReport.getDecodedSignal().getSizeInBytes());

		// We should be able to read the decoded image
		File decodedFile = decodeReport.getDestFile();
		BufferedImage decodedImage = ImageIO.read(decodedFile);

		// The image's meta-data should be the same as the source's
		BufferedImage originalImage = ImageIO.read(sourceFile);
		assertEquals(originalImage.getWidth(), decodedImage.getWidth());
		assertEquals(originalImage.getHeight(), decodedImage.getHeight());
		assertEquals(originalImage.getTransparency(), decodedImage.getTransparency());
		assertEquals(originalImage.getType(), decodedImage.getType());
	}

	/**
	 * Verify the report generated by encoding and decoding a short WAV file
	 * @throws UnsupportedAudioFileException 
	 */
	@Test
	public void audioReportTest() throws IOException, UnsupportedAudioFileException {
		// Copy the resource from the class-path to a temporary file
		File sourceFile = getFileFor("beep-100ms-pcm.wav");

		// Generate the report via the main function
		FractalCodecReport report = getReportFor(sourceFile);
	
		// Verify basic properties
		assertEquals(sourceFile, report.getSourceFile());
		assertEquals(AudioSignal.TYPE, report.getSignalType());

		// We should have two decode reports with scales 1 and 4
		List<DecodeReport> decodeReports = report.getDecodeReports();
		assertEquals(2, decodeReports.size());

		assertEquals(1, decodeReports.get(0).getScale());
		assertEquals(4, decodeReports.get(1).getScale());

		// The number of range partitions should be greater than 0
		assertTrue(report.getNumRangePartitions() > 0);

		// The number of transforms should equal the number of range partitions
		assertEquals(report.getNumRangePartitions(), report.getNumTransforms());

		// The transforms should use a single symmetry
		Set<Symmetry> symmetriesUsed = new HashSet<Symmetry>();
		for (Transform t : report.getTransforms()) {
			symmetriesUsed.add((Symmetry)t.getKernelParameters().get("symmetry"));
		}
		assertEquals(1, symmetriesUsed.size());

		// Grab the first decode report
		DecodeReport decodeReport =  decodeReports.get(0);

		// The size of the decoded signal should be non-zero
		assertNotEquals(0, decodeReport.getDecodedSignal().getSizeInBytes());

		// We should be able to read the decoded audio file
		File decodedFile = decodeReport.getDestFile();
		AudioInputStream decodedAudio = AudioSystem.getAudioInputStream(decodedFile);
		AudioFormat decodedFormat = decodedAudio.getFormat();

		// The audio's meta-data should be the same as the source's
		AudioInputStream originalAudio = AudioSystem.getAudioInputStream(sourceFile);
		AudioFormat originalFormat = originalAudio.getFormat();
		
		assertEquals(originalAudio.getFrameLength(), decodedAudio.getFrameLength());
		assertEquals(originalFormat.getChannels(), decodedFormat.getChannels());
		assertEquals(originalFormat.getFrameRate(), decodedFormat.getFrameRate(), TestUtils.DELTA);
		assertEquals(originalFormat.getFrameSize(), decodedFormat.getFrameSize());
		assertEquals(originalFormat.isBigEndian(), decodedFormat.isBigEndian());
	}

	/**
	 * Copy the file a fixed directory in the class-path to a temporary file
	 */
	private File getFileFor(String classpathResource) throws IOException {
		Resource source = new ClassPathResource("math/nyx/samples/" + classpathResource); 
		File sourceFile = testFolder.newFile(classpathResource);
		try (
			InputStream in = source.getInputStream();
			OutputStream out = new FileOutputStream(sourceFile);
		) {
			ByteStreams.copy(source.getInputStream(), out);
		}
		return sourceFile;
	}

	/**
	 * Generate the report by passing the arguments as strings via the main function
	 */
	private FractalCodecReport getReportFor(File sourceFile) throws IOException {
		String args[] = new String[]{"-s", "4", // Decode at 1x and at 4x
									 "-o", sourceFile.getParent(), // Point the output folder to the temp dir.
									 sourceFile.getAbsolutePath()};
		List<FractalCodecReport> reports = nyxRunner.doMain(args);
		assertEquals(1, reports.size());
		return reports.get(0);
	}
}
