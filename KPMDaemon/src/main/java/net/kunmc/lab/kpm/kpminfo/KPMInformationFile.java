package net.kunmc.lab.kpm.kpminfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.kpm.hook.HookRecipientList;
import net.kunmc.lab.kpm.hook.KPMHookRecipient;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.utils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * KPMの情報ファイルを表すクラスです。
 */
@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class KPMInformationFile
{
    /**
     * 対応するKPMのバージョンです。
     * このバージョンより古いKPMではこの情報ファイルを読み込むことができません。
     * YAMLのキーは{@code kpm}です。
     */
    @NotNull
    Version kpmVersion;
    /**
     * アップデートで使用するクエリです。
     * YAMLのキーは{@code update}です。
     */
    @Nullable
    QueryContext updateQuery;
    /**
     * KPMフックを受け取る {@link KPMHookRecipient} のクラス名です。
     * KPMフックは、KPMがプラグインを読み込む際や、プラグインをアンロードする際に呼び出されるフックです。
     *
     * @see net.kunmc.lab.kpm.hook.KPMHook
     * @see KPMHookRecipient
     * @see net.kunmc.lab.kpm.hook.HookListener
     */
    @NotNull
    HookRecipientList hooks;
}
