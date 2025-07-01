package com.dlsc.fxtoolkit;

import com.dlsc.fxtoolkit.icon.SvgIcon;
import com.dlsc.fxtoolkit.util.IconCreator;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Map;

public class FxCssCompletionContributor extends CompletionContributor {

    public FxCssCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(CSSLanguage.INSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        Project project = parameters.getEditor().getProject();
                        if (project == null) return;

                        FxCssService service = FxCssService.getInstance(project);
                        Map<String, String> constantMap = service.getConstantMap();
                        DecimalFormat formatter = new DecimalFormat("0.##");

                        for (Map.Entry<String, String> entry : constantMap.entrySet()) {
                            String name = entry.getKey();
                            String resolvedValue = service.resolveConstantValue(name);

                            if (resolvedValue == null) continue;

                            String typeText = resolvedValue;
                            Icon icon = IconCreator.createIcon(resolvedValue, project);

                            // if the icon is an instance of SvgIcon, we can get its bounds
                            if (icon instanceof SvgIcon) {
                                Rectangle2D bounds = ((SvgIcon) icon).getBounds();
                                if (bounds != null && bounds.getWidth() > 0 && bounds.getHeight() > 0) {
                                    typeText = "W:" + formatter.format(bounds.getWidth()) + " H:" + formatter.format(bounds.getHeight());
                                }
                            }
                            LookupElementBuilder builder = LookupElementBuilder.create("-" + name)
                                    .withTypeText(typeText, true);

                            if (icon != null) {
                                builder = builder.withIcon(icon);
                            }

                            result.addElement(builder);
                        }
                    }
                });
    }
}
