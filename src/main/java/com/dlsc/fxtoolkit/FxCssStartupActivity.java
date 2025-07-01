package com.dlsc.fxtoolkit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class FxCssStartupActivity implements ProjectActivity {

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        // Write initialization logic here, such as scanning CSS files, registering listeners, etc.
        FxCssService service = FxCssService.getInstance(project);
        service.scanAllCssFiles();
        service.registerFileListener();
        return CompletableFuture.completedFuture(null);
    }

}
