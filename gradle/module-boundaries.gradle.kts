import org.gradle.api.artifacts.ProjectDependency

tasks.register("verifyModuleBoundaries") {
    group = "verification"
    description = "Checks that features are independent and core never depends on features or app."
    doLast {
        val violations = rootProject.subprojects.flatMap { source ->
            source.configurations.flatMap { configuration ->
                configuration.dependencies.withType(ProjectDependency::class.java).mapNotNull { dependency ->
                    val target = dependency.dependencyProject.path
                    if (target == source.path) return@mapNotNull null
                    val invalid = target == ":app" ||
                        (source.path.startsWith(":core:") && target.startsWith(":feature:")) ||
                        (source.path.startsWith(":feature:") && target.startsWith(":feature:")) ||
                        (source.path.startsWith(":feature:") && target in setOf(":core:data", ":core:apps") &&
                            !configuration.name.contains("test", ignoreCase = true))
                    if (invalid) "${source.path} -> $target (${configuration.name})" else null
                }
            }
        }.distinct()
        check(violations.isEmpty()) { "Invalid module dependencies:\n" + violations.joinToString("\n") }
    }
}

gradle.projectsEvaluated {
    rootProject.subprojects.forEach { module ->
        module.tasks.matching { it.name == "check" || it.name == "preBuild" }.configureEach {
            dependsOn(rootProject.tasks.named("verifyModuleBoundaries"))
        }
    }
}
