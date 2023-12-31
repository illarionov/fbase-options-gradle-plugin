# Fbase Config Generator Gradle Plugin

Simple plugin that generates Firebase initialization code using values from a given configuration file.
Can be used as an addition or replacement for [Google Services Gradle Plugin]

#### Features

- Not tied to the location of the `google-services.json`
- Not tied to the format of the input file. You can use `google-servies.json`, Java Properties file, or implement your
  own Gradle Provider.
- Can generate multiple [FirebaseOptions] to use more than one instance of the Firebase in an application.
- Uses Android Resources minimally
- Supports Variants API, allowing customization for specific Android build types or product flavors

## Requirements

The latest version of this plugin requires:

- Android Gradle Plugin `8.1.4`or above
- Gradle `7.5.1` or above

## Installation

Release and snapshot versions of the plugin are published to a temporary repository, since they are currently
used only in one pet project. File a bug report if you think this plugin could be useful on Gradle Plugin Portal.

Add the following to your project's settings.gradle:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pixnews.ru")
            mavenContent {
                includeGroup("ru.pixnews.gradle.fbase")
            }
        }
    }
}
```

Add the plugin in the Application module of your project, where Firebase is initialized:

```
plugins { 
    id("ru.pixnews.gradle.fbase") version "0.1-SNAPSHOT"
}
```

## Usage

Using this plugin implies manual initialization of the [FirebaseApp] by passing the generated [FirebaseOptions] to the `FirebaseApp#initializeApp()`. Such kind of initialization can be useful in the following cases:

- When using more than one instance of the Firebase in an application (as described in 
  ["Use multiple projects in your application"][Use multiple projects in your application])
- If you want to postpone initialization of the Firebase to reduce app startup time
- When you need to use Firebase in your own [App Startup Initializers][App Startup Initializer] or WorkManager tasks
  and injecting ready-to-use Firebase instance thorough DI is more reliable than using the Firebase singleton.
- If you prefer to manually control the lifecycle of components in your application

Set the plugin's behavior through the `firebaseConfig` block in build.gradle.kts of the application module.

```kotlin
firebaseConfig {
    configurations {
        create("firebaseOptions") {
            fromGoogleServicesJson {
                location = layout.projectDirectory.file("google-services.json")
            }
            targetPackage = "com.example.config"
        }
    }
}
```

For this configuration, the plugin generates a [FirebaseOptions] property with the configuration values from the
specified `google-services.json` file, similar to this:

```kotlin
package com.example.config

import com.google.firebase.FirebaseOptions

internal val firebaseOptions: FirebaseOptions = FirebaseOptions.Builder()  
    .setProjectId("sample-en")
    .setApiKey("AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZ8")
    .setApplicationId("1:1035469437089:android:112233445566778899aabb")
    .setDatabaseUrl("https://sample-en.firebaseio.com")
    .setGaTrackingId("UA-65557217-3")
    .setGcmSenderId("1035469437089")
    .setStorageBucket("sample-en.appspot.com")
    .build()
```

This property can be used to initialize a Firebase singleton. For example:

```kotlin
class Application : Application() {
    var firebaseApp: FirebaseApp? = null

    override fun onCreate() {
        super.onCreate()
        firebaseApp = FirebaseApp.initializeApp(
            this,
            com.example.samplefbase.config.firebaseOptions,
        )
    }
}
```

In addition to this, a string android resource `google_app_id` will be added with the same value as 
in `setApplicationId()`.
It is required to initialize Google Analytics (only one instance of the analytics service can be used in a project).
Generation of this resource value can be disabled using the `addGoogleAppIdResource = false` parameter.

You can generate more than one configuration. For example:

```kotlin
firebaseConfig {
    primaryConfiguration = "firebaseOptionsApp1"
    configurations {
        create("firebaseOptionsApp1") {
            fromGoogleServicesJson {
                location = layout.projectDirectory.file("google-services-app1.json")
            }
        }
        create("firebaseOptionsApp2") {
            fromGoogleServicesJson {
                location = layout.projectDirectory.file("google-services-app2.json")
            }
        }
        create("firebaseOptionsApp3") {
            fromPropertiesFile {
                location = layout.projectDirectory.file("firebase-app3.properties")
            }
        }
    }
}
```

