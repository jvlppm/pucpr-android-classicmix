package br.pucpr.jvlppm.classicmix.activities;

import br.pucpr.jvlppm.classicmix.services.Sound;

public class MusicController {
    private final boolean keepOnFinish;
    public boolean keepMusic;

    public MusicController(boolean keepOnFinish) {
        this.keepOnFinish = keepOnFinish;
    }

    public void pause(boolean isFinishing) {
        if(keepOnFinish && isFinishing)
            keepMusic();

        if(!keepMusic)
            Sound.getInstance().pause();
        else if (!isFinishing)
            keepMusic = false;
    }

    public void resume() {
        Sound.getInstance().resume();
    }

    public void destroy(boolean isFinishing) {
        if(isFinishing && !keepMusic)
            Sound.getInstance().stop();
    }

    public void keepMusic() {
        keepMusic = true;
    }
}
