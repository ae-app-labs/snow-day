package com.ae.apps.snowday.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ae.apps.snowday.R;
import com.ae.apps.snowday.utils.CameraCallback;
import com.ae.apps.snowday.utils.CameraPreview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashFragment extends android.support.v4.app.Fragment {

    public static final String SNOW_DAY = "/SnowDay/";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    private static final int SEE_ACTION_REQUEST_CODE = 5060;
    private Context mContext;
    private boolean isCheckingForSnow = false;

    private View mLayout;

    public DashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mLayout = inflater.inflate(R.layout.fragment_dash, container, false);

        // Set a Random forecast for the SnowDay
        TextView snowdayAnswer = (TextView) mLayout.findViewById(R.id.snowday_a);
        String[] forecast = getResources().getStringArray(R.array.snowday_forecast);
        Random random = new Random();
        snowdayAnswer.setText(forecast[random.nextInt(forecast.length)]);

        // The coordinator layout is needed to display the snackbar
        final View coordinatorLayout = mLayout.findViewById(R.id.snackbarPosition);

        final String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + SNOW_DAY;

        // Create the directory if it doesn't exist
        File snowDayPicturesDir = new File(directory);
        boolean mkdirsResult = snowDayPicturesDir.mkdirs();

        // Request the OS for an image from the Camera, this is a standard approach but will be deprecated
        Button btnSeeMore = (Button) mLayout.findViewById(R.id.btnSeeMore);
        btnSeeMore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
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

                startActivityForResult(intent, SEE_ACTION_REQUEST_CODE);
            }
        });

        // Take image using CameraAPI - this is what we want with this app
        Button btnSeeMoreMore = (Button) mLayout.findViewById(R.id.btnSeeMore2);
        btnSeeMoreMore.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {


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
                                        Snackbar.make(mLayout, R.string.snowday_snack, Snackbar.LENGTH_SHORT)
                                                .show();
                                    }
                                });
                            }catch(Exception ex) {
                                if (null != camera) {
                                    camera.release();
                                    ;
                                }
                                isCheckingForSnow = false;
                                throw new RuntimeException(ex);
                            }
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
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton) mLayout.findViewById(R.id.myFAB);
        floatingActionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

            }
        });

        return mLayout;
    }

    @NonNull
    private String getImageFileName(String directory) {
        Date currentTime = Calendar.getInstance().getTime();

        // construct the file name
        return directory + DATE_FORMAT.format(currentTime) + ".jpg";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SEE_ACTION_REQUEST_CODE){
            String resultStr = "Failed";
            if( resultCode == -1){
                resultStr = "Success";
            }
            Toast.makeText(mContext, resultStr, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
