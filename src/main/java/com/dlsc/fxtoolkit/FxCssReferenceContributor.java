package com.dlsc.fxtoolkit;

import com.intellij.lang.css.CSSLanguage;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class FxCssReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiElement.class).withLanguage(CSSLanguage.INSTANCE),
            new PsiReferenceProvider() {
                @Override
                public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                    String text = element.getText();
                    if (text != null && text.startsWith("-")) {
                        return new PsiReference[]{new FxCssPsiReference(element)};
                    }
                    return PsiReference.EMPTY_ARRAY;
                }
            }
        );
    }
}
