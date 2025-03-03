package io.github.qudtlib.maven.shacl;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DurationStringTest {
    @ParameterizedTest
    @MethodSource
    public void testDurationString(long duration, String expected) {
        Assertions.assertEquals(expected, AbstractShacMojo.makeDurationString(duration));
    }

    public static Stream<Arguments> testDurationString() {
        return Stream.of(
                Arguments.of(345, "345ms"),
                Arguments.of(3450, "3.4s"),
                Arguments.of(34503, "34.5s"),
                Arguments.of(345034, "5m 45s"),
                Arguments.of(3450345, "57m 30s"),
                Arguments.of(34503453, "9h 35m 3s"),
                Arguments.of(345034534, "95h 50m 34s"),
                Arguments.of(3450345345L, "958h 25m 45s"),
                Arguments.of(34503453453L, "9584h 17m 33s"));
    }
}
