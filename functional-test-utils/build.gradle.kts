import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.jvm)
}

group = "ru.pixnews.gradle.fbase.test"

dependencies {
    api(gradleApi())
    api(gradleTestKit())
    api(libs.assertk)
    api(libs.junit.jupiter.api)
    api(platform(libs.junit.bom))
    implementation(libs.android.tools.apkparser.apkanalyzer)
    implementation(libs.android.tools.apkparser.binary.resources)
    implementation(libs.android.tools.common)
    implementation(libs.android.tools.smali.dexlib2)

    testImplementation(libs.assertk)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        // https://docs.gradle.org/current/userguide/compatibility.html#kotlin
        apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_6
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all",
        )
        if (!this@configureEach.name.endsWith("TestKotlin")) {
            freeCompilerArgs.addAll("-Xexplicit-api=warning")
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}
