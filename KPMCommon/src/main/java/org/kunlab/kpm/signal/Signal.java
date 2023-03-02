package org.kunlab.kpm.signal;

import lombok.Getter;
import lombok.Setter;

/**
 * インストーラやタスクなどからスローされるシグナルです。
 * シグナルは、主に次のことに使用されます：
 * <ul>
 *     <li>インストールの進捗状況のリアルタイム通知</li>
 *     <li>インストール中に発生した予期しないエラーの通知</li>
 *     <li>インストーラがユーザに選択を求める</li>
 * </ul>
 * <p>
 * このシグナルは、フロントエンドとバックエンドの隔離のために作成されました。
 */
@Getter
@Setter
public abstract class Signal
{
    /**
     * シグナルがハンドルされたかどうかを示すフラグです。
     * このフラグが {@code true} の場合, ハンドルマネージャはこれ以降のハンドラを呼び出しません。
     */
    private boolean handled;
}
