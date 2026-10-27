plugins {
    id("launcher.android.compose")
}
dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:theme"))
    testImplementation(libs.junit)
    testImplementation(project(":core:data"))
    testImplementation(libs.kotlinx.serialization.json)
}
