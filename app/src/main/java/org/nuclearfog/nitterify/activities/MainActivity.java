package org.nuclearfog.nitterify.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
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
import org.nuclearfog.nitterify.backend.InstanceLoader;
import org.nuclearfog.nitterify.backend.InstanceLoader.InstanceCallback;
import org.nuclearfog.nitterify.model.Instance;
import org.nuclearfog.nitterify.store.Preferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity of this application, used to show network selector and handle link conversion
 *
 * @author nuclearfog
 */
public class MainActivity extends Activity implements OnClickListener, OnItemSelectedListener, DialogInterface.OnClickListener, InstanceCallback {

	private Preferences settings;
	private InstanceLoader instanceLoader;
	private List<Instance> instances;

	private Spinner selector;
	private EditText urlInput;
	private View urlContainer;
	private Button instanceButton;
	private AlertDialog instanceDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		selector = findViewById(R.id.network_selector);
		View confirmButton = findViewById(R.id.network_confirm);
		Button settingsButton = findViewById(R.id.open_app_settings);
		instanceButton = findViewById(R.id.choose_instance);
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
		instanceButton.setOnClickListener(this);
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (instanceLoader != null) {
			instanceLoader.cancel();
		}
		if (instanceDialog != null) {
			instanceDialog.dismiss();
		}
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
		} else if (v.getId() == R.id.choose_instance) {
			instanceButton.setEnabled(false);
			instanceLoader = new InstanceLoader(this);
			instanceLoader.start();
		}
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		String url = instances.get(which).getUrl();
		settings.setDomain(url);
		settings.setMode(Preferences.MODE_CUSTOM);
		selector.setSelection(NetworkAdapter.INDEX_CUSTOM);
		urlContainer.setVisibility(View.VISIBLE);
		urlInput.setText(url);
		urlInput.setError(null);
		Toast.makeText(getApplicationContext(), R.string.info_instance_set, Toast.LENGTH_SHORT).show();
		dialog.dismiss();
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


	@Override
	public void onInstancesLoaded(List<Instance> instances) {
		instanceLoader = null;
		instanceButton.setEnabled(true);
		if (instances == null) {
			Toast.makeText(getApplicationContext(), R.string.error_load_instances, Toast.LENGTH_SHORT).show();
		} else if (instances.isEmpty()) {
			Toast.makeText(getApplicationContext(), R.string.error_no_instance, Toast.LENGTH_SHORT).show();
		} else {
			this.instances = instances;
			CharSequence[] items = new CharSequence[instances.size()];
			int selection = -1;
			for (int i = 0; i < items.length; i++) {
				Instance instance = instances.get(i);
				items[i] = getInstanceDescription(instance);
				// mark the currently used instance
				if (settings.getMode() == Preferences.MODE_CUSTOM && instance.getUrl().equals(settings.getDomain())) {
					selection = i;
				}
			}
			instanceDialog = new AlertDialog.Builder(this).setTitle(R.string.title_instances).setSingleChoiceItems(items, selection, this).show();
		}
	}


	/**
	 * create a dialog item showing the instance domain followed by its location and health information
	 *
	 * @param instance instance to describe
	 * @return two line text
	 */
	private CharSequence getInstanceDescription(Instance instance) {
		List<String> details = new ArrayList<>();
		if (!instance.getCountry().isEmpty()) {
			details.add(instance.getCountry());
		}
		if (instance.getPing() >= 0) {
			details.add(getString(R.string.instance_ping, instance.getPing()));
		}
		if (instance.getUptime() >= 0) {
			details.add(getString(R.string.instance_uptime, instance.getUptime()));
		}
		SpannableStringBuilder description = new SpannableStringBuilder(instance.getDomain());
		int start = description.length();
		description.append('\n').append(TextUtils.join(" · ", details));
		description.setSpan(new RelativeSizeSpan(0.75f), start, description.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return description;
	}
}