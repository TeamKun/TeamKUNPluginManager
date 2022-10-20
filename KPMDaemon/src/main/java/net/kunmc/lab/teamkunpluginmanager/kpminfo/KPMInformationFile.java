package net.kunmc.lab.teamkunpluginmanager.kpminfo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.teamkunpluginmanager.utils.versioning.Version;

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
    Version kpmVersion;

}
