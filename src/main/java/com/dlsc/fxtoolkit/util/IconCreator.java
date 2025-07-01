package com.dlsc.fxtoolkit.util;

import com.dlsc.fxtoolkit.icon.ColorIcon;
import com.dlsc.fxtoolkit.icon.GradientIcon;
import com.dlsc.fxtoolkit.icon.SvgIcon;
import com.dlsc.fxtoolkit.model.GradientInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public final class IconCreator {

    private IconCreator() {
    }

    public static Icon createIcon(@NotNull String val, @NotNull Project project, int iconSize) {
      String value = val.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        if (ColorConverter.isValidColor(value)) {
            return ColorConverter.parseColor(value)
                    .map(color -> new ColorIcon(iconSize, color))
                    .orElse(null);
        }

        if (value.startsWith("linear-gradient") || value.startsWith("radial-gradient")) {
            GradientInfo info = GradientConverter.parse(value, project);
            return info != null ? new GradientIcon(iconSize, info) : null;
        }

        if (value.trim().matches("(?i)^\\s*[mlczaqhvstf].*\\d.*")) {
            return new SvgIcon(value, iconSize, iconSize);
        }

        return null;
    }

    public static Icon createIcon(@NotNull String val, @NotNull Project project) {
        return createIcon(val, project, getIconSize(project));
    }

    public static int getIconSize(Project project) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor != null) {
            int fontSize = editor.getColorsScheme().getEditorFontSize();
            // float lineSpacing = editor.getColorsScheme().getLineSpacing();
            return Math.max(12, fontSize - 4);
        }
        return 12;
    }
}
