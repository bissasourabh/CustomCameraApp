package in.lastlocal.customcameraapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by user on 07-Jul-17.
 */

public class CameraFragment extends Fragment {

	private static final String TAG = "CamTestActivity";
	Preview preview;
	Button buttonClick, buttonFlash, buttonRotate;
	Camera camera;
	Context ctx;
	ImageView imageView;
	private int mCameraId = 0;
	boolean hasFlash, isFlashOn = false, hasPrimaryCamera = true;
	FrameLayout rootLayout;
	View view;


	public static CameraFragment newInstance() {
		return new CameraFragment();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		ctx = getActivity();
		view = inflater.inflate(R.layout.fragment_camera, container, false);

		hasFlash = ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		preview = new Preview(ctx, (SurfaceView) view.findViewById(R.id.surfaceView));
		preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		rootLayout = ((FrameLayout) view.findViewById(R.id.layout));
		rootLayout.addView(preview);

		imageView = (ImageView) view.findViewById(R.id.img);
		preview.setKeepScreenOn(true);

		buttonClick = (Button) view.findViewById(R.id.button_capture);
		buttonFlash = (Button) view.findViewById(R.id.btn_flash);

		// Spinner for camera ID
		Spinner spinnerCamera = (Spinner) view.findViewById(R.id.spinner_camera);
		ArrayAdapter<String> adapter;
		adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCamera.setAdapter(adapter);
		spinnerCamera.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
		adapter.add("0");
		adapter.add("1");
		adapter.add("2");

//		buttonRotate = (Button) view.findViewById(R.id.btn_rotate);
//		buttonRotate.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				if (Camera.getNumberOfCameras() < 2)
//					return;
//
//				resetView();
//
//				if (!hasPrimaryCamera)
//					mCameraId = 1;
//				else
//					mCameraId = 0;
//
//				rotateScreen(mCameraId);
//
//			}
//		});
		buttonFlash.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (hasFlash) {
					Camera.Parameters p = camera.getParameters();
					if (!isFlashOn) {
						isFlashOn = true;
						p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
						camera.setParameters(p);
						camera.startPreview();

					} else {
						isFlashOn = false;
						p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
						camera.setParameters(p);
						camera.startPreview();

					}
				}

			}
		});
		buttonClick.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
			}
		});

//		preview.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
//			}
//		});

		Toast.makeText(ctx, getString(R.string.take_photo_help), Toast.LENGTH_LONG).show();

		//		buttonClick = (Button) findViewById(R.id.btnCapture);
		//
		//		buttonClick.setOnClickListener(new OnClickListener() {
		//			public void onClick(View v) {
		////				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		//				camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		//			}
		//		});
		//
		//		buttonClick.setOnLongClickListener(new OnLongClickListener(){
		//			@Override
		//			public boolean onLongClick(View arg0) {
		//				camera.autoFocus(new AutoFocusCallback(){
		//					@Override
		//					public void onAutoFocus(boolean arg0, Camera arg1) {
		//						//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		//					}
		//				});
		//				return true;
		//			}
		//		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		int numCams = Camera.getNumberOfCameras();
		if (numCams > 0) {
			try {
				camera = Camera.open(0);
				camera.startPreview();
				preview.setCamera(camera);
			} catch (RuntimeException ex) {
				Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onPause() {
		if (camera != null) {
			camera.stopPreview();
			preview.setCamera(null);
			camera.release();
			camera = null;
		}
		super.onPause();
	}

	private void resetCam() {
		camera.startPreview();
		preview.setCamera(camera);
	}

	private void refreshGallery(File file) {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(Uri.fromFile(file));
		getActivity().sendBroadcast(mediaScanIntent);
	}

	Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
		public void onShutter() {
			//			 Log.d(TAG, "onShutter'd");
		}
	};

	Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			//			 Log.d(TAG, "onPictureTaken - raw");
		}
	};

	Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SaveImageTask().execute(data);
			resetCam();
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};

	public static Bitmap rotateBitmap(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

		File outFile;

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			if (outFile.exists()) {

				Bitmap myBitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());
				imageView.setImageBitmap(myBitmap);

			}
		}

		@Override
		protected Void doInBackground(byte[]... data) {
			FileOutputStream outStream = null;
			// Write to SD Card
			try {
//				File sdCard = Environment.getDataDirectory();
//				File dir = new File(sdCard.getAbsolutePath() + "/camtest");
//				dir.mkdirs();
				String fileName = String.format("%d.jpg", System.currentTimeMillis());
				outFile = new File(getActivity().getExternalFilesDir(null), fileName);

				outStream = new FileOutputStream(outFile);
				outStream.write(data[0]);
				outStream.flush();
				outStream.close();

				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

				refreshGallery(outFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			return null;
		}
	}

	private void resetView() {
		preview.stop();
		rootLayout.removeView(preview);
	}

	private void rotateScreen(int cameraId) {

		preview = new Preview(ctx, (SurfaceView) view.findViewById(R.id.surfaceView));
		preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		rootLayout.addView(preview);
	}

}
