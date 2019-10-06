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
package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.common;

import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.terasoluna.gfw.common.exception.ExceptionCodeResolver;
import org.terasoluna.gfw.common.exception.ExceptionLogger;
import org.terasoluna.gfw.common.exception.ResultMessagesLoggingInterceptor;

import io.github.yoshikawaa.gfw.config.common.ExceptionCodeResolverConfigurer;

/**
 * Spring Boot Auto Configuration for {@literal terasoluna-gfw-common}.
 *
 * @author Atsushi Yoshikawa
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ ExceptionCodeResolver.class, ExceptionLogger.class, ResultMessagesLoggingInterceptor.class })
public class TerasolunaGfwCommonAutoConfiguration {

    /**
     * Default Configurer for {@link ExceptionCodeResolver}.
     *
     * @return {@link ExceptionCodeResolverConfigurer}
     */
    @Bean
    @ConditionalOnMissingBean(ExceptionCodeResolverConfigurer.class)
    public ExceptionCodeResolverConfigurer exceptionCodeResolverConfigurer() {

        return builder -> {
            builder //
                    .mapping("ResourceNotFoundException", "e.xx.fw.5001") //
                    .mapping("InvalidTransactionTokenException", "e.xx.fw.7001") //
                    .mapping("BusinessException", "e.xx.fw.8001") //
                    .mapping(".DataAccessException", "e.xx.fw.9002") //
                    .defaultExceptionCode("e.xx.fw.9001");
        };
    }

    /**
     * Build {@link ExceptionCodeResolver}.
     *
     * @param configurer {@link ExceptionCodeResolverConfigurer} bean
     * @return Configured {@link ExceptionCodeResolver}
     */
    @Bean
    public ExceptionCodeResolver exceptionCodeResolver(ExceptionCodeResolverConfigurer configurer) {
        return configurer.get();
    }

    /**
     * Build {@link ExceptionLogger}.
     *
     * @param exceptionCodeResolver {@link ExceptionCodeResolver} bean
     * @return Configured {@link ExceptionLogger}
     */
    @Bean
    @ConditionalOnMissingBean(ExceptionLogger.class)
    public ExceptionLogger exceptionLogger(ExceptionCodeResolver exceptionCodeResolver) {

        ExceptionLogger exceptionLogger = new ExceptionLogger();
        exceptionLogger.setExceptionCodeResolver(exceptionCodeResolver);
        return exceptionLogger;
    }

    /**
     * Build {@link ResultMessagesLoggingInterceptor}.
     *
     * @param exceptionLogger {@link ExceptionLogger} bean
     * @return Configured {@link ResultMessagesLoggingInterceptor}
     */
    @Bean
    public ResultMessagesLoggingInterceptor resultMessagesLoggingInterceptor(ExceptionLogger exceptionLogger) {

        ResultMessagesLoggingInterceptor interceptor = new ResultMessagesLoggingInterceptor();
        interceptor.setExceptionLogger(exceptionLogger);
        return interceptor;
    }

    /**
     * Setup Spring AOP for {@link ResultMessagesLoggingInterceptor}.
     *
     * @param resultMessagesLoggingInterceptor {@link ResultMessagesLoggingInterceptor}
     *                                         bean
     * @return Configured {@link AspectJExpressionPointcutAdvisor}
     */
    @Bean
    public AspectJExpressionPointcutAdvisor resultMessagesLoggingAdvisor(
            ResultMessagesLoggingInterceptor resultMessagesLoggingInterceptor) {

        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setAdvice(resultMessagesLoggingInterceptor);
        advisor.setExpression("@within(org.springframework.stereotype.Service)");
        return advisor;
    }
}
