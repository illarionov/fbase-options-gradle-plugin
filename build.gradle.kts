plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jvm) apply false
    id("ru.pixnews.gradle.fbase.buildlogic.project.lint.detekt")
    id("ru.pixnews.gradle.fbase.buildlogic.project.lint.diktat")
    id("ru.pixnews.gradle.fbase.buildlogic.project.lint.spotless")
}

tasks.register("styleCheck") {
    group = "Verification"
    description = "Runs code style checking tools (excluding tests and Android Lint)"
    dependsOn("detektCheck", "spotlessCheck", "diktatCheck")
}
