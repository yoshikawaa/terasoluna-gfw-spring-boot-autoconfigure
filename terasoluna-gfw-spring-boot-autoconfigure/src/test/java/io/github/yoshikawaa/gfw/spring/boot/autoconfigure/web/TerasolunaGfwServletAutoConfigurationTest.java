package io.github.yoshikawaa.gfw.spring.boot.autoconfigure.web;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class TerasolunaGfwServletAutoConfigurationTest {

    @Nested
    @WebMvcTest(controllers = TestConfig.MdcController.class)
    @ImportAutoConfiguration({ TerasolunaGfwServletAutoConfiguration.class,
            TerasolunaGfwWebMvcAutoConfiguration.class })
    @Import(TestConfig.class)
    @WithMockUser
    @ExtendWith(OutputCaptureExtension.class)
    class MdcTest {

        @Test
        void testXTrack(@Autowired MockMvc mvc, CapturedOutput output) throws Exception {
            mvc.perform(get("/mdc"));
            mvc.perform(get("/mdc"));

            List<String> xTracks = Arrays.stream(output.getOut().split(System.getProperty("line.separator"))) //
                    .filter(line -> line.contains("MdcController.get()")) //
                    .map(line -> Arrays.stream(line.split(" ")) //
                            .filter(block -> block.contains("X-Track:")).findFirst() //
                            .get()) //
                    .collect(toList());

            String firstXTrack = xTracks.get(0);
            String secondXTrack = xTracks.get(3);
            assertThat(xTracks).containsExactly(firstXTrack, firstXTrack, firstXTrack, secondXTrack, secondXTrack,
                    secondXTrack);
        }

        @Test
        void testUserId(@Autowired MockMvc mvc, CapturedOutput output) throws Exception {
            mvc.perform(get("/mdc"));
            mvc.perform(get("/mdc"));

            List<String> userIds = Arrays.stream(output.getOut().split(System.getProperty("line.separator"))) //
                    .filter(line -> line.contains("MdcController.get")) //
                    .map(line -> Arrays.stream(line.split(" ")) //
                            .filter(block -> block.contains("UserId:")).findFirst() //
                            .get()) //
                    .collect(toList());

            String userId = "UserId:user";
            assertThat(userIds).containsExactly(userId, userId, userId, userId, userId, userId);
        }
    }

    @TestConfiguration
    static class TestConfig {

        @RestController
        @RequestMapping("/mdc")
        static class MdcController {
            @GetMapping
            public String get() {
                return "mdc";
            }
        }
    }
}
