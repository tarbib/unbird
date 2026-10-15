package org.nuclearfog.nitterify.store

import android.content.Context
import android.content.SharedPreferences

/**
 * App settings class storing Nitter selection
 *
 * @author nuclearfog
 */
class Preferences private constructor(context: Context) {

	private val settings: SharedPreferences = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE)

	/**
	 * redirect mode, [MODE_DEFAULT] or [MODE_CUSTOM]
	 */
	var mode: Int = settings.getInt(SETTINGS_MODE, MODE_DEFAULT)
		set(value) {
			field = value
			settings.edit().putInt(SETTINGS_MODE, value).apply()
		}

	/**
	 * nitter domain url
	 */
	var domain: String = settings.getString(SETTINGS_DOMAIN, DOMAIN_DEF_VALUE) ?: DOMAIN_DEF_VALUE
		set(value) {
			field = value
			settings.edit().putString(SETTINGS_DOMAIN, value).apply()
		}

	companion object {

		/**
		 * mode to use default Nitter instance
		 */
		const val MODE_DEFAULT = 10

		/**
		 * mode to use custom Nitter instance
		 */
		const val MODE_CUSTOM = 11

		private const val SETTINGS_NAME = "settings"
		private const val SETTINGS_DOMAIN = "domain"
		private const val SETTINGS_MODE = "mode"
		private const val DOMAIN_DEF_VALUE = "https://nitter.net"

		private var instance: Preferences? = null

		/**
		 * get singleton instance
		 *
		 * @return instance of this class
		 */
		fun get(context: Context): Preferences {
			return instance ?: Preferences(context).also { instance = it }
		}
	}
}
