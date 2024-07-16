plugins {
    java
    checkstyle
    jacoco
    id("brazil-gradle")
    id("brazil-gradle-java-presets")
    id("com.github.spotbugs")
}

brazilGradle {
    configureBasicDependencies()
    configureAnnotationProcessors()
}

spotbugs {
    ignoreFailures.set(false)
}

tasks.withType<Test> {
  useJUnitPlatform()
}
