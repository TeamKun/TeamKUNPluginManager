package net.kunmc.lab.kpm.kpminfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
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
}
