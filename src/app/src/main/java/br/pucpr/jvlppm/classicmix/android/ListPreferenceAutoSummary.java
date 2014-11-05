package br.pucpr.jvlppm.classicmix.android;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class ListPreferenceAutoSummary extends ListPreference {
    public ListPreferenceAutoSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListPreferenceAutoSummary(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        CharSequence summary = super.getSummary();
        if(summary == null) {
            String value = getValue();
            CharSequence[] values = getEntryValues();
            for (int i = 0; i < values.length; i++) {
                if (value.equals(values[i])) {
                    return getEntries()[i];
                }
            }
        }

        return super.getSummary();
    }
}
