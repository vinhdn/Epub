package com.vinhblue.epub.Utils;

/**
 * Created by trungdo on 3/31/15.
 */
import android.content.Context;
import android.util.DisplayMetrics;

public class ConvertUtil {

    /**
     * px dip into
     *
     * @param dipValue
     * @return
     */
    public static int convertDips2Pixels(Context context, float dipValue) {
        return (int) convertDpToPixel(context, dipValue);
    }

    /**
     * This method convets dp unit to equivalent device specific value in
     * pixels.
     *
     *            A value in dp(Device independent pixels) unit. Which we need
     *            to convert into pixels
     * @param context
     *            Context to get resources and device specific display metrics
     * @return A float value to represent Pixels equivalent to dp according to
     *         device
     */
    public static float convertDpToPixel(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dipValue * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to device independent pixels.
     *
     *            A value in px (pixels) unit. Which we need to convert into db
     * @param context
     *            Context to get resources and device specific display metrics
     * @return A float value to represent db equivalent to px value
     */
    public static float convertPixelsToDp(Context context, float pixels) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float dp = pixels / (metrics.densityDpi / 160f);
        return dp;
    }
}