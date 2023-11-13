package com.example.cameradodi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.size.SizeSelector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private Activity activity;
    private int requestCode;
    private CameraView camera;
    private RelativeLayout cameraTools1;
    private RelativeLayout cameraTools2;
    private RelativeLayout columnPickAsk;
    private ImageButton capturePhoto;
    private SeekBar bar;
    private ImageSwitcher flash;
    private ImageSwitcher switcher;
    private ImageView aspectRatioDialog;
    private TouchImageView preview;
    private TextView askCancel;
    private TextView askRetry;
    private TextView askSave;
    AlertDialog ratioDialog;
    String size;
    private String path;
    Bitmap picture;
    Bitmap rotated;
    Bitmap coba;
    byte[] pict;
    boolean landscape;
    private int lockFaceCamera;
    private int aspectRatio;
    private int quality;
    private int flashDefault;
    private int cameraFace;
    private int faceDetect;
    SizeSelector dimensions;
    SizeSelector ratio;
    SizeSelector result;
    private FrameProcessor frameProcessor;
    byte[] data;
    int rotation;
    int format;
    int width;
    int height;
    boolean fd;
    int a;
    public static final int CAMERA_CODE = 707;
    public static final int CAMERA_REAR = 0;
    public static final int CAMERA_FRONT = 1;
    public static final int FLASH_OFF = 0;
    public static final int FLASH_ON = 1;
    public static final int FLASH_AUTO = 2;
    public static final int UNLOCK_SWITCH_CAMERA = 0;
    public static final int LOCK_SWITCH_CAMERA = 1;
    public static final int LOW = 50;
    public static final int MEDIUM = 70;
    public static final int HIGH = 100;
    public static final int RATIO_4_3 = 0;
    public static final int RATIO_16_9 = 1;
    public static final int FACE_UNDETECT = 0;
    public static final int FACE_DETECT = 1;

    public CameraActivity() {
        this.activity = null;
        this.requestCode = -1;
        this.landscape = false;
        this.a = 0;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_camera);
        this.hideStatusBar();
        this.camera = (CameraView) this.findViewById(R.id.req_camera);
        this.cameraTools1 = (RelativeLayout)this.findViewById(R.id.cameraTools1);
        this.cameraTools2 = (RelativeLayout)this.findViewById(R.id.cameraTools2);
        this.columnPickAsk = (RelativeLayout)this.findViewById(R.id.column_pict_ask);
        this.capturePhoto = (ImageButton)this.findViewById(R.id.buttonCapture);
        this.bar = (SeekBar)this.findViewById(R.id.seekbar);
        this.flash = (ImageSwitcher)this.findViewById(R.id.flash);
        this.switcher = (ImageSwitcher)this.findViewById(R.id.switcher);
        this.preview = (TouchImageView)this.findViewById(R.id.image_preview);
        this.askCancel = (TextView)this.findViewById(R.id.ask_cancel);
        this.askRetry = (TextView)this.findViewById(R.id.ask_retry);
        this.askSave = (TextView)this.findViewById(R.id.ask_save);
        this.fd = false;
        this.cameraView();
        this.imageButton();
        this.seekBar();
        this.imageSwithcer();
    }

    public void onBackPressed() {
        if (this.preview.getVisibility() == View.VISIBLE) {
            this.recreate();
        } else {
            super.onBackPressed();
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        this.camera.destroy();
    }

    protected void onPause() {
        super.onPause();
        this.camera.close();
    }

    protected void onResume() {
        super.onResume();
        this.camera.open();
        this.hideStatusBar();
    }

    private void cameraView() {
        int flagnya = ShrdPref.getFlag(this);
        switch (flagnya) {
            case 0:
                this.quality = this.getIntent().getIntExtra("QUALITY", 0);
                this.aspectRatio = this.getIntent().getIntExtra("RATIO", 0);
                //this.setSizenya();
                ShrdPref.removeFlag(this);
                break;
            case 1:
                this.quality = 100;
                this.aspectRatio = 0;
                //this.setSizenya();
                ShrdPref.removeFlag(this);
                break;
            case 2:
                this.quality = 100;
                this.aspectRatio = 1;
                //this.setSizenya();
                ShrdPref.removeFlag(this);
                break;
            case 3:
                this.quality = 70;
                this.aspectRatio = 0;
                //this.setSizenya();
                ShrdPref.removeFlag(this);
                break;
            case 4:
                this.quality = 70;
                this.aspectRatio = 1;
                //this.setSizenya();
                ShrdPref.removeFlag(this);
                break;
            case 5:
                this.quality = 50;
                this.aspectRatio = 0;
                //this.setSizenya();
                ShrdPref.removeFlag(this);
        }

        this.camera.addCameraListener(new CameraListener() {
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
                if (orientation == 270) {
                    CameraActivity.this.landscape = true;
                } else {
                    CameraActivity.this.landscape = false;
                }

            }

            public void onCameraOpened(CameraOptions var1) {
                // $FF: Couldn't be decompiled
            }

            public void onPictureTaken(@NonNull PictureResult result) {

                super.onPictureTaken(result);
                Log.w("zharfan", "onPictureTaken: "+result);
                CameraActivity.this.pict = result.getData();
                CameraActivity.this.picture = BitmapFactory.decodeByteArray(result.getData(), 0, result.getData().length);
                if (CameraActivity.this.fd) {
                    CameraActivity.this.camera.clearFrameProcessors();
                }

                if (!CameraActivity.this.landscape) {
                    if (CameraActivity.this.picture.getWidth() > CameraActivity.this.picture.getHeight()) {
                        if (CameraActivity.this.camera.getFacing() == Facing.FRONT) {
                            CameraActivity.this.rotateImage(270);
                        } else {
                            CameraActivity.this.rotateImage(90);
                        }

                        CameraActivity.this.preview.setImageBitmap(CameraActivity.this.rotated);
                    } else {
                        CameraActivity.this.preview.setImageBitmap(CameraActivity.this.picture);
                    }
                } else if (CameraActivity.this.picture.getWidth() < CameraActivity.this.picture.getHeight()) {
                    CameraActivity.this.rotateImage(90);
                    CameraActivity.this.preview.setImageBitmap(CameraActivity.this.rotated);
                } else {
                    CameraActivity.this.preview.setImageBitmap(CameraActivity.this.picture);
                }

                CameraActivity.this.preview.setVisibility(View.VISIBLE);
                CameraActivity.this.columnPickAsk.setVisibility(View.VISIBLE);
                CameraActivity.this.columnPickAsk.setBackgroundColor(Color.parseColor("#40000000"));
                CameraActivity.this.askCancel.setTextColor(Color.parseColor("#FFFFFF"));
                CameraActivity.this.askRetry.setTextColor(Color.parseColor("#FFFFFF"));
                CameraActivity.this.askSave.setTextColor(Color.parseColor("#FFFFFF"));
                CameraActivity.this.cameraTools1.setVisibility(View.INVISIBLE);
                CameraActivity.this.cameraTools2.setVisibility(View.INVISIBLE);
                CameraActivity.this.camera.setVisibility(View.INVISIBLE);
            }

            @SuppressLint({"WrongThread"})
            public void onCameraClosed() {
                super.onCameraClosed();
            }
        });
    }

    private void rotateImage(int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float)degree);
        this.rotated = Bitmap.createBitmap(this.picture, 0, 0, this.picture.getWidth(), this.picture.getHeight(), matrix, true);
    }

    private void imageButton() {
        this.capturePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (CameraActivity.this.fd) {
                    CameraActivity.this.camera.takePictureSnapshot();
                } else {
                    CameraActivity.this.camera.takePicture();
                }

            }
        });
        this.askCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent putPhoto = new Intent();
                CameraActivity.this.setResult(0, putPhoto);
                CameraActivity.this.finish();
            }
        });
        this.askRetry.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CameraActivity.this.onBackPressed();
            }
        });
        this.askSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //CameraActivity.this.savePhoto();
            }
        });
    }

    private void seekBar() {
        this.bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float level = (float)(i - 3);
                CameraActivity.this.camera.setExposureCorrection(level);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void imageSwithcer() {
        this.flash.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                ImageView flashView = new ImageView(CameraActivity.this.getApplicationContext());
                flashView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                flashView.setLayoutParams(new FrameLayout.LayoutParams(60, 60));
                return flashView;
            }
        });
        this.flash.setImageResource(R.drawable.outline_flash_off_white_18dp);
        this.flash.setOnClickListener(new View.OnClickListener() {
            int fl = 0;

            public void onClick(View view) {
                if (this.fl == 0) {
                    CameraActivity.this.flash.setImageResource(R.drawable.outline_flash_on_white_18dp);
                    CameraActivity.this.camera.setFlash(Flash.ON);
                    this.fl = 1;
                } else if (this.fl == 1) {
                    CameraActivity.this.flash.setImageResource(R.drawable.outline_flash_auto_white_18dp);
                    CameraActivity.this.camera.setFlash(Flash.AUTO);
                    this.fl = 2;
                } else if (this.fl == 2) {
                    CameraActivity.this.flash.setImageResource(R.drawable.outline_flash_off_white_18dp);
                    CameraActivity.this.camera.setFlash(Flash.OFF);
                    this.fl = 0;
                }

            }
        });
        this.switcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                ImageView switcherView = new ImageView(CameraActivity.this.getApplicationContext());
                switcherView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                switcherView.setLayoutParams(new FrameLayout.LayoutParams(60, 60));
                return switcherView;
            }
        });
        this.switcher.setImageResource(R.drawable.baseline_camera_front_white_18dp);
        this.switcher.setOnClickListener(new View.OnClickListener() {
            int sw = 0;

            public void onClick(View view) {
                if (this.sw == 0) {
                    CameraActivity.this.switcher.setImageResource(R.drawable.baseline_camera_rear_white_18dp);
                    CameraActivity.this.camera.setFacing(Facing.FRONT);
                    this.sw = 1;
                } else if (this.sw == 1) {
                    CameraActivity.this.switcher.setImageResource(R.drawable.baseline_camera_front_white_18dp);
                    CameraActivity.this.camera.setFacing(Facing.BACK);
                    this.sw = 0;
                }

            }
        });
    }

    private void showAspectRatio() {
        this.size = this.camera.getPictureSize().toString();
        int choice = -1;
        switch (this.size) {
            case "1024x768":
                choice = 0;
                break;
            case "1280x720":
                choice = 1;
                break;
            case "800x600":
                choice = 2;
                break;
            case "640x360":
                if (this.quality == 70) {
                    choice = 3;
                }

                if (this.quality == 50) {
                    choice = 5;
                }
                break;
            case "640x480":
                choice = 4;
        }

        CharSequence[] ratioList = new CharSequence[]{"4:3 (High)", "16:9 (High)", "4:3 (Medium)", "16:9 (Medium)", "4:3 (Low)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose picture size");
        builder.setSingleChoiceItems(ratioList, choice, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case 0:
                        ShrdPref.setFlag(CameraActivity.this.getBaseContext(), 1);
                        CameraActivity.this.recreate();
                        break;
                    case 1:
                        ShrdPref.setFlag(CameraActivity.this.getBaseContext(), 2);
                        CameraActivity.this.recreate();
                        break;
                    case 2:
                        ShrdPref.setFlag(CameraActivity.this.getBaseContext(), 3);
                        CameraActivity.this.recreate();
                        break;
                    case 3:
                        ShrdPref.setFlag(CameraActivity.this.getBaseContext(), 4);
                        CameraActivity.this.recreate();
                        break;
                    case 4:
                        ShrdPref.setFlag(CameraActivity.this.getBaseContext(), 5);
                        CameraActivity.this.recreate();
                }

                CameraActivity.this.ratioDialog.dismiss();
                CameraActivity.this.hideStatusBar();
            }
        });
        this.ratioDialog = builder.create();
        this.ratioDialog.setCancelable(false);
        this.ratioDialog.show();
        this.hideStatusBar();
    }

    private void hideStatusBar() {
        View decorView = this.getWindow().getDecorView();
        int uiOptions = 4;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /*private void setSizenya() {
        switch (this.quality) {
            case 50:
                this.camera.setJpegQuality(50);
                if (this.aspectRatio == 0) {
                    this.dimensions = SizeSelectors.and(new SizeSelector[]{SizeSelectors.maxWidth(750), SizeSelectors.maxHeight(750)});
                }
                break;
            case 70:
                this.camera.setJpegQuality(70);
                if (this.aspectRatio != 0) {
                    this.dimensions = SizeSelectors.and(new SizeSelector[]{SizeSelectors.maxWidth(1250), SizeSelectors.maxHeight(1250)});
                } else {
                    this.dimensions = SizeSelectors.and(new SizeSelector[]{SizeSelectors.maxWidth(1000), SizeSelectors.maxHeight(1000)});
                }
                break;
            case 100:
                this.camera.setJpegQuality(100);
                if (this.aspectRatio != 0) {
                    this.dimensions = SizeSelectors.and(new SizeSelector[]{SizeSelectors.maxWidth(1500), SizeSelectors.maxHeight(1500)});
                } else {
                    this.dimensions = SizeSelectors.and(new SizeSelector[]{SizeSelectors.maxWidth(1250), SizeSelectors.maxHeight(1250)});
                }
        }

        switch (this.aspectRatio) {
            case 0:
                this.ratio = SizeSelectors.aspectRatio(AspectRatio.of(3, 4), 0.0F);
                break;
            case 1:
                this.ratio = SizeSelectors.aspectRatio(AspectRatio.of(9, 16), 0.0F);
        }

        this.result = SizeSelectors.and(new SizeSelector[]{this.dimensions, this.ratio, SizeSelectors.biggest()});
        this.camera.setPictureSize(this.result);
    }*/


    private void savePhoto() {
        File pictures = new File(Environment.getExternalStorageDirectory(), "Pictures");
        if (!pictures.exists()) {
            pictures.mkdir();
        }

        File byonchat = new File(Environment.getExternalStorageDirectory(), "Pictures/com.byonchat.android");
        if (!byonchat.exists()) {
            byonchat.mkdir();
        }

        File photo = new File(Environment.getExternalStorageDirectory(), "Pictures/com.byonchat.android/" + this.path);
        BitmapDrawable drawable = (BitmapDrawable)this.preview.getDrawable();
        this.coba = drawable.getBitmap();

        try {
            int MAX_IMAGE_SIZE = 102400;
            int compressQuality = 100;

            int streamLength;
            do {
                ByteArrayOutputStream bmpStream = new ByteArrayOutputStream();
                this.coba.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream);
                byte[] bmpPicByteArray = bmpStream.toByteArray();
                streamLength = bmpPicByteArray.length;
                compressQuality -= 5;
            } while(streamLength >= 102400);

            FileOutputStream test = new FileOutputStream(photo.getAbsolutePath());
            this.coba.compress(Bitmap.CompressFormat.JPEG, compressQuality, test);
            test.flush();
            test.close();
            Intent putPhoto = new Intent();
            putPhoto.putExtra("PICTURE", photo.getAbsolutePath());
            this.setResult(-1, putPhoto);
            this.finish();
        } catch (IOException var10) {
            var10.printStackTrace();
            Log.w("zharfan", "Message : " + var10.getMessage());
            Intent putPhoto = new Intent();
            this.setResult(0, putPhoto);
            this.finish();
        }

    }

    private CameraActivity(Activity activity, int requestCode) {
        this.activity = null;
        this.requestCode = -1;
        this.landscape = false;
        this.a = 0;
        this.activity = activity;
        this.requestCode = requestCode;
    }

    public Activity getActivity() {
        return this.activity;
    }

    public int getRequestCode() {
        return this.requestCode;
    }

    public int getLockFaceCamera() {
        return this.lockFaceCamera;
    }

    public int getAspectRatio() {
        return this.aspectRatio;
    }

    public int getQuality() {
        return this.quality;
    }

    public int getFaceDetect() {
        return this.faceDetect;
    }

    public int getFlashDefault() {
        return this.flashDefault;
    }

    public int getCameraFace() {
        return this.cameraFace;
    }

    public String getFileName() {
        return this.path;
    }

    public static class Builder {
        private CameraActivity cameraActivity;

        public Builder(Activity activity, int requestCode) {
            this.cameraActivity = new CameraActivity(activity, requestCode);
        }

        public Builder setCameraFace(int cameraFace) {
            this.cameraActivity.cameraFace = cameraFace;
            return this;
        }

        public Builder setFlashMode(int flashMode) {
            this.cameraActivity.flashDefault = flashMode;
            return this;
        }

        public Builder setLockSwitch(int lockSwitch) {
            this.cameraActivity.lockFaceCamera = lockSwitch;
            return this;
        }

        public Builder setQuality(int quality) {
            this.cameraActivity.quality = quality;
            return this;
        }

        public Builder setRatio(int ratio) {
            this.cameraActivity.aspectRatio = ratio;
            return this;
        }

        public Builder setFaceDetect(int faceDetect) {
            this.cameraActivity.faceDetect = faceDetect;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.cameraActivity.path = fileName;
            return this;
        }

        public CameraActivity build() throws IllegalArgumentException {
            if (this.cameraActivity.requestCode < 0) {
                throw new IllegalArgumentException("Wrong request code. Please set the value > 0");
            } else {
                return this.cameraActivity;
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FaceDetect {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Ratio {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Quality {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SwitchMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface FlashMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraFace {
    }
}