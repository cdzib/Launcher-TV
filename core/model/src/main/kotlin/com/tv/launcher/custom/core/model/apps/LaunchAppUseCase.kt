package com.tv.launcher.custom.core.model.apps

import com.tv.launcher.custom.core.model.DomainResult

class LaunchAppUseCase(private val repository: AppsRepository) {
    suspend operator fun invoke(packageName: String): DomainResult<Unit> =
        repository.launch(packageName)
}
