package com.tv.launcher.custom.core.model.apps

import kotlinx.coroutines.flow.Flow

class ObserveAppsUseCase(private val repository: AppsRepository) {
    operator fun invoke(): Flow<List<AppEntry>> = repository.observeApps()
}
