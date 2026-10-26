plugins {
    id("launcher.android.compose")
}
dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
