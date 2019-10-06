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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.servlet.support.csrf.CsrfRequestDataValueProcessor;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.springframework.web.util.NestedServletException;
import org.terasoluna.gfw.common.exception.ExceptionCodeResolver;
import org.terasoluna.gfw.web.codelist.CodeListInterceptor;
import org.terasoluna.gfw.web.exception.SystemExceptionResolver;
import org.terasoluna.gfw.web.logging.TraceLoggingInterceptor;
import org.terasoluna.gfw.web.mvc.support.CompositeRequestDataValueProcessor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInterceptor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenRequestDataValueProcessor;

import io.github.yoshikawaa.gfw.config.web.SystemExceptionResolverConfigurer;
import io.github.yoshikawaa.gfw.spring.boot.autoconfigure.common.TerasolunaGfwCommonAutoConfiguration;

/**
 * Spring Boot Auto Configuration for Web MVC of {@literal terasoluna-gfw-web}.
 *
 * @author Atsushi Yoshikawa
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ TraceLoggingInterceptor.class, TransactionTokenInterceptor.class, CodeListInterceptor.class,
        SystemExceptionResolver.class, ExceptionCodeResolver.class, TransactionTokenRequestDataValueProcessor.class })
@AutoConfigureAfter({ SecurityAutoConfiguration.class, TerasolunaGfwCommonAutoConfiguration.class })
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@EnableConfigurationProperties(TerasolunaGfwWebMvcProperties.class)
public class TerasolunaGfwWebMvcAutoConfiguration {

    /**
     * Build {@link TraceLoggingInterceptor}.
     *
     * @param gfwWebMvcProperties Properties for Terasoluna Gfw Web
     * @return Configured {@link TraceLoggingInterceptor}
     */
    @Bean
    public TraceLoggingInterceptor traceLoggingInterceptor(TerasolunaGfwWebMvcProperties gfwWebMvcProperties) {

        TraceLoggingInterceptor interceptor = new TraceLoggingInterceptor();
        Optional.ofNullable(gfwWebMvcProperties.getTraceLogging().getWarnHandlingNanos())
                .ifPresent(warnHandlingNanos -> interceptor.setWarnHandlingNanos(warnHandlingNanos));
        return interceptor;
    }

    /**
     * Build {@link TransactionTokenInterceptor}.
     *
     * @param gfwWebMvcProperties Properties for Terasoluna Gfw Web
     * @return Configured {@link TransactionTokenInterceptor}
     */
    @Bean
    public TransactionTokenInterceptor transactionTokenInterceptor(TerasolunaGfwWebMvcProperties gfwWebMvcProperties) {

        Integer sizePerTokenName = gfwWebMvcProperties.getTransactionToken().getSizePerTokenName();
        return sizePerTokenName == null ? new TransactionTokenInterceptor()
                : new TransactionTokenInterceptor(sizePerTokenName);
    }

    /**
     * Build {@link CodeListInterceptor}.
     *
     * @param gfwWebMvcProperties Properties for Terasoluna Gfw Web
     * @return Configured {@link CodeListInterceptor}
     */
    @Bean
    @ConditionalOnProperty(prefix = TerasolunaGfwWebMvcProperties.PROPERTIES_PREFIX, name = TerasolunaGfwWebMvcProperties.PROPERTIES_CODELIST_ENABLED, havingValue = "true", matchIfMissing = true)
    public CodeListInterceptor codeListInterceptor(TerasolunaGfwWebMvcProperties gfwWebMvcProperties) {

        String codeListIdPattern = gfwWebMvcProperties.getCodelist().getIdPattern();
        CodeListInterceptor interceptor = new CodeListInterceptor();
        interceptor.setCodeListIdPattern(Pattern.compile(codeListIdPattern));
        return interceptor;
    }

    /**
     * Default Configurer for {@link SystemExceptionResolver}.
     *
     * @return {@link SystemExceptionResolverConfigurer}
     */
    @Bean
    @ConditionalOnMissingBean(SystemExceptionResolverConfigurer.class)
    public SystemExceptionResolverConfigurer systemExceptionResolverConfigurer() {
        return builder -> builder //
                .mapping("ResourceNotFoundException", "error/resourceNotFoundError") //
                .mapping("BusinessException", "error/businessError") //
                .mapping("InvalidTransactionTokenException", "error/transactionTokenError") //
                .mapping(".DataAccessException", "error/dataAccessError") //
                .statusCode("error/resourceNotFoundError", 404) //
                .statusCode("error/businessError", 409) //
                .statusCode("error/transactionTokenError", 409) //
                .statusCode("error/dataAccessError", 500) //
                .exclude(NestedServletException.class) //
                .defaultErrorView("error") //
                .defaultStatusCode(500);
    }

    /**
     * Build {@link SystemExceptionResolver}.
     *
     * @param configurer                    {@link SystemExceptionResolverConfigurer}
     *                                      bean
     * @param exceptionCodeResolverProvider {@link ExceptionCodeResolver} bean
     * @return Configured {@link SystemExceptionResolver}
     */
    @Bean
    @Order(3)
    @ConditionalOnMissingBean(SystemExceptionResolver.class)
    public SystemExceptionResolver systemExceptionResolver(SystemExceptionResolverConfigurer configurer,
            ObjectProvider<ExceptionCodeResolver> exceptionCodeResolverProvider) {

        SystemExceptionResolver exceptionResolver = configurer.get();
        exceptionCodeResolverProvider.ifAvailable(
                exceptionCodeResolver -> exceptionResolver.setExceptionCodeResolver(exceptionCodeResolver));

        return exceptionResolver;
    }

    /**
     * Web MVC Configurer for Terasoluna Gfw Web.
     *
     * @author Atsushi Yoshikawa
     */
    @Configuration(proxyBeanMethods = false)
    public static class TerasolunaGfwWebMvcAutoConfigurer implements WebMvcConfigurer {

        private final List<HandlerInterceptor> interceptors = new ArrayList<>();
        private final ObjectProvider<SystemExceptionResolver> systemExceptionResolverProvider;

        /**
         * Construct Web MVC Configurer.
         *
         * @param traceLoggingInterceptorProvider     {@link TraceLoggingInterceptor}
         * @param transactionTokenInterceptorProvider {@link TransactionTokenInterceptor}
         * @param codeListInterceptorProvider         {@link CodeListInterceptor}
         * @param systemExceptionResolverProvider     {@link SystemExceptionResolver}
         */
        public TerasolunaGfwWebMvcAutoConfigurer(
                ObjectProvider<TraceLoggingInterceptor> traceLoggingInterceptorProvider,
                ObjectProvider<TransactionTokenInterceptor> transactionTokenInterceptorProvider,
                ObjectProvider<CodeListInterceptor> codeListInterceptorProvider,
                ObjectProvider<SystemExceptionResolver> systemExceptionResolverProvider) {

            traceLoggingInterceptorProvider.ifAvailable(interceptor -> interceptors.add(interceptor));
            transactionTokenInterceptorProvider.ifAvailable(interceptor -> interceptors.add(interceptor));
            codeListInterceptorProvider.ifAvailable(interceptor -> interceptors.add(interceptor));
            this.systemExceptionResolverProvider = systemExceptionResolverProvider;
        }

        /**
         * Register interceptors.
         *
         * @see WebMvcConfigurer#addInterceptors(InterceptorRegistry)
         */
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            interceptors.forEach(interceptor -> registry.addInterceptor(interceptor));
        }

        /**
         * Register exception resolver.
         *
         * @see WebMvcConfigurer#configureHandlerExceptionResolvers(List)
         */
        @Override
        public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
            systemExceptionResolverProvider.ifAvailable(exceptionResolver -> resolvers.add(exceptionResolver));
        }
    }

    /**
     * Re-Build {@link RequestDataValueProcessor} contains
     * {@link CsrfRequestDataValueProcessor}.
     *
     * @return {@link BeanDefinitionRegistryPostProcessor}
     */
    @Bean
    public BeanDefinitionRegistryPostProcessor requestDataValueProcessorPostProcesser() {

        final String beanName = "requestDataValueProcessor";
        final String csrfClassName = "org.springframework.security.web.servlet.support.csrf.CsrfRequestDataValueProcessor";
        return new BeanDefinitionRegistryPostProcessor() {

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                // nothing to do.
            }

            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

                if (registry.containsBeanDefinition(beanName)) {
                    registry.removeBeanDefinition(beanName);
                    registry.registerBeanDefinition(beanName,
                            new RootBeanDefinition(RequestDataValueProcessor.class, () -> {
                                Class<?> csrfClass = ClassUtils.resolveClassName(csrfClassName,
                                        ClassUtils.getDefaultClassLoader());
                                RequestDataValueProcessor csrf = (RequestDataValueProcessor) BeanUtils
                                        .instantiateClass(csrfClass);
                                return new CompositeRequestDataValueProcessor(csrf,
                                        new TransactionTokenRequestDataValueProcessor());
                            }));
                } else {
                    registry.registerBeanDefinition(beanName,
                            new RootBeanDefinition(TransactionTokenRequestDataValueProcessor.class));
                }
            }
        };
    }
}
