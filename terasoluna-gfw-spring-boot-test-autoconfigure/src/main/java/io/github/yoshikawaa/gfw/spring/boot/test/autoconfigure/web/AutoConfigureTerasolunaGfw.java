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
package io.github.yoshikawaa.gfw.spring.boot.test.autoconfigure.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
import org.terasoluna.gfw.web.codelist.CodeListInterceptor;

import io.github.yoshikawaa.gfw.spring.boot.autoconfigure.web.TerasolunaGfwWebMvcProperties;

/**
 * Annotation to auto configure Terasoluna Gfw.
 *
 * @author Atsushi Yoshikawa
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration
@PropertyMapping(TerasolunaGfwWebMvcProperties.PROPERTIES_PREFIX)
public @interface AutoConfigureTerasolunaGfw {

    /**
     * Enable {@link CodeListInterceptor}.
     *
     * @return enabled
     */
    @PropertyMapping(TerasolunaGfwWebMvcProperties.PROPERTIES_CODELIST_ENABLED)
    boolean codeListEnabled() default true;
}
