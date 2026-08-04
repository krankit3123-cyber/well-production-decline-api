package com.bp.decline.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Serves a custom Swagger UI page that extends the default with
 * Chart.js-based decline curve visualization.
 *
 * <p>Spring MVC controllers have higher priority than resource handlers,
 * so this overrides the default index.html served from the swagger-ui webjar
 * without modifying any SpringDoc internals.</p>
 */
@Controller
public class CustomSwaggerUiController {

    @Value("classpath:custom-swagger/index.html")
    private Resource customSwaggerUiPage;

    @GetMapping(value = "/swagger-ui/index.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String swaggerUi() throws IOException {
        return customSwaggerUiPage.getContentAsString(StandardCharsets.UTF_8);
    }
}
