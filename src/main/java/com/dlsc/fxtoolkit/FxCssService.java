package com.dlsc.fxtoolkit;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssTerm;
import com.intellij.psi.css.CssTermList;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FxCssService is a service that manages CSS files in a JavaFX project.
 * It scans all CSS files for global constants defined in the `.root` block,
 * caches them, and provides methods to resolve these constants.
 */
@Service(Service.Level.PROJECT)
public final class FxCssService implements Disposable {

    private final Project project;

    private final Map<String, String> globalConstantMap = new ConcurrentHashMap<>();

    private final Map<VirtualFile, Map<String, String>> fileConstantsCache = new ConcurrentHashMap<>();

    private static final int DEBOUNCE_DELAY = 1000;

    public FxCssService(Project project) {
        this.project = project;
    }

    public static FxCssService getInstance(@NotNull Project project) {
        return project.getService(FxCssService.class);
    }

    /**
     * Scans all CSS files in the project, Refreshes the caches and the editors.
     */
    public synchronized void scanAllCssFiles() {
        fileConstantsCache.clear();
        globalConstantMap.clear();

        ReadAction.run(() -> {
            Collection<VirtualFile> cssFiles = FilenameIndex.getAllFilesByExt(
                    project, "css", GlobalSearchScope.projectScope(project));
            for (VirtualFile vf : cssFiles) {
                processFile(vf);
            }
        });

        recalcGlobalConstants();
        refreshEditorsAndRestart();
    }

    /**
     * Incrementally processes a single CSS file: parses variable definitions inside the `.root` block
     * and updates the internal cache accordingly.
     */
    public synchronized void processFile(@NotNull VirtualFile file) {
        if (!file.getName().endsWith(".css")) return;
        String text = getDocumentText(file);
        String rootBlock = extractRootBlock(text);
        if (rootBlock == null) {
            fileConstantsCache.remove(file);
            return;
        }
        Map<String, String> defs = parseRootDefinitions(file);
        fileConstantsCache.put(file, defs);
    }

    private void recalcGlobalConstants() {
        globalConstantMap.clear();
        for (Map<String, String> defs : fileConstantsCache.values()) {
            globalConstantMap.putAll(defs);
        }
    }

