// ./gradlew clean build publishToMavenLocal
import org.tomlj.Toml
import java.nio.file.Files

plugins {
    id("java-library")
    id("maven-publish")
    //id("org.jetbrains.dokka") version "2.1.0"
}

val libVersion = "1.0.0" // VERSION + README.md Version + ANY NEW DEP
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

            artifact(file("$libName-$libVersion.aar"))

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

    // Path to your libs.versions.toml file
    val tomlFile = file(libVersionsToml)

    // Parse the TOML file
    val parsedToml = Toml.parse(Files.newBufferedReader(tomlFile.toPath()))

    // Get versions map
    parsedToml.getTable("versions")?.also { versions ->
        // Get libraries table
        parsedToml.getTable("libraries")?.also { libsTable ->
            libsTable.keySet().forEach { alias ->
                libsTable.getTable(alias)?.also { libData ->
                    val module = libData.getString("module")
                    val versionRef = libData.getString("version.ref")

                    if (module != null && versionRef != null) {
                        val (group, artifact) = module.split(":")
                        val version = versions.getString(versionRef)

                        val depNode = dependenciesNode.appendNode("dependency")
                        depNode.appendNode("groupId", group)
                        depNode.appendNode("artifactId", artifact)
                        depNode.appendNode("version", version)
                        //depNode.appendNode("scope", "runtime")
                        if (artifact.endsWith("-bom")) {
                            depNode.appendNode("type", "pom")
                            depNode.appendNode("scope", "import")
                        }
                    }
                }
            }
        }

    }
}