# git-version-gradle-plugin
Sets project version based on git tags.

If the current commit does not correspond to a tag, a new tag may be created.
For this, the setting `doCreateMissingTag` must be set to `true`.
E.g. using the command line:

```
./gradlew -PdoCreateMissingTag=true
```

If the version cannot be determined by git for any reason (not a git repository, or no git command found), the version will be "unspecified".
