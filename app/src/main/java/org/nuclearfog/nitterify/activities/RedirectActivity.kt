package org.nuclearfog.nitterify.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import org.nuclearfog.nitterify.R
import org.nuclearfog.nitterify.store.Preferences

/**
 * Activity used to redirect to Nitter instance
 *
 * @author nuclearfog
 */
class RedirectActivity : Activity() {


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val settings = Preferences.get(applicationContext)
		// fetch url to redirect
		val data = intent.data ?: return
		val path = data.path.orEmpty()
		val redirectUrl = if (settings.mode == Preferences.MODE_CUSTOM) {
			val url = Uri.parse(settings.domain + path)
			if (url.scheme == null) Uri.parse("https://" + settings.domain + path) else url
		} else {
			Uri.parse("https://nitter.net$path")
		}
		val viewIntent = Intent(Intent.ACTION_VIEW)
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		viewIntent.data = redirectUrl
		try {
			applicationContext.startActivity(viewIntent)
		} catch (exception: ActivityNotFoundException) {
			Toast.makeText(applicationContext, R.string.error_open_link, Toast.LENGTH_SHORT).show()
		}
	}


	override fun onStop() {
		super.onStop()
		finish()
	}
}
