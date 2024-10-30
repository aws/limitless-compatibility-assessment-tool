import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    `java-library`
    `maven-publish`
    id("software.amazon.peru.maven-publish") version "1.0.+"
    id("software.amazon.peru.brazilcli") version "1.0.+"
    // JaCoCo for coverage metrics and reports of Java source files. Read more at:
    // https://docs.gradle.org/current/userguide/jacoco_plugin.html
    jacoco
    id("io.freefair.lombok") version "8.2.2"
    id("com.autonomousapps.dependency-analysis") version "1.0.+"
    antlr
}
group = "com.amazon.limitless.assessment"
version = "1.0"

dependencies {
    // implementation("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation("commons-cli:commons-cli:1.6.0")
    implementation("commons-lang:commons-lang:2.6")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.mockito:mockito-core:5.13.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.13.0")
// remove this block if your project does not use Junit.
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    antlr("org.antlr:antlr4:4.13.2")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.jar {
    manifest.attributes["Main-Class"] = "com.amazon.limitless.assessment.LimitlessCompatibilityAssessmentTool"
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree) // OR .map { zipTree(it) }
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks {
    test {
        useJUnitPlatform()
        /*
            Print the tests to STDOUT to verify that they actually run
        */
        testLogging {
            events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        }
        finalizedBy(jacocoTestReport)
    }

   /*
     Configures the JaCoCo "jacoco" plugin. Remove this if you want to skip
     these checks and report generation.
     Set minimum code coverage to fail build, where 0.01 = 1%.
    */
    jacocoTestCoverageVerification {
        violationRules {
//            rule { limit { minimum = BigDecimal.valueOf(0.75) } }
        }
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }

    check {
        dependsOn(jacocoTestCoverageVerification)
    }

    generateGrammarSource {
        // outputDirectory = file("src/main/antlr/")
    }

}

sourceSets {
  main {
    java {
      srcDir(tasks.generateGrammarSource)
    }
  }
}

dependencyAnalysis {
    issues {
        all { // all projects
            onAny {
                severity("warn")
            }
        }
    }
}

gradle.taskGraph.whenReady {
    allTasks
        .filter { it.hasProperty("duplicatesStrategy") }
        .forEach {
            it.setProperty("duplicatesStrategy", "EXCLUDE")
        }
}