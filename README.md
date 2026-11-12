# ComposeUI-Sturdy: OmAr-Kader (UNDER DEV)

[![](https://img.shields.io/jitpack/version/com.github.OmAr-Kader/ComposeUI-Sturdy.svg?label=Jitpack&color=9738EA)](https://jitpack.io/#OmAr-Kader/ComposeUI-Sturdy)
[![](https://img.shields.io/jitpack/v/github/OmAr-Kader/ComposeUI-Sturdy?label=Documentation&color=4285F4)](https://javadoc.jitpack.io/com/github/OmAr-Kader/ComposeUI-Sturdy/ComposeUI-Sturdy/latest/javadoc/)
[![](https://img.shields.io/github/license/OmAr-Kader/ComposeUI-Sturdy?label=License)](./LICENSE)


## Installation

### 1. Add JitPack repository

In your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Configure the Version Catalog

In `gradle/libs.versions.toml`:

```toml
[versions]
composeSturdy = "1.1.4"

[libraries]
compose-sturdy = { module = "com.github.OmAr-Kader.ComposeUI-Sturdy:Core", version.ref = "composeSturdy" }

[plugins]
sturdy-dimenscale = { id = "com.github.OmAr-Kader.ComposeUI-Sturdy", version.ref = "composeSturdy" }
```

### 3. Declare the DimenScale plugin

In your root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.sturdy.dimenscale) apply false
}
```

`apply false` makes the plugin available to subprojects without applying it to the root project.

### 4. Apply DimenScale to your Android module

In your app or library module's `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.sturdy.dimenscale)
}
```

### 5. Add ComposeUI-Sturdy

In the same module's `build.gradle.kts`:

```kotlin
dependencies {
    api(libs.compose.sturdy)
}
```

### What each dependency provides

- **DimenScale Gradle Plugin:** Integrates DimenScale into your Gradle build.
- **ComposeUI-Sturdy:** Adds the ComposeUI-Sturdy library to your module's compile and runtime classpaths.

Both are required when using ComposeUI-Sturdy together with DimenScale.