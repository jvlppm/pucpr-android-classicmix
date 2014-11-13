package br.pucpr.jvlppm.classicmix.services;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

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
    MediaPlayer playing;

    public static synchronized Sound getInstance() {
        if(instance == null)
            throw new UnsupportedOperationException("HighScore not initialized");
        return instance;
    }

    private Sound(Context context) {
        this.context = context;
    }

    public static synchronized void init(Context context) {
        instance = new Sound(context);
    }

    public void play(String name) throws IOException {
        if(currentlyPlayingMusic != null && currentlyPlayingMusic.equals(name))
            return;

        currentlyPlayingMusic = name;
        AssetManager am = context.getAssets();
        MediaPlayer mp = new MediaPlayer();

        if(playing != null) {
            fadeOut(playing, 1, Settings.Sound.getMusicVolume());
        }
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
            fadeIn(mp, 1, Settings.Sound.getMusicVolume());
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
                    currentlyPlayingMusic = null;
                    playing = null;
                }
            });
        }
    }

    public void updateVolume() {
        if(playing != null) {
            playing.setVolume(Settings.Sound.getMusicVolume(), Settings.Sound.getMusicVolume());
        }
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
}
