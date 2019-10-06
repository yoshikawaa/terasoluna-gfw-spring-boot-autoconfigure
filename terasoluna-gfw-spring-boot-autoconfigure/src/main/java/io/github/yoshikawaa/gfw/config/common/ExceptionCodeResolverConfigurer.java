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
package io.github.yoshikawaa.gfw.config.common;

import java.util.LinkedHashMap;

import org.terasoluna.gfw.common.exception.ExceptionCodeResolver;
import org.terasoluna.gfw.common.exception.SimpleMappingExceptionCodeResolver;

/**
 * Configurer for {@link ExceptionCodeResolver}.
 *
 * @author Atsushi Yoshikawa
 */
@FunctionalInterface
public interface ExceptionCodeResolverConfigurer {

    /**
     * Configure {@link ExceptionCodeResolver}.
     *
     * @param builder builder for {@link ExceptionCodeResolver}
     */
    void configure(ExceptionCodeResolverBuilder builder);

    /**
     * Apply configuration.
     *
     * @return {@link ExceptionCodeResolver}
     */
    default ExceptionCodeResolver get() {
        ExceptionCodeResolverBuilder builder = new ExceptionCodeResolverBuilder();
        configure(builder);
        return builder.build();
    }

    /**
     * Builder for {@link SimpleMappingExceptionCodeResolver}.
     *
     * @author Atsushi Yoshikawa
     */
    static class ExceptionCodeResolverBuilder {

        private final LinkedHashMap<String, String> exceptionMappings = new LinkedHashMap<>();
        private String defaultExceptionCode;

        /**
         * Add exception mapping.
         *
         * @param exception pattern of exception class name
         * @param code      exception message code
         * @return this
         */
        public ExceptionCodeResolverBuilder mapping(String exception, String code) {
            this.exceptionMappings.put(exception, code);
            return this;
        }

        /**
         * Default exception code.
         *
         * @param defaultExceptionCode exception message code
         * @return this
         */
        public ExceptionCodeResolverBuilder defaultExceptionCode(String defaultExceptionCode) {
            this.defaultExceptionCode = defaultExceptionCode;
            return this;
        }

        /**
         * Build {@link SimpleMappingExceptionCodeResolver}.
         *
         * @return {@link SimpleMappingExceptionCodeResolver}
         */
        public ExceptionCodeResolver build() {
            SimpleMappingExceptionCodeResolver exceptionCodeResolver = new SimpleMappingExceptionCodeResolver();
            if (!exceptionMappings.isEmpty())
                exceptionCodeResolver.setExceptionMappings(exceptionMappings);
            if (defaultExceptionCode != null)
                exceptionCodeResolver.setDefaultExceptionCode(defaultExceptionCode);
            return exceptionCodeResolver;
        }
    }
}
