package ru.pyatkinmv.pognaleey;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class PognaleeyAppTest {

    @Test
    void test() {
        assertThat(true).isTrue();
    }
}
