package il.ronmad.speedruntimer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class ComparisonTarget {
    PERSONAL_BEST,
    SUM_OF_BEST,
    ATTEMPT,
    NONE
}

@Parcelize
data class Category(
    val name: String,
    val splits: MutableList<Split> = mutableListOf(),
    val attempts: MutableList<Attempt> = mutableListOf(),
    var comparisonTarget: ComparisonTarget = ComparisonTarget.PERSONAL_BEST,
    var comparisonAttemptId: Long = -1L
) : Parcelable {


    val hasPb: Boolean get() = splits.any { it.pbTime > 0L }


    val comparisonAttempt: Attempt?
        get() = attempts.firstOrNull { it.id == comparisonAttemptId }


    val sumOfBestSegments: LongArray
        get() {
            val result = LongArray(splits.size) { splits[it].bestSegment }
            return result
        }


    fun comparisonSegmentTimes(): LongArray? = when (comparisonTarget) {
        ComparisonTarget.PERSONAL_BEST -> {
            if (!hasPb) null
            else LongArray(splits.size) { i ->
                if (i == 0) splits[0].pbTime
                else splits[i].pbTime - splits[i - 1].pbTime
            }
        }
        ComparisonTarget.SUM_OF_BEST -> {
            val sob = sumOfBestSegments
            if (sob.all { it == 0L }) null else sob
        }
        ComparisonTarget.ATTEMPT -> {
            comparisonAttempt?.takeIf { it.segmentTimes.size == splits.size }
                ?.segmentTimes
        }
        ComparisonTarget.NONE -> null
    }
}
