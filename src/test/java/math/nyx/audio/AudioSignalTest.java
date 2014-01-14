package math.nyx.audio;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SerializationUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/resources/applicationContext.xml"}) 
public class AudioSignalTest {
	private void testAudioSignalConversion(Resource audioFile) throws IOException, UnsupportedAudioFileException {
		// Encode the audio into a signal
		AudioSignal signal = new AudioSignal(audioFile.getInputStream());
		AudioInputStream audioStream = signal.getAudioStream();

		// Serialize and de-serialize the signal
		byte[] signalAsBytes = SerializationUtils.serialize(signal);
		signal = (AudioSignal)SerializationUtils.deserialize(signalAsBytes);

		// Now retrieve the audio stream from the signal
		AudioInputStream decodedAudioStream = signal.getAudioStream();

		// Verify the image meta-data
		assertEquals(audioStream.getFormat().toString(), decodedAudioStream.getFormat().toString());
		assertEquals(audioStream.getFrameLength(), decodedAudioStream.getFrameLength());

		// Now compare the contents of the stream
		audioStream = AudioSystem.getAudioInputStream(audioFile.getInputStream());
		int size = audioStream.available();
		byte[] sourceBytes = new byte[size];
		audioStream.read(sourceBytes);
		
		size = decodedAudioStream.available();
		byte[] decodedBytes = new byte[size];
		decodedAudioStream.read(decodedBytes);
		
		assertEquals(sourceBytes.length, decodedBytes.length);
		for (int i = 0; i < sourceBytes.length; i++) {
			assertEquals(String.format("Audio at [%d]", i), sourceBytes[i], decodedBytes[i]);
		}
	}

	@Test
	public void testWavFile() throws IOException, UnsupportedAudioFileException {
		String audioFileName = "disconnect.wav";
		Resource audioFile = new ClassPathResource("math/nyx/resources/" + audioFileName);
		testAudioSignalConversion(audioFile);
	}
}
