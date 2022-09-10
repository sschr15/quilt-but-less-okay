plugins {
    kotlin("jvm") version "1.7.10"
}

dependencies {
    implementation(kotlin("reflect"))
}

kotlin {
    explicitApi()
}