    /**
     * Parses variable definitions from the `.root` block of the given CSS file.
     * This method must be executed within a ReadAction.
     * <p>
     * The following built-in font-related properties are ignored:
     * - -fx-font
     * - -fx-font-family
     * - -fx-font-size
     * - -fx-font-weight
     * - -fx-font-style
     */
    private Map<String, String> parseRootDefinitions(@NotNull VirtualFile file) {
        return ReadAction.compute(() -> {
            Map<String, String> map = new HashMap<>();
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile == null) return map;
            psiFile.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof CssDeclaration decl) {
                        String propertyName = decl.getPropertyName();
                        if (!propertyName.startsWith("-")) return;
                        if (propertyName.equalsIgnoreCase("-fx-background-color") ||
                                propertyName.equalsIgnoreCase("-fx-font") ||
                                propertyName.equalsIgnoreCase("-fx-font-family") ||
                                propertyName.equalsIgnoreCase("-fx-font-size") ||
                                propertyName.equalsIgnoreCase("-fx-font-weight") ||
                                propertyName.equalsIgnoreCase("-fx-font-style")) {
                            return;
                        }
                        CssRuleset ruleset = PsiTreeUtil.getParentOfType(decl, CssRuleset.class);
                        if (ruleset == null) return;
                        String selectorsText = ruleset.getText();
                        if (selectorsText == null || !selectorsText.contains(".root")) return;
                        CssTermList valueList = decl.getValue();
                        if (valueList == null) return;
                        String rawValue = extractRawValue(valueList);
                        if (rawValue.isEmpty()) return;
                        String varName = propertyName.substring(1);
                        map.put(varName, rawValue);
                    }
                    super.visitElement(element);
                }
            });
            return map;
        });
    }

    private String extractRawValue(CssTermList termList) {
        StringBuilder sb = new StringBuilder();
        for (CssTerm term : termList.getTerms()) {
            sb.append(term.getText());
        }
        return sb.toString().trim();
    }

    /**
     * Retrieves the document text of the given file within a ReadAction.
     */
    private String getDocumentText(@NotNull VirtualFile file) {
        return ReadAction.compute(() -> {
            var document = FileDocumentManager.getInstance().getDocument(file);
            return document != null ? document.getText() : "";
        });
    }

    /**
     * Extracts the content of the `.root` block by locating the first pair of braces
     * following the ".root" selector. Includes the curly braces in the result.
     */
    private String extractRootBlock(String text) {
        int index = text.indexOf(".root");
        if (index == -1) return null;
        int braceStart = text.indexOf("{", index);
        if (braceStart == -1) return null;
        int braceEnd = text.indexOf("}", braceStart);
        if (braceEnd == -1) return null;
        return text.substring(braceStart, braceEnd + 1).trim();
    }

    /**
     * Returns an unmodifiable view of the global constant map.
     */
    public Map<String, String> getConstantMap() {
        return Collections.unmodifiableMap(globalConstantMap);
    }

    /**
     * Resolves variable aliases recursively, e.g., graphs-fill-1 -> -primary-500 -> #hexColor.
     * Returns null if the value cannot be resolved or if a circular reference is detected.
     */
    public String resolveConstantValue(@NotNull String name) {
        Set<String> visited = new HashSet<>();
        return doResolveConstantValue(name, visited);
    }

    private String doResolveConstantValue(@NotNull String name, @NotNull Set<String> visited) {
        if (!globalConstantMap.containsKey(name)) {
            return null;
        }
        if (!visited.add(name)) {
            return null;
        }
        String rawVal = globalConstantMap.get(name);
        if (rawVal.startsWith("-")) {
            return doResolveConstantValue(rawVal.substring(1), visited);
        }
        return rawVal;
    }

    /**
     * Registers a file listener.
     * Uses MessageBus and BulkFileListener instead of the deprecated addVirtualFileListener API.
     * When a `.css` file changes, and the content of its `.root` block is modified,
     * a debounced global re-scan is triggered.
     */
    public void registerFileListener() {
        final Map<VirtualFile, Alarm> fileAlarms = new HashMap<>();

        project.getMessageBus().connect(this).subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (com.intellij.openapi.vfs.newvfs.events.VFileEvent event : events) {
                    VirtualFile file = event.getFile();
                    if (file == null || !file.getName().endsWith(".css")) {
                        continue;
                    }

                    if (event instanceof com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent) {
                        String text = getDocumentText(file);
                        String newRootBlock = extractRootBlock(text);
                        if (newRootBlock == null) {
                            fileConstantsCache.remove(file);
                            debounceScan(file);
                            return;
                        }
                        debounceScan(file);
                    } else if (event instanceof com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent) {
                        String text = getDocumentText(file);
                        String newRootBlock = extractRootBlock(text);
                        if (newRootBlock != null) {
                            debounceScan(file);
                        }
                    } else if (event instanceof com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent) {
                        fileConstantsCache.remove(file);
                        scanAllCssFiles();
                    }
                }
            }

            private void debounceScan(VirtualFile file) {
                Alarm alarm = fileAlarms.computeIfAbsent(file, k -> new Alarm(Alarm.ThreadToUse.POOLED_THREAD, FxCssService.this));
                if (!alarm.isDisposed()) {
                    alarm.cancelAllRequests();
                    alarm.addRequest(() -> {
                        processFile(file);
                        recalcGlobalConstants();
                        refreshEditorsAndRestart();
                    }, DEBOUNCE_DELAY);
                }
            }
        });
    }

    /**
     * Commits all documents, repaints all open editors, and restarts the highlighting daemon.
     */
    private void refreshEditorsAndRestart() {
        ApplicationManager.getApplication().invokeLater(() -> {
            PsiDocumentManager.getInstance(project).commitAllDocuments();
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            for (var editor : fileEditorManager.getAllEditors()) {
                editor.getComponent().repaint();
            }
            DaemonCodeAnalyzer.getInstance(project).restart();
        });
    }

    @Override
    public void dispose() {

    }
}
