package br.pucpr.jvlppm.classicmix.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.List;

import br.pucpr.jvlppm.classicmix.R;
import br.pucpr.jvlppm.classicmix.services.Sound;

public class SettingsActivity extends PreferenceActivity {
    private final MusicController musicController = new MusicController(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        String[] allowedFragments = new String[] {
                GameplayPreferencesFragment.class.getName(),
                SoundPreferencesFragment.class.getName(),
                GraphicsPreferencesFragment.class.getName()
        };

        for(String allowed : allowedFragments)
            if (allowed.equals(fragmentName))
                return true;

        return false;
    }

    @Override
    public void startActivity(Intent intent) {
        musicController.keepMusic();
        super.startActivity(intent);
    }

    @Override
    protected void onPause() {
        musicController.pause(isFinishing());
        super.onPause();
    }

    @Override
    protected void onResume() {
        musicController.resume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        musicController.destroy(isFinishing());
        super.onDestroy();
    }

    public static class BasePreferencesFragment extends PreferenceFragment {
        private final MusicController musicController = new MusicController(true);
        private boolean minimizing;

        @Override
        public void onPause() {
            super.onPause();
            musicController.pause(!minimizing);
            minimizing = false;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            minimizing = true;
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onResume() {
            musicController.resume();
            super.onResume();
        }
    }

    public static class GameplayPreferencesFragment extends BasePreferencesFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_gameplay);
        }
    }

    public static class GraphicsPreferencesFragment extends BasePreferencesFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_graphics);
        }
    }

    public static class SoundPreferencesFragment extends BasePreferencesFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_sound);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Sound.getInstance().updateVolume();
        }
    }
}