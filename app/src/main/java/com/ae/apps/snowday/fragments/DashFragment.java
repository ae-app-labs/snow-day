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
import com.ae.apps.snowday.utils.SnowChecker;

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

        final SnowChecker snowChecker = new SnowChecker(mContext);

        Button btnSeeMore = (Button) mLayout.findViewById(R.id.btnSeeMore);

        btnSeeMore.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                // Request the OS for an image from the Camera, this is a standard approach but we don't want this

                // Intent intent = snowChecker.getDeviceCamera(directory);
                // startActivityForResult(intent, SEE_ACTION_REQUEST_CODE);
                Snackbar.make(mLayout, R.string.str_no_updates, Snackbar.LENGTH_SHORT)
                        .show();
            }
        });


        // Take image using CameraAPI - this is what we want with this app
        Button btnSeeMoreMore = (Button) mLayout.findViewById(R.id.btnSeeMore2);
        btnSeeMoreMore.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                snowChecker.checkForSnow(directory, mLayout);
            }

        });

        // Assign a click event for the FAB
        FloatingActionButton floatingActionButton = (FloatingActionButton) mLayout.findViewById(R.id.myFAB);
        floatingActionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                // snowChecker.checkForSnow(directory, mLayout);
                Snackbar.make(mLayout, R.string.str_no_updates, Snackbar.LENGTH_SHORT)
                        .show();
            }
        });

        return mLayout;
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
