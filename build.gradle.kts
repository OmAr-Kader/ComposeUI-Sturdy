// ./gradlew clean build publishToMavenLocal

import org.tomlj.Toml
//import java.nio.file.Files

plugins {
    id("base")
    id("maven-publish")
}

val libVersionsToml = "libs.versions.toml"

val libVersion = "1.1.3-alpha-1" // Keep in sync with README.md and libs.versions.toml.
val libName = "ComposeUI-Sturdy"
val libRepoUrl = "https://github.com/OmAr-Kader/ComposeUI-Sturdy"

val idGroup = "com.github.OmAr-Kader"
val composeArtifactId = "Core"
val dimenScaleArtifactId = "Dimenscale"

val dimenScalePluginId = "com.github.OmAr-Kader.ComposeUI-Sturdy"

group = idGroup
version = libVersion

buildscript {
    dependencies {
        classpath("org.tomlj:tomlj:2.2.0")
        classpath("org.checkerframework:checker-qual:3.55.1")
    }
}

publishing {
    publications {
        create<MavenPublication>("dimenscalePluginMarker") {
            groupId = dimenScalePluginId
            artifactId = "$dimenScalePluginId.gradle.plugin"
            version = libVersion

            pom {
                packaging = "pom"
                name.set("DimenScale Gradle Plugin")
                description.set("Plugin marker for DimenScale Gradle Plugin")
                url.set(libRepoUrl)

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("OmAr-Kader")
                        name.set("Omar Kader")
                        url.set("https://github.com/OmAr-Kader")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/OmAr-Kader/ComposeUI-Sturdy.git")
                    developerConnection.set("scm:git:ssh://github.com/OmAr-Kader/ComposeUI-Sturdy.git")
                    url.set(libRepoUrl)
                }

                withXml {
                    val dependency = asNode()
                        .appendNode("dependencies")
                        .appendNode("dependency")

                    dependency.appendNode("groupId", idGroup)
                    dependency.appendNode("artifactId", dimenScaleArtifactId)
                    dependency.appendNode("version", libVersion)
                }
            }
        }
        create<MavenPublication>("maven") {
            groupId = idGroup
            artifactId = composeArtifactId
            version = libVersion

            // These are prebuilt files in the public project's root directory.
            artifact(file("$libName-$libVersion.aar"))

            artifact(file("$libName-$libVersion-javadoc.jar")) {
                classifier = "javadoc"
            }

            artifact(file("$libName-$libVersion-sources.jar")) {
                classifier = "sources"
            }

            artifact(file("$libName-$libVersion-dimenscale.jar")) {
                classifier = "dimenscale"
            }

            pom {
                packaging = "aar"
                name.set(libName)
                description.set("Compose UI library for Android - Bridge to SturdyRoot")
                url.set(libRepoUrl)

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("OmAr-Kader")
                        name.set("Omar Kader")
                        url.set("https://github.com/OmAr-Kader")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/OmAr-Kader/ComposeUI-Sturdy.git")
                    developerConnection.set("scm:git:ssh://github.com/OmAr-Kader/ComposeUI-Sturdy.git")
                    url.set(libRepoUrl)
                }

                // Do not add maven.source.skip: it is unrelated to Gradle/Android Studio
                // attaching the already-published *-sources.jar.
                withXml {
                    fetchDependencies()
                }
            }
        }

        // Publish DimenScale as a standalone JAR without the Android library dependencies.
        create<MavenPublication>("dimenscalePlugin") {
            groupId = idGroup
            artifactId = dimenScaleArtifactId
            version = libVersion

            artifact(file("$libName-$libVersion-dimenscale.jar"))

            pom {
                packaging = "jar"
                name.set("DimenScale Gradle Plugin")
                description.set("Standalone DimenScale Gradle and compiler plugin")
                url.set(libRepoUrl)

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("OmAr-Kader")
                        name.set("Omar Kader")
                        url.set("https://github.com/OmAr-Kader")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/OmAr-Kader/ComposeUI-Sturdy.git")
                    developerConnection.set("scm:git:ssh://github.com/OmAr-Kader/ComposeUI-Sturdy.git")
                    url.set(libRepoUrl)
                }
            }
        }
    }
}

/**
 * Adds version-catalog dependencies to the generated POM.
 * BOMs must be declared under dependencyManagement with scope=import;
 * ordinary libraries belong under the top-level dependencie element.
 */
fun XmlProvider.fetchDependencies() {
    val root = asNode()
    val tomlFile = file(libVersionsToml)

    require(tomlFile.isFile) {
        "Version catalog not found: ${tomlFile.absolutePath}"
    }

    /*val parsedToml = Files.newBufferedReader(tomlFile.toPath()).use { reader ->
        Toml.parse(reader)
    }*/
    val parsedToml = tomlFile.bufferedReader(Charsets.UTF_8).use { reader ->
        Toml.parse(reader)
    }

    val versions = parsedToml.getTable("versions")
        ?: error("[versions] table not found in $libVersionsToml")
    val libraries = parsedToml.getTable("libraries")
        ?: error("[libraries] table not found in $libVersionsToml")

    data class PomDependency(
        val groupId: String,
        val artifactId: String,
        val version: String,
        val isBom: Boolean
    )

    val pomDependencies = mutableListOf<PomDependency>()

    libraries.keySet().forEach { alias ->
        val library = libraries.getTable(alias) ?: return@forEach
        val module = library.getString("module") ?: return@forEach
        val moduleParts = module.split(":", limit = 2)

        require(moduleParts.size == 2) {
            "Invalid module '$module' in version catalog alias '$alias'. Expected group:artifact."
        }

        /*val versionRef = library.getTable("version")?.getString("ref")
            ?: library.getString("version.ref")
        val dependencyVersion = versionRef?.let { versions.getString(it) }
            ?: library.getString("version")
            ?: return@forEach*/

        val versionRef: String? =
            library.getTable("version")?.getString("ref")
                ?: library.getString("version.ref")

        val dependencyVersion: String =
            versionRef?.let { versions.getString(it) }
                ?: library.getString("version")
                ?: return@forEach

        pomDependencies += PomDependency(
            groupId = moduleParts[0],
            artifactId = moduleParts[1],
            version = dependencyVersion,
            isBom = moduleParts[1].endsWith("-bom")
        )
    }

    val bomDependencies = pomDependencies.filter { it.isBom }
    val regularDependencies = pomDependencies.filterNot { it.isBom }

    // Maven POM element order: dependencyManagement must precede dependencies.
    if (bomDependencies.isNotEmpty()) {
        val managedDependencies = root
            .appendNode("dependencyManagement")
            .appendNode("dependencies")

        bomDependencies.forEach { dependency ->
            val node = managedDependencies.appendNode("dependency")
            node.appendNode("groupId", dependency.groupId)
            node.appendNode("artifactId", dependency.artifactId)
            node.appendNode("version", dependency.version)
            node.appendNode("type", "pom")
            node.appendNode("scope", "import")
        }
    }

    if (regularDependencies.isNotEmpty()) {
        val dependenciesNode = root.appendNode("dependencies")

        regularDependencies.forEach { dependency ->
            val node = dependenciesNode.appendNode("dependency")
            node.appendNode("groupId", dependency.groupId)
            node.appendNode("artifactId", dependency.artifactId)
            node.appendNode("version", dependency.version)
        }
    }
}
