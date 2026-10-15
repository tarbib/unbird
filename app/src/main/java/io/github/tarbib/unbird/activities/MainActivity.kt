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
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import io.github.tarbib.unbird.R
import io.github.tarbib.unbird.adapter.NetworkAdapter
import io.github.tarbib.unbird.backend.InstanceLoader
import io.github.tarbib.unbird.model.Instance
import io.github.tarbib.unbird.store.Preferences

/**
 * Main activity of this application, used to show network selector and handle link conversion
 *
 * @author nuclearfog
 */
class MainActivity : Activity(), View.OnClickListener, AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener, InstanceLoader.InstanceCallback {

	private lateinit var settings: Preferences
	private var instanceLoader: InstanceLoader? = null
	private var instances: List<Instance> = emptyList()

	private lateinit var selector: Spinner
	private lateinit var urlInput: EditText
	private lateinit var urlContainer: View
	private lateinit var instanceButton: Button
	private var instanceDialog: AlertDialog? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.layout_main)
		selector = findViewById(R.id.network_selector)
		val confirmButton = findViewById<View>(R.id.network_confirm)
		val settingsButton = findViewById<Button>(R.id.open_app_settings)
		instanceButton = findViewById(R.id.choose_instance)
		urlContainer = findViewById(R.id.network_input_container)
		urlInput = findViewById(R.id.network_input)

		settings = Preferences.get(applicationContext)
		selector.adapter = NetworkAdapter()
		if (settings.mode == Preferences.MODE_DEFAULT) {
			selector.setSelection(NetworkAdapter.INDEX_DEFAULT, false)
			urlContainer.visibility = View.INVISIBLE
		} else if (settings.mode == Preferences.MODE_CUSTOM) {
			selector.setSelection(NetworkAdapter.INDEX_CUSTOM, false)
			urlContainer.visibility = View.VISIBLE
			urlInput.setText(settings.domain)
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			settingsButton.visibility = View.VISIBLE
		}
		selector.onItemSelectedListener = this
		settingsButton.setOnClickListener(this)
		confirmButton.setOnClickListener(this)
		instanceButton.setOnClickListener(this)
	}


	override fun onDestroy() {
		super.onDestroy()
		instanceLoader?.cancel()
		instanceDialog?.dismiss()
	}


	@SuppressLint("InlinedApi")
	override fun onClick(v: View) {
		when (v.id) {
			R.id.network_confirm -> {
				val url = urlInput.text.toString()
				if (Patterns.WEB_URL.matcher(url).matches()) {
					urlInput.setText(url)
					settings.domain = url
					settings.mode = Preferences.MODE_CUSTOM
					Toast.makeText(applicationContext, R.string.info_instance_set, Toast.LENGTH_SHORT).show()
				} else {
					urlInput.error = getString(R.string.error_wrong_url)
				}
			}

			R.id.open_app_settings -> {
				val intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:$packageName"))
				intent.addCategory(Intent.CATEGORY_DEFAULT)
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				startActivity(intent)
			}

			R.id.choose_instance -> {
				instanceButton.isEnabled = false
				instanceLoader = InstanceLoader(this).also { it.start() }
			}
		}
	}


	override fun onClick(dialog: DialogInterface, which: Int) {
		val url = instances[which].url
		settings.domain = url
		settings.mode = Preferences.MODE_CUSTOM
		selector.setSelection(NetworkAdapter.INDEX_CUSTOM)
		urlContainer.visibility = View.VISIBLE
		urlInput.setText(url)
		urlInput.error = null
		Toast.makeText(applicationContext, R.string.info_instance_set, Toast.LENGTH_SHORT).show()
		dialog.dismiss()
	}


	override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
		if (parent.id == R.id.network_selector) {
			if (position == NetworkAdapter.INDEX_DEFAULT) {
				urlContainer.visibility = View.INVISIBLE
				settings.mode = Preferences.MODE_DEFAULT
				settings.domain = ""
			} else if (position == NetworkAdapter.INDEX_CUSTOM) {
				urlContainer.visibility = View.VISIBLE
				urlInput.setText(settings.domain)
			}
		}
	}


	override fun onNothingSelected(parent: AdapterView<*>) {
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
			// mark the currently used instance
			val selection = if (settings.mode == Preferences.MODE_CUSTOM) instances.indexOfFirst { it.url == settings.domain } else -1
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
