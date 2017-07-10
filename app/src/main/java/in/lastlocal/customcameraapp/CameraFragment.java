package in.lastlocal.customcameraapp;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
	//	boolean hasFlash, isFlashOn = false, hasPrimaryCamera = true;
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

//		hasFlash = ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
		preview = new Preview(ctx, getActivity(), (SurfaceView) view.findViewById(R.id.surfaceView));
		preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		rootLayout = ((FrameLayout) view.findViewById(R.id.layout));
		rootLayout.addView(preview);

		imageView = (ImageView) view.findViewById(R.id.img);
		preview.setKeepScreenOn(true);

		buttonClick = (Button) view.findViewById(R.id.button_capture);
		buttonFlash = (Button) view.findViewById(R.id.btn_flash);
		buttonRotate = (Button) view.findViewById(R.id.btn_rotate);
		buttonRotate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {


//				camera.stopPreview();
//
////NB: if you don't release the current camera before switching, you app will crash
//				camera.release();
//				camera = null;
////swap the id of the camera to be used
//				if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
//					mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
//				} else {
//					mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
//				}
//				camera = Camera.open(mCameraId);
//
//				setCameraDisplayOrientation(getActivity(), mCameraId, camera);
//				preview.setCamera(camera);
//				camera.startPreview();


				preview.switchCamera(mCameraId);

				mCameraId = preview.getCameraID();

				if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
					buttonFlash.setEnabled(false);
				else
					buttonFlash.setEnabled(true);


			}
		});
		buttonFlash.setOnClickListener(new View.OnClickListener()

		{
			@Override
			public void onClick(View view) {

				preview.setflash(Camera.CameraInfo.CAMERA_FACING_BACK);
			}
		});
		buttonClick.setOnClickListener(new View.OnClickListener()

		{

			@Override
			public void onClick(View arg0) {

				preview.getmCamera().takePicture(shutterCallback, rawCallback, jpegCallback);
			}
		});


		Toast.makeText(ctx, getString(R.string.take_photo_help), Toast.LENGTH_LONG).show();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		preview.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		preview.onPause();
	}

	private void resetCam() {
		preview.getmCamera().startPreview();
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
				if (mCameraId == 0)
					myBitmap = rotateBitmap(myBitmap, 90);
				else
					myBitmap = rotateBitmap(myBitmap, 270);
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

//	private void resetView() {
//		if (camera != null) {
//			camera.stopPreview();
//			preview.setCamera(null);
//			camera.release();
//			camera = null;
//		}
//	}
//
//	private void rotateScreen(int cameraId) {
//
//		preview = new Preview(ctx, (SurfaceView) view.findViewById(R.id.surfaceView));
//		preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//		rootLayout.addView(preview);
//	}

}
