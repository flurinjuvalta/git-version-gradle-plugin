package ch.avelon.git

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author Flurin Juvalta <flurin.juvalta@avelon.ch>
 */
class GitVersionPlugin implements Plugin<Project> {

    Git git = new Git()

    void apply(Project target) {
        if (!git.isInitialized()) {
            println('Not in a Git reposiitory. Use "git init" to initialize a git repository.')
        } else if (!git.hasTags()) {
            println('No git tags found. Use "git tag -a <tagname>" to create your first tag.')
        } else {
            try {
                target.version = getVersionFromGit(target.hasProperty('doCreateMissingTag') && target.doCreateMissingTag)
                target.ext {
                    repository = getRepositoryFromGit()
                }
            } catch (Exception e) {
                println 'An Error ocurred while getting the version from git.' + e.toString()
            }
        }

        target.task('version').doLast() {
            println("version = ${project.version}")
        }

        target.task('repository').doLast() {
            println("repository = ${project.repository}")
        }
    }

    /**
     * Gets the version name from the latest Git tag. If increment is larger than 0,
     * that value is added to the minor version number, and the revision is set to 0.
     *
     * @param increment the number to add to the minor version  number
     */
    String getVersionName(int increment) {
        def version = git.currentTag
        // check if tag is a correct version tag. otherwise get the latest version tag (tag with largest version number)
        def m = version =~ /\d+\.\d+\.\d+.*/
        if (!m.matches()) {
            version = git.allTags.findAll(/\d+\.\d+\.\d+/).sort(false) { a, b ->
                [a, b]*.tokenize('.')*.collect { it as int }.with { u, v ->
                    [u, v].transpose().findResult { x, y -> x <=> y ?: null } ?: u.size() <=> v.size()
                }
            }[-1]
        }

        if (increment > 0) {
            def versions = version.split('\\.')
            return versions[0] + '.' + (versions[1].toInteger() + increment).toString() + '.0'
        } else {
            return version
        }
    }

    String getVersionFromGit(boolean doCreateMissingTag) {
        if (git.onMasterBranch) {
            def version = getVersionName(0)
            // check if version number is a version tag
            // create and push a new tag if not
            if (doCreateMissingTag) {
                def m = version =~ /(\d+)\.(\d+)\.(\d+)(.*)/


                if (m.matches() && !m[0][4].isEmpty()) {
                    version = m[0][1].toInteger() + "." + m[0][2].toInteger() + "." + (m[0][3].toInteger() + 1)
                    println("'create tag $version")
                    git.createAndPushTag(version)
                }
            }

            return version
        } else if (git.onReleaseBranch) {
            return getVersionName(1) + "." + git.SHA
        } else {
            // develop or feature branch -> increment master version by one
            // check if there is a current release branch and increment master version by 2 if so
            def increment = git.releaseExists() ? 2 : 1
            // If the upload is done to a snapshots repository, the artifact must end in
            // SNAPSHOT. Otherwise the repository will refuse the upload
            return getVersionName(increment) + "." + git.SHA + "-SNAPSHOT"
        }
    }

    /**
     * Gets the nexus repository based on the git branch name
     * @return
     */
    String getRepositoryFromGit() {
        if (git.onMasterBranch) {
            return "releases"
        } else if (git.onReleaseBranch) {
            return "release-candidates"
        } else {
            return "snapshots"
        }
    }
}
