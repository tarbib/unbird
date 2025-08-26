package org.nuclearfog.nitterify.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.nuclearfog.nitterify.R;
import org.nuclearfog.nitterify.adapter.NetworkAdapter;
import org.nuclearfog.nitterify.store.Preferences;

/**
 * Main activity of this application, used to show network selector and handle link conversion
 *
 * @author nuclearfog
 */
public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener {

	private Preferences settings;

	private EditText urlInput;
	private View urlContainer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		Spinner selector = findViewById(R.id.network_selector);
		View confirmButton = findViewById(R.id.network_confirm);
		Button settingsButton = findViewById(R.id.open_app_settings);
		urlContainer = findViewById(R.id.network_input_container);
		urlInput = findViewById(R.id.network_input);

		settings = Preferences.get(getApplicationContext());
		NetworkAdapter adapter = new NetworkAdapter();

		selector.setAdapter(adapter);
		if (settings.getMode() == Preferences.MODE_DEFAULT) {
			selector.setSelection(NetworkAdapter.INDEX_DEFAULT, false);
			urlContainer.setVisibility(View.INVISIBLE);
		} else if (settings.getMode() == Preferences.MODE_CUSTOM) {
			selector.setSelection(NetworkAdapter.INDEX_CUSTOM, false);
			urlContainer.setVisibility(View.VISIBLE);
			urlInput.setText(settings.getDomain());
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
			settingsButton.setVisibility(View.VISIBLE);
		}
		selector.setOnItemSelectedListener(this);
		settingsButton.setOnClickListener(this);
		confirmButton.setOnClickListener(this);
	}


	@Override
	@SuppressLint("InlinedApi")
	public void onClick(View v) {
		if (v.getId() == R.id.network_confirm) {
			String url = urlInput.getText().toString();
			if (Patterns.WEB_URL.matcher(url).matches()) {
				urlInput.setText(url);
				settings.setDomain(url);
				settings.setMode(Preferences.MODE_CUSTOM);
				Toast.makeText(getApplicationContext(), R.string.info_instance_set, Toast.LENGTH_SHORT).show();
			} else {
				urlInput.setError(getString(R.string.error_wrong_url));
			}
		} else if (v.getId() == R.id.open_app_settings) {
			Intent intent = new Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:" + getPackageName()));
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.network_selector) {
			if (position == NetworkAdapter.INDEX_DEFAULT) {
				urlContainer.setVisibility(View.INVISIBLE);
				settings.setMode(Preferences.MODE_DEFAULT);
				settings.setDomain("");
			} else if (position == NetworkAdapter.INDEX_CUSTOM) {
				urlContainer.setVisibility(View.VISIBLE);
				urlInput.setText(settings.getDomain());
			}
		}
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
}