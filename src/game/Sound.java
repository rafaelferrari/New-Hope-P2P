package game;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Handles playing, stoping, and looping of sounds for the game.
 * 
 * @author Tyler Thomas
 * 
 */

public class Sound {
	private Clip clip;

	/**
	 * Especifíca o som a ser tocado
	 * ao construir o objeto 
	 * 
	 */
	public Sound(String fileName) {
		try {
//			URL url = Sound.class.getResource(fileName);
//			File file = new File(url.toURI());
			InputStream str = Sound.class.getResourceAsStream(fileName);
			InputStream bufferedIn = new BufferedInputStream(str);
			if (str != null) {
				AudioInputStream sound = AudioSystem.getAudioInputStream(bufferedIn);
				// load the sound into memory (a Clip)
				clip = AudioSystem.getClip();
				clip.open(sound);
			} else {
				throw new RuntimeException("Sound: file not found: " + fileName);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException("Sound: Malformed URL: " + e);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			throw new RuntimeException("Sound: Unsupported Audio File: " + e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Sound: Input/Output Error: " + e);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Sound: Line Unavailable Exception Error: " + e);
		} 
	}

	public void play() {
		clip.setFramePosition(0); // Must always rewind!
		clip.start();
	}

	public void loop() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stop() {
		clip.stop();
	}

	/**
	 * Troca o som e toca o novo
	 */
	public static void changeSE(Sound effect, String neweffect) {
		if (effect != null)
			effect.stop();
		effect = new Sound(neweffect);
		effect.play();
	}
	


	
}