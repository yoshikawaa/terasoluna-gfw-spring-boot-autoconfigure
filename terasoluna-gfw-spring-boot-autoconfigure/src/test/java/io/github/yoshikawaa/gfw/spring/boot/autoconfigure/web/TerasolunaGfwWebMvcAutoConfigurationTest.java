package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.web;

import static io.github.yoshikawaa.gfw.test.web.servlet.request.TerasolunaGfwMockMvcRequestPostProcessors.transaction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.util.Collections;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.NestedServletException;
import org.terasoluna.gfw.common.codelist.CodeList;
import org.terasoluna.gfw.common.codelist.SimpleMapCodeList;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.exception.SystemException;
import org.terasoluna.gfw.common.message.ResultMessages;
import org.terasoluna.gfw.web.exception.SystemExceptionResolver;
import org.terasoluna.gfw.web.logging.TraceLoggingInterceptor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenCheck;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInterceptor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenStore;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenType;

import io.github.yoshikawaa.gfw.config.web.SystemExceptionResolverConfigurer;
import io.github.yoshikawaa.gfw.spring.boot.autoconfigure.common.TerasolunaGfwCommonAutoConfiguration;

class TerasolunaGfwWebMvcAutoConfigurationTest {

    @Nested
    @WebMvcTest
    @ContextConfiguration(classes = TestConfig.class)
    @ImportAutoConfiguration(TerasolunaGfwWebMvcAutoConfiguration.class)
    @WithMockUser
    @ExtendWith(OutputCaptureExtension.class)
    class DefaultTest {

        @Test
        void test(@Autowired MockMvc mvc, CapturedOutput output) throws Exception {
            mvc.perform(get("/tracelogging")) //
                    .andExpect(status().isOk()) //
                    .andExpect(content().string(containsString("tracelogging")));

            assertThat(output).contains( //
                    "[START CONTROLLER] TraceLoggingController.get()", //
                    "[END CONTROLLER  ] TraceLoggingController.get()", //
                    "[HANDLING TIME   ] TraceLoggingController.get()");
        }

        @Test
        void testTransactionToken(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/transactiontoken")) //
                    .andExpect(status().isOk()) //
                    .andExpect(view().name("transactiontoken")) //
                    .andExpect(xpath("//form/input[@type='hidden']").nodeCount(2)) //
                    .andExpect(xpath("//form/input[@type='hidden' and @name='_csrf']").exists()) //
                    .andExpect(xpath("//form/input[@type='hidden' and @name='_TRANSACTION_TOKEN']").exists());

            mvc.perform(post("/transactiontoken").with(csrf())) //
                    .andExpect(status().isConflict()) //
                    .andExpect(view().name("error/transactionTokenError"));

            mvc.perform(post("/transactiontoken").with(csrf()).with(transaction("/transactiontoken"))) //
                    .andExpect(status().isOk()) //
                    .andExpect(view().name("transactiontoken"));
        }

        @Test
        void testCodeList(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/codelist")) //
                    .andExpect(status().isOk()) //
                    .andExpect(view().name("codelist")) //
                    .andExpect(xpath("//ul").nodeCount(1)) //
                    .andExpect(xpath("//ul/li").nodeCount(1)) //
                    .andExpect(xpath("//ul/li").string("Tom"));
        }

        @Test
        void testSystemException(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/exceptionresolver/systemException")) //
                    .andExpect(status().isInternalServerError()) //
                    .andExpect(view().name("error"));
        }

        @Test
        void testBusinessException(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/exceptionresolver/businessException")) //
                    .andExpect(status().isConflict()) //
                    .andExpect(view().name("error/businessError"));
        }
    }

    @Nested
    @WebMvcTest(controllers = TestConfig.TraceLoggingController.class)
    @ContextConfiguration(classes = TestConfig.class)
    @ImportAutoConfiguration(TerasolunaGfwWebMvcAutoConfiguration.class)
    @WithMockUser
    @TestPropertySource(properties = "gfw.mvc.trace-logging.warn-handling-nanos=100")
    class TraceLoggingWarnHandlingNanosTest {

