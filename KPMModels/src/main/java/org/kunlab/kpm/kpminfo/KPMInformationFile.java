package org.kunlab.kpm.kpminfo;

import org.kunlab.kpm.hook.HookListener;
import org.kunlab.kpm.hook.interfaces.HookRecipientList;
import org.kunlab.kpm.hook.interfaces.KPMHook;
import org.kunlab.kpm.hook.interfaces.KPMHookRecipient;
import org.kunlab.kpm.resolver.interfaces.QueryContext;
import org.kunlab.kpm.versioning.Version;

import java.util.Map;

/**
 * KPMの情報ファイルを表すクラスです。
 */
public interface KPMInformationFile
{
    /**
     * 対応するKPMのバージョンです。
     * このバージョンより古いKPMではこの情報ファイルを読み込めません。
     *
     * @return 対応するKPMのバージョンです。
     */
    Version getKpmVersion();

    /**
     * アップデートで使用するクエリです。
     *
     * @return アップデートで使用するクエリです。
     */

    QueryContext getUpdateQuery();

    /**
     * KPMフックを受け取る {@link KPMHookRecipient} のクラス名のリストを記述します。
     * KPMフックは、KPMがプラグインを読み込む際や、プラグインをアンロードする際に呼び出されるフックです。
     *
     * @return KPMフックを受け取る {@link KPMHookRecipient} のクラス名のリストを記述します。
     * @see KPMHook
     * @see KPMHookRecipient
     * @see HookListener
     */
    HookRecipientList getHooks();

    /**
     * プラグインが登録したレシピの名前空間または名前空間キーです。
     * これらは、プラグインが登録したレシピを識別し、アンインストール時に正常に削除するために使用されます。
     *
     * @return プラグインが登録したレシピの名前空間または名前空間キーです。
     */
    String[] getRecipes();

    /**
     * 依存関係の解決クエリです。
     *
     * @return 依存関係の解決クエリです。
     */
    Map<String, QueryContext> getDependencies();

    /**
     * 手動インストールできるプラグインかどうかを指定します。
     * {@code false} の場合、 KPM の UI を介して行われるインストールはキャンセルされます。
     *
     * @return 手動インストールできるプラグインかどうかを指定します。
     */
    boolean isAllowManuallyInstall();
}
