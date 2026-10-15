package io.github.tarbib.unbird.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import io.github.tarbib.unbird.R
import io.github.tarbib.unbird.backend.InstanceLoader
import io.github.tarbib.unbird.model.Instance
import io.github.tarbib.unbird.store.Preferences

/**
 * Main activity of this application, used to show and choose the Nitter instance
 *
 * @author nuclearfog
 */
class MainActivity : Activity(), View.OnClickListener, DialogInterface.OnClickListener, InstanceLoader.InstanceCallback {

	private lateinit var preferences: Preferences
	private lateinit var instanceText: TextView
	private lateinit var instanceButton: Button
	private var instanceLoader: InstanceLoader? = null
	private var instanceDialog: AlertDialog? = null
	private var instances: List<Instance> = emptyList()


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.layout_main)
		instanceText = findViewById(R.id.current_instance)
		instanceButton = findViewById(R.id.choose_instance)
		val settingsButton = findViewById<Button>(R.id.open_app_settings)

		preferences = Preferences(this)
		instanceText.text = preferences.instance
		// since Android 12 opening X/Twitter links with this app must be allowed manually
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			settingsButton.visibility = View.VISIBLE
		}
		instanceButton.setOnClickListener(this)
		settingsButton.setOnClickListener(this)
	}


	override fun onDestroy() {
		super.onDestroy()
		instanceLoader?.cancel()
		instanceDialog?.dismiss()
	}


	@SuppressLint("InlinedApi")
	override fun onClick(v: View) {
		if (v.id == R.id.choose_instance) {
			instanceButton.isEnabled = false
			instanceLoader = InstanceLoader(this).also { it.start() }
		} else if (v.id == R.id.open_app_settings) {
			startActivity(Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:$packageName")))
		}
	}


	override fun onClick(dialog: DialogInterface, which: Int) {
		preferences.instance = instances[which].url
		instanceText.text = preferences.instance
		dialog.dismiss()
	}


	override fun onInstancesLoaded(instances: List<Instance>?) {
		instanceLoader = null
		instanceButton.isEnabled = true
		if (instances == null) {
			Toast.makeText(applicationContext, R.string.error_load_instances, Toast.LENGTH_SHORT).show()
		} else if (instances.isEmpty()) {
			Toast.makeText(applicationContext, R.string.error_no_instance, Toast.LENGTH_SHORT).show()
		} else {
			this.instances = instances
			val items = instances.map { getInstanceDescription(it) }.toTypedArray()
			// mark the instance currently used
			val selection = instances.indexOfFirst { it.url == preferences.instance }
			instanceDialog = AlertDialog.Builder(this).setTitle(R.string.title_instances).setSingleChoiceItems(items, selection, this).show()
		}
	}


	/**
	 * create a dialog item showing the instance domain followed by its location and health information
	 *
	 * @param instance instance to describe
	 * @return two line text
	 */
	private fun getInstanceDescription(instance: Instance): CharSequence {
		val details = listOfNotNull(
			instance.country.ifEmpty { null },
			if (instance.ping >= 0) getString(R.string.instance_ping, instance.ping) else null,
			if (instance.uptime >= 0) getString(R.string.instance_uptime, instance.uptime) else null
		)
		val description = SpannableStringBuilder(instance.domain)
		val start = description.length
		description.append('\n').append(details.joinToString(" · "))
		description.setSpan(RelativeSizeSpan(0.75f), start, description.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		return description
	}
}
