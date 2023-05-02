package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.common.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.terasoluna.gfw.common.time.ClockFactory;

class TerasolunaGfwDateTimeAutoConfigurationTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ImportAutoConfiguration(TerasolunaGfwDateTimeAutoConfiguration.class)
    class ClockFactoryTest {

        @Test
        void testClockFactory(@Autowired ClockFactory clockFactory) {
            assertThat(LocalDateTime.now(clockFactory.fixed())).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }
}
