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
package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.terasoluna.gfw.security.web.logging.UserIdMDCPutFilter;
import org.terasoluna.gfw.web.logging.HttpSessionEventLoggingListener;
import org.terasoluna.gfw.web.logging.mdc.MDCClearFilter;
import org.terasoluna.gfw.web.logging.mdc.XTrackMDCPutFilter;

/**
 * Spring Boot Auto Configuration for Web of {@literal terasoluna-gfw-web},
 * {@literal terasoluna-gfw-security-web}.
 *
 * @author Atsushi Yoshikawa
 */
@AutoConfiguration(before = SecurityAutoConfiguration.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ HttpSessionEventLoggingListener.class, MDCClearFilter.class, XTrackMDCPutFilter.class })
public class TerasolunaGfwServletAutoConfiguration {

    /**
     * Build {@link HttpSessionEventLoggingListener}.
     *
     * @return Configured {@link HttpSessionEventLoggingListener}
     * @since 1.0.1
     */
    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventLoggingListener> configureHttpSessionEventLoggingListener() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventLoggingListener());
    }

    /**
     * Build {@link MDCClearFilter}.
     *
     * @return Configured {@link MDCClearFilter}
     */
    @Bean
    public FilterRegistrationBean<MDCClearFilter> mdcClearFilter() {

        FilterRegistrationBean<MDCClearFilter> bean = new FilterRegistrationBean<>(new MDCClearFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /**
     * Build {@link XTrackMDCPutFilter}.
     *
     * @return Configured {@link XTrackMDCPutFilter}
     */
    @Bean
    public FilterRegistrationBean<XTrackMDCPutFilter> xTrackMDCPutFilter() {

        FilterRegistrationBean<XTrackMDCPutFilter> bean = new FilterRegistrationBean<>(new XTrackMDCPutFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return bean;
    }

    /**
     * Apply {@link UserIdMDCPutFilter} after {@link AnonymousAuthenticationFilter}.
     *
     * @param http {@link HttpSecurity}
     * @return Configured {@link SecurityFilterChain}
     * @throws Exception failed to build {@link SecurityFilterChain}
     */
    @Bean
    @ConditionalOnClass({ UserIdMDCPutFilter.class, SecurityFilterChain.class })
    public SecurityFilterChain configureUserIdMDCPutFilter(HttpSecurity http) throws Exception {

        http.addFilterAfter(new UserIdMDCPutFilter(), AnonymousAuthenticationFilter.class);
        return http.build();
    }
}
