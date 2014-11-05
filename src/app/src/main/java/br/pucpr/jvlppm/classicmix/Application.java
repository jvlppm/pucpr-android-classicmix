package br.pucpr.jvlppm.classicmix;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Settings.init(this);
    }
}
