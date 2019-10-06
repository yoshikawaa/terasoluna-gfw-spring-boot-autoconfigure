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
package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.thymeleaf;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;

import io.github.yoshikawaa.gfw.web.thymeleaf.dialect.TerasolunaGfwDialect;

/**
 * Spring Boot Auto Configuration for {@literal thymeleaf-extras-terasoluna-gfw}.
 *
 * @author Atsushi Yoshikawa
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ TerasolunaGfwDialect.class, SpringTemplateEngine.class })
@AutoConfigureBefore(ThymeleafAutoConfiguration.class)
public class TerasolunaGfwThymeleafAutoConfiguration {

    /**
     * Build {@link TerasolunaGfwDialect}.
     *
     * @return Configured {@link TerasolunaGfwDialect}
     */
    @Bean
    @ConditionalOnMissingBean(TerasolunaGfwDialect.class)
    public TerasolunaGfwDialect terasolunaGfwDialect() {
        return new TerasolunaGfwDialect();
    }
}
