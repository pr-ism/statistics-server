package com.prism.statistics.application.collect.inbox.aop;

import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InboxEnqueue {

    CollectInboxType value();
}
