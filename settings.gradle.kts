pluginManagement {
    includeBuild("build-logic")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "tv-launcher"
include(":app")
include(":core:model", ":core:ui", ":core:theme", ":core:apps", ":core:data")
include(":feature:home", ":feature:search", ":feature:settings", ":feature:editor", ":feature:remote")
