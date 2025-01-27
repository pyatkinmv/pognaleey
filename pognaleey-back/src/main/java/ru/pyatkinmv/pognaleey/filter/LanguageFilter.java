package ru.pyatkinmv.pognaleey.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import ru.pyatkinmv.pognaleey.model.Language;
import ru.pyatkinmv.pognaleey.service.LanguageContextHolder;

import java.io.IOException;
import java.util.Optional;

@Component
public class LanguageFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            var acceptLanguage = httpRequest.getHeader("Accept-Language");
            var language = Optional.ofNullable(acceptLanguage)
                    .flatMap(Language::from)
                    .orElseGet(Language::getDefault);
            LanguageContextHolder.setLanguage(language);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            LanguageContextHolder.clear();
        }
    }
}
