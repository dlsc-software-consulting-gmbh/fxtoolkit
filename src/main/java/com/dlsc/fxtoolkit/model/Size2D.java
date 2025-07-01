package com.dlsc.fxtoolkit.model;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a 2D size with width and height.
 * This class is immutable and provides a method to create a zero size instance.
 */
public record Size2D(double width, double height) {

    public static Size2D zero() {
        return new Size2D(0, 0);
    }

    @Override
    public @NotNull String toString() {
        return "Size2D{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
