package com.example.cameradodi;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.RequiresPermission;

public class Camera {
    private CameraActivity cameraActivity;

    public Camera(Activity activity, int requestCode) {
        CameraActivity.Builder builder = new CameraActivity.Builder(activity, requestCode);
        this.cameraActivity = builder.build();
    }

    public Camera(CameraActivity cameraActivity) {
        this.cameraActivity = cameraActivity;
    }

    @RequiresPermission("android.permission.CAMERA")
    public void lauchCamera() {
        if (this.cameraActivity != null && this.cameraActivity.getActivity() != null) {
            Intent openCamera = new Intent(this.cameraActivity.getActivity(), CameraActivity.class);
            openCamera.putExtra("LOCK_FACE_CAMERA", this.cameraActivity.getLockFaceCamera());
            openCamera.putExtra("CAMERA_FACE", this.cameraActivity.getCameraFace());
            openCamera.putExtra("FLASH", this.cameraActivity.getFlashDefault());
            openCamera.putExtra("QUALITY", this.cameraActivity.getQuality());
            openCamera.putExtra("RATIO", this.cameraActivity.getAspectRatio());
            openCamera.putExtra("FACE", this.cameraActivity.getFaceDetect());
            openCamera.putExtra("FILE_NAME", this.cameraActivity.getFileName());
            openCamera.putExtra("REQUEST_CODE", this.cameraActivity.getRequestCode());
            ShrdPref.setFlag(this.cameraActivity.getActivity(), ShrdPref.START_CAMERA_LIB);
            this.cameraActivity.getActivity().startActivityForResult(openCamera, this.cameraActivity.getRequestCode());
        }
    }
}
