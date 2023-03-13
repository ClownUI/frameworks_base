package com.evillium.prjct.utils;

import android.annotation.ColorInt;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.os.UserHandle;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.widget.TextView;
import java.util.Random;

public class EvlUtils {

    public static final int transColor = 0x00FFFFFF;
    public static final int transValue = 24;

    public static Bitmap resizeMaxDeviceSize(Context context, Drawable image) {
        Bitmap i2b = ((BitmapDrawable) image).getBitmap();
        return resizeMaxDeviceSize(context, i2b);
    }

    public static Bitmap resizeMaxDeviceSize(Context context, Bitmap image) {
        Bitmap imageToBitmap;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = context.getSystemService(WindowManager.class);
        wm.getDefaultDisplay().getRealMetrics(metrics);
        int maxHeight = metrics.heightPixels;
        int maxWidth = metrics.widthPixels;
        try {
            imageToBitmap = RGB565toARGB888(image);
            if (maxHeight > 0 && maxWidth > 0) {
                int width = imageToBitmap.getWidth();
                int height = imageToBitmap.getHeight();
                float ratioBitmap = (float) width / (float) height;
                float ratioMax = (float) maxWidth / (float) maxHeight;

                int finalWidth = maxWidth;
                int finalHeight = maxHeight;
                if (ratioMax > ratioBitmap) {
                    finalWidth = (int) ((float)maxHeight * ratioBitmap);
                } else {
                    finalHeight = (int) ((float)maxWidth / ratioBitmap);
                }
                imageToBitmap = Bitmap.createScaledBitmap(imageToBitmap, finalWidth, finalHeight, true);
                return imageToBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    private static Bitmap RGB565toARGB888(Bitmap img) throws Exception {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];
        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }

    public static int getValueInDp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                                                    Resources.getSystem().getDisplayMetrics()));
    }

    public static int dpToPx(float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                                                    Resources.getSystem().getDisplayMetrics()));
    }

    public static int getResources(Context context, String res) {
        return context.getResources().getIdentifier(res, null, context.getPackageName());
    }

    public static int getOrientation() {
        return Resources.getSystem().getConfiguration().orientation;
    }

    public static boolean getQSRowsColumns(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(),
                                             "evl_qs_rows_and_columns",
                                             0, UserHandle.USER_CURRENT) != 0;
    }

    public static int getQSMaxRowsLandscape(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(),
                                             "evl_qs_max_rows_landscape", 2, UserHandle.USER_CURRENT);
    }

    public static int getQSMaxRowsPotrait(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(),
                                             "evl_qs_max_rows_potrait", 4, UserHandle.USER_CURRENT);
    }

    public static int getQSNumColumns(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(),
                                             "evl_qs_num_columns", 2, UserHandle.USER_CURRENT);
    }

    public static int getQSMaxTiles(Context context) {
        return Settings.System.getIntForUser(context.getContentResolver(),
                                             "evl_qs_max_tiles", 4, UserHandle.USER_CURRENT);
    }

    /* screenShot routine */
    public static Bitmap screenshotSurface(Context context) {
        float BITMAP_SCALE = 0.35f;
        Bitmap bitmap;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        defaultDisplay.getRealMetrics(displayMetrics);
        final IBinder displayToken = SurfaceControl.getInternalDisplayToken();
        final SurfaceControl.DisplayCaptureArgs captureArgs =
            new SurfaceControl.DisplayCaptureArgs.Builder(displayToken)
            //.setSourceCrop(crop)
            .setSize(Math.round(displayMetrics.widthPixels * BITMAP_SCALE),
                     Math.round(displayMetrics.heightPixels * BITMAP_SCALE))
            .build();
        final SurfaceControl.ScreenshotHardwareBuffer screenshotBuffer =
            SurfaceControl.captureDisplay(captureArgs);
        final Bitmap screenshot = screenshotBuffer == null ? null : screenshotBuffer.asBitmap();
        bitmap = screenshot;
        if (bitmap == null) {
            Log.e("ScreenShotHelper", "screenShotBitmap error bitmap is null");
            return null;
        }
        bitmap.prepareToDraw();
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    /* blur routine */
    public static Bitmap blurImage(Context context, Bitmap inputBitmap, int intensity) {
        float BLUR_RADIUS = 7.5f;

        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        if (intensity > 0 && intensity <= 100) {
            BLUR_RADIUS = (float) intensity * 0.25f;
        }
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    public static Bitmap getBitmapScreenSize(Context context, Bitmap bitmap) {
        int sourceWidth = bitmap.getWidth();
        int sourceHeight = bitmap.getHeight();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        defaultDisplay.getRealMetrics(displayMetrics);
        int widthScreen = displayMetrics.widthPixels;
        int heightScreen = displayMetrics.heightPixels;

        float scaleX = (float) widthScreen / sourceWidth;
        float scaleY = (float) heightScreen / sourceHeight;
        float scale = Math.max(scaleX, scaleY);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (widthScreen - scaledWidth) / 2;
        float top = (heightScreen - scaledHeight) / 2;

        RectF rectF = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap bm = Bitmap.createBitmap(widthScreen, heightScreen, bitmap.getConfig());

        Canvas canvas = new Canvas(bm);
        canvas.drawBitmap(bitmap, null, rectF, null);
        return bm;
    }

    @ColorInt
    public static int getColorAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[] {attr});
        @ColorInt int color = ta.getColor(0, 0);
        ta.recycle();
        return color;
    }

    public static Drawable getDrawableAttr(Context context, int attr) {
        TypedArray ta = context.obtainStyledAttributes(new int[] {attr});
        Drawable drawable = ta.getDrawable(0);
        ta.recycle();
        return drawable;
    }

    public static int applyAlphaColor(int color, int value) {
        return (color & transColor) | (value << transValue);
    }

    public static int getRandomColor(Context context) {
        final Random random = new Random();
        // This is the base color which will be mixed with the generated one
        final int color = isThemeDark(context) ? transColor : Color.GRAY;

        final int red = Color.red(color);
        final int green = Color.green(color);
        final int blue = Color.blue(color);

        final int r = (red + random.nextInt(256)) / 2;
        final int g = (green + random.nextInt(256)) / 2;
        final int b = (blue + random.nextInt(256)) / 2;

        return Color.rgb(r, g, b);
    }

    public static int getLightDarkColor(Context context, int lightColor, int darkColor) {
        Resources res = context.getResources();
        return !isThemeDark(context) ? res.getColor(lightColor) : res.getColor(darkColor);
    }

    private static Boolean isThemeDark(Context context) {
        switch (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_NO:
                return false;
            default:
                return false;
        }
    }

    public static void setFontStyle(TextView textView, int font) {
        switch (font) {
            case 0:
            default:
                textView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                break;
            case 1:
                textView.setTypeface(Typeface.create("sans-serif", Typeface.ITALIC));
                break;
            case 2:
                textView.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                break;
            case 3:
                textView.setTypeface(Typeface.create("sans-serif", Typeface.BOLD_ITALIC));
                break;
            case 4:
                textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                break;
            case 5:
                textView.setTypeface(Typeface.create("sans-serif-light", Typeface.ITALIC));
                break;
            case 6:
                textView.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
                break;
            case 7:
                textView.setTypeface(Typeface.create("sans-serif-thin", Typeface.ITALIC));
                break;
            case 8:
                textView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                break;
            case 9:
                textView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.ITALIC));
                break;
            case 10:
                textView.setTypeface(Typeface.create("sans-serif-condensed-light", Typeface.NORMAL));
                break;
            case 11:
                textView.setTypeface(Typeface.create("sans-serif-condensed-light", Typeface.ITALIC));
                break;
            case 12:
                textView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
                break;
            case 13:
                textView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC));
                break;
            case 14:
                textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                break;
            case 15:
                textView.setTypeface(Typeface.create("sans-serif-medium", Typeface.ITALIC));
                break;
            case 16:
                textView.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                break;
            case 17:
                textView.setTypeface(Typeface.create("sans-serif-black", Typeface.ITALIC));
                break;
            case 18:
                textView.setTypeface(Typeface.create("cursive", Typeface.NORMAL));
                break;
            case 19:
                textView.setTypeface(Typeface.create("cursive", Typeface.BOLD));
                break;
            case 20:
                textView.setTypeface(Typeface.create("casual", Typeface.NORMAL));
                break;
            case 21:
                textView.setTypeface(Typeface.create("serif", Typeface.NORMAL));
                break;
            case 22:
                textView.setTypeface(Typeface.create("serif", Typeface.ITALIC));
                break;
            case 23:
                textView.setTypeface(Typeface.create("serif", Typeface.BOLD));
                break;
            case 24:
                textView.setTypeface(Typeface.create("serif", Typeface.BOLD_ITALIC));
                break;
            case 25:
                textView.setTypeface(Typeface.create("google-sans-medium", Typeface.NORMAL));
                break;
            case 26:
                textView.setTypeface(Typeface.create("google-sans-bold", Typeface.NORMAL));
                break;
            case 27:
                textView.setTypeface(Typeface.create("google-sans-text-italic", Typeface.NORMAL));
                break;
            case 28:
                textView.setTypeface(Typeface.create("archivonar", Typeface.NORMAL));
                break;
            case 29:
                textView.setTypeface(Typeface.create("badscript", Typeface.NORMAL));
                break;
            case 30:
                textView.setTypeface(Typeface.create("cherryswash", Typeface.NORMAL));
                break;
            case 31:
                textView.setTypeface(Typeface.create("codystar", Typeface.NORMAL));
                break;
            case 32:
                textView.setTypeface(Typeface.create("kellyslab", Typeface.NORMAL));
                break;
            case 33:
                textView.setTypeface(Typeface.create("reemkufi", Typeface.NORMAL));
                break;
            case 34:
                textView.setTypeface(Typeface.create("satisfy", Typeface.NORMAL));
                break;
            case 35:
                textView.setTypeface(Typeface.create("voltaire", Typeface.NORMAL));
                break;
            case 36:
                textView.setTypeface(Typeface.create("aclonica", Typeface.NORMAL));
                break;
            case 37:
                textView.setTypeface(Typeface.create("amarante", Typeface.NORMAL));
                break;
            case 38:
                textView.setTypeface(Typeface.create("bariol", Typeface.NORMAL));
                break;
            case 39:
                textView.setTypeface(Typeface.create("cagliostro", Typeface.NORMAL));
                break;
            case 40:
                textView.setTypeface(Typeface.create("lgsmartgothic", Typeface.NORMAL));
                break;
            case 41:
                textView.setTypeface(Typeface.create("op-sans", Typeface.NORMAL));
                break;
            case 42:
                textView.setTypeface(Typeface.create("rosemary", Typeface.NORMAL));
                break;
            case 43:
                textView.setTypeface(Typeface.create("sonysketch", Typeface.NORMAL));
                break;
            case 44:
                textView.setTypeface(Typeface.create("surfer", Typeface.NORMAL));
                break;
            case 45:
                textView.setTypeface(Typeface.create("oneplusslate", Typeface.NORMAL));
                break;
            case 46:
                textView.setTypeface(Typeface.create("samsungone-semi-bold", Typeface.NORMAL));
                break;
            case 47:
                textView.setTypeface(Typeface.create("comicsans", Typeface.NORMAL));
                break;
            case 48:
                textView.setTypeface(Typeface.create("montserrat", Typeface.NORMAL));
                break;
            case 49:
                textView.setTypeface(Typeface.create("arbutus-slab", Typeface.NORMAL));
                break;
            case 50:
                textView.setTypeface(Typeface.create("lato-bold", Typeface.NORMAL));
                break;
        }
    }

    public static boolean isPackageInstalled(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }
}
