# Partial Build Plugin

[![Build Status](https://travis-ci.org/lesfurets/partial-build-plugin.svg?branch=develop)](https://travis-ci.org/lesfurets/partial-build-plugin)

A maven plugin for partially building multi-module projects based on changes in the Git repository.

Partial Build Plugin allows to build (or test) only the sub-set of modules impacted by the changes between the base (current) branch and a reference branch. 
Additionally it writes the list of impacted projects into files and/or maven properties to be exploited later in the build workflow.

Partial Build Plugin can be integrated into different kinds of development workflows, whether feature branching, promiscuous branching or trunk-based development. 

_**Disclosure** : This plugin is forked and based on the project [gitflow-incremental-builder](https://github.com/vackosar/gitflow-incremental-builder) by Vaclav Kosar._

## Usage

Partial Build Plugin leverages [Maven build extensions](https://maven.apache.org/examples/maven-3-lifecycle-extensions.html) to modify the projects to be build. 
So be sure to add `<extension>true</extension>` in the plugin definition to enable the partial build.
```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.lesfurets</groupId>
      <artifactId>partial-build-plugin</artifactId>
      <version>version</version>
      <extension>true</extension>
       <configuration>
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
      <version>version</version>
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
      <version>version</version>
      <extension>true</extension>
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
| referenceBranch                | No       | refs/remotes/origin/develop           | Reference branch                                                                                                                                                                                                                                         |
| baseBranch                     | No       | HEAD                                  | Base branch                                                                                                                                                                                                                                              |
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


## Use Cases



## Requirements

- Maven version 3+.

## To-do