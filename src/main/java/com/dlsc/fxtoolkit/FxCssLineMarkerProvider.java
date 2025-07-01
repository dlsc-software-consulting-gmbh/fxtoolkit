package com.dlsc.fxtoolkit;

import com.dlsc.fxtoolkit.components.ColorPreviewPanel;
import com.dlsc.fxtoolkit.components.GradientPreviewPanel;
import com.dlsc.fxtoolkit.components.SvgResizePreviewPanel;
import com.dlsc.fxtoolkit.icon.ColorIcon;
import com.dlsc.fxtoolkit.icon.GradientIcon;
import com.dlsc.fxtoolkit.icon.SvgIcon;
import com.dlsc.fxtoolkit.model.GradientInfo;
import com.dlsc.fxtoolkit.model.Size2D;
import com.dlsc.fxtoolkit.util.ColorConverter;
import com.dlsc.fxtoolkit.util.IconCreator;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssBlock;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRuleset;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides gutter icons for JavaFX CSS variable declarations in the editor.
 * <p>
 * This {@link com.intellij.codeInsight.daemon.LineMarkerProvider} implementation analyzes `CssDeclaration` nodes
 * and attaches visual icons to help preview custom JavaFX styles such as colors, gradients, or SVG shapes.
 * <p>
 * Supported features:
 * <ul>
 *     <li>💠 Color icons for valid color values (including variables and resolved tokens)</li>
 *     <li>🌈 Gradient preview icons for linear/radial gradients</li>
 *     <li>🟦 SVG icons for vector shapes (via `-fx-shape` path data)</li>
 * </ul>
 * <p>
 * When the user clicks the gutter icon:
 * <ul>
 *     <li>Color: Shows a popup with a color preview and raw definition</li>
 *     <li>Gradient: Shows a popup rendering the parsed gradient</li>
 *     <li>SVG: Shows a resizable preview and allows inserting `-fx-pref-width` and `-fx-pref-height` into the CSS block</li>
 * </ul>
 *
 * <p>
 * Icons are only shown if the declaration resolves to a recognizable and previewable value.
 * Raw color literals (like `#ff0000`) are skipped intentionally to reduce clutter.
 * </p>
 */
