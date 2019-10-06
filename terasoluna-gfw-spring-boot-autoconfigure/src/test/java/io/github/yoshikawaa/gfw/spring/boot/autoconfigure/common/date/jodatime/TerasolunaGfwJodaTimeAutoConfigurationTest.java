package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.common.date.jodatime;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.terasoluna.gfw.common.date.jodatime.JodaTimeDateFactory;

class TerasolunaGfwJodaTimeAutoConfigurationTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ImportAutoConfiguration(TerasolunaGfwJodaTimeAutoConfiguration.class)
    class DateFactoryTest {

        @Test
        void testDateFactory(@Autowired JodaTimeDateFactory dateFactory) {
            assertThat(dateFactory.newDateTime()).isLessThanOrEqualTo(DateTime.now());
        }
    }
}
