package br.pucpr.jvlppm.classicmix.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.List;

import br.pucpr.jvlppm.classicmix.R;

public class SettingsActivity extends PreferenceActivity {
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

    public static class GameplayPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_gameplay);
        }
    }

    public static class GraphicsPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_graphics);
        }
    }

    public static class SoundPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_sound);
        }
    }
}