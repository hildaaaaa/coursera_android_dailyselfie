package android.coursera.dailyselfie;

import android.graphics.Bitmap;

/**
 * Created by hildachung on 5/2/2018.
 */

public class Selfie {
    private Bitmap selfie;
    private String name;
    private String path;

    public Selfie() {
        this.selfie = null;
        this.name = "";
        this.path = "";
    }

    public Selfie(Bitmap selfie, String name, String path) {
        this.selfie = selfie;
        this.name = name;
        this.path = path;
    }

    public Bitmap getSelfie() {
        return selfie;
    }

    public void setSelfie(Bitmap selfie) {
        this.selfie = selfie;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
