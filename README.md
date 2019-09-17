# version-support-plugin

Gradle plugin to handle project versions.

Experimental.

## Usage 

To use the plugin you need a git repository with a remote with push permissions for involved branches and tags.
 
### Git Authentication

#### Token in URL

If your git hosting supports credentials in URLs with a token you may use a suitable remote url. (e.g. github using a token)
`JGitManager(remoteUri = "https://${GITHUB_TOKEN}@github.com:/path/to/repository.git")`
The token can be provided as encrypted environment variable. 

#### SSH private key

You need to have a "default" ssh private key without a passphrase at the systems default location.
In a continuous integration environment you may achieve that by adding the `id_rsa` from a (secure) environment variable configured in you CI system.

```bash
mkdir -p ~/.ssh
echo "$SSH_PRIVATE_KEY" | tr -d '\r'  > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa
```

Alternatively you may rely on `ssh-agent` with native `git` if available on your build machine.
```bash
  which ssh-agent || ( apt-get update -y && apt-get install openssh-client -y )
  eval $(ssh-agent -s)
  mkdir -p ~/.ssh
  echo -n "$SSH_PRIVATE_KEY" | ssh-add - >/dev/null
```

### Git operation

You may either rely on a native `git` on your build machine or use JGit based implementation that does not require further binaries on you build machine. 

`build.gralde.kts`
```kotlin
plugins {
	// ...
    id("com.namics.oss.gradle.version-support-plugin") version "1.3.0"
}

// ...

// either rely on native git using defaults (default values)
versionSupport {
    git = NativeGitManager(project) 
    majorBranches = emptyList()
    minorBranches = listOf(Regex("""^develop.*"""))
    patchBranches = listOf(Regex("""^hotfix.*"""))
}

// or explicitly configure plugin to use JGit
versionSupport {
    git = JGitManager(
            project = project,
            initialBranch = System.getenv("CI_COMMIT_REF_NAME"),
            remote = "upstream",
            remoteUri = "git@ssh.git.hosting:/path/to/repository.git" )
}

```

Adds 2 Task the project 

- `release`: perform a release
- `enforceSnapshotOnBranch`: enforces the next snapshot version on the current branch

Invoke:

```bash
gradle release
```

 ```bash
gradle enforceSnapshotOnBranch


```
 
## Development

### Commit Message Format

Each commit message consists of a **header**, a **body** and a **footer**.  The header has a special
format that includes a **type**, a **scope** and a **subject**:

```
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>
```

> [Full explanation](https://github.com/conventional-changelog/conventional-changelog-angular/blob/master/convention.md)

The **header** is mandatory and the **scope** of the header is optional.

#### Examples

Appears under "Features" header, pencil subheader:

```
feat(pencil): add 'graphiteWidth' option
```

Appears under "Bug Fixes" header, graphite subheader, with a link to issue #28:

```
fix(graphite): stop graphite breaking when width < 0.1

Closes #28
```

### Revert

If the commit reverts a previous commit, it should begin with `revert: `, followed by the header of the reverted commit. In the body it should say: `This reverts commit <hash>.`, where the hash is the SHA of the commit being reverted.

### Type

If the prefix is `feat`, `fix` or `perf`, it will appear in the changelog. However if there is any [BREAKING CHANGE](#footer), the commit will always appear in the changelog.

Other prefixes are up to your discretion. Suggested prefixes are `docs`, `chore`, `style`, `refactor`, and `test` for non-changelog related tasks.

### Scope

The scope could be anything specifying place of the commit change. For example `$location`,
`$browser`, `$compile`, `$rootScope`, `ngHref`, `ngClick`, `ngView`, etc...

### Subject

The subject contains succinct description of the change:

* use the imperative, present tense: "change" not "changed" nor "changes"
* don't capitalize first letter
* no dot (.) at the end

### Body

Just as in the **subject**, use the imperative, present tense: "change" not "changed" nor "changes".
The body should include the motivation for the change and contrast this with previous behavior.

### Footer

The footer should contain any information about **Breaking Changes** and is also the place to
reference GitHub issues that this commit **Closes**.

**Breaking Changes** should start with the word `BREAKING CHANGE:` with a space or two newlines. The rest of the commit message is then used for this.

A detailed explanation can be found in this [document][commit-message-format].

Based on https://github.com/angular/angular.js/blob/master/CONTRIBUTING.md#commit

[commit-message-format]: https://docs.google.com/document/d/1QrDFcIiPjSLDn3EL15IJygNPiHORgU1_OOAqWjiDU5Y/edit#
