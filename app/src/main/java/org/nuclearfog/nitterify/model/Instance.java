package org.nuclearfog.nitterify.model;

import org.json.JSONObject;

/**
 * Nitter instance listed by the instance health tracker status.d420.de
 */
public class Instance {

	private String url;
	private String domain;
	private String country;
	private int ping;
	private int uptime;

	/**
	 * @param json host object returned by the status.d420.de API
	 */
	public Instance(JSONObject json) {
		url = json.optString("url");
		domain = json.optString("domain");
		country = json.optString("country");
		ping = json.optInt("ping_avg", -1);
		uptime = json.optInt("healthy_percentage_overall", -1);
	}

	/**
	 * get instance url
	 *
	 * @return url e.g. https://nitter.net
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * get instance domain
	 *
	 * @return domain e.g. nitter.net
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * get host location
	 *
	 * @return country flag emoji(s), empty if unknown
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * get average response time of the last 3 hours
	 *
	 * @return response time in milliseconds or -1 if unknown
	 */
	public int getPing() {
		return ping;
	}

	/**
	 * get all time percentage of the instance being healthy
	 *
	 * @return percentage or -1 if unknown
	 */
	public int getUptime() {
		return uptime;
	}
}