public class FxCssLineMarkerProvider implements LineMarkerProvider {

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof CssDeclaration decl)) {
            return null;
        }

        Project project = decl.getProject();
        String rawValue = decl.getValue() != null ? decl.getValue().getText().trim() : "";
        if (rawValue.isEmpty()) {
            return null;
        }

        // If the value is a valid color literal, do not show a custom icon
        if (ColorConverter.isValidColor(rawValue)) {
            return null;
        }

        String resolvedValue;
        if (rawValue.startsWith("-")) {
            resolvedValue = FxCssService.getInstance(project).resolveConstantValue(rawValue.substring(1));
        } else {
            resolvedValue = rawValue;
        }

        if (resolvedValue == null || resolvedValue.isEmpty()) {
            return null;
        }

        Icon icon = IconCreator.createIcon(resolvedValue, project);

        if (icon != null) {
            PsiElement anchor = decl.getPropertyNameElement() != null ? decl.getPropertyNameElement() : element;

            String accessibleName;
            GutterIconNavigationHandler<PsiElement> handler = null;
            if (icon instanceof SvgIcon) {
                accessibleName = "Click to preview svg";
                handler = new SvgPreviewNavigationHandler((SvgIcon) icon, decl);
            } else if (icon instanceof GradientIcon) {
                accessibleName = "Click to preview gradient";
                handler = new GradientPreviewNavigationHandler((GradientIcon) icon);
            } else if (icon instanceof ColorIcon) {
                accessibleName = "Click to preview color";
                handler = new ColorPreviewNavigationHandler((ColorIcon) icon, resolvedValue);
            } else {
                accessibleName = "";
            }

            if (handler != null) {
                return new LineMarkerInfo<>(anchor, anchor.getTextRange(), icon, null, handler, GutterIconRenderer.Alignment.CENTER, () -> accessibleName);
            }
        }

        return null;
    }

    private boolean isInRootBlock(PsiElement element) {
        CssRuleset ruleset = com.intellij.psi.util.PsiTreeUtil.getParentOfType(element, CssRuleset.class);
        if (ruleset != null) {
            String selectorsText = ruleset.getSelectorList().getText();
            return selectorsText.equals(".root") || selectorsText.startsWith(".root ");
        }
        return false;
    }

    private record ColorPreviewNavigationHandler(ColorIcon icon, String originalDefinition)
            implements GutterIconNavigationHandler<PsiElement> {

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
            Color color = icon.getColor();
            if (color == null) return;

            ColorPreviewPanel previewPanel = new ColorPreviewPanel(color, originalDefinition);

            JBPopup popup = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(previewPanel, previewPanel)
                    .setTitle("Color Preview")
                    .setMovable(true)
                    .setResizable(false)
                    .setRequestFocus(true)
                    .createPopup();

            popup.show(new RelativePoint(e));
        }
    }

    private record GradientPreviewNavigationHandler(GradientIcon icon)
            implements GutterIconNavigationHandler<PsiElement> {
        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
            GradientInfo info = icon.getGradientInfo();
            if (info == null) {
                return;
            }

            GradientPreviewPanel previewPanel = new GradientPreviewPanel(info, 210, 128);

            JBPopup popup = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(previewPanel, previewPanel)
                    .setTitle("Gradient Preview")
                    .setMovable(true)
                    .setResizable(false)
                    .setRequestFocus(true)
                    .createPopup();

            popup.show(new RelativePoint(e));
        }
    }


    private record SvgPreviewNavigationHandler(SvgIcon icon, CssDeclaration declaration)
            implements GutterIconNavigationHandler<PsiElement> {

        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
            CssRuleset ruleset = com.intellij.psi.util.PsiTreeUtil.getParentOfType(declaration, CssRuleset.class);
            if (ruleset == null) return;

            final JBPopup[] popupHolder = new JBPopup[1];

            SvgResizePreviewPanel previewPanel = new SvgResizePreviewPanel(icon, (size) -> {
                addOrUpdateSizeProperties(ruleset, size);
                if (popupHolder[0] != null) {
                    popupHolder[0].cancel();
                }
            });

            JBPopup popup = JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(previewPanel, previewPanel.getPreferredFocusedComponent())
                    .setTitle("SVG Preview")
                    .setMovable(true)
                    .setResizable(true)
                    .setRequestFocus(true)
                    .createPopup();

            popupHolder[0] = popup;
            popup.show(new RelativePoint(e));
        }

        private void addOrUpdateSizeProperties(CssRuleset ruleset, Size2D size) {
            DecimalFormat formatter = new DecimalFormat("0.###");
            Project project = ruleset.getProject();
            String widthValue = formatter.format(size.width()) + "px";
            String heightValue = formatter.format(size.height()) + "px";

            WriteCommandAction.runWriteCommandAction(project, "Apply SVG Size", null, () -> {
                CssBlock block = ruleset.getBlock();
                if (block == null) {
                    return;
                }

                Document document = PsiDocumentManager.getInstance(project).getDocument(ruleset.getContainingFile());
                if (document == null) {
                    return;
                }

                PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
                updateCssPropertyInDocument(document, block, "-fx-pref-width", widthValue);

                PsiDocumentManager.getInstance(project).commitDocument(document);
                updateCssPropertyInDocument(document, block, "-fx-pref-height", heightValue);

                PsiDocumentManager.getInstance(project).commitDocument(document);

            }, ruleset.getContainingFile());
        }

        private void updateCssPropertyInDocument(Document document, CssBlock block, String propertyName, String value) {
            try {
                int blockStart = block.getTextRange().getStartOffset();
                int blockEnd = block.getTextRange().getEndOffset();
                String blockText = document.getText(new com.intellij.openapi.util.TextRange(blockStart, blockEnd));

                String indent = detectIndentation(blockText);
                String newDeclaration = indent + propertyName + ": " + value + ";";
                Pattern pattern = Pattern.compile("^(\\s*)" + Pattern.quote(propertyName) + "\\s*:\\s*[^;\\n\\r]*;?\\s*$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(blockText);

                String newBlockText;
                if (matcher.find()) {
                    newBlockText = matcher.replaceFirst(newDeclaration);
                } else {
                    int rbracePosInBlock = blockText.lastIndexOf('}');
                    if (rbracePosInBlock != -1) {
                        String beforeRbrace = blockText.substring(0, rbracePosInBlock);
                        beforeRbrace = beforeRbrace.replaceAll("\\s+$", "");
                        newBlockText = beforeRbrace + "\n" + newDeclaration + "\n" + "}";
                    } else {
                        newBlockText = blockText + "\n" + newDeclaration + "\n";
                    }
                }
                document.replaceString(blockStart, blockEnd, newBlockText);
            } catch (Exception ex) {
                // Handle exceptions if necessary
            }
        }

        private String detectIndentation(String blockText) {
            Pattern indentPattern = Pattern.compile("^(\\s+)[a-zA-Z-]+\\s*:", Pattern.MULTILINE);
            Matcher matcher = indentPattern.matcher(blockText);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return "    ";
        }
    }
}
