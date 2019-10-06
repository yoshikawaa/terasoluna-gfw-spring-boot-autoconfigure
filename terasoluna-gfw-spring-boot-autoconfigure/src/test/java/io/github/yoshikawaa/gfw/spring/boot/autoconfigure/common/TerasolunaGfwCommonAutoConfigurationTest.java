package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.NestedServletException;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.exception.ExceptionCodeResolver;
import org.terasoluna.gfw.common.exception.ExceptionLogger;
import org.terasoluna.gfw.common.message.ResultMessages;

import io.github.yoshikawaa.gfw.config.common.ExceptionCodeResolverConfigurer;

class TerasolunaGfwCommonAutoConfigurationTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ImportAutoConfiguration(TerasolunaGfwCommonAutoConfiguration.class)
    class ExceptionResolverTest {

        @Test
        void testNestedServletException(@Autowired ExceptionCodeResolver exceptionCodeResolver) {
            String code = exceptionCodeResolver.resolveExceptionCode(new NestedServletException("test-message"));
            assertThat(code).isEqualTo("e.xx.fw.9001");
        }

        @Test
        void testBusinessException(@Autowired ExceptionCodeResolver exceptionCodeResolver) {
            String code = exceptionCodeResolver
                    .resolveExceptionCode(new BusinessException(ResultMessages.error().add("test-code")));
            assertThat(code).isEqualTo("e.xx.fw.8001");
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ImportAutoConfiguration(TerasolunaGfwCommonAutoConfiguration.class)
    @Import(TestExceptionCodeConfig.class)
    class ExceptionCodeResolverCustomTest {

        @Test
        void testNestedServletException(@Autowired ExceptionCodeResolver exceptionCodeResolver) {
            String code = exceptionCodeResolver.resolveExceptionCode(new NestedServletException("test-message"));
            assertThat(code).isEqualTo("e.xx.fw.9001");
        }

        @Test
        void testBusinessException(@Autowired ExceptionCodeResolver exceptionCodeResolver) {
            String code = exceptionCodeResolver
                    .resolveExceptionCode(new BusinessException(ResultMessages.error().add("test-code")));
            assertThat(code).isEqualTo("e.xx.fw.9001");
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ImportAutoConfiguration(TerasolunaGfwCommonAutoConfiguration.class)
    @ExtendWith(OutputCaptureExtension.class)
    class ExceptionLoggerTest {

        @Test
        void testExceptionLogger(@Autowired ExceptionLogger exceptionLogger, CapturedOutput output) {
            exceptionLogger.log(new BusinessException(ResultMessages.error().add("test-code")));
            assertThat(output).contains("ERROR", "ExceptionLogger",
                    "[e.xx.fw.8001] ResultMessages [type=error, list=[ResultMessage [code=test-code, args=[], text=null]]]");
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ImportAutoConfiguration(TerasolunaGfwCommonAutoConfiguration.class)
    @Import(TestServiceConfig.class)
    @ExtendWith(OutputCaptureExtension.class)
    class ResultMessagesLoggingInterceptorTest {

        @Test
        void test(@Autowired TestServiceConfig.TestService service, CapturedOutput output) {
            try {
                service.test();
            } catch (BusinessException e) {
                assertThat(output).contains("WARN", "ExceptionLogger",
                        "[e.xx.fw.8001] ResultMessages [type=error, list=[ResultMessage [code=test-code, args=[], text=null]]]");
            }
        }
    }

    @TestConfiguration
    @EnableAspectJAutoProxy
    static class TestExceptionCodeConfig {
        @Bean
        ExceptionCodeResolverConfigurer exceptionCodeResolverConfigurer() {
            return builder -> {
                builder //
                        .mapping("ResourceNotFoundException", "e.xx.fw.5001") //
                        .mapping("InvalidTransactionTokenException", "e.xx.fw.7001") //
                        // .mapping("BusinessException", "e.xx.fw.8001") //
                        .mapping(".DataAccessException", "e.xx.fw.9002") //
                        .defaultExceptionCode("e.xx.fw.9001");
            };
        }
    }

    @TestConfiguration
    @EnableAspectJAutoProxy
    static class TestServiceConfig {
        @Service
        static class TestService {
            void test() {
                throw new BusinessException(ResultMessages.error().add("test-code"));
            }
        }
    }
}
