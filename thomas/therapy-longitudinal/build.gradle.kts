import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":thomas:domain"))
    api(project(":thomas:engine"))
    api(project(":thomas:safety"))
    api(project(":thomas:context-packet"))
    api(project(":thomas:retrieval"))
    api(project(":thomas:language-evidence"))
    api(project(":thomas:longitudinal-admission"))
    api(project(":thomas:longitudinal"))
    api(project(":thomas:ontology"))
    api(project(":thomas:provenance"))
    testImplementation(libs.junit)
}
