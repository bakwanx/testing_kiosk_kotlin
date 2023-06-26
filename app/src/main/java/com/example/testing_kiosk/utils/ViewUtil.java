package com.example.testing_kiosk.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ViewUtil {
    private static final double RATIO_SCALE_BOTH  = 12.5;//5.52
    private static final double RATIO_SCALE_WIDTH = 0.155;
    private static final double RATIO_SCALE_TEXT  = 105;

    public static void doAdaptViews(@NonNull WindowManager winMgr, @NonNull TextureView main, ImageView captureView){
        DisplayMetrics metric = new DisplayMetrics();
        winMgr.getDefaultDisplay().getMetrics(metric);
        final int scaleRatio = (int) (metric.widthPixels / RATIO_SCALE_BOTH);

        main.post(() -> {
            // Update TextureView layout params with 3:4
            ViewGroup.LayoutParams p = main.getLayoutParams();
            p.width = 3 * scaleRatio;
            p.height = 4* scaleRatio;
            main.setLayoutParams(p);

            p=captureView.getLayoutParams();
            p.width = 3 * scaleRatio;
            p.height = 4* scaleRatio;
            captureView.setLayoutParams(p);
        });
    }

    public static void doAdaptViews(@NonNull WindowManager winMgr, @NonNull TextureView main, View... views){
        DisplayMetrics metric = new DisplayMetrics();
        winMgr.getDefaultDisplay().getMetrics(metric);

        final float aspectRatio = (float) (metric.widthPixels * 1.0 / metric.heightPixels);
        final boolean notTablet = aspectRatio > 1.5 && metric.heightPixels > 1000;

        final int scaleRatio = (int) (metric.widthPixels / RATIO_SCALE_BOTH - (notTablet ? 8 : 0));
        final int buttonWidth = (int) (metric.heightPixels * RATIO_SCALE_WIDTH);
        final int textSize = (int) ((metric.widthPixels+metric.heightPixels)/ RATIO_SCALE_TEXT - (notTablet ? 5 : 0));

        main.post(() -> {
            // Update TextureView layout params with 3:4
            ViewGroup.LayoutParams p = main.getLayoutParams();
            p.width = 3 * scaleRatio;
            p.height = 4* scaleRatio;
            main.setLayoutParams(p);

            if(views!=null) for (View view : views) {

                // Update Buttons layout params(Width)
                if (view instanceof Button) {
                    p=view.getLayoutParams();
                    p.width = buttonWidth;
                    view.setLayoutParams(p);
                }

                // Update TextView layout params(Text size)
                if (view instanceof TextView) {
                    ((TextView)view).setTextSize(textSize);
                }
            }
        });
    }

    public static void doAdaptViews(@NonNull WindowManager winMgr, @NonNull View... views){
        DisplayMetrics metric = new DisplayMetrics();
        winMgr.getDefaultDisplay().getMetrics(metric);

        final float aspectRatio = (float) (metric.widthPixels * 1.0 / metric.heightPixels);
        final boolean notTablet = aspectRatio > 1.5 && metric.heightPixels > 1000;

        final int buttonWidth = (int) (metric.widthPixels * RATIO_SCALE_WIDTH);
        final int textSize = (int) ((metric.heightPixels+metric.widthPixels)/ RATIO_SCALE_TEXT - (notTablet ? 10 : 0));

        final View main = views[0];
        main.post(() -> {
            for (View view : views) {

                // Update Buttons layout params(Width)
                if (view instanceof Button) {
                    ViewGroup.LayoutParams p=view.getLayoutParams();
                    p.width = buttonWidth;
                    view.setLayoutParams(p);
                }

                // Update TextView layout params(Text size)
                if (view instanceof TextView) {
                    ((TextView)view).setTextSize(textSize);
                }
            }
        });
    }

    public static Rect  getAdaptViewRect(View dst, Bitmap bitmap){

        int src_h = bitmap.getHeight();
        int src_w = bitmap.getWidth();
        int view_w = dst.getWidth();
        int view_h = dst.getHeight();

        float srcRatio  = (float) src_w  / (float) src_h;
        float viewRatio = (float) view_w / (float) view_h;
        int factWidth;
        int factHeight;
        int x1, y1, x2, y2;
        if (srcRatio > viewRatio) {
            factWidth  = view_w;
            factHeight = (int)(view_w / srcRatio);
            x1 = 0;
            y1 = (view_h - factHeight) / 2;
        } else {
            factHeight = view_h;
            factWidth = (int)(factHeight * srcRatio);
            x1 = (view_w - factWidth) / 2;
            y1 = 0;
        }
        x2 = x1 + factWidth;
        y2 = y1 + factHeight;
        return new Rect(x1, y1, x2, y2);
    }
}
