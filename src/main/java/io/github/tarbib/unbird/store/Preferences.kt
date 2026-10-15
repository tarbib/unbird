package io.github.tarbib.unbird.store

import android.content.Context

/**
 * App settings storing the Nitter instance used for redirects
 *
 * @author nuclearfog
 */
class Preferences(context: Context) {

	private val settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

	/**
	 * url of the Nitter instance e.g. https://nitter.net
	 */
	var instance: String
		get() = settings.getString(KEY_INSTANCE, null) ?: DEFAULT_INSTANCE
		set(value) = settings.edit().putString(KEY_INSTANCE, value).apply()

	companion object {
		private const val KEY_INSTANCE = "instance"
		private const val DEFAULT_INSTANCE = "https://nitter.net"
	}
}
