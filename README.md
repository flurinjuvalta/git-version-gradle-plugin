# git-version-gradle-plugin
Sets project version and publishing repository based on git tags.

The plugin will also create missing git tags for you.

## Usage

You may use the plugin using the plugin repository or the buildscript.

### Plugin Repository
Add the avelon repository to the plugin repositories.
For this the following is required in your `gradle.settings`:

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal() // Include public Gradle plugin portal
        maven {
            url 'http://nexus.avelon/nexus/content/repositories/releases'
        }
    }
}
```

Then you can include the plugin in the `plugins` section of your `build.gradle`:
```groovy
plugins {
    id "ch.avelon.git-version-gradle-plugin" version "1.0.0"
    // other plugins
}
```
(Don't forget to update the version accordingly) 

### Buildscript
Yoou may also use the buildscript approach. Fir this the following has to be added at the
beginning of your `build.gradle` in the `buildscript` section:

```groovy
buildscript {
    repositories {
        maven {
            url 'http://nexus.avelon/nexus/content/repositories/releases'
        }
    }
    dependencies {
        classpath 'ch.avelon:git-version-gradle-plugin:1.0.0'
    }
}
```

And then apply the plugin using

```groovy
apply plugin: 'ch.avelon.git-version-gradle-plugin'
```

## Logic

### Version
If the version cannot be determined by git for any reason (not a git repository, or no
git command found), the version will be set to "unspecified".

The version depends on the type of branch. Any branch different than master or releas are
treated as develop branch. Only the master branch has to be tagged.
The version is calculated as following:

  1. If you are on the master branch and `doCreateMissingTag` i set, a new Tag is created.
  2. If you are on the release branch or there is no release branch, the minor version is
     incremented by 1.
  3. If there is a release branch and you are on a develop branch, the minor version is
     incremented by 2.
  4. The short version of the git commit SHA is appended if the curent commit does
     not correspond to the latest tag.
  5. If you are on a develop branch, the term "SNAPSHOT" is added to the version. 
  
e.g.
  - on master: `7.0.23` -> `7.0.23-12eba997`
  - on release/*: `7.0.23` -> `7.1.0-12eba997`
  - on a develop branch without a release branch: `7.0.23` -> `7.1.0-12eba997-SNAPSHOT`
  - on a develop branch with a release branch: `7.0.23` -> `7.2.0-12eba997-SNAPSHOT`

### Repository

The nexus repository for uploading the artifact depends on the branch:
- master -> `releases`
- release -> `release-candidates`
- develop -> `snapshots`

### Automatically create a tag

If the current commit does not correspond to a tag, a new tag may be created.
For this, the setting `doCreateMissingTag` must be set to `true`.
E.g. using the command line:

```
./gradlew -PdoCreateMissingTag=true
```

The new tag will take the last commit and increment the patch number.
e.g. last tag `1.1.0` -> newly created tag `1.1.1`. 

