package com.ae.apps.snowday.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ae.apps.snowday.R;

import java.io.File;
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
                                    File file = new File(getImageFileName(directory));

                                    try{
                                        FileOutputStream fos = new FileOutputStream(file);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);

                                        fos.flush();
                                        fos.close();

                                        // Fix for orientation issues
                                        // http://stackoverflow.com/questions/11674816/android-image-orientation-issue-with-custom-camera-activity?lq=1
                                    } catch(Exception ex){

                                    }
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

    public Intent getDeviceCamera(final String directory){
        String fileName = getImageFileName(directory);

        File targetFile = new File(fileName);

        try{
            targetFile.createNewFile();
        } catch (IOException e){
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Uri outputFileUri = Uri.fromFile(targetFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        return intent;
    }


    @NonNull
    private String getImageFileName(String directory) {
        Date currentTime = Calendar.getInstance().getTime();

        // construct the file name
        return directory + DATE_FORMAT.format(currentTime) + ".jpg";
    }
}
