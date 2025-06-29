package com.github.scoliossis.events;

// in theory I don't need this, I could use comments to annotate, but that looks worse and could lead to me FAILING
public @interface SubscribeEvent {
    int priority() default 1000;
}
