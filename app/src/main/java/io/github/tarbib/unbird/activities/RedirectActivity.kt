package io.github.tarbib.unbird.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import io.github.tarbib.unbird.R
import io.github.tarbib.unbird.store.Preferences

/**
 * Activity used to redirect X/Twitter links to the selected Nitter instance
 *
 * @author nuclearfog
 */
class RedirectActivity : Activity() {


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// keep the path of the link and replace the X/Twitter host with the Nitter instance
		val path = intent.data?.path
		if (path != null) {
			val redirect = Intent(Intent.ACTION_VIEW, Uri.parse(Preferences(this).instance + path))
			redirect.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			try {
				startActivity(redirect)
			} catch (exception: ActivityNotFoundException) {
				Toast.makeText(applicationContext, R.string.error_open_link, Toast.LENGTH_SHORT).show()
			}
		}
		finish()
	}
}
