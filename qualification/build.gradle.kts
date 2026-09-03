import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":thomas:domain"))
    implementation(project(":thomas:provenance"))
    implementation(project(":thomas:ontology"))
    implementation(project(":thomas:engine"))
    implementation(project(":thomas:safety"))
    testImplementation(libs.junit)
    testImplementation(project(":thomas:runtime"))
}

tasks.test {
    systemProperty("thomas.repositoryRoot", rootProject.projectDir.absolutePath)
}
