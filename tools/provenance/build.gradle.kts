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
    implementation(libs.sqlite.jdbc)
    implementation(project(":thomas:provenance"))
    testImplementation(libs.junit)
}

tasks.register<JavaExec>("generateRuntimeProvenanceDb") {
    group = "provenance"
    description = "Generates the local provenance SQLite database from tracked inputs."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.conundrum.thomas.v2.tools.provenance.ProvenanceDatabaseBuilderKt")
    args(rootProject.projectDir.absolutePath)
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.test {
    systemProperty("thomas.repositoryRoot", rootProject.projectDir.absolutePath)
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
