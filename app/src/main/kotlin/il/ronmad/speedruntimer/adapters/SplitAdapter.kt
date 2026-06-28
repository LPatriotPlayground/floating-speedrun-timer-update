package il.ronmad.speedruntimer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class SplitsAdapter(
    private val context: Context,
    private val splits: List<Split>,
    private val timerState: TimerState
) : RecyclerView.Adapter<SplitsAdapter.VH>() {

    var nowElapsed: Long = 0L

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView     = view.findViewById(R.id.iv_split_icon)
        val tvName: TextView      = view.findViewById(R.id.tv_split_name)
        val tvSplitTime: TextView = view.findViewById(R.id.tv_split_time)
        val tvDelta: TextView     = view.findViewById(R.id.tv_split_delta)
        val tvLiveSeg: TextView   = view.findViewById(R.id.tv_live_segment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_split, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = splits.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val split = splits[position]
        val isCurrent = position == timerState.currentSplitIndex
        val isCompleted = position < timerState.currentSplitIndex

        val iconUri = split.iconUri
        if (iconUri != null) {
            holder.ivIcon.visibility = View.VISIBLE
            try {
                holder.ivIcon.setImageURI(Uri.parse(iconUri))
            } catch (e: Exception) {
                holder.ivIcon.setImageResource(R.drawable.ic_split_placeholder)
            }
        } else {
            holder.ivIcon.visibility = View.GONE
        }

        holder.tvName.text = split.name
        holder.tvName.setTextColor(
            if (isCurrent) Color.WHITE
            else ContextCompat.getColor(context, R.color.split_inactive)
        )

        when {
            isCompleted -> {

                val segTimes = timerState.splitHistory
                var cumulative = 0L
                for (i in 0..position) cumulative += segTimes.getOrElse(i) { 0L }
                holder.tvSplitTime.text = Util.formatTime(cumulative)
            }
            isCurrent -> {

                val refTime = timerState.cumulativeComparison(position)
                holder.tvSplitTime.text = if (refTime != null) Util.formatTime(refTime) else "-"
            }
            else -> {

                val refTime = timerState.cumulativeComparison(position)
                holder.tvSplitTime.text = if (refTime != null) Util.formatTime(refTime) else "-"
                holder.tvSplitTime.alpha = 0.5f
            }
        }
        if (!isCurrent) holder.tvSplitTime.alpha = if (isCompleted) 1f else 0.5f

        if (isCompleted) {
            val delta = timerState.splitDelta(position, nowElapsed)
            if (delta != null) {
                holder.tvDelta.visibility = View.VISIBLE
                holder.tvDelta.text = formatDelta(delta)
                holder.tvDelta.setTextColor(deltaColor(delta))
            } else {
                holder.tvDelta.visibility = View.INVISIBLE
            }
        } else {
            holder.tvDelta.visibility = View.INVISIBLE
        }

        if (isCurrent && timerState.isRunning) {
            timerState.updateLiveSegment(nowElapsed)
            val liveDelta = timerState.liveSegmentDelta
            if (liveDelta != null) {
                holder.tvLiveSeg.visibility = View.VISIBLE
                holder.tvLiveSeg.text = formatDelta(liveDelta)

                holder.tvLiveSeg.setTextColor(
                    if (liveDelta < 0) Color.parseColor("#FFD700")
                    else Color.parseColor("#FF4444")
                )
            } else {
                holder.tvLiveSeg.visibility = View.GONE
            }
        } else {
            holder.tvLiveSeg.visibility = View.GONE
        }
    }

    private fun formatDelta(deltaMs: Long): String {
        val sign  = if (deltaMs < 0) "−" else "+"
        val abs   = Math.abs(deltaMs)
        val secs  = abs / 1000
        val millis = (abs % 1000) / 10
        return if (secs >= 60) {
            val m = secs / 60; val s = secs % 60
            "$sign${m}:${s.toString().padStart(2,'0')}"
        } else {
            "$sign${secs}.${millis.toString().padStart(2,'0')}"
        }
    }

    private fun deltaColor(deltaMs: Long): Int =
        if (deltaMs < 0) Color.parseColor("#00CC44")
        else             Color.parseColor("#FF4444")
}
