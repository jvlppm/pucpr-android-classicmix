package br.pucpr.jvlppm.classicmix.android;

import br.pucpr.jvlppm.classicmix.services.Settings;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.init(this);
    }
}
