package com.tv.launcher.custom.feature.apps.domain.usecase

import com.tv.launcher.custom.core.domain.DomainResult
import com.tv.launcher.custom.feature.apps.domain.repository.AppsRepository
import javax.inject.Inject

class LaunchAppUseCase @Inject constructor(private val repository: AppsRepository) {
    suspend operator fun invoke(packageName: String): DomainResult<Unit> =
        repository.launch(packageName)
}
