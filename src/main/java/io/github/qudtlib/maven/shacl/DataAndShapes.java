package io.github.qudtlib.maven.shacl;

import org.apache.maven.plugins.annotations.Parameter;

public class DataAndShapes {
    @Parameter private IncludeExcludePatterns shapes;

    @Parameter private IncludeExcludePatterns data;

    @Parameter private String outputFile;

    @Parameter(defaultValue = "false")
    private boolean skip;

    public IncludeExcludePatterns getShapes() {
        return shapes;
    }

    public IncludeExcludePatterns getData() {
        return data;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public boolean isSkip() {
        return skip;
    }

    @Override
    public String toString() {
        return "Check{" + "shapes='" + shapes + '\'' + ", data='" + data + '\'' + '}';
    }
}
