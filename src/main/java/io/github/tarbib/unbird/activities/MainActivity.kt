package io.github.tarbib.unbird.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
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
class MainActivity : Activity() {

	private lateinit var preferences: Preferences
	private lateinit var instanceText: TextView
	private lateinit var instanceButton: Button
	private var instanceLoader: InstanceLoader? = null
	private var instanceDialog: AlertDialog? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.layout_main)
		instanceText = findViewById(R.id.current_instance)
		instanceButton = findViewById(R.id.choose_instance)
		val settingsButton = findViewById<Button>(R.id.open_app_settings)

		preferences = Preferences(this)
		instanceText.text = preferences.instance
		instanceButton.setOnClickListener {
			instanceButton.isEnabled = false
			instanceLoader = InstanceLoader { showInstances(it) }.also { it.start() }
		}
		// since Android 12 opening X/Twitter links with this app must be allowed manually
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			settingsButton.visibility = View.VISIBLE
			settingsButton.setOnClickListener {
				startActivity(Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:$packageName")))
			}
		}
	}


	override fun onDestroy() {
		super.onDestroy()
		instanceLoader?.cancel()
		instanceDialog?.dismiss()
	}

	/**
	 * show the healthy instances followed by the custom instance option
	 *
	 * @param instances loaded instances or null if the list could not be loaded
	 */
	private fun showInstances(instances: List<Instance>?) {
		instanceLoader = null
		instanceButton.isEnabled = true
		if (instances == null) {
			Toast.makeText(applicationContext, R.string.error_load_instances, Toast.LENGTH_SHORT).show()
		} else if (instances.isEmpty()) {
			Toast.makeText(applicationContext, R.string.error_no_instance, Toast.LENGTH_SHORT).show()
		}
		val list = instances.orEmpty()
		val items = list.map { getInstanceDescription(it) } + getString(R.string.custom_instance)
		// mark the instance currently used
		val selection = list.indexOfFirst { it.url == preferences.instance }
		instanceDialog = AlertDialog.Builder(this)
			.setTitle(R.string.choose_instance)
			.setSingleChoiceItems(items.toTypedArray(), selection) { dialog, which ->
				dialog.dismiss()
				if (which < list.size) {
					setInstance(list[which].url)
				} else {
					showCustomInstanceDialog()
				}
			}
			.show()
	}

	/**
	 * ask for the url of a custom instance
	 */
	private fun showCustomInstanceDialog() {
		val input = EditText(this)
		input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
		input.setText(preferences.instance)
		instanceDialog = AlertDialog.Builder(this)
			.setTitle(R.string.custom_instance)
			.setView(input)
			.setPositiveButton(android.R.string.ok) { _, _ -> setCustomInstance(input.text.toString()) }
			.setNegativeButton(android.R.string.cancel, null)
			.show()
	}

	/**
	 * check and store a custom instance url, https is used if the url has no scheme
	 *
	 * @param text url entered by the user
	 */
	private fun setCustomInstance(text: String) {
		var url = text.trim().trimEnd('/')
		if (!url.contains("://")) {
			url = "https://$url"
		}
		if (Patterns.WEB_URL.matcher(url).matches()) {
			setInstance(url)
		} else {
			Toast.makeText(applicationContext, R.string.error_wrong_url, Toast.LENGTH_SHORT).show()
		}
	}

	/**
	 * store and show the instance used for redirects
	 *
	 * @param url instance url
	 */
	private fun setInstance(url: String) {
		preferences.instance = url
		instanceText.text = url
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
