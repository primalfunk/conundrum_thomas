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
    implementation(project(":thomas:longitudinal"))
    implementation(project(":thomas:longitudinal-store"))
    implementation(project(":thomas:language-evidence"))
    implementation(project(":thomas:journal"))
    implementation(project(":thomas:biographer"))
    implementation(project(":thomas:retrieval"))
    implementation(project(":thomas:context-packet"))
    implementation(project(":thomas:therapy-longitudinal"))
    implementation(project(":thomas:language-renderer"))
    testImplementation(libs.junit)
    testImplementation(project(":thomas:runtime"))
}

tasks.test {
    systemProperty("thomas.repositoryRoot", rootProject.projectDir.absolutePath)
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
