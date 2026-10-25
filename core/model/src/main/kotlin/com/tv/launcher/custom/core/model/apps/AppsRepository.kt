package com.tv.launcher.custom.core.model.apps

import com.tv.launcher.custom.core.model.DomainResult
import kotlinx.coroutines.flow.Flow

interface AppsRepository {
    fun observeApps(): Flow<List<AppEntry>>
    suspend fun launch(packageName: String): DomainResult<Unit>
}
