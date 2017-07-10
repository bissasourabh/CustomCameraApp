package in.lastlocal.customcameraapp;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getFragmentManager().beginTransaction().replace(R.id.content, Camera2Fragment.newInstance()).commit();
		else
			getFragmentManager().beginTransaction().replace(R.id.content, CameraFragment.newInstance()).commit();


//		else
//			replaceFragment();
	}
}
