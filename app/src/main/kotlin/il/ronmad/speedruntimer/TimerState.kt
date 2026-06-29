package il.ronmad.speedruntimer

class TimerState(val category: Category) {


    var currentSplitIndex: Int = 0
        private set


    var isRunning: Boolean = false


    var runStartElapsed: Long = 0L


    var segmentStartElapsed: Long = 0L


    private val _splitHistory: ArrayDeque<Long> = ArrayDeque()


    val splitHistory: List<Long> get() = _splitHistory


    val canRevert: Boolean get() = _splitHistory.isNotEmpty()


    var comparisonSegments: LongArray? = null
        private set


    fun cumulativeComparison(splitIndex: Int): Long? {
        val segs = comparisonSegments ?: return null
        var acc = 0L
        for (i in 0..splitIndex) {
            acc += segs.getOrElse(i) { return null }
        }
        return acc
    }


    var liveSegmentDelta: Long? = null
        private set


    fun updateLiveSegment(nowElapsed: Long) {
        val segs = comparisonSegments ?: run { liveSegmentDelta = null; return }
        val refSegTime = segs.getOrElse(currentSplitIndex) { 0L }
        if (refSegTime == 0L) { liveSegmentDelta = null; return }

        val currentSegElapsed = nowElapsed - segmentStartElapsed
        liveSegmentDelta = currentSegElapsed - refSegTime
    }


    fun onStart(nowElapsed: Long, cat: Category) {
        isRunning = true
        runStartElapsed = nowElapsed
        segmentStartElapsed = nowElapsed
        currentSplitIndex = 0
        _splitHistory.clear()
        liveSegmentDelta = null
        comparisonSegments = cat.comparisonSegmentTimes()
    }


    fun onSplit(nowElapsed: Long): Long {
        val segTime = nowElapsed - segmentStartElapsed
        _splitHistory.addLast(segTime)
        segmentStartElapsed = nowElapsed
        currentSplitIndex++
        liveSegmentDelta = null
        return segTime
    }


    fun onRevert(nowElapsed: Long): Long? {
        if (!canRevert) return null
        val undoneSegTime = _splitHistory.removeLast()
        currentSplitIndex--

        segmentStartElapsed = nowElapsed - undoneSegTime
        liveSegmentDelta = null
        return undoneSegTime
    }


    fun totalElapsed(nowElapsed: Long): Long = nowElapsed - runStartElapsed


    fun segmentElapsed(nowElapsed: Long): Long = nowElapsed - segmentStartElapsed


    fun splitDelta(splitIndex: Int, nowElapsed: Long): Long? {
        if (splitIndex < 0 || splitIndex >= _splitHistory.size) return null
        val refCumulative = cumulativeComparison(splitIndex) ?: return null
        var actualCumulative = 0L
        for (i in 0..splitIndex) actualCumulative += _splitHistory[i]
        return actualCumulative - refCumulative
    }
}
