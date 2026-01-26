package com.prism.statistics.context.security;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.security.core.context.SecurityContextHolder;

public class WithOAuth2UserExtension implements BeforeEachCallback, AfterEachCallback {

    private final WithOAuth2UserSecurityContextFactory securityContextFactory =
            new WithOAuth2UserSecurityContextFactory();

    @Override
    public void beforeEach(ExtensionContext context) {
        Optional<WithOAuth2User> annotation = findAnnotation(context);
        if (annotation.isEmpty()) {
            return;
        }

        SecurityContextHolder.setContext(
                securityContextFactory.createSecurityContext(annotation.get())
        );
    }

    @Override
    public void afterEach(ExtensionContext context) {
        SecurityContextHolder.clearContext();
    }

    private Optional<WithOAuth2User> findAnnotation(ExtensionContext context) {
        Optional<AnnotatedElement> element = context.getElement();
        if (element.isPresent()) {
            Optional<WithOAuth2User> onElement = AnnotationSupport.findAnnotation(element.get(), WithOAuth2User.class);
            if (onElement.isPresent()) {
                return onElement;
            }
        }

        return context.getTestClass()
                      .flatMap(testClass -> AnnotationSupport.findAnnotation(testClass, WithOAuth2User.class));
    }
}
