import java.util.Properties

val localPropertiesFile = File(rootDir, "local.properties")
val localProperties = Properties()
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.reader())
}

pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
           google {
                content {
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                }
            }
            mavenCentral()
            maven(url = "https://androidx.dev/storage/compose-compiler/repository")
            maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
            gradlePluginPortal {
                content {
                }
            }
            maven {
                url = uri("https://jitpack.io")
                content {
                    includeGroup("com.github.QuickBirdEng.kotlin-snapshot-testing")
                }
            }
        }
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                // Appengine plugin doesn't publish the marker
                "com.google.cloud.tools.appengine" -> useModule("com.google.cloud.tools:appengine-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()

        // Kotzilla Github Repo
        maven {
            url = uri("https://maven.pkg.github.com/kotzilla-io/kotzilla-sdk")
            credentials {
                username =  localProperties.getProperty("GH_USER")
                password =  localProperties.getProperty("GH_TOKEN")
            }
        }
    }
}

rootProject.name = "Confetti"
include(":androidApp")
include(":androidBenchmark")
include(":androidTest")
include(":automotiveApp")
include(":common:car")
include(":shared")
include(":backend")
include(":backend:service-graphql")
include(":backend:datastore")
include(":backend:service-import")
include(":backend:terraform")
include(":landing-page")
include(":wearApp")
include(":wearBenchmark")
include(":webApp")
include(":compose-desktop")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    "This project needs to be run with Java 17 or higher (found: ${JavaVersion.current()})."
}
