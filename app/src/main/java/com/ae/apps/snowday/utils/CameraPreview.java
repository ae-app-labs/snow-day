package com.ae.apps.snowday.utils;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Gravity;
import android.view.TextureView;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * Created by user on 11/5/2015.
 */
public class CameraPreview  extends TextureView implements TextureView.SurfaceTextureListener{

    private Camera mCamera;

    public  CameraPreview(Context context, Camera camera){
        super(context);
        mCamera = camera;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        setLayoutParams(new FrameLayout.LayoutParams(previewSize.width, previewSize.height, Gravity.CENTER));

        try{
            mCamera.setPreviewTexture(surfaceTexture);
        } catch(IOException ex){

        }

        mCamera.startPreview();
        setVisibility(INVISIBLE);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
