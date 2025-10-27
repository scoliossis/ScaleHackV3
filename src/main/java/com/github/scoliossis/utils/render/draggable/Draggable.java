package com.github.scoliossis.utils.render.draggable;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

public class Draggable {
    public String id;
    public Callable<double[]> render;
    /// if module / mode is enabled
    public Predicate<?> canRender;
    /// conditions to render if enabled
    public Predicate<?> conditions;

    public int x = 100;
    public int y = 100;

    public Draggable(String id, Callable<double[]> render, Predicate<?> canRender, Predicate<?> conditions) {
        this.id = id;
        this.render = render;
        this.canRender = canRender;
        this.conditions = conditions;

        DraggableRenderer.draggables.add(this);
    }
}
