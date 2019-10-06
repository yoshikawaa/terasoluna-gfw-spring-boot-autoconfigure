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
package io.github.yoshikawaa.gfw.config.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.terasoluna.gfw.web.exception.SystemExceptionResolver;

/**
 * Configurer for {@link SystemExceptionResolver}.
 *
 * @author Atsushi Yoshikawa
 */
@FunctionalInterface
public interface SystemExceptionResolverConfigurer {

    /**
     * Configure {@link SystemExceptionResolver}.
     *
     * @param builder builder for {@link SystemExceptionResolver}
     */
    void configure(SystemExceptionResolverBuilder builder);

    /**
     * Apply configuration.
     *
     * @return {@link SystemExceptionResolver}
     */
    default SystemExceptionResolver get() {
        SystemExceptionResolverBuilder builder = new SystemExceptionResolverBuilder();
        configure(builder);
        return builder.build();
    }

    /**
     * Builder for {@link SystemExceptionResolver}.
     *
     * @author Atsushi Yoshikawa
     */
    static class SystemExceptionResolverBuilder {

        private final Properties exceptionMappings = new Properties();
        private final Properties statusCodes = new Properties();
        private final List<Class<? extends Throwable>> excludeExceptions = new ArrayList<Class<? extends Throwable>>();
        private String defaultErrorView;
        private Integer defaultStatusCode;

        /**
         * Add exception mapping.
         *
         * @param exception pattern of exception class name
         * @param viewname  error view
         * @return this
         */
        public SystemExceptionResolverBuilder mapping(String exception, String viewname) {
            this.exceptionMappings.put(exception, viewname);
            return this;
        }

        /**
         * Add status code mapping.
         *
         * @param viewname   error view
         * @param statusCode response status code
         * @return this
         */
        public SystemExceptionResolverBuilder statusCode(String viewname, int statusCode) {
            this.statusCodes.put(viewname, Integer.toString(statusCode));
            return this;
        }

        /**
         * Exclude exception.
         *
         * @param exceptionClass class of exclude exception
         * @return this
         */
        public SystemExceptionResolverBuilder exclude(Class<? extends Throwable> exceptionClass) {
            this.excludeExceptions.add(exceptionClass);
            return this;
        }

        /**
         * Default error view.
         *
         * @param defaultErrorView error view
         * @return this
         */
        public SystemExceptionResolverBuilder defaultErrorView(String defaultErrorView) {
            this.defaultErrorView = defaultErrorView;
            return this;
        }

        /**
         * Default status code.
         *
         * @param defaultStatusCode response status code
         * @return this
         */
        public SystemExceptionResolverBuilder defaultStatusCode(int defaultStatusCode) {
            this.defaultStatusCode = defaultStatusCode;
            return this;
        }

        /**
         * Build {@link SystemExceptionResolver}.
         *
         * @return {@link SystemExceptionResolver}
         */
        public SystemExceptionResolver build() {
            SystemExceptionResolver systemExceptionResolver = new SystemExceptionResolver();
            if (!exceptionMappings.isEmpty())
                systemExceptionResolver.setExceptionMappings(exceptionMappings);
            if (!statusCodes.isEmpty())
                systemExceptionResolver.setStatusCodes(statusCodes);
            if (!excludeExceptions.isEmpty())
                systemExceptionResolver.setExcludedExceptions(excludeExceptions.toArray(new Class[excludeExceptions.size()]));
            if (defaultErrorView != null)
                systemExceptionResolver.setDefaultErrorView(defaultErrorView);
            if (defaultStatusCode != null)
                systemExceptionResolver.setDefaultStatusCode(defaultStatusCode);
            return systemExceptionResolver;
        }
    }
}
