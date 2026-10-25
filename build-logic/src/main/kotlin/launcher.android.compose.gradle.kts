import com.android.build.gradle.LibraryExtension
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("launcher.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<LibraryExtension> { buildFeatures { compose = true } }
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    add("implementation", platform(catalog.findLibrary("androidx-compose-bom").get()))
    listOf("androidx-compose-ui", "androidx-compose-foundation", "androidx-compose-ui-tooling-preview",
        "androidx-compose-material-icons-extended", "androidx-compose-material3", "androidx-tv-material",
        "kotlinx-collections-immutable").forEach { add("implementation", catalog.findLibrary(it).get()) }
    add("debugImplementation", catalog.findLibrary("androidx-compose-ui-tooling").get())
}
