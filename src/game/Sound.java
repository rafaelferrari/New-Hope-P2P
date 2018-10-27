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
	 * Especif�ca o som a ser tocado
	 * ao construir o objeto 
	 * 
	 */
	Sound(String fileName) {
		try {
			InputStream str = Sound.class.getResourceAsStream(fileName);
			InputStream bufferedIn = new BufferedInputStream(str);
			AudioInputStream sound = AudioSystem.getAudioInputStream(bufferedIn);
			// load the sound into memory (a Clip)
			clip = AudioSystem.getClip();
			clip.open(sound);
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

	void play() {
		clip.setFramePosition(0); // Must always rewind!
		clip.start();
	}

	void loop() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	private void stop() {
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