package io.github.qudtlib.maven.shacl;

import org.apache.maven.plugins.annotations.Parameter;

public class DataAndShapes {
    @Parameter private IncludeExcludePatterns shapes;

    @Parameter private IncludeExcludePatterns data;

    @Parameter private String outputFile;

    @Parameter(defaultValue = "false")
    private boolean skip;

    @Parameter String message;

    @Parameter String failureMessage;

    @Parameter String successMessage;

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

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Check{" + "shapes='" + shapes + '\'' + ", data='" + data + '\'' + '}';
    }
}
