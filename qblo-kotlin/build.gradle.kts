plugins {
    kotlin("jvm") version "1.7.10"
    `maven-publish`
}

group = "sschr15.tools.qblo"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://maven.concern.i.ng/releases/")
}

dependencies {
    implementation("sschr15.tools.qblo:quilt-but-less-okay:0.2.1")
    implementation(kotlin("reflect"))
}

// Configure the maven publication
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        maven {
            name = "concernmaven"
            url = uri("https://maven.concern.i.ng/releases")
            credentials {
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_PASS")
            }
        }
    }
}

kotlin {
    explicitApi()
}
