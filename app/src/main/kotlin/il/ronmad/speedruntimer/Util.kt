package il.ronmad.speedruntimer

object Util {

    fun formatTime(ms: Long): String {
        if (ms <= 0L) return "0:00.00"
        val totalCentis = ms / 10
        val centis = totalCentis % 100
        val totalSecs = totalCentis / 100
        val secs = totalSecs % 60
        val totalMins = totalSecs / 60
        val mins = totalMins % 60
        val hours = totalMins / 60

        return if (hours > 0) {
            "%d:%02d:%02d.%02d".format(hours, mins, secs, centis)
        } else {
            "%d:%02d.%02d".format(mins, secs, centis)
        }
    }

    fun formatDelta(deltaMs: Long): String {
        val sign = if (deltaMs < 0) "−" else "+"
        val abs  = Math.abs(deltaMs)
        val secs = abs / 1000
        val cents = (abs % 1000) / 10
        return if (secs >= 60) {
            val m = secs / 60; val s = secs % 60
            "$sign${m}:${s.toString().padStart(2, '0')}"
        } else {
            "$sign${secs}.${cents.toString().padStart(2, '0')}"
        }
    }
}