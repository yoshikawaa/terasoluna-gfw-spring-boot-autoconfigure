package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.thymeleaf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenCheck;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenType;

import io.github.yoshikawaa.gfw.spring.boot.autoconfigure.web.TerasolunaGfwWebMvcAutoConfiguration;

class TerasolunaGfwThymeleafAutoConfigurationTest {

    @Nested
    @WebMvcTest(controllers = TestConfig.DialectController.class)
    @ImportAutoConfiguration({ TerasolunaGfwWebMvcAutoConfiguration.class, TerasolunaGfwThymeleafAutoConfiguration.class })
    @WithMockUser
    class DialectTest {

        @Test
        void test(@Autowired MockMvc mvc) throws Exception {
            mvc.perform(get("/")) //
                    .andExpect(status().isOk()) //
                    .andExpect(view().name("dialect")) //
                    .andExpect(xpath("//form/input[@type='hidden']").nodeCount(1)) //
                    .andExpect(xpath("//form/input[@type='hidden' and @name='_TRANSACTION_TOKEN']").exists());
        }
    }

    @SpringBootConfiguration
    static class TestConfig {
        
        @Controller
        @TransactionTokenCheck("/")
        static class DialectController {
            @GetMapping
            @TransactionTokenCheck(type = TransactionTokenType.BEGIN)
            public String get() {
                return "dialect";
            }
        }
    }

}
