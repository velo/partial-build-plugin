package com.lesfurets.maven.partial.core;

public enum Property {

    enabled("true"),
    repositorySshKey(""),
    referenceBranch("refs/remotes/origin/develop"),
    baseBranch("HEAD"),
    uncommited("true"),
    untracked("false"),
    skipTestsForNotImpactedModules("false"),
    buildAll("false"),
    compareToMergeBase("true"),
    fetchBaseBranch("false"),
    fetchReferenceBranch("false"),
    outputFile("changed.projects"),
    writeChanged("true"),
    ignoreChanged(""),
    buildSnapshotDependencies("false"),
    impacted("true"),
    ignoreAllReactorProjects("false"),
    useNativeGit("false");

    public static final String PREFIX = "partial.";

    private final String defaultValue;

    private String value;

    Property(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String fullName() {
        return PREFIX + this.name();
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public String getValue() {
        return value == null ? defaultValue : value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private String exemplify() {
        return "<" + fullName() + ">" + (defaultValue == null ? "" : defaultValue) + "</" + fullName() + ">";
    }

    public static String exemplifyAll() {
        StringBuilder builder = new StringBuilder();
        builder.append("<properties>\n");
        for (Property value : Property.values()) {
            builder.append("\t").append(value.exemplify()).append("\n");
        }
        builder.append("</properties>\n");
        return builder.toString();
    }

}
