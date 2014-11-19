package br.pucpr.jvlppm.classicmix.services;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Sound {
    private static class Wrapper<T> {
        public T data;
        public Wrapper(T data) {
            this.data = data;
        }
    }

    private static interface SoundFadeListener {
        void onComplete(MediaPlayer mp);
    }

    private static Sound instance;
    private final Context context;
    private String currentlyPlayingMusic;
    private MediaPlayer playing;
    private final SoundPool soundPool;
    private final int effectBounce;
    private final int effectExplosion;
    private final int effectDestroy;
    private final int effectHit;

    private float effectsVolume;

    public static synchronized Sound getInstance() {
        if(instance == null)
            throw new UnsupportedOperationException("HighScore not initialized");
        return instance;
    }

    private Sound(Context context) {
        this.context = context;
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        effectBounce = loadEffect("Bounce");
        effectExplosion = loadEffect("Explosion");
        effectDestroy = loadEffect("Hit_Destroy");
        effectHit = loadEffect("Hit_Hurt");
        updateVolume();
    }

    private int loadEffect(String name) {
        try {
            AssetManager am = context.getAssets();
            AssetFileDescriptor descriptor = am.openFd("sounds/" + name + ".wav");
            return soundPool.load(descriptor, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static synchronized void init(Context context) {
        instance = new Sound(context);
    }

    public void play(String name) throws IOException {
        float musicVolume = Settings.Sound.getMusicVolume();

        if(currentlyPlayingMusic != null && currentlyPlayingMusic.equals(name) && playing != null)
            return;

        currentlyPlayingMusic = name;

        if(playing != null) {
            fadeOut(playing, 1, musicVolume);
            playing = null;
        }

        if(musicVolume == 0)
            return;

        AssetManager am = context.getAssets();
        MediaPlayer mp = new MediaPlayer();

        AssetFileDescriptor descriptor = am.openFd("sounds/" + name + ".mp3");
        mp.setDataSource(
                descriptor.getFileDescriptor(),
                descriptor.getStartOffset(),
                descriptor.getLength());
        mp.prepare();
        mp.setLooping(true);
        mp.start();

        if(playing != null) {
            mp.setVolume(0, 0);
            fadeIn(mp, 1, musicVolume);
        }
        playing = mp;
    }

    public void pause() {
        if(playing != null && playing.isPlaying()) {
            fadeTo(playing, 1, Settings.Sound.getMusicVolume(), 0, new SoundFadeListener() {
                @Override
                public void onComplete(MediaPlayer mp) {
                    playing.pause();
                }
            });
        }
    }

    public void resume() {
        if(playing != null && !playing.isPlaying()) {
            fadeTo(playing, 1, 0, Settings.Sound.getMusicVolume(), new SoundFadeListener() {
                @Override
                public void onComplete(MediaPlayer mp) {
                    playing.start();
                }
            });
        }
    }

    public void stop() {
        if(playing != null) {
            fadeTo(playing, 1, Settings.Sound.getMusicVolume(), 0, new SoundFadeListener() {
                @Override
                public void onComplete(MediaPlayer mp) {
                    mp.stop();
                    mp.release();
                    playing = null;
                }
            });
        }
    }

    public void updateVolume() {

        float musicVolume = Settings.Sound.getMusicVolume();
        if(playing != null && musicVolume == 0) {
            stop();
        }
        else {
            if(playing == null) {
                try {
                    if (currentlyPlayingMusic != null)
                        play(currentlyPlayingMusic);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
                playing.setVolume(musicVolume, musicVolume);
        }

        effectsVolume = Settings.Sound.getEffectsVolume();
    }

    public void playLevelMusic(int level) {
        try {
            play("music" + (level + 1));
        }
        catch (IOException e) {
            e.printStackTrace();
            try {
                play("music1");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void fadeIn(final MediaPlayer mp, float fadeDuration, final float volume) {
        fadeTo(mp, fadeDuration, 0, volume, null);
    }

    private void fadeOut(final MediaPlayer mp, float fadeDuration, final float volume) {
        fadeTo(mp, fadeDuration, volume, 0, new SoundFadeListener() {
            @Override
            public void onComplete(MediaPlayer mp) {
                mp.stop();
                mp.release();
                if(playing == mp)
                    playing = null;
            }
        });
    }

    private void fadeTo(final MediaPlayer mp, float fadeDuration, final float startVolume, final float endVolume, final SoundFadeListener listener) {
        final Wrapper<Float> iVolume = new Wrapper<Float>(startVolume);

        final float delay = 0.014f;
        final float increment = (endVolume - startVolume) / fadeDuration;

        final boolean increasing = endVolume > startVolume;

        final Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                iVolume.data += increment * delay;
                if((increasing && iVolume.data > endVolume) || (!increasing && iVolume.data < endVolume))
                    iVolume.data = endVolume;

                mp.setVolume(iVolume.data, iVolume.data);

                if (iVolume.data == endVolume)
                {
                    timer.cancel();
                    timer.purge();
                    if(listener != null)
                        listener.onComplete(mp);
                }
            }
        };

        long millisecondsDelay = (long)(delay * 1000);
        timer.schedule(timerTask, millisecondsDelay, millisecondsDelay);
    }

    public void playBounce() {
        if (effectsVolume == 0) return;
        soundPool.play(effectBounce, effectsVolume, effectsVolume, 1, 0, 1);
    }

    public void playHit() {
        if (effectsVolume == 0) return;
        soundPool.play(effectHit, effectsVolume, effectsVolume, 1, 0, 1);
    }

    public void playDestroy() {
        if (effectsVolume == 0) return;
        soundPool.play(effectDestroy, effectsVolume, effectsVolume, 1, 0, 1);
    }

    public void playExplosion() {
        if (effectsVolume == 0) return;
        soundPool.play(effectExplosion, effectsVolume, effectsVolume, 1, 0, 1);
    }
}