In this case, properties `firebaseOptionsApp1`, `firebaseOptionsApp2`, `firebaseOptionsApp3` will be created.
When using multiple configurations, primary configuration should be specified using the `primaryConfiguration` 
parameter. This configuration will be used to populate the string resource `google_app_id`
(for analytics service).

Plugin supports the following formats of the input data:

- `fromGoogleServicesJson {}`: from JSON file format used in [Google Services Gradle Plugin]
- `fromPropertiesFile {}`: from the Java .properties file
- `fromValues {}`: custom provider

#### google-services.json

`fromGoogleServicesJson {}` allows you to read settings from a configuration file in the format used in the Google
Services Gradle Plugin (`google-services.json`).

Usage example:

```kotlin
fromGoogleServicesJson {
	location = layout.projectDirectory.file("google-services.json")
	applicationId = "com.example.app"
}
```

If the `location` parameter is not specified, then the `google-services.json` file in the root of the subproject
will be used. If it does not exist, then the list of paths from the Google Services Gradle Plugin will be looked up.

`applicationId` - optional parameter. When using plugin in library modules, it allows you to specify the
Application ID that will be used to select the required configuration from the `google-services.json` file.

#### Properties file

`fromPropertiesFile` allows you to read settings from the Java properties file.

Usage example:

```kotlin
create("firebaseOptionsApp") {
    fromPropertiesFile {
        location = layout.projectDirectory.file("firebase-app.properties")
        applicationId = "com.example.app"
    }
}
```

Sample firebase-app.properties file:

```properties
# Value of the {YOUR_CLIENT}/client_info/mobilesdk_app_id in google-services.json
firebase_google_app_id = 1:1035469437089:android:73a4fb8297b2cd4f

# Value of the project_info/project_number in google-services.json
firebase_gcm_default_sender_id = 1035469437089
  
# Value of the {YOUR_CLIENT}/services/analytics-service/analytics_property/tracking_id in google-services.json
firebase_ga_tracking_id = UA-65557217-3

# Value of the project_info/firebase_url in google-services.json
firebase_database_url = https://sample-en.firebaseio.com

# Value of the {YOUR_CLIENT}/api_key/current_key in google-services.json
firebase_google_api_key = AIzbSyCILMsOuUKwN3qhtxrPq7FFemDJUAXTyZ8
  
# Value of the project_info/project_id in google-services.json
firebase_project_id = sample-en
  
# Value of the project_info/storage_bucket in google-services.json
firebase_storage_bucket = sample-en.appspot.com

# google_app_id for different build types
firebase_com_example_release_google_app_id = 1:1035469437089:android:112233445566778899aabb
firebase_com_example_benchmark_google_app_id = 1:1035469437089:android:2233445566778899aabbcc
firebase_com_example_sample_google_app_id = 1:1035469437089:android:33445566778899aabbccdd
```

Keys in `firebase_<application_id>_<key>` format can be used to override generated configuration values in build
variants with specific application ids.

Suppose the following keys are defined:

```properties
firebase_google_app_id = 1:1035469437089:android:73a4fb8297b2cd4f
firebase_com_example_release_google_app_id = 1:1035469437089:android:112233445566778899aabb
```

Value of the `.setApplicationId()` will be `1:1035469437089:android:112233445566778899aabb` in build variants with
namespace `com.example.release` and `1:1035469437089:android:73a4fb8297b2cd4f` in other build types.

Optional `applicationId` parameter can be used to specify the application id used in the applied keys.

#### Provider

`fromValues {}` can be used for your own implementation of the input data provider.
It requires implementation of the `Property<FbaseOptions>` interface.

For example, you can use [ValueSource]:

```kotlin
create("firebaseOptionsApp4") {
    fromValues {
        source = providers.of(CustomFbaseOptions::class.java) {}
    }
}

abstract class CustomFbaseOptions : ValueSource<FbaseOptions, ValueSourceParameters.None> {
    override fun obtain(): FbaseOptions {

        // … Read configuration parameters

        return FbaseOptions(
            projectId = "…",
            applicationId = "…",
            apiKey = "…",
            databaseUrl = "…",
            gaTrackingId = "…",
            gcmSenderId = "…",
            storageBucket = "…"
        )
    }
}
```

## Variant API

This plugin can be customized at the level of specific Android build types and product flavors.

