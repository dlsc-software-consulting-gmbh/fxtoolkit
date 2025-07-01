package com.dlsc.fxtoolkit.model;

import java.awt.Color;

public class GradientInfo {
    public final Color[] colors;
    public final float[] fractions;
    public final GradientType type;

    // For linear gradients
    public float startX = 0f, startY = 0f, endX = 1f, endY = 0f;

    // For radial gradients
    public float centerX = 0.5f, centerY = 0.5f, radius = 1f;

    public GradientInfo(Color[] colors, float[] fractions, GradientType type) {
        this.colors = colors;
        this.fractions = fractions;
        this.type = type;
    }
}
