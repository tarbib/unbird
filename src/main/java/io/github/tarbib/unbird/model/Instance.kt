package io.github.tarbib.unbird.model

import org.json.JSONObject

/**
 * Nitter instance listed by the instance health tracker status.d420.de
 *
 * @param json host object returned by the status.d420.de API
 */
class Instance(json: JSONObject) {

	/**
	 * instance url e.g. https://nitter.net
	 */
	val url: String = json.optString("url")

	/**
	 * instance domain e.g. nitter.net
	 */
	val domain: String = json.optString("domain")

	/**
	 * country flag emoji(s) of the host location, empty if unknown
	 */
	val country: String = json.optString("country")

	/**
	 * average response time of the last 3 hours in milliseconds or -1 if unknown
	 */
	val ping: Int = json.optInt("ping_avg", -1)

	/**
	 * all time percentage of the instance being healthy or -1 if unknown
	 */
	val uptime: Int = json.optInt("healthy_percentage_overall", -1)
}
