package com.tv.launcher.custom.feature.apps.domain.usecase

import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.apps.domain.repository.AppsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppsUseCase @Inject constructor(private val repository: AppsRepository) {
    operator fun invoke(): Flow<List<AppEntry>> = repository.observeApps()
}
