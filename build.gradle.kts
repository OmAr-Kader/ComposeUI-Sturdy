// ./gradlew clean build publishToMavenLocal

import org.tomlj.Toml
import java.nio.file.Files

plugins {
    id("maven-publish")
}

val libVersion = "1.0.6-alpha1" // VERSION + README.md Version + UPDATE libs.versions.toml
val libVersionsToml = "libs.versions.toml" // UPDATE
val libName = "ComposeUI-Sturdy"

group = "com.github.OmAr-Kader"
version = libVersion

buildscript {
    dependencies {
        classpath("org.tomlj:tomlj:1.1.1")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.OmAr-Kader"
            artifactId = libName
            version = libVersion

            // Explicitly tell the publication this artifact behaves as an AAR packaging type
            artifact(file("$libName-$libVersion.aar")) {
                extension = "aar"
            }

            // Your pre-built Dokka jar from RepoA
            artifact(file("$libName-$libVersion.jar")) {
                classifier = "javadoc"
                extension = "jar"
            }

            pom {
                packaging = "aar"
                name.set(libName)
                description.set("Library Description")
                url.set("https://github.com/OmAr-Kader/ComposeUI-Sturdy")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
                    url.set("https://github.com/OmAr-Kader/ComposeUI-Sturdy")
                }

                withXml {
                    fetchDependencies()
                }
            }
        }
    }
}

fun XmlProvider.fetchDependencies() {
    val dependenciesNode = asNode().appendNode("dependencies")
    val tomlFile = file(libVersionsToml)

    if (!tomlFile.exists()) return

    val parsedToml = Toml.parse(Files.newBufferedReader(tomlFile.toPath()))

    parsedToml.getTable("versions")?.also { versions ->
        parsedToml.getTable("libraries")?.also { libsTable ->
            libsTable.keySet().forEach { alias ->

                // SKIP test and debug dependencies to avoid bloating the production SDK
                if (alias.contains("test") || alias.contains("junit") || alias.contains("mockk")) {
                    return@forEach
                }

                libsTable.getTable(alias)?.also { libData ->
                    val versionRef = libData.getString("version.ref")

                    var finalGroup: String? = null
                    var finalArtifact: String? = null

                    // Fixes Bug #2: Safely check for both string module vs split group/name declarations
                    val module = libData.getString("module")
                    if (module != null) {
                        val parts = module.split(":")
                        if (parts.size == 2) {
                            finalGroup = parts[0]
                            finalArtifact = parts[1]
                        }
                    } else {
                        finalGroup = libData.getString("group")
                        finalArtifact = libData.getString("name")
                    }

                    if (finalGroup != null && finalArtifact != null && versionRef != null) {
                        val version = versions.getString(versionRef)

                        val depNode = dependenciesNode.appendNode("dependency")
                        depNode.appendNode("groupId", finalGroup)
                        depNode.appendNode("artifactId", finalArtifact)
                        depNode.appendNode("version", version)
                        depNode.appendNode("scope", "runtime") // Standard for compiled library distribution

                        if (finalArtifact.endsWith("-bom")) {
                            depNode.appendNode("type", "pom")
                            depNode.appendNode("scope", "import")
                        }
                    }
                }
            }
        }
    }
}