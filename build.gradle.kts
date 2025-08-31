plugins {
    kotlin("jvm") version "1.9.25"
    id("application")
}

group = "com.onskhaldi.kaboo"
version = "1.0"

val bgwVersion = "0.10"

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))

    implementation("tools.aqua:bgw-gui:$bgwVersion")
    implementation("tools.aqua:bgw-net-common:$bgwVersion")
    implementation("tools.aqua:bgw-net-client:$bgwVersion")
}

tasks.clean {
    delete("public")
}
