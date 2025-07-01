package com.dlsc.fxtoolkit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class FxCssPsiReference extends PsiReferenceBase<PsiElement> {

    public FxCssPsiReference(@NotNull PsiElement element) {
        super(element, new TextRange(0, element.getTextLength()));
    }

    @Override
    public @Nullable PsiElement resolve() {
        String refText = getElement().getText();
        if (refText == null || !refText.startsWith("-")) {
            return null;
        }
        // remove the leading dash "-"
        String varName = refText.substring(1);
        Project project = getElement().getProject();
        // Search for all CSS files in the project
        Collection<VirtualFile> cssFiles = FilenameIndex.getAllFilesByExt(project, "css", GlobalSearchScope.allScope(project));
        for (VirtualFile file : cssFiles) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile == null) continue;

            if (!psiFile.getText().contains(".root")) {
                continue;
            }
            // Find all CssDeclaration elements in the file
            for (CssDeclaration decl : PsiTreeUtil.findChildrenOfType(psiFile, CssDeclaration.class)) {
                String propName = decl.getPropertyName();
                if (propName.trim().equalsIgnoreCase("-" + varName)) {
                    CssRuleset ruleset = PsiTreeUtil.getParentOfType(decl, CssRuleset.class);
                    if (ruleset != null && ruleset.getText().contains(".root")) {
                        return decl;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }
}
