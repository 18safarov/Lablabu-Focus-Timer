package lablabu.service;

import javafx.scene.media.AudioClip;

public class SoundService {
    private final AudioClip completionSound;

    public SoundService() {
        completionSound = new AudioClip(getClass().getResource("/lablabu/ding.mp3").toExternalForm());
        completionSound.setVolume(0.6);
    }

    public void playCompletionSound() {
        completionSound.play();
    }
}
