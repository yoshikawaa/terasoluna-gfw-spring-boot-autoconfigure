/**
 * Copyright (c) 2019 Atsushi Yoshikawa (https://yoshikawaa.github.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.common.time;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.terasoluna.gfw.common.time.ClockFactory;
import org.terasoluna.gfw.common.time.DefaultClockFactory;

/**
 * Spring Boot Auto Configuration for {@literal terasoluna-gfw-common} JSR-310 Date and Time API.
 *
 * @author Atsushi Yoshikawa
 * @since 1.1.0
 */
@AutoConfiguration
@ConditionalOnClass(ClockFactory.class)
public class TerasolunaGfwDateTimeAutoConfiguration {

    /**
     * Build {@link DefaultClockFactory}.
     *
     * @return Configured {@link DefaultClockFactory}
     */
    @Bean
    @ConditionalOnMissingBean(ClockFactory.class)
    public ClockFactory clockFactory() {
        return new DefaultClockFactory();
    }
}
