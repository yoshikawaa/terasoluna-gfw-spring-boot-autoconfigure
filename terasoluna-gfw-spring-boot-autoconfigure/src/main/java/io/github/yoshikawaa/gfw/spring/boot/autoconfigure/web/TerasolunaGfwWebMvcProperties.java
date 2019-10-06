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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.terasoluna.gfw.web.codelist.CodeListInterceptor;
import org.terasoluna.gfw.web.logging.TraceLoggingInterceptor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInterceptor;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration Properties for Terasoluna Gfw Web MVC.
 *
 * @author Atsushi Yoshikawa
 */
@Getter
@Setter
@ConfigurationProperties(prefix = TerasolunaGfwWebMvcProperties.PROPERTIES_PREFIX)
public class TerasolunaGfwWebMvcProperties {

    public static final String PROPERTIES_PREFIX = "gfw.mvc";
    public static final String PROPERTIES_CODELIST_ENABLED = "codelist.enabled";

    private TraceLoggingProperties traceLogging = new TraceLoggingProperties();
    private TransactionTokenProperties transactionToken = new TransactionTokenProperties();
    private CodeListProperties codelist = new CodeListProperties();
    
    /**
     * Configuration Properties for {@link TraceLoggingInterceptor}.
     *
     * @author Atsushi Yoshikawa
     */
    @Getter
    @Setter
    public static class TraceLoggingProperties {
        private Long warnHandlingNanos;
    }

    /**
     * Configuration Properties for {@link TransactionTokenInterceptor}.
     *
     * @author Atsushi Yoshikawa
     */
    @Getter
    @Setter
    public static class TransactionTokenProperties {
        private Integer sizePerTokenName;
    }

    /**
     * Configuration Properties for {@link CodeListInterceptor}.
     *
     * @author Atsushi Yoshikawa
     */
    @Getter
    @Setter
    public static class CodeListProperties {
        private boolean enabled;
        private String idPattern = "CL_.+";
    }
}
