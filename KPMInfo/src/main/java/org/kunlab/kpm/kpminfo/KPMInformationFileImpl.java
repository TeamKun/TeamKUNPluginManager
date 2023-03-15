package org.kunlab.kpm.kpminfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.hook.interfaces.HookRecipientList;
import org.kunlab.kpm.resolver.QueryContext;
import org.kunlab.kpm.versioning.Version;

import java.util.Map;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class KPMInformationFileImpl implements KPMInformationFile
{
    @NotNull
    Version kpmVersion;
    @Nullable
    QueryContext updateQuery;
    @NotNull
    HookRecipientList hooks;
    @NotNull
    String[] recipes;
    @NotNull
    Map<String, QueryContext> dependencies;
    boolean allowManuallyInstall;
}
