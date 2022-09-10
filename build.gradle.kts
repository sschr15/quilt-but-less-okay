@Suppress("DSL_SCOPE_VIOLATION", "MISSING_DEPENDENCY_CLASS", "UNRESOLVED_REFERENCE_WRONG_RECEIVER", "FUNCTION_CALL_EXPECTED")
plugins {
	`maven-publish`
    java
//	alias(libs.plugins.quilt.loom)
	alias(libs.plugins.quilt.licenser)
}

//archivesBaseName = "quilt-but-less-okay"
allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")
    apply(plugin = "org.quiltmc.gradle.licenser")

    version = "0.4.0"
    group = "sschr15.tools.qblo"

    repositories {
        mavenCentral()
    }

// All the dependencies are declared at gradle/libs.version.toml and referenced with "libs.<id>"
// See https://docs.gradle.org/current/userguide/platforms.html for information on how version catalogs work.
    dependencies {
        implementation(rootProject.libs.jetbrains.annotations)
    }

    tasks.processResources {
        inputs.property("version", version)

        filesMatching(listOf("quilt.mod.json", "fabric.mod.json", "mods.toml")) {
            expand(mapOf("version" to version))
        }
    }

//tasks.withType(JavaCompile::class.java).configureEach {
//	options.encoding = "UTF-8"
//}

    tasks.compileJava {
        val exports = mapOf(
            "java.base" to listOf(
                listOf("reflect", "misc", "access", "loader").map { "jdk.internal.$it" },
                listOf("commons", "signature", "tree", "tree.analysis", "util").map { "jdk.internal.org.objectweb.asm.$it" },
                listOf("jdk.internal.org.objectweb.asm"),
            ).flatten(),
        )
        for (export in exports) {
            val mod = export.key
            val packages = export.value
            for (pkg in packages) {
                options.compilerArgs.addAll(listOf("--add-exports", "$mod/$pkg=ALL-UNNAMED"))
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task if it is present.
        // If you remove this line, sources will not be generated.
        withSourcesJar()

        // If this mod is going to be a library, then it should also generate Javadocs in order to aid with development.
        // Uncomment this line to generate them.
        // withJavadocJar()
    }

// If you plan to use a different file for the license, don't forget to change the file name here!
    tasks.jar {
        from("LICENSE") {
            rename { "${it}_quilt-but-less-okay" }
        }
    }

// Configure the maven publication
    publishing {
        publications {
            val mavenJava by creating(MavenPublication::class.java) {
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

    license {
        rule(rootProject.file("codeformat/HEADER"))

        include("**/*.java")
        include("**/*.kt")
    }
}

subprojects {
    dependencies {
        implementation(rootProject)
    }
}
