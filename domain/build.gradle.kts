plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.register<JavaExec>("runDomainTests") {
    group = "verification"
    description = "Runs domain tests without external test frameworks."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.ttsandroid.domain.DomainTestRunner")
}

tasks.named("check") {
    dependsOn("runDomainTests")
}