        @Test
        void test(@Autowired TraceLoggingInterceptor interceptor) throws Exception {

            long warnHandlingNanos = (Long) ReflectionTestUtils.getField(interceptor, "warnHandlingNanos");
            assertThat(warnHandlingNanos).isEqualTo(100L);
        }
    }

    @Nested
    @WebMvcTest(controllers = TestConfig.TransactionTokenController.class)
    @ContextConfiguration(classes = TestConfig.class)
    @ImportAutoConfiguration(TerasolunaGfwWebMvcAutoConfiguration.class)
    @WithMockUser
    @TestPropertySource(properties = "gfw.mvc.transaction-token.size-per-token-name=100")
    class TransactionTokenSizePerTokenNameTest {

        @Test
        void test(@Autowired TransactionTokenInterceptor interceptor) throws Exception {
            TransactionTokenStore tokenStore = (TransactionTokenStore) ReflectionTestUtils.getField(interceptor,
                    "tokenStore");
            int transactionTokensPerTokenName = (Integer) ReflectionTestUtils.getField(tokenStore,
                    "transactionTokensPerTokenName");
            assertThat(transactionTokensPerTokenName).isEqualTo(100);
        }
    }

    @Nested
    @WebMvcTest(controllers = TestConfig.TransactionTokenController.class)
    @ContextConfiguration(classes = TestConfig.class)
    @ImportAutoConfiguration({ TerasolunaGfwWebMvcAutoConfiguration.class, TestDisableCsrfAutoConfig.class })
    @WithMockUser
    class TransactionTokenCsrfDisabledTest {

        @Test
        void test(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/transactiontoken")) //
                    .andExpect(status().isOk()) //
                    .andExpect(view().name("transactiontoken")) //
                    .andExpect(xpath("//form/input[@type='hidden']").nodeCount(1)) //
                    .andExpect(xpath("//form/input[@type='hidden' and @name='_csrf']").doesNotExist()) //
                    .andExpect(xpath("//form/input[@type='hidden' and @name='_TRANSACTION_TOKEN']").exists());
        }
    }

    @Nested
    @WebMvcTest(controllers = TestConfig.CodeListController.class)
    @ContextConfiguration(classes = TestConfig.class)
    @ImportAutoConfiguration(TerasolunaGfwWebMvcAutoConfiguration.class)
    @WithMockUser
    @TestPropertySource(properties = "gfw.mvc.codelist.enabled=false")
    class CodeListDisabledTest {

        @Test
        void test(@Autowired MockMvc mvc, @Autowired TerasolunaGfwWebMvcProperties properties) throws Exception {
            mvc.perform(get("/codelist")) //
                    .andExpect(status().isOk()) //
                    .andExpect(view().name("codelist")) //
                    .andExpect(xpath("//ul").nodeCount(0));

            assertThat(properties.getCodelist().isEnabled()).isFalse();
        }
    }

    @Nested
    @WebMvcTest(controllers = TestConfig.CodeListController.class)
    @ContextConfiguration(classes = TestConfig.class)
    @ImportAutoConfiguration(TerasolunaGfwWebMvcAutoConfiguration.class)
    @WithMockUser
    @TestPropertySource(properties = "gfw.mvc.codelist.id-pattern=CODELIST_.+")
    class CodeListIdPatternTest {

        @Test
        void test(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/codelist")) //
                    .andExpect(status().isOk()) //
                    .andExpect(view().name("codelist")) //
                    .andExpect(xpath("//ul").nodeCount(1)) //
                    .andExpect(xpath("//ul/li").nodeCount(1)) //
                    .andExpect(xpath("//ul/li").string("John"));
        }
    }

    @Nested
    @WebMvcTest(controllers = TestConfig.ExceptionResolverController.class)
    @ContextConfiguration(classes = TestConfig.class)
    @ImportAutoConfiguration(TerasolunaGfwWebMvcAutoConfiguration.class)
    @Import(TestExceptionResolverConfig.class)
    @WithMockUser
    class ExceptionResolverCustomTest {

