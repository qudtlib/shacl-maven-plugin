package io.github.qudtlib.maven.shacl;

public enum ShaclResultSeverity {
    Violation,
    Warning,
    Info;

    public boolean isEqualOrHigher(ShaclResultSeverity other) {
        switch (this) {
            case Violation:
                return true;
            case Warning:
                return other != Violation;
            case Info:
                return other != Violation && other != Warning;
        }
        return false;
    }
}
