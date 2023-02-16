package org.kunlab.kpm.interfaces.hook;

import org.kunlab.kpm.interfaces.KPMRegistry;

import java.lang.reflect.Method;

/**
 * KPMフックを受け取るためのインターフェースです。
 * このクラスを継承し、KPMフックを受け取るクラスであることを宣言します。
 */
public interface KPMHookRecipient
{
    /**
     * KPMフックを受け取るメソッドを取得します。
     *
     * @param hook 受け取るKPMフックのクラス
     * @return 受け取るメソッド
     */
    Method getHookListener(Class<? extends KPMHook> hook);

    /**
     * KPMデーモンのインスタンスです。
     */
    KPMRegistry getRegistry();
}
