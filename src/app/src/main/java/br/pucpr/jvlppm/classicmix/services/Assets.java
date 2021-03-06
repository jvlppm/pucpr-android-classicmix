package br.pucpr.jvlppm.classicmix.services;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.NinePatch;

public class Assets {
    static Assets instance;

    public static synchronized Assets getInstance() {
        if(instance == null)
            instance = new Assets();
        return instance;
    }

    public static interface LoadListener {
        public void onLoadCompleted();
    }

    List<LoadListener> listeners;

    boolean loading;
    boolean loaded;

    public NinePatch paddleBlue;
    public NinePatch paddleRed;
    public NinePatch paddleShadow;
    public Frame ballBlue;
    public Frame ballGray;
    public Frame ballShadow;
    public Frame brickWhite;
    public Frame brickBlue;
    public Frame brickGray;
    public Frame brickGreen;
    public Frame brickPurple;
    public Frame brickRed;
    public Frame brickYellow;
    public Frame brickShadow;
    public Frame brickReinforcement;
    public List<Frame> brickStrength;
    public Frame msgGameOver;
    public Frame msgMoveToBegin;
    public Frame lifeIndicator;
    public Frame itemEnlarge;
    public Frame itemLaser;
    public Frame itemPierce;
    public Frame itemSlowBall;
    public Frame itemBall;
    public Frame bottomBar;
    public List<Frame> background;
    public List<Frame> laser;

    public final Map<String, Frame> byName;
    private final Map<Integer, Frame> brickColors;

    private Assets() {
        listeners = new ArrayList<LoadListener>();
        byName = new HashMap<String, Frame>();
        brickColors = new HashMap<Integer, Frame>();
    }

    public Frame createBrick(int color) {
        if(brickColors.containsKey(color))
            return brickColors.get(color);

        Bitmap texture = Bitmap.createBitmap(brickWhite.texture.getWidth(), brickWhite.texture.getHeight(), Bitmap.Config.RGB_565);
        for(int y = 0; y < texture.getHeight(); y++) {
            for (int x = 0; x < texture.getWidth(); x++) {
                int pixel = brickWhite.texture.getPixel(x, y);
                float r = Color.red(pixel);
                float g = Color.green(pixel);
                float b = Color.blue(pixel);

                r *= Color.red(color) / 255f;
                g *= Color.green(color) / 255f;
                b *= Color.blue(color) / 255f;

                try {
                    texture.setPixel(x, y, Color.argb(Color.alpha(pixel),
                            (int) Math.max(0, Math.min(255, r)),
                            (int) Math.max(0, Math.min(255, g)),
                            (int) Math.max(0, Math.min(255, b))));
                }
                catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
        Frame frame = new Frame(texture, brickWhite.rect, 1);
        brickColors.put(color, frame);
        return frame;
    }

    private void loadAssets(Context context) {
        for(Field field : getClass().getDeclaredFields()) {
            if(field.getType().getSimpleName().equals("Frame")) {
                try {
                    Frame frame = loadFrame(context, field.getName() + ".png");
                    field.set(this, frame);
                    byName.put(field.getName(), frame);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            else if(field.getType().getSimpleName().equals("NinePatch")) {
                try {
                    Frame frame = loadFrame(context, field.getName() + ".9.png");
                    field.set(this, new NinePatch(frame.texture));
                    byName.put(field.getName(), frame);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            else if(field.getType().getSimpleName().equals("List")) {
                int i = 1;
                List<Frame> frames = new ArrayList<Frame>();
                while(true) {
                    String name = field.getName() + i++;
                    try {
                        Frame frame = loadFrame(context, name + ".png");
                        frames.add(frame);
                        byName.put(name, frame);
                    }
                    catch (Exception e) {
                        break;
                    }
                }

                try {
                    field.set(this, frames);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Frame loadFrame(Context context, String fileName) {
        AssetManager assets = context.getAssets();
        InputStream in = null;
        Bitmap bitmap = null;
        try {
            in = assets.open(fileName);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load bitmap from asset '"
                    + fileName + "'");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        if (bitmap == null)
            throw new RuntimeException("Couldn't load bitmap from asset '" + fileName + "'");

        return new Frame(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), 1);
    }

    public void loadAssetsAsync(final Activity context, final LoadListener listener) {
        synchronized (this) {
            if(loaded) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onLoadCompleted();
                    }
                });
                return;
            }
            if(loading) {
                listeners.add(listener);
                return;
            }

            loading = true;
        }

        Thread loadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadAssets(context);

                synchronized (this) {
                    loaded = true;

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for(LoadListener registeredListener : listeners)
                                registeredListener.onLoadCompleted();
                            listener.onLoadCompleted();
                        }
                    });
                }
            }
        });
        loadThread.start();
    }
}
