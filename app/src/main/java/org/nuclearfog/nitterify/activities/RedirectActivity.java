package org.nuclearfog.nitterify.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import org.nuclearfog.nitterify.R;
import org.nuclearfog.nitterify.store.Preferences;

/**
 * Activity used to redirect to Nitter instance
 *
 * @author nuclearfog
 */
public class RedirectActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Preferences settings = Preferences.get(getApplicationContext());
		// fetch url to redirect
		if (getIntent().getData() != null) {
			Uri redirectUrl;
			String path = getIntent().getData().getPath();
			if (settings.getMode() == Preferences.MODE_CUSTOM) {
				redirectUrl = Uri.parse(settings.getDomain() + path);
				if (redirectUrl.getScheme() == null) {
					redirectUrl = Uri.parse("https://" + settings.getDomain() + path);
				}
			} else {
				redirectUrl = Uri.parse("https://nitter.net" + path);
			}
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(redirectUrl);
			try {
				getApplicationContext().startActivity(intent);
			} catch (ActivityNotFoundException exception) {
				Toast.makeText(getApplicationContext(), R.string.error_open_link, Toast.LENGTH_SHORT).show();
			}
		}
	}


	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
}