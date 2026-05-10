package com.roadmap.urlshorteningservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorTest {

    private ShortCodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ShortCodeGenerator();
    }

    @Test
    void generate_returnsCodeOfLengthSix() {
        String code = generator.generate();

        assertThat(code).hasSize(6);
    }

    @Test
    void generate_containsOnlyBase62Characters() {
        String code = generator.generate();

        assertThat(code).matches("[a-zA-Z0-9]+");
    }

    @Test
    void generate_producesUniqueValues() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            codes.add(generator.generate());
        }

        // With 56 billion possible codes, 1000 calls should yield 1000 unique values
        assertThat(codes).hasSize(1000);
    }
}