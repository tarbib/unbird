package io.github.tarbib.unbird.activities

import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.listitem.ListItemLayout
import com.google.android.material.textfield.TextInputLayout
import io.github.tarbib.unbird.R
import io.github.tarbib.unbird.backend.InstanceLoader
import io.github.tarbib.unbird.model.Instance
import io.github.tarbib.unbird.store.Preferences
import com.google.android.material.R as MaterialR

/**
 * Main activity of this application, used to show and choose the Nitter instance
 *
 * @author nuclearfog
 */
class MainActivity : AppCompatActivity() {

	private lateinit var preferences: Preferences
	private lateinit var instanceCard: View
	private lateinit var instanceText: TextView
	private lateinit var instanceArrow: View
	private lateinit var instanceProgress: View
	private lateinit var linksState: TextView
	private lateinit var linksAllowed: View
	private lateinit var linksAllow: View
	private var instanceLoader: InstanceLoader? = null
	private var instanceDialog: AlertDialog? = null


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.layout_main)
		instanceCard = findViewById(R.id.instance_card)
		instanceText = findViewById(R.id.current_instance)
		instanceArrow = findViewById(R.id.instance_arrow)
		instanceProgress = findViewById(R.id.instance_progress)
		linksState = findViewById(R.id.links_state)
		linksAllowed = findViewById(R.id.links_allowed)
		linksAllow = findViewById(R.id.links_allow)
		val instanceItem = findViewById<ListItemLayout>(R.id.instance_item)
		val linksItem = findViewById<ListItemLayout>(R.id.links_item)

		// the app is drawn behind the system bars, keep the content clear of them
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
			val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
			view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
			insets
		}
		preferences = Preferences(this)
		showInstance()
		instanceCard.setOnClickListener { loadInstances() }
		// since Android 12 opening X/Twitter links with this app must be allowed manually
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			linksItem.visibility = View.VISIBLE
			instanceItem.updateAppearance(0, 2)
			linksItem.updateAppearance(1, 2)
			val openSettings = View.OnClickListener {
				startActivity(Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:$packageName")))
			}
			findViewById<View>(R.id.links_card).setOnClickListener(openSettings)
			linksAllow.setOnClickListener(openSettings)
		} else {
			instanceItem.updateAppearance(0, 1)
		}
	}


	override fun onResume() {
		super.onResume()
		// links may have been allowed in the settings meanwhile
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			showLinksState()
		}
	}


	override fun onDestroy() {
		super.onDestroy()
		instanceLoader?.cancel()
		instanceDialog?.dismiss()
	}

	/**
	 * load the healthy instances and show them
	 */
	private fun loadInstances() {
		setLoading(true)
		instanceLoader = InstanceLoader { showInstances(it) }.also { it.start() }
	}

	/**
	 * replace the arrow of the instance item with a loading indicator and ignore clicks while loading
	 *
	 * @param loading true if the instances are loading
	 */
	private fun setLoading(loading: Boolean) {
		instanceCard.isEnabled = !loading
		instanceArrow.visibility = if (loading) View.GONE else View.VISIBLE
		instanceProgress.visibility = if (loading) View.VISIBLE else View.GONE
	}

	/**
	 * show the healthy instances followed by the custom instance option
	 *
	 * @param instances loaded instances or null if the list could not be loaded
	 */
	private fun showInstances(instances: List<Instance>?) {
		instanceLoader = null
		setLoading(false)
		if (instances == null) {
			Toast.makeText(applicationContext, R.string.error_load_instances, Toast.LENGTH_SHORT).show()
		} else if (instances.isEmpty()) {
			Toast.makeText(applicationContext, R.string.error_no_instance, Toast.LENGTH_SHORT).show()
		}
		val list = instances.orEmpty()
		val builder = MaterialAlertDialogBuilder(this)
		val items = list.map { getInstanceDescription(builder.context, it) } + getString(R.string.custom_instance)
		// mark the instance currently used
		val selection = list.indexOfFirst { it.url == preferences.instance }
		instanceDialog = builder
			.setTitle(R.string.choose_instance)
			.setSingleChoiceItems(items.toTypedArray(), selection) { dialog, which ->
				dialog.dismiss()
				if (which < list.size) {
					setInstance(list[which].url)
				} else {
					showCustomInstanceDialog()
				}
			}
			.setNegativeButton(android.R.string.cancel, null)
			.show()
	}

	/**
	 * ask for the url of a custom instance
	 */
	private fun showCustomInstanceDialog() {
		val builder = MaterialAlertDialogBuilder(this)
		val view = LayoutInflater.from(builder.context).inflate(R.layout.dialog_instance, null)
		val inputLayout = view.findViewById<TextInputLayout>(R.id.instance_input)
		val input = view.findViewById<EditText>(R.id.instance_url)
		input.setText(preferences.instance.removePrefix("https://"))
		input.selectAll()
		val dialog = builder
			.setTitle(R.string.custom_instance)
			.setView(view)
			.setPositiveButton(android.R.string.ok, null)
			.setNegativeButton(android.R.string.cancel, null)
			.create()
		// keep the dialog open until the url is valid
		dialog.setOnShowListener {
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
				val url = getCustomInstanceUrl(input.text.toString())
				if (url != null) {
					setInstance(url)
					dialog.dismiss()
				} else {
					inputLayout.error = getString(R.string.error_wrong_url)
				}
			}
		}
		input.setOnEditorActionListener { _, actionId, _ ->
			actionId == EditorInfo.IME_ACTION_DONE && dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
		}
		dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
		dialog.show()
		instanceDialog = dialog
	}

	/**
	 * check the url of a custom instance, https is used if the url has no scheme
	 *
	 * @param text url entered by the user
	 * @return instance url or null if the url is not valid
	 */
	private fun getCustomInstanceUrl(text: String): String? {
		var url = text.trim().trimEnd('/')
		if (!url.contains("://")) {
			url = "https://$url"
		}
		return if (Patterns.WEB_URL.matcher(url).matches()) url else null
	}

	/**
	 * store and show the instance used for redirects
	 *
	 * @param url instance url
	 */
	private fun setInstance(url: String) {
		preferences.instance = url
		showInstance()
	}

	/**
	 * show the instance used for redirects, https is implied
	 */
	private fun showInstance() {
		instanceText.text = preferences.instance.removePrefix("https://")
	}

	/**
	 * show if X/Twitter links open with this app, which needs all their domains to be selected in the settings
	 */
	@RequiresApi(Build.VERSION_CODES.S)
	private fun showLinksState() {
		val state = getSystemService(DomainVerificationManager::class.java)?.getDomainVerificationUserState(packageName)
		val domains = state?.hostToStateMap?.values.orEmpty()
		val allowed = state != null && state.isLinkHandlingAllowed && domains.isNotEmpty() &&
			domains.none { it == DomainVerificationUserState.DOMAIN_STATE_NONE }
		linksState.setText(if (allowed) R.string.links_allowed else R.string.links_not_allowed)
		linksAllowed.visibility = if (allowed) View.VISIBLE else View.GONE
		linksAllow.visibility = if (allowed) View.GONE else View.VISIBLE
	}

	/**
	 * create a dialog item showing the instance domain followed by its location and health information
	 *
	 * @param context themed context of the dialog
	 * @param instance instance to describe
	 * @return two line text
	 */
	private fun getInstanceDescription(context: Context, instance: Instance): CharSequence {
		val details = listOfNotNull(
			instance.country.ifEmpty { null },
			if (instance.ping >= 0) getString(R.string.instance_ping, instance.ping) else null,
			if (instance.uptime >= 0) getString(R.string.instance_uptime, instance.uptime) else null
		)
		val description = SpannableStringBuilder(instance.domain)
		val start = description.length
		description.append('\n').append(details.joinToString(" · "))
		val color = MaterialColors.getColor(context, MaterialR.attr.colorOnSurfaceVariant, 0)
		description.setSpan(TextAppearanceSpan(context, MaterialR.style.TextAppearance_Material3_BodyMedium), start, description.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		description.setSpan(ForegroundColorSpan(color), start, description.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		return description
	}
}
