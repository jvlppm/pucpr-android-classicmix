package br.pucpr.jvlppm.classicmix.android;

import br.pucpr.jvlppm.classicmix.services.HighScore;
import br.pucpr.jvlppm.classicmix.services.Settings;
import br.pucpr.jvlppm.classicmix.services.Sound;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.init(this);
        HighScore.init(this);
        Sound.init(this);
    }
}
