package eu.opertusmundi.message.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import eu.opertusmundi.message.model.BasicMessageCode;
import eu.opertusmundi.message.model.MessageCode;
import eu.opertusmundi.message.model.RestResponse;

@Controller
public class HomeController {

    private static final String DEVELOPMENT_PROFILE = "development";

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Value("${springdoc.api-docs.server:http://localhost:8080}")
    private String serverUrl;

    @Value("${springdoc.api-docs.path}")
    private String openApiSpec;

    @Autowired
    private MessageSource messageSource;

    /**
     * Default request handler
     */
    @GetMapping("/")
    public String index() {
        if (this.isDevelopmentProfileActive()) {
            return "redirect:docs";
        }
        return "index";
    }

    /**
     * Render documentation using the ReDoc {@link https://github.com/Redocly/redoc}
     */
    @GetMapping("/docs")
    public ModelAndView docs() {
        final ModelAndView modelAndView = new ModelAndView("docs");

        modelAndView.getModel().put("endpoint", this.serverUrl + this.openApiSpec);

        return modelAndView;
    }

    /**
     * Handles HTML error pages
     */
    @GetMapping(path = "/error/{id}", produces = MediaType.TEXT_HTML_VALUE )
    public String errorHtml(@PathVariable(name = "id", required = true) int id) {
        return "index";
    }

    /**
     * Handles errors for XHR requests
     */
    @GetMapping(path = "/error/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> error(@PathVariable(name = "id", required = true) int id) {
        final HttpStatus status = HttpStatus.valueOf(id) == null ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.valueOf(id);

        final MessageCode code = BasicMessageCode.fromStatusCode(status);

        final String description = this.messageSource.getMessage(code.key(), null, Locale.getDefault());

        final RestResponse<?> response = RestResponse.error(code, description);

        return new ResponseEntity<>(response, status);
    }

    private boolean isDevelopmentProfileActive() {
        for (final String profileName : this.activeProfile.split(",")) {
            if (profileName.equalsIgnoreCase(DEVELOPMENT_PROFILE)) {
                return true;
            }
        }
        return false;
    }

}
