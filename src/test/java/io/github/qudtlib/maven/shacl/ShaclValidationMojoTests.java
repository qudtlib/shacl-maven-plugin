package io.github.qudtlib.maven.shacl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ShaclValidationMojoTests {

    @ParameterizedTest
    @MethodSource
    public void testPatternSplit(String input, String[] expectedOutput) {
        assertArrayEquals(expectedOutput, ShaclValidationMojo.splitPatterns(input));
    }

    public static Stream<Arguments> testPatternSplit() {
        return Stream.of(
                Arguments.of(
                        "target/text/*.ttl,src/main/resources/**/*.txt",
                        new String[] {"target/text/*.ttl", "src/main/resources/**/*.txt"}),
                Arguments.of(
                        "target/text/*.ttl, src/main/resources/**/*.txt",
                        new String[] {"target/text/*.ttl", "src/main/resources/**/*.txt"}),
                Arguments.of(
                        "target/text/*.ttl , src/main/resources/**/*.txt",
                        new String[] {"target/text/*.ttl", "src/main/resources/**/*.txt"}),
                Arguments.of(
                        " target/text/*.ttl , src/main/resources/**/*.txt",
                        new String[] {"target/text/*.ttl", "src/main/resources/**/*.txt"}),
                Arguments.of(
                        " target/text/*.ttl , src/main/resources/**/*.txt ",
                        new String[] {"target/text/*.ttl", "src/main/resources/**/*.txt"}),
                Arguments.of(
                        """
                                                        target/text/*.ttl ,
                                                        src/main/resources/**/*.txt""",
                        new String[] {"target/text/*.ttl", "src/main/resources/**/*.txt"}),
                Arguments.of(
                        """
                                                        target/text/*.ttl ,
                                                        src/main/resources/**/*.txt,""",
                        new String[] {"target/text/*.ttl", "src/main/resources/**/*.txt"}),
                Arguments.of(
                        """
                                                        target/text/*.ttl ,
                                                        src/main/resources/**/*.txt,

                                                        src/main/x.y""",
                        new String[] {
                            "target/text/*.ttl", "src/main/resources/**/*.txt", "src/main/x.y"
                        }),
                Arguments.of(
                        """
                                                        target/text/*.ttl
                                                        src/main/resources/**/*.txt,

                                                        src/main/x.y,""",
                        new String[] {
                            "target/text/*.ttl", "src/main/resources/**/*.txt", "src/main/x.y"
                        }));
    }
}
