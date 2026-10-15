package org.nuclearfog.nitterify.backend;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuclearfog.nitterify.model.Instance;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Background task used to load healthy Nitter instances from the instance health tracker status.d420.de
 */
public class InstanceLoader extends Thread {

	/**
	 * API endpoint returning all instances tracked by status.d420.de
	 */
	private static final String API_URL = "https://status.d420.de/api/v1/instances";

	/**
	 * time in milliseconds to reuse a loaded instance list,
	 * status.d420.de only updates every 15 minutes and rate limits frequent requests
	 */
	private static final long CACHE_DURATION = 15 * 60 * 1000;

	/**
	 * connect and read timeout in milliseconds
	 */
	private static final int TIMEOUT = 10000;

	private static List<Instance> cache;
	private static long cacheTime;

	private Handler handler;
	private InstanceCallback callback;

	/**
	 * @param callback callback receiving the result on the main thread
	 */
	public InstanceLoader(InstanceCallback callback) {
		this.callback = callback;
		handler = new Handler(Looper.getMainLooper());
	}


	@Override
	public void run() {
		List<Instance> result = getCachedInstances();
		if (result == null) {
			try {
				result = loadInstances();
				setCachedInstances(result);
			} catch (IOException | JSONException exception) {
				// result stays null to report the error
			}
		}
		final List<Instance> instances = result;
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (callback != null) {
					callback.onInstancesLoaded(instances);
				}
			}
		});
	}

	/**
	 * discard the result, used when the callback receiver is destroyed
	 */
	public void cancel() {
		callback = null;
	}

	/**
	 * download the instance list
	 *
	 * @return healthy instances ordered by the score of status.d420.de
	 */
	private List<Instance> loadInstances() throws IOException, JSONException {
		HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
		try {
			connection.setConnectTimeout(TIMEOUT);
			connection.setReadTimeout(TIMEOUT);
			// replace the default user agent which contains device model and Android version
			connection.setRequestProperty("User-Agent", "Nitterify");
			connection.setRequestProperty("Accept", "application/json");
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new IOException("HTTP " + responseCode);
			}
			Reader reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
			StringBuilder json = new StringBuilder();
			char[] buffer = new char[4096];
			for (int count = reader.read(buffer); count >= 0; count = reader.read(buffer)) {
				json.append(buffer, 0, count);
			}
			reader.close();
			JSONArray hosts = new JSONObject(json.toString()).getJSONArray("hosts");
			List<Instance> instances = new ArrayList<>();
			for (int i = 0; i < hosts.length(); i++) {
				JSONObject host = hosts.getJSONObject(i);
				// skip unreachable hosts and hosts preventing health checks
				if (host.optBoolean("healthy") && !host.optBoolean("is_bad_host")) {
					Instance instance = new Instance(host);
					if (instance.getUrl().startsWith("https://")) {
						instances.add(instance);
					}
				}
			}
			return instances;
		} finally {
			connection.disconnect();
		}
	}

	/**
	 * get the last loaded instance list
	 *
	 * @return instance list or null if there is no recent list
	 */
	private static synchronized List<Instance> getCachedInstances() {
		if (cache != null && SystemClock.elapsedRealtime() - cacheTime < CACHE_DURATION) {
			return cache;
		}
		return null;
	}

	/**
	 * store loaded instance list
	 *
	 * @param instances loaded instance list
	 */
	private static synchronized void setCachedInstances(List<Instance> instances) {
		cache = instances;
		cacheTime = SystemClock.elapsedRealtime();
	}

	/**
	 * callback used to return the instance list
	 */
	public interface InstanceCallback {

		/**
		 * called on the main thread when loading finished
		 *
		 * @param instances healthy instances ordered by score or null if the list could not be loaded
		 */
		void onInstancesLoaded(List<Instance> instances);
	}
}
