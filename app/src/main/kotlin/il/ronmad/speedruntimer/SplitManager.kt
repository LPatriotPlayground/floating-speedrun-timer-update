package il.ronmad.speedruntimer

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SplitManager {

    private const val PREFS_NAME = "SpeedrunTimerPrefs"
    private const val KEY_GAMES   = "games_v2"

    private val gson = Gson()

    fun loadGames(context: Context): MutableList<Game> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json  = prefs.getString(KEY_GAMES, null) ?: return mutableListOf()
        val type  = object : TypeToken<MutableList<Game>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveGames(context: Context, games: List<Game>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_GAMES, gson.toJson(games)).apply()
    }


    fun recordAttempt(
        category: Category,
        segmentTimes: LongArray,
        totalTime: Long,
        note: String = ""
    ): Attempt {

        for (i in segmentTimes.indices) {
            val seg = segmentTimes[i]
            if (seg > 0L && (category.splits[i].bestSegment == 0L || seg < category.splits[i].bestSegment)) {
                category.splits[i].bestSegment = seg
            }
        }

        val previousPbTotal = if (category.hasPb) {
            category.splits.last().pbTime
        } else {
            Long.MAX_VALUE
        }

        if (totalTime in 1 until previousPbTotal) {

            var cumulative = 0L
            for (i in segmentTimes.indices) {
                cumulative += segmentTimes[i]
                category.splits[i].pbTime = cumulative
            }
        }

        val attempt = Attempt(
            id           = System.currentTimeMillis(),
            totalTime    = totalTime,
            segmentTimes = segmentTimes.copyOf(),
            note         = note
        )
        category.attempts.add(attempt)

        return attempt
    }


    fun recordReset(
        category: Category,
        segmentTimes: LongArray
    ) {
        for (i in segmentTimes.indices) {
            val seg = segmentTimes[i]
            if (seg > 0L && (category.splits[i].bestSegment == 0L || seg < category.splits[i].bestSegment)) {
                category.splits[i].bestSegment = seg
            }
        }
        category.attempts.add(
            Attempt(
                id           = System.currentTimeMillis(),
                totalTime    = 0L,
                segmentTimes = segmentTimes.copyOf()
            )
        )
    }


    fun setSplitIcon(split: Split, iconUri: String?) {
        split.iconUri = iconUri
    }


    fun setComparison(
        category: Category,
        target: ComparisonTarget,
        attemptId: Long = -1L
    ) {
        category.comparisonTarget    = target
        category.comparisonAttemptId = if (target == ComparisonTarget.ATTEMPT) attemptId else -1L
    }


    fun comparisonLabel(category: Category): String = when (category.comparisonTarget) {
        ComparisonTarget.PERSONAL_BEST -> "Personal Best"
        ComparisonTarget.SUM_OF_BEST   -> "Sum of Best"
        ComparisonTarget.NONE          -> "-"
        ComparisonTarget.ATTEMPT       -> {
            val attempt = category.comparisonAttempt
            if (attempt == null) "Custom Run"
            else {
                val secs = attempt.totalTime / 1000
                val label = if (attempt.note.isNotBlank()) attempt.note
                            else Util.formatTime(attempt.totalTime)
                "Run: $label"
            }
        }
    }
}