Example of adding configurations for a specific build type:

```kotlin
android {
  …
    buildTypes {
    // …
        create("benchmark") {
            applicationIdSuffix = ".benchmark"
            extensions.configure<FbaseGeneratorExtension> {
                primaryConfiguration.set("benchmarkFirebaseOptions")
                configurations.create("benchmarkFirebaseOptions") {
                    fromPropertiesFile {
                        location.set(layout.projectDirectory.file("firebase_benchmark.properties"))
                    }
                }
            }
        }
    }
}
```

Adding configurations in product flavor:

```kotlin
android {
  …  
  productFlavors {
      …
      create("full") {
          dimension = "mode"
          applicationIdSuffix = ".full"
          extensions.configure<FbaseGeneratorExtension> {
              primaryConfiguration.set("fullFirebaseOptions")
              configurations.create("fullFirebaseOptions") {
                  fromPropertiesFile {
                      location.set(layout.projectDirectory.file("firebase_full.properties"))
                  }
              }
          }
      }
  }
}
```

You can also customize plugin in the `androidComponents.onVariants` block:

```kotlin
android {
    …
    androidComponents {
        onVariants(androidComponents.selector().withBuildType("release")) { variant ->
            variant.getExtension(FbaseGeneratorExtension::class.java)?.let { generator ->
                generator.configurations.create("releaseFirebaseOptions") {
                    fromPropertiesFile {
                        location.set(layout.projectDirectory.file("firebase_release.properties"))
                    }
                }
            }
        }
    }
}
```

## Other

It may be useful to disable Firebase's built-in automatic initialization via [FirebaseInitProvider] when
implementing manual initialization. This can be done by adding the following to `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"  
    xmlns:tools="http://schemas.android.com/tools">
    <application>
        <provider
            android:name="com.google.firebase.provider.FirebaseInitProvider"
            android:authorities="${applicationId}.firebaseinitprovider"
            tools:node="remove" />
    </application>
</manifest>
```

## Development notes

Project has 3 test suites:

- `test`: a suite of basic unit tests.
- `functionalTest`: a set of functional tests running on one fixed configuration of AGP, Gradle, Firebase
  and Java versions.
- `functionalMatrixTest`: a separate set of functional tests running on different combinations of AGP, Gradle,
  Firebase and Java versions. Not executed with the `test` Gradle lifecycle task.

The source code of the plugin is located in the `generator` module.
The `samples` directory contains a project with some sample applications. This project is also build on CI as part of
the test workflow.

Basic commands:

- Build the plugin: `./gradlew assemble`
- Run unit tests and basic functional tests: `./gradlew test`
- Running matrix tests: `./gradlew functionalMatrixTest`

By default, all tests in the `functionalMatrixTest` suite will be executed against all compatible versions of the
Android Gradle Plugin and Gradle. You can restrict execution to specific versions by setting the following environment
variables:

- `GRADLE_VERSION`
- `AGP_VERSION`
- `TEST_JDK_VERSION`
- `FIREBASE_VERSION`

Example: `GRADLE_VERSION=8.5 AGP_VERSION=8.2.0 TEST_JDK_VERSION=17 ./gradlew functionalMatrixTest`

## Contributing

Any type of contributions are welcome. Please see [the contribution guide](CONTRIBUTING.md).

The following types of contributions are especially welcome:

- Additions and corrections to documentation
- New feature proposals

### License

Fbase Config Generator Gradle Plugin is distributed under the terms of the Apache License (Version 2.0). See the
[license](https://github.com/illarionov/fbase-config-generator-gradle-plugin/blob/main/LICENSE) for more information.

[FirebaseApp]: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/FirebaseApp
[FirebaseInitProvider]: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/provider/FirebaseInitProvider
[initializeApp]: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/FirebaseApp#initializeApp(android.content.Context,com.google.firebase.FirebaseOptions)
[FirebaseOptions]: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/FirebaseOptions
[App Startup Initializer]: https://developer.android.com/reference/kotlin/androidx/startup/Initializer

[Use multiple projects in your application]: https://firebase.google.com/docs/projects/multiprojects#use_multiple_projects_in_your_application
[Google Services Gradle Plugin]: https://developers.google.com/android/guides/google-services-plugin
[ValueSource]: https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:requirements:external_processes
