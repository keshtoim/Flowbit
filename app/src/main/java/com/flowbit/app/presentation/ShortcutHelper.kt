package com.flowbit.app.presentation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.flowbit.app.domain.model.Habit

object ShortcutHelper {
    const val EXTRA_HABIT_ID = "shortcut_habit_id"
    const val ACTION_OPEN_HABIT = "com.flowbit.app.OPEN_HABIT"

    fun updateShortcuts(context: Context, habits: List<Habit>) {
        val shortcuts = habits.take(5).mapIndexed { index, habit ->
            ShortcutInfoCompat.Builder(context, "habit_${habit.id}")
                .setRank(index)
                .setShortLabel("${habit.emoji} ${habit.name.take(12)}")
                .setLongLabel(habit.name)
                .setIcon(IconCompat.createWithBitmap(emojiToBitmap(habit.emoji)))
                .setIntent(
                    Intent(context, MainActivity::class.java)
                        .setAction(ACTION_OPEN_HABIT)
                        .putExtra(EXTRA_HABIT_ID, habit.id)
                )
                .build()
        }
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }

    private fun emojiToBitmap(emoji: String, size: Int = 108): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size * 0.62f
            textAlign = Paint.Align.CENTER
        }
        val y = (size - (paint.descent() + paint.ascent())) / 2f
        canvas.drawText(emoji, size / 2f, y, paint)
        return bitmap
    }
}
