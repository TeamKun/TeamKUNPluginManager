package org.kunlab.kpm;

/**
 * KPM 内部で発生した例外を全てキャッチする機能のインターフェースです。
 */
public interface ExceptionHandler
{
    /**
     * 例外をキャッチします。
     *
     * @param e 例外
     * @return 例外を処理したかどうかです。処理できなかった場合は, RuntimeException が投げられます。
     */
    boolean on(Throwable e);
}
