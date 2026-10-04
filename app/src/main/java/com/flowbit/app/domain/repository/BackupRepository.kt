package com.flowbit.app.domain.repository

import android.net.Uri

interface BackupRepository {
    suspend fun exportJson(uri: Uri)
    suspend fun importJson(uri: Uri)
    suspend fun exportCsv(uri: Uri)
}
