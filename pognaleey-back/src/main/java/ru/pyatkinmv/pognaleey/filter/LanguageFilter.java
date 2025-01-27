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

    private static String getFirstLocale(String acceptLanguage) {
        if (acceptLanguage.length() > 2) {
            return acceptLanguage.split(",")[0].trim();
        } else {
            return acceptLanguage;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            var acceptLanguage = httpRequest.getHeader("Accept-Language");
            var language = Optional.ofNullable(acceptLanguage)
                    .map(LanguageFilter::getFirstLocale)
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
