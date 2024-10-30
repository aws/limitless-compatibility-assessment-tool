plugins {
}
rootProject.name = "LimitlessAssessmentToolInternal"
val dependencyModel: String? by settings
if (dependencyModel == "peru") {
    rootProject.buildFileName = "peru.gradle.kts"
} else {
    rootProject.buildFileName = "build.gradle"
}
