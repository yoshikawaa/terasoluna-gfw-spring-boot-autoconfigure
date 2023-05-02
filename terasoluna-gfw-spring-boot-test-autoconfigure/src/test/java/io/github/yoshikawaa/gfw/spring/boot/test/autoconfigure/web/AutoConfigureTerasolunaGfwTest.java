package io.github.yoshikawaa.gfw.spring.boot.test.autoconfigure.web;

import static io.github.yoshikawaa.gfw.test.web.servlet.request.TerasolunaGfwMockMvcRequestPostProcessors.transaction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.terasoluna.gfw.common.codelist.CodeList;
import org.terasoluna.gfw.common.codelist.SimpleMapCodeList;
import org.terasoluna.gfw.common.exception.BusinessException;
import org.terasoluna.gfw.common.exception.SystemException;
import org.terasoluna.gfw.common.message.ResultMessages;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenCheck;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenType;

class AutoConfigureTerasolunaGfwTest {

    @Nested
    @WebMvcTest(controllers = TestConfig.ExceptionResolverController.class)
    @AutoConfigureTerasolunaGfw
    @ExtendWith(OutputCaptureExtension.class)
    @TestPropertySource(properties = "logging.level.org.terasoluna.gfw.web.logging.TraceLoggingInterceptor=trace")
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
                    .andExpect(xpath("//form/input[@type='hidden']").nodeCount(1)) //
                    .andExpect(xpath("//form/input[@type='hidden' and @name='_TRANSACTION_TOKEN']").exists());

            mvc.perform(post("/transactiontoken")) //
                    .andExpect(status().isConflict()) //
                        .andExpect(view().name("error/transactionTokenError"));

            mvc.perform(post("/transactiontoken").with(transaction("/transactiontoken"))) //
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
    @WebMvcTest(controllers = TestConfig.CodeListController.class)
    @AutoConfigureTerasolunaGfw(codeListEnabled = false)
    class CodeListDisabledTest {

        @Test
        void test(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/codelist")) //
                    .andExpect(status().isOk()) //
                    .andExpect(view().name("codelist")) //
                    .andExpect(xpath("//ul").nodeCount(0));
        }
    }

    @SpringBootConfiguration
    static class TestConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
            http.csrf(csrf -> csrf.disable());
            return http.build();
        }

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
}
