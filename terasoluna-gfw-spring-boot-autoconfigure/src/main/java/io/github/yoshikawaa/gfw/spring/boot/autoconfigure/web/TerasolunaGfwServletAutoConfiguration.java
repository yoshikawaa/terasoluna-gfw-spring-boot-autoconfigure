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

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.terasoluna.gfw.security.web.logging.UserIdMDCPutFilter;
import org.terasoluna.gfw.web.logging.mdc.MDCClearFilter;
import org.terasoluna.gfw.web.logging.mdc.XTrackMDCPutFilter;

/**
 * Spring Boot Auto Configuration for Web of {@literal terasoluna-gfw-web},
 * {@literal terasoluna-gfw-security-web}.
 *
 * @author Atsushi Yoshikawa
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ MDCClearFilter.class, XTrackMDCPutFilter.class })
@AutoConfigureBefore(SecurityAutoConfiguration.class)
public class TerasolunaGfwServletAutoConfiguration {

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
     * Web Security Configurer for {@link UserIdMDCPutFilter}.
     *
     * @author Atsushi Yoshikawa
     */
    @Configuration
    @ConditionalOnClass({ UserIdMDCPutFilter.class, WebSecurityConfigurer.class })
    public class TerasolunaGfwWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.addFilterAfter(new UserIdMDCPutFilter(), AnonymousAuthenticationFilter.class);
        }
    }
}
