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
package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.common.date.jodatime;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.terasoluna.gfw.common.date.jodatime.DefaultJodaTimeDateFactory;
import org.terasoluna.gfw.common.date.jodatime.JodaTimeDateFactory;

/**
 * Spring Boot Auto Configuration for {@literal terasoluna-gfw-jodatime}.
 *
 * @author Atsushi Yoshikawa
 */
@AutoConfiguration
@ConditionalOnClass(JodaTimeDateFactory.class)
public class TerasolunaGfwJodaTimeAutoConfiguration {

    /**
     * Build {@link DefaultJodaTimeDateFactory}.
     *
     * @return Configured {@link DefaultJodaTimeDateFactory}
     */
    @Bean
    @ConditionalOnMissingBean(JodaTimeDateFactory.class)
    public JodaTimeDateFactory dateFactory() {
        return new DefaultJodaTimeDateFactory();
    }
}
