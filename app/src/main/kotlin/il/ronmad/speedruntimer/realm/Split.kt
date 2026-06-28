package il.ronmad.speedruntimer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Split(
    val name: String,

    var pbTime: Long = 0L,

    var bestSegment: Long = 0L,

    var iconUri: String? = null
) : Parcelable {


    val pbSegmentTime: Long
        get() = pbTime

    companion object {
        const val NO_TIME = 0L
    }
}

@Parcelize
data class Attempt(
    val id: Long = System.currentTimeMillis(),
    val totalTime: Long = 0L,
    val segmentTimes: LongArray = LongArray(0),
    val note: String = ""
) : Parcelable {


    val isComplete: Boolean get() = totalTime > 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Attempt) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
