plugins {
    id("java")
}

group = "ReminderAppParentFolder"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("com.github.kwhat:jnativehook:2.2.2")
}

tasks.test {
    useJUnitPlatform()
}