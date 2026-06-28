package il.ronmad.speedruntimer

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView

object ComparisonPickerDialog {

    fun show(
        context: Context,
        category: Category,
        onChanged: (Category) -> Unit
    ) {

        val fixedTargets = listOf(
            ComparisonTarget.PERSONAL_BEST,
            ComparisonTarget.SUM_OF_BEST,
            ComparisonTarget.NONE
        )

        val completeAttempts = category.attempts
            .filter { it.isComplete }
            .sortedBy { it.totalTime }

        data class Item(
            val label: String,
            val target: ComparisonTarget,
            val attemptId: Long = -1L
        )

        val items = mutableListOf<Item>()
        items += Item("Personal Best",  ComparisonTarget.PERSONAL_BEST)
        items += Item("Sum of Best",    ComparisonTarget.SUM_OF_BEST)
        items += Item("No comparison",  ComparisonTarget.NONE)

        completeAttempts.forEachIndexed { index, attempt ->
            val timeStr = Util.formatTime(attempt.totalTime)
            val noteStr = if (attempt.note.isNotBlank()) "  \"${attempt.note}\"" else ""
            val rank    = when (index) {
                0 -> "🥇 "
                1 -> "🥈 "
                2 -> "🥉 "
                else -> "#${index + 1} "
            }
            items += Item("${rank}${timeStr}${noteStr}", ComparisonTarget.ATTEMPT, attempt.id)
        }

        val currentIndex = when (category.comparisonTarget) {
            ComparisonTarget.PERSONAL_BEST -> 0
            ComparisonTarget.SUM_OF_BEST   -> 1
            ComparisonTarget.NONE          -> 2
            ComparisonTarget.ATTEMPT       -> {
                val idx = items.indexOfFirst {
                    it.target == ComparisonTarget.ATTEMPT &&
                    it.attemptId == category.comparisonAttemptId
                }
                if (idx < 0) 0 else idx
            }
        }

        AlertDialog.Builder(context)
            .setTitle("Compare against...")
            .setSingleChoiceItems(
                items.map { it.label }.toTypedArray(),
                currentIndex
            ) { dialog, which ->
                val chosen = items[which]
                SplitManager.setComparison(category, chosen.target, chosen.attemptId)
                onChanged(category)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
