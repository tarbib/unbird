package org.nuclearfog.nitterify.adapter

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.nuclearfog.nitterify.R

/**
 * Dropdown adapter used to show network options
 *
 * @author nuclearfog
 */
class NetworkAdapter : BaseAdapter() {

	/**
	 * item string resources, ordered by [INDEX_DEFAULT] and [INDEX_CUSTOM]
	 */
	private val itemRes = intArrayOf(R.string.select_nitter, R.string.custom_instance)


	override fun getCount(): Int {
		return itemRes.size
	}


	override fun getItem(position: Int): Int {
		return itemRes[position]
	}


	override fun getItemId(position: Int): Long {
		return position.toLong()
	}


	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val text = convertView as? TextView ?: TextView(parent.context).apply {
			setTextSize(TypedValue.COMPLEX_UNIT_PX, parent.context.resources.getDimensionPixelSize(R.dimen.text_size_dropdown).toFloat())
			setSingleLine(true)
		}
		text.setText(getItem(position))
		return text
	}

	companion object {

		/**
		 * item index of the "default nitter instance" entry
		 */
		const val INDEX_DEFAULT = 0

		/**
		 * item index of the "custom nitter instance" entry
		 */
		const val INDEX_CUSTOM = 1
	}
}
