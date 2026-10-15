package org.nuclearfog.nitterify.backend

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import org.json.JSONException
import org.json.JSONObject
import org.nuclearfog.nitterify.model.Instance
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Background task used to load healthy Nitter instances from the instance health tracker status.d420.de
 *
 * @param callback callback receiving the result on the main thread
 */
class InstanceLoader(private var callback: InstanceCallback?) : Thread() {

	private val handler = Handler(Looper.getMainLooper())


	override fun run() {
		var result = getCachedInstances()
		if (result == null) {
			try {
				result = loadInstances()
				setCachedInstances(result)
			} catch (exception: IOException) {
				// result stays null to report the error
			} catch (exception: JSONException) {
				// result stays null to report the error
			}
		}
		val instances = result
		handler.post { callback?.onInstancesLoaded(instances) }
	}

	/**
	 * discard the result, used when the callback receiver is destroyed
	 */
	fun cancel() {
		callback = null
	}

	/**
	 * download the instance list
	 *
	 * @return healthy instances ordered by the score of status.d420.de
	 */
	private fun loadInstances(): List<Instance> {
		val connection = URL(API_URL).openConnection() as HttpURLConnection
		try {
			connection.connectTimeout = TIMEOUT
			connection.readTimeout = TIMEOUT
			// replace the default user agent which contains device model and Android version
			connection.setRequestProperty("User-Agent", "Nitterify")
			connection.setRequestProperty("Accept", "application/json")
			val responseCode = connection.responseCode
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw IOException("HTTP $responseCode")
			}
			val json = connection.inputStream.reader(Charsets.UTF_8).use { it.readText() }
			val hosts = JSONObject(json).getJSONArray("hosts")
			val instances = ArrayList<Instance>()
			for (i in 0 until hosts.length()) {
				val host = hosts.getJSONObject(i)
				// skip unreachable hosts and hosts preventing health checks
				if (host.optBoolean("healthy") && !host.optBoolean("is_bad_host")) {
					val instance = Instance(host)
					if (instance.url.startsWith("https://")) {
						instances.add(instance)
					}
				}
			}
			return instances
		} finally {
			connection.disconnect()
		}
	}

	/**
	 * callback used to return the instance list
	 */
	fun interface InstanceCallback {

		/**
		 * called on the main thread when loading finished
		 *
		 * @param instances healthy instances ordered by score or null if the list could not be loaded
		 */
		fun onInstancesLoaded(instances: List<Instance>?)
	}

	companion object {

		/**
		 * API endpoint returning all instances tracked by status.d420.de
		 */
		private const val API_URL = "https://status.d420.de/api/v1/instances"

		/**
		 * time in milliseconds to reuse a loaded instance list,
		 * status.d420.de only updates every 15 minutes and rate limits frequent requests
		 */
		private const val CACHE_DURATION = 15 * 60 * 1000L

		/**
		 * connect and read timeout in milliseconds
		 */
		private const val TIMEOUT = 10000

		private var cache: List<Instance>? = null
		private var cacheTime = 0L

		/**
		 * get the last loaded instance list
		 *
		 * @return instance list or null if there is no recent list
		 */
		@Synchronized
		private fun getCachedInstances(): List<Instance>? {
			val instances = cache
			if (instances != null && SystemClock.elapsedRealtime() - cacheTime < CACHE_DURATION) {
				return instances
			}
			return null
		}

		/**
		 * store loaded instance list
		 *
		 * @param instances loaded instance list
		 */
		@Synchronized
		private fun setCachedInstances(instances: List<Instance>) {
			cache = instances
			cacheTime = SystemClock.elapsedRealtime()
		}
	}
}
