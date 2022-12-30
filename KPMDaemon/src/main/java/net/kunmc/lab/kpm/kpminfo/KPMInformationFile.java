package net.kunmc.lab.kpm.kpminfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.kpm.hook.HookRecipientList;
import net.kunmc.lab.kpm.hook.KPMHookRecipient;
import net.kunmc.lab.kpm.resolver.QueryContext;
import net.kunmc.lab.kpm.versioning.Version;
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
     *
     * @serial kpm {@link Version} 対応するKPMのバージョンです。
     */
    @NotNull
    Version kpmVersion;
    /**
     * アップデートで使用するクエリです。
     *
     * @serial update {@link QueryContext} アップデートで使用するクエリです。
     */
    @Nullable
    QueryContext updateQuery;
    /**
     * KPMフックを受け取る {@link KPMHookRecipient} のクラス名です。
     * KPMフックは、KPMがプラグインを読み込む際や、プラグインをアンロードする際に呼び出されるフックです。
     *
     * @serial hooks {@link HookRecipientList} KPMフックを受け取る {@link KPMHookRecipient} のクラス名です。
     * @see net.kunmc.lab.kpm.hook.KPMHook
     * @see KPMHookRecipient
     * @see net.kunmc.lab.kpm.hook.HookListener
     */
    @NotNull
    HookRecipientList hooks;
    /**
     * プラグインが登録したレシピの名前空間または名前空間キーです。
     * これらは、プラグインが登録したレシピを識別し、アンインストール時に正常に削除するために使用されます。
     *
     * @serial recipes {@link String} プラグインが登録したレシピの名前空間または名前空間キーです。
     */
    @NotNull
    String[] recipes;
}
