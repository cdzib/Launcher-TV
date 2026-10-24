package com.tv.launcher.custom.feature.apps.domain.repository

import com.tv.launcher.custom.core.domain.DomainResult
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import kotlinx.coroutines.flow.Flow

interface AppsRepository {
    fun observeApps(): Flow<List<AppEntry>>
    suspend fun launch(packageName: String): DomainResult<Unit>
}
