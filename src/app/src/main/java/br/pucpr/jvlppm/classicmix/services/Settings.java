package br.pucpr.jvlppm.classicmix.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import br.pucpr.jvlppm.classicmix.R;

public class Settings {
    private static Context context;

    public static void init(Context context) {
        Settings.context = context;
        PreferenceManager.setDefaultValues(context, R.xml.preferences_graphics, true);
        PreferenceManager.setDefaultValues(context, R.xml.preferences_gameplay, true);
        PreferenceManager.setDefaultValues(context, R.xml.preferences_sound, true);
    }

    private static SharedPreferences getPreferences() {
        if(context == null)
            throw new IllegalStateException("Settings not initialized");

        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static class Sound {
        public static float getMusicVolume() {
            return getPreferences().getFloat("sound_music_volume", 0);
        }

        public static float getEffectsVolume() {
            return getPreferences().getFloat("sound_effects_volume", 0);
        }
    }

    public static class Gameplay {
        public static enum Difficulty { Easy, Medium, Hard }

        public static Difficulty getDifficulty() {
            String savedValue = getPreferences().getString("gameplay_difficulty", "");

            switch (Integer.valueOf(savedValue)) {
                case 0: return Difficulty.Easy;
                case 1: return Difficulty.Medium;
                case 2: return Difficulty.Hard;
            }
            throw new UnsupportedOperationException("Invalid difficulty level");
        }
    }

    public static class Graphics {
        public static boolean areBackgroundAnimationsEnabled() {
            return getPreferences().getBoolean("background_animations", false);
        }

        public static float getBackgroundOpacity() {
            return getPreferences().getFloat("background_opacity", 0);
        }

        public static boolean useThreadedFrameBuffer() {
            return getPreferences().getBoolean("threaded_framebuffer", true);
        }

        public static boolean displayFps() {
            return getPreferences().getBoolean("display_fps", false);
        }
    }
}
