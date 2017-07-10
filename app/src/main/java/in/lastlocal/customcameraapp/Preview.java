package in.lastlocal.customcameraapp;

/**
 * Created by user on 06-Jul-17.
 */

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;


public class Preview extends ViewGroup implements SurfaceHolder.Callback {
	private final String TAG = "Preview";

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;
	boolean isPreviewRunning = true;
	Context context;
	Activity activity;
	boolean hasFlash, isFlashOn = false, hasPrimaryCamera = true;
	int mCameraID;

	Preview(Context context, Activity activity, SurfaceView sv) {
		super(context);

		this.context = context;
		this.activity = activity;
		mSurfaceView = sv;
//        addView(mSurfaceView);

		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
			requestLayout();

			// get Camera parameters
			Camera.Parameters params = mCamera.getParameters();

			List<String> focusModes = params.getSupportedFocusModes();
			if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				// set the focus mode
				params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				// set Camera parameters
				mCamera.setParameters(params);
			}
		}
	}

	public void stop() {
		if (null == mCamera) {
			return;
		}
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		setMeasuredDimension(width, height);

		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height / previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0,
						(width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width / previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2,
						width, (height + scaledChildHeight) / 2);
			}
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	public Camera getmCamera() {
		return mCamera;
	}

	public int getCameraID() {
		return mCameraID;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null) return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mCamera != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			requestLayout();

			mCamera.setParameters(parameters);
			mCamera.setDisplayOrientation(90);
			mCamera.startPreview();
		}
	}


	public void switchCamera(int currentCameraId) {

		//here write code for switch the camera
		if (isPreviewRunning) {
			mCamera.stopPreview();
		}
//NB: if you don't release the current camera before switching, you app will crash
		mCamera.release();


//swap the id of the camera to be used
		if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
		} else {
			currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
		}
		mCamera = Camera.open(currentCameraId);

		setCameraDisplayOrientation(activity, currentCameraId, mCamera);
		try {

			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}

		mCameraID = currentCameraId;
		mCamera.startPreview();

	}

	public void setflash(int currentCameraId) {

		hasFlash = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

		if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
			if (hasFlash) {
				Camera.Parameters p = mCamera.getParameters();
				if (!isFlashOn) {
					isFlashOn = true;
					p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					mCamera.setParameters(p);
					mCamera.startPreview();

				} else {
					isFlashOn = false;
					p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					mCamera.setParameters(p);
					mCamera.startPreview();

				}
			}
		}

		mCameraID = currentCameraId;
	}

	public static void setCameraDisplayOrientation(Activity activity,
												   int cameraId, Camera camera) {
		Camera.CameraInfo info =
				new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}


	public void onResume() {

		int numCams = Camera.getNumberOfCameras();
		if (numCams > 0) {
			try {
				mCamera = Camera.open(0);
				mCamera.startPreview();
				setCamera(mCamera);
			} catch (RuntimeException ex) {
				Toast.makeText(context, context.getResources().getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
			}
		}

	}

	public void onPause() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
//			setCamera(null);
		}

	}

}
