plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":platform"))
}

tasks.register<JavaExec>("runAppTests") {
    group = "verification"
    description = "Runs app tests without external test frameworks."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.ttsandroid.app.AppTestRunner")
}

tasks.named("check") {
    dependsOn("runAppTests")
}
