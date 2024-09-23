package io.github.qudtlib.maven.shacl;

import org.apache.maven.plugins.annotations.Parameter;

public class IncludeExcludePatterns {
    /**
     * Comma-separated list of ant-style patterns, such as <code>
     * target/txt/**\/*.txt,target/pdf/**\/*.pdf</code>
     */
    @Parameter private String include;

    /**
     * Comma-separated list of ant-style patterns, such as <code>
     * target/txt/**\/*.txt,target/pdf/**\/*.pdf</code>
     */
    @Parameter private String exclude;

    public String getInclude() {
        return include == null ? "" : include;
    }

    public String getExclude() {
        return exclude == null ? "" : exclude;
    }

    @Override
    public String toString() {
        return "IncludeExcludePatterns{"
                + "include='"
                + include
                + '\''
                + ", exclude='"
                + exclude
                + '\''
                + '}';
    }
}
