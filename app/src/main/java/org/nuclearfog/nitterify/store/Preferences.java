package org.nuclearfog.nitterify.store;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * App settings class storing Nitter selection
 *
 * @author nuclearfog
 */
public class Preferences {

	/**
	 * mode to use default Nitter instance
	 */
	public static final int MODE_DEFAULT = 10;

	/**
	 * mode to use custom Nitter instance
	 */
	public static final int MODE_CUSTOM = 11;

	private static final String SETTINGS_NAME = "settings";
	private static final String SETTINGS_DOMAIN = "domain";
	private static final String SETTINGS_MODE = "mode";
	private static final String DOMAIN_DEF_VALUE = "https://nitter.net";

	private static Preferences instance;

	private SharedPreferences settings;

	private String domain;
	private int mode;

	/**
	 *
	 */
	private Preferences(Context context) {
		settings = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
		domain = settings.getString(SETTINGS_DOMAIN, DOMAIN_DEF_VALUE);
		mode = settings.getInt(SETTINGS_MODE, MODE_DEFAULT);
	}

	/**
	 * get redirect mode
	 *
	 * @return {@link #MODE_DEFAULT,#MODE_CUSTOM}
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * set redirect mode
	 *
	 * @param mode redirect mode {@link #MODE_DEFAULT,#MODE_CUSTOM}
	 */
	public void setMode(int mode) {
		this.mode = mode;
		settings.edit().putInt(SETTINGS_MODE, mode).apply();
	}


	/**
	 * get nitter domain
	 *
	 * @return domain url
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * set nitter domain
	 *
	 * @param domain domain url
	 */
	public void setDomain(String domain) {
		this.domain = domain;
		settings.edit().putString(SETTINGS_DOMAIN, domain).apply();
	}

	/**
	 * get singleton instance
	 *
	 * @return instance of this class
	 */
	public static Preferences get(Context context) {
		if (instance == null) {
			instance = new Preferences(context);
		}
		return instance;
	}
}