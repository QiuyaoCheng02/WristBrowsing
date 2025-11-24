package fragment;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenSizeUtils {

    private static ScreenSizeUtils instance;
    private Context context;

    private ScreenSizeUtils(Context context) {
        this.context = context;
    }

    public static ScreenSizeUtils getInstance(Context context) {
        if (instance == null) {
            instance = new ScreenSizeUtils(context);
        }
        return instance;
    }

    public int getScreenWidth() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public int getScreenHeight() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }
}
