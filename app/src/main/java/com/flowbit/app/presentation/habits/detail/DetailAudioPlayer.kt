package com.flowbit.app.presentation.habits.detail

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// ── Встроенный аудиоплеер для мотивационного трека ───────────────────────────
// MediaPlayer освобождается через DisposableEffect при выходе из композиции

@Composable
internal fun DetailAudioPlayer(audioUri: String) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer() }
    var isPlaying by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }

    DisposableEffect(audioUri) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(context, Uri.parse(audioUri))
            mediaPlayer.setOnPreparedListener { isPrepared = true }
            mediaPlayer.setOnCompletionListener { isPlaying = false }
            mediaPlayer.prepareAsync()
        } catch (_: Exception) {
            isPrepared = false
        }
        onDispose {
            // Безопасный релиз — игрок мог не быть в состоянии started
            try { mediaPlayer.stop() } catch (_: Exception) { }
            try { mediaPlayer.release() } catch (_: Exception) { }
        }
    }

    // Получаем имя файла из ContentResolver; fallback — последний сегмент URI
    val fileName = remember(audioUri) {
        try {
            val cursor = context.contentResolver.query(
                Uri.parse(audioUri),
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null, null, null,
            )
            cursor?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        } catch (_: Exception) { null }
    } ?: audioUri.substringAfterLast('/')

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (!isPrepared) return@IconButton
                    if (isPlaying) {
                        mediaPlayer.pause()
                        isPlaying = false
                    } else {
                        mediaPlayer.start()
                        isPlaying = true
                    }
                },
                enabled = isPrepared,
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
