# terasoluna-gfw-spring-boot-autoconfigure

[![Build Status](https://travis-ci.org/yoshikawaa/terasoluna-gfw-spring-boot-autoconfigure.svg?branch=master)](https://travis-ci.org/yoshikawaa/terasoluna-gfw-spring-boot-autoconfigure)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c3f96f6bfac049d9a722d910dcde35b3)](https://www.codacy.com/manual/yoshikawaa/terasoluna-gfw-spring-boot-autoconfigure?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yoshikawaa/terasoluna-gfw-spring-boot-autoconfigure&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/c3f96f6bfac049d9a722d910dcde35b3)](https://www.codacy.com/app/yoshikawaa/terasoluna-gfw-spring-boot-autoconfigure?utm_source=github.com&utm_medium=referral&utm_content=yoshikawaa/terasoluna-gfw-spring-boot-autoconfigure&utm_campaign=Badge_Coverage)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.yoshikawaa.gfw.spring.boot/terasoluna-gfw-spring-boot-autoconfigure.svg)](https://repo.maven.apache.org/maven2/io/github/yoshikawaa/gfw/spring/boot/terasoluna-gfw-spring-boot-autoconfigure/)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg?style=flat)](https://github.com/yoshikawaa/terasoluna-gfw-spring-boot-autoconfigure/blob/master/LICENSE.txt)

Spring Boot Auto-Configure for Terasoluna Framework 5.x Common Libraries & Extensions.

## Notes

* Supports upper Java 11
* Supports Spring Boot 2.7.4
* Supports Terasoluna Gfw 5.7.1.SP1

----

## Artifacts

* [terasoluna-gfw-spring-boot-autoconfigure](#terasoluna-gfw-spring-boot-autoconfigure)
* [terasoluna-gfw-spring-boot-test-autoconfigure](#terasoluna-gfw-spring-boot-test-autoconfigure)

----

## terasoluna-gfw-spring-boot-autoconfigure

Auto-Configure for Application using Terasoluna Framework 5.x Common Libraries.

### Getting Start

```xml
<dependency>
    <groupId>io.github.yoshikawaa.gfw.spring.boot</groupId>
    <artifactId>terasoluna-gfw-spring-boot-autoconfigure</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Auto-Configure Features

* Common Features
  - [Logging Exceptions](#logging-exceptions)
  - [Date Factory](#date-factory)
* Web MVC Features
  * [Transaction Token](#transaction-token)
  * [CodeList](#codelist)
  * [Exception Handling](#exception-handling)
  * [Trace Logging for Controllers](#trace-logging-for-controllers)
* Web Features
  * [Logging with Mapped Diagnostic Context (MDC)](#logging-with-mapped-diagnostic-context-mdc)
* Thymeleaf Features
  * [Thymeleaf Dialect](#thymeleaf-dialect) (not Terasoluna Gfw support)

> More infomation of features [Terasoluna Gfw](https://github.com/terasolunaorg/terasoluna-gfw).

#### Logging Exceptions

Enable Logging Exceptions if provided `terasoluna-gfw-common`.

* `ExceptionCodeResolver`
* `ExceptionLogger`
* `ResultMessagesLoggingInterceptor`

`ExceptionCodeResolverConfigurer` bean provide to configure `ExceptionCodeResolver`.

```java
@Bean
ExceptionCodeResolverConfigurer exceptionCodeResolverConfigurer() {
    return builder -> {
        builder
            .mapping("ResourceNotFoundException", "e.xx.fw.5001")
            .mapping("InvalidTransactionTokenException", "e.xx.fw.7001")
            .mapping("BusinessException", "e.xx.fw.8001")
            .mapping(".DataAccessException", "e.xx.fw.9002")
            .defaultExceptionCode("e.xx.fw.9001");
    };
}
```

> `ExceptionCodeResolver` is also used by `SystemExceptionResolver`.

#### Date Factory

Enable Date Factory if provided `terasoluna-gfw-jodatime`.

* `DefaultJodaTimeDateFactory`

`JodaTimeDateFactory` bean override `DefaultJodaTimeDateFactory` bean.

```java
@Bean
JodaTimeDateFactory dateFactory(DataSource dataSource) {
    JdbcFixedJodaTimeDateFactory dateFactory = new JdbcFixedJodaTimeDateFactory();
    dateFacotry.setDataSource(dataSource);
    return dateFactory;
}
```

> Date Factory does not yet support JSR-310.

#### Transaction Token

Enable Transaction Token if provided `terasoluna-gfw-web`.

* `TransactionTokenInterceptor`
* `TransactionTokenRequestDataValueProcessor`

Set property to change behavior.

| name                                          | type    | default |
|-----------------------------------------------|---------|---------|
| gfw.mvc.transaction-token.size-per-token-name | Integer | 10      |

> If a bean named `requestDataValueProcessor` is registered by Spring Security, wrap it with `CompositeRequestDataValueProcessor`.

#### CodeList

Enable CodeList if provided `terasoluna-gfw-web`.

* `CodeListInterceptor`

Register `CodeList` bean.

```java
@Bean("CL_SIMPLE")
public CodeList simple() {
    SimpleMapCodeList codeList = new SimpleMapCodeList();
    codeList.setMap(Collections.singletonMap("001", "Tom"));
    return codeList;
}
```

Set property to change behavior.

| name                                          | type    | default |
|-----------------------------------------------|---------|---------|
| gfw.mvc.codelist.enabled                      | boolean | true    |
| gfw.mvc.codelist.id-pattern                   | String  | `CL_.+` |

> It is recommended to set `gfw.mvc.codelist.enabled=false` to improve performance
> if obtain `CodeList` bean directly using Spring EL.

#### Exception Handling

Enable Exception Handling if provided `terasoluna-gfw-web`.

* `SystemExceptionResolver`

`SystemExceptionResolverConfigurer` bean provide to configure `SystemExceptionResolver`.

```java
@Bean
SystemExceptionResolverConfigurer systemExceptionResolverConfigurer() {
    return builder -> builder
        .mapping("ResourceNotFoundException", "error/resourceNotFoundError")
        .mapping("BusinessException", "error/businessError")
        .mapping("InvalidTransactionTokenException", "error/transactionTokenError")
        .mapping(".DataAccessException", "error/dataAccessError")
        .statusCode("error/resourceNotFoundError", 404)
        .statusCode("error/businessError", 409)
        .statusCode("error/transactionTokenError", 409)
        .statusCode("error/dataAccessError", 500)
        .exclude(NestedServletException.class)
        .defaultErrorView("error")
        .defaultStatusCode(500);
}
```

If you want to configure completely, you can define `SystemExceptionResolver` bean to override.

> Logging exceptions is not supported.
> * `HandlerExceptionResolverLoggingInterceptor` : Please consider to use `spring.mvc.log-resolved-exception`.
> * `ExceptionLoggingFilter` : Please consider to use `server.error.include-stacktrace` or intercept `ErrorController`.

#### Trace Logging for Controllers

Enable Trace Logging if provided `terasoluna-gfw-web`.

* `TraceLoggingInterceptor`

Please set `logging.level.org.terasoluna.gfw.web.logging.TraceLoggingInterceptor=trace` to enable trace log.

Set property to change behavior.

| name                                          | type    | default |
|-----------------------------------------------|---------|---------|
| gfw.mvc.trace-logging.warn-handling-nanos     | Long    | -       |

#### Logging with Mapped Diagnostic Context (MDC)

Enable MDC if provided `terasoluna-gfw-web`/`terasoluna-gfw-security-web`.

* `MDCClearFilter`
* `XTrackMDCPutFilter`
* `UserIdMDCPutFilter` (require `terasoluna-gfw-security-web` & `spring-boot-security-starter`)

Please set `logging.pattern.level` to contain MDC key `X-Track` or `USER`.
For example `logging.pattern.level=X-Track:%X{X-Track} UserId:%X{USER} %5p`.

#### Thymeleaf Dialect

Enable Thymeleaf Dialect if provided `thymeleaf-extras-terasoluna-gfw`.

* `TerasolunaGfwDialect`

> Please see [`thymeleaf-extras-terasoluna-gfw`](https://github.com/yoshikawaa/thymeleaf-extras-terasoluna-gfw).

----

## terasoluna-gfw-spring-boot-test-autoconfigure

Auto-Configure for Test using Terasoluna Framework 5.x Common Libraries.

### Getting Start

```xml
<dependency>
    <groupId>io.github.yoshikawaa.gfw.spring.boot</groupId>
    <artifactId>terasoluna-gfw-spring-boot-test-autoconfigure</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Auto-Configure Features

* [`@AutoConfigureTerasolunaGfw`](#autoconfigureterasolunagfw)

> Terasoluna Gfw not support Testing functions. Please use [Recommended Feature](#recommended-feature).

#### `@AutoConfigureTerasolunaGfw`

```java
@WebMvcTest
@AutoConfigureTerasolunaGfw // <- anotate!
class SampleTest {
    @Test
    void test() {
        //
    }
}
```

Provide attributes to **disable features**.

| name                     | mapped property                    |
|--------------------------|------------------------------------|
| codeListEnabled          | gfw.mvc.codelist.enabled           |

### Recommended Features

Enable useful support for `MockMvc` if provided `spring-test-terasoluna-gfw`.

> Please see [`spring-test-terasoluna-gfw`](https://github.com/yoshikawaa/spring-test-terasoluna-gfw).
