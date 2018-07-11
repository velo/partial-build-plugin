# Partial Build Plugin

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.lesfurets/partial-build-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.lesfurets/partial-build-plugin)
[![Build Status](https://travis-ci.org/lesfurets/partial-build-plugin.svg?branch=develop)](https://travis-ci.org/lesfurets/partial-build-plugin)
[![Build Status](https://ci.appveyor.com/api/projects/status/github/lesfurets/partial-build-plugin?branch=master&svg=true)](https://ci.appveyor.com/project/ozangunalp/partial-build-plugin)

A maven plugin for partially building multi-module projects based on changes in the Git repository.

Partial Build Plugin allows to build (or test) only the sub-set of modules impacted by the changes between the base (current) branch and a reference branch. 
Additionally it writes the list of impacted projects into files and/or maven properties to be exploited later in the build workflow.

Partial Build Plugin can be integrated into different kinds of development workflows, whether feature branching, promiscuous branching or trunk-based development. 

_**Disclosure** : This plugin is forked and based on the project [gitflow-incremental-builder](https://github.com/vackosar/gitflow-incremental-builder) by Vaclav Kosar._

## Usage

Partial Build Plugin leverages [Maven build extensions](https://maven.apache.org/examples/maven-3-lifecycle-extensions.html) to modify the projects to be build. 
So be sure to add `<extensions>true</extensions>` in the plugin definition to enable the partial build.
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.lesfurets</groupId>
      <artifactId>partial-build-plugin</artifactId>
      <version>VERSION</version>
      <extensions>true</extensions>
       <configuration>
        <referenceBranch>refs/remotes/origin/master</referenceBranch>
          ...
       </configuration>
    </plugin>
  </plugins>
</build>
```

If you are only interested to include the information on changed projects into your build lifecycle, you can use the goal called `writeChanged` without the build extension. 
This will write the list of changed projects into the output file.

> Default phase : Validate

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.lesfurets</groupId>
      <artifactId>partial-build-plugin</artifactId>
      <version>VERSION</version>
       <executions>
        <execution>
          <id>changed</id>
          <goals>
            <goal>writeChanged</goal>
          </goals>
          <configuration>
           <outputFile>${project.build.directory}/changed.projects</outputFile>
           ...
          </configuration>
        </execution>
       </executions>
    </plugin>
  </plugins>
</build>
```

## Configuration

### In the configuration of the plugin

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.lesfurets</groupId>
      <artifactId>partial-build-plugin</artifactId>
      <version>VERSION</version>
      <extensions>true</extensions>
       <configuration>
          <referenceBranch>refs/remote/heads/master</referenceBranch>
          <buildAll>true</buildAll>
          <skipTestsForNotImpactedModules>true</skipTestsForNotImpactedModules>
          <ignoreChanged>
            com.lesfurets:some-project,
            com.lesfurets:other-project
          </ignoreChanged>
       </configuration>
    </plugin>
  </plugins>
</build>
```

### Through Maven properties

```xml
<properties>
	<partial.referenceBranch>HEAD~2</partial.referenceBranch>
	<partial.baseBranch>HEAD</partial.baseBranch>
	<partial.uncommited>true</partial.uncommited>
	<partial.untracked>false</partial.untracked>
	<partial.buildAll>false</partial.buildAll>
	<partial.outputFile>changed.projects</partial.outputFile>
	<partial.writeChanged>false</partial.writeChanged>
</properties>
```

### Through User properties

`mvn clean install -Dpartial.uncommited=true -Dpartial.referenceBranch=HEAD`

### Through System properties

```bash
export partial.referenceBranch=origin/master
export partial.outputFile=changed.modules
mvn clean install 
```

### Configuration order

User properties override system properties overrides plugin configuration, overrides maven properties.

### Configuration parameters

| Parameter                      | Required | Default                               | Description                                                                                                                                                                                                                                              |
|--------------------------------|----------|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| enabled                        | No       | TRUE                                  | Whether the partial plugin is enabled completely or not                                                                                                                                                                                                  |
| repositorySshKey               | No       | empty                                 | Ssh key used for fetching branches if configured                                                                                                                                                                                                         |
| referenceBranch                | No       | refs/remotes/origin/develop           | 'To' end of branch comparison. Branch name or refspec                                                                                                                                                                                                    |
| baseBranch                     | No       | HEAD                                  | 'From' end of branch comparison. Checked out if different from HEAD                                                                                                                                                                                      |
| uncommited                     | No       | TRUE                                  | Whether to include uncommited changes in branch difference                                                                                                                                                                                               |
| untracked                      | No       | FALSE                                 | Whether to include untracked file changes in branch difference                                                                                                                                                                                           |
| skipTestsForNotImpactedModules | No       | FALSE                                 | Used with buildAll to true, skips tests for modules not impacted modules                                                                                                                                                                                 |
| buildAll                       | No       | FALSE                                 | Whether to build all modules or just the changed                                                                                                                                                                                                         |
| compareToMergeBase             | No       | TRUE                                  | Compare base branch to its merge base with reference branch                                                                                                                                                                                              |
| fetchBaseBranch                | No       | FALSE                                 | Fetch base branch before execution                                                                                                                                                                                                                       |
| fetchReferenceBranch           | No       | FALSE                                 | Fetch reference branch before execution                                                                                                                                                                                                                  |
| outputFile                     | No       | ${project.basedir}/changed.properties | Path of the file to write the changed projects output                                                                                                                                                                                                    |
| writeChanged                   | No       | TRUE                                  | Whether to write or not the changed projects output                                                                                                                                                                                                      |
| ignoreChanged                  | No       | empty                                 | Comma separated pattern of project Id's to ignore from changed project calculation. Ex. com.acme:* ignores changes from all projects with group Id com.acme. These projects are included in the build if they are considered in the default maven build. |
| buildAnyways                   | No       | empty                                 | Comma separated pattern of project Id's to add to build, whether these projects are changed or not. Difference from `ignoreChanged` is that these projects are used to calculate impacted project calculation.                                           |
| buildSnapshotDependencies      | No       | FALSE                                 | Builds all dependencies that are in SNAPSHOT versions. This is necessary if you are using partial build on a reactor where multiple SNAPSHOT projects depend on each other.                                                                              |
| ignoreAllReactorProjects       | No       | FALSE                                 | Ignore reactor projects (pom packaging with modules). Reactor projects are included in the build whether they are changed or not.                                                                                                                        |
| impacted                       | No       | TRUE                                  | Disables whether the partial build includes also projects impacted by changed projects.                                                                                                                                                                  |
| useNativeGit                   | No       | FALSE                                 | Use Native Git commands instead of JGit for detecting changed files. It should also cut down the build bootstrap by a couple of seconds                                                                                                                  |

## Getting Started

Let's illustrate the working principle of the plugin with a simple use case.
Here we have a simple multi-module project, versioned in Git : 

* reactor
    * child1
    * child2
        * grandchild1
        * grandchild2
    * child3
    * child4

If we build this project on reactor root we would see the following.

```bash
mvn validate -Dpartial.enabled=false
[INFO] Scanning for projects...
[INFO] Partial build disabled...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] parent
[INFO] child1
[INFO] child2
[INFO] grandchild1
[INFO] grandchild2
[INFO] child3
[INFO] child4
[INFO]                                                                         
[INFO] ---------------------
```

So far so good. 
Maven reactor aggregated all projects and constructed the standard build order. 
Let's make some changes in modules child3 and child4 and commit those. 

```bash
    touch child3/file
    touch child4/file
    git commit --all -m 'modify child3 and child4'
```

Now we activate the partial build and tell it to take into account only changes in the last commit. 

```bash
mvn validate -Dpartial.enabled=true -Dpartial.referenceBranch=HEAD~1 -Dpartial.baseBranch=HEAD
[INFO] Scanning for projects...
[INFO] Starting Partial build...
[INFO] Git root is: /Users/ogunalp/dev/partial-test/.git
[INFO] Head of branch HEAD is commit of id: commit 94bcac65da63a8578fbec2b241edc7f122219c5d 1470907753 -----p
[INFO] Head of branch HEAD~1 is commit of id: commit eebfc84bed00343ab2c4dd203b1c26e7771d8f6b 1470907544 -----p
[INFO] Using merge base of id: commit eebfc84bed00343ab2c4dd203b1c26e7771d8f6b 1470907544 -tr-sp
[INFO] ------------------------------------------------------------------------
[INFO] Changed Projects:
[INFO] 
[INFO] child4
[INFO] child3
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] child3
[INFO] child4
[INFO]              
```

Here the plugin only included changed projects in the build session and omitted the others. 
It also listed changed projets in the file `changed.projets` : 

```
    com.test:child3:1.0-SNAPSHOT
    com.test:child4:1.0-SNAPSHOT
```

This case was overly simplistic.
The plugin does more than that. 
It follows dependencies between projects to calculate projects to build. 
It can be used in complex build configurations and integrated in your build and release lifecycle.

Try it out, tell us what you think.

## Known Issues

* `--resume-from` builds are not supported yet.
* Changed projects console dump is not ordered.
* JGit currently does not support git worktree's (see https://git.eclipse.org/r/#/q/topic:worktree), `useNativeGit` option can be used to work in worktrees. 


## Requirements

- Maven version 3+.
