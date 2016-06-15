package com.ae.apps.snowday.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.ae.apps.snowday.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by user on 1/21/2016.
 */
public class SnowChecker {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private Context mContext;
    private boolean isCheckingForSnow = false;

    public SnowChecker(Context context){
        this.mContext = context;
    }

    public void checkForSnow(final String directory, final View snackBarView){
        String fileName = getImageFileName(directory);

        // http://developer.android.com/training/camera/cameradirect.html
        // http://stackoverflow.com/questions/10775942/android-sdk-get-raw-preview-camera-image-without-displaying-it/10776349#10776349

        // http://stackoverflow.com/questions/2386025/taking-picture-from-camera-without-preview/14227517#14227517
        final SurfaceView preview = new SurfaceView(mContext);
        preview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                if(false == isCheckingForSnow){
                    isCheckingForSnow = true;
                    Camera camera = null;
                    try {
                        camera = Camera.open();

                        try {
                            // preview is a dummy surface view that is never attached to the screen
                            camera.setPreviewDisplay(preview.getHolder());
                        } catch (IOException ex) {
                            isCheckingForSnow = false;
                            throw new RuntimeException(ex);
                        }

                        // Start preview to a dummy surface view before taking the picture
                        camera.startPreview();

                        // Take a picture with the camera when you are ready, should do some autofocus
                        camera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] bytes, Camera camera) {
                                camera.release();

                                // Decode and save the image
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                if(null != bitmap){

                                    try{
                                        File file = new File(getImageFileName(directory));
                                        writeBitmapToDisk(bitmap, file);

                                        // Fix for orientation issues
                                        // http://stackoverflow.com/questions/11674816/android-image-orientation-issue-with-custom-camera-activity?lq=1

                                        ExifInterface exifInterface = new ExifInterface(file.getPath());
                                        int exifOrientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                                        int rotate = 0;
                                        switch(exifOrientation){
                                            case ExifInterface.ORIENTATION_ROTATE_90:
                                                rotate = 90;
                                                break;
                                            case ExifInterface.ORIENTATION_ROTATE_180:
                                                rotate = 180;
                                                break;
                                            case ExifInterface.ORIENTATION_ROTATE_270:
                                                rotate = 270;
                                                break;
                                        }

                                        if(rotate != 0){
                                            int w = bitmap.getWidth();
                                            int h = bitmap.getHeight();

                                            Matrix matrix = new Matrix();
                                            matrix.preRotate(rotate);

                                            // Rotate and re save the image
                                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, false);

                                            writeBitmapToDisk(bitmap, file);
                                        }

                                    } catch(Exception ex){

                                    }
                                }
                                if(null != bitmap){
                                    bitmap.recycle();
                                }

                                // Show a SnackBar as feedback on completing this task successfully
                                Snackbar.make(snackBarView, R.string.snowday_snack, Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    }catch(Exception ex) {
                        if (null != camera) {
                            camera.release();
                        }
                        throw new RuntimeException(ex);
                    }
                    isCheckingForSnow = false;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });

        // This requires the SYSTEM_ALERT_WINDOW permission
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0, PixelFormat.UNKNOWN);
        windowManager.addView(preview, layoutParams);
    }

    private void writeBitmapToDisk(Bitmap bitmap, File file) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);

        fos.flush();
        fos.close();
    }


    public Intent getDeviceCamera(final String directory) {
        return null;
    }


    @NonNull
    private String getImageFileName(String directory) {
        Date currentTime = Calendar.getInstance().getTime();

        // construct the file name
        return directory + DATE_FORMAT.format(currentTime) + ".jpg";
    }
}