        @Test
        void testSystemException(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/exceptionresolver/systemException")) //
                    .andExpect(status().isInternalServerError()) //
                    .andExpect(view().name("error"));
        }

        @Test
        void testBusinessException(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/exceptionresolver/businessException")) //
                    .andExpect(status().isInternalServerError()) //
                    .andExpect(view().name("error"));
        }
    }

    @Nested
    @WebMvcTest(controllers = TestConfig.ExceptionResolverController.class)
    @ContextConfiguration(classes = TestConfig.class)
    @ImportAutoConfiguration({ TerasolunaGfwWebMvcAutoConfiguration.class, TerasolunaGfwCommonAutoConfiguration.class })
    @WithMockUser
    class ExceptionResolverNonCodeResolverTest {

        @Test
        void test(@Autowired SystemExceptionResolver exceptionResolver) throws Exception {
            //
        }
    }

    @SpringBootConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @RestController
        @RequestMapping("/tracelogging")
        static class TraceLoggingController {
            @GetMapping
            public String get() {
                return "tracelogging";
            }
        }

        @Controller
        @RequestMapping("/transactiontoken")
        @TransactionTokenCheck("/transactiontoken")
        static class TransactionTokenController {
            @GetMapping
            @TransactionTokenCheck(type = TransactionTokenType.BEGIN)
            public String get() {
                return "transactiontoken";
            }

            @PostMapping
            @TransactionTokenCheck(type = TransactionTokenType.IN)
            public String post() {
                return "transactiontoken";
            }
        }

        @Controller
        @RequestMapping("/codelist")
        static class CodeListController {
            @GetMapping
            public String get() {
                return "codelist";
            }
        }

        @Bean("CL_SIMPLE")
        public CodeList simple() {
            SimpleMapCodeList codeList = new SimpleMapCodeList();
            codeList.setMap(Collections.singletonMap("001", "Tom"));
            return codeList;
        }

        @Bean("CODELIST_EXTEND")
        public CodeList extend() {
            SimpleMapCodeList codeList = new SimpleMapCodeList();
            codeList.setMap(Collections.singletonMap("001", "John"));
            return codeList;
        }

        @Controller
        @RequestMapping("/exceptionresolver")
        static class ExceptionResolverController {
            @GetMapping("systemException")
            public String sytemException() {
                throw new SystemException("test-code", "test-message");
            }

            @GetMapping("businessException")
            public String businessException() {
                throw new BusinessException(ResultMessages.warning().add("test-code"));
            }
        }
    }

    @Configuration
    @AutoConfigureAfter(SecurityAutoConfiguration.class)
    @AutoConfigureBefore(TerasolunaGfwWebMvcAutoConfiguration.class)
    static class TestDisableCsrfAutoConfig {
        @Bean
        public BeanDefinitionRegistryPostProcessor disableCsrfRequestDataValueProcessor() {
            final String beanName = "requestDataValueProcessor";
            return new BeanDefinitionRegistryPostProcessor() {

                @Override
                public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                    // nothing to do.
                }

                @Override
                public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                    registry.removeBeanDefinition(beanName);
                }
            };
        }
    }

    @TestConfiguration
    static class TestExceptionResolverConfig {
        @Bean
        public SystemExceptionResolverConfigurer systemExceptionResolverConfigurer() {
            return builder -> builder //
                    .mapping("ResourceNotFoundException", "error/resourceNotFoundError") //
                    // .mapping("BusinessException", "error/businessError") //
                    .mapping("InvalidTransactionTokenException", "error/transactionTokenError") //
                    .mapping(".DataAccessException", "error/dataAccessError") //
                    .statusCode("error/resourceNotFoundError", 404) //
                    // .statusCode("error/businessError", 409) //
                    .statusCode("error/transactionTokenError", 409) //
                    .statusCode("error/dataAccessError", 500) //
                    .exclude(NestedServletException.class) //
                    .defaultErrorView("error") //
                    .defaultStatusCode(500);
        }
    }
}
