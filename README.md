# version-support-plugin

Gradle plugin to handle project versions.

Experimental.


## Usage 

Ensure you have a working git installation with permission to push!

`build.gralde.kts`
```kotlin
plugins {
	// ...
    id("com.namics.oss.gradle.version-support-plugin") version "0.1.0"
}
```


Adds 2 Task the project 

- `release`: perform a release
- `enforceSnapshotOnBranch`: enforces the next snapshot version on the current branch

Invoke:
```bash
gradle enforceLicenses
```
 
