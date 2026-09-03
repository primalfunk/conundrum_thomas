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
    api(project(":thomas:longitudinal-admission"))
    implementation(libs.sqlite.jdbc)
    testImplementation(libs.junit)
}

tasks.test {
    systemProperty("thomas.repositoryRoot", rootProject.projectDir.absolutePath)
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
