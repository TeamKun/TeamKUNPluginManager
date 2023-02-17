package org.kunlab.kpm.kpminfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.hook.HookListener;
import org.kunlab.kpm.interfaces.hook.HookRecipientList;
import org.kunlab.kpm.interfaces.hook.KPMHook;
import org.kunlab.kpm.interfaces.hook.KPMHookRecipient;
import org.kunlab.kpm.resolver.QueryContext;
import org.kunlab.kpm.versioning.Version;

import java.util.Map;

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
     * KPMフックを受け取る {@link KPMHookRecipient} のクラス名のリストを記述します。
     * KPMフックは、KPMがプラグインを読み込む際や、プラグインをアンロードする際に呼び出されるフックです。
     *
     * @serial hooks {@link HookRecipientList} KPMフックを受け取る {@link KPMHookRecipient} のリストです。
     * @see KPMHook
     * @see KPMHookRecipient
     * @see HookListener
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
    /**
     * 依存関係の解決クエリです。
     */
    @NotNull
    Map<String, QueryContext> dependencies;
    /**
     * 手動インストールできるプラグインかどうかを指定します。
     * {@code false} の場合、 KPM の UI を介して行われるインストールはキャンセルされます。
     */
    boolean allowManuallyInstall;
}
