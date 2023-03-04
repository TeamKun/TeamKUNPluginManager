package org.kunlab.kpm;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KPM のデバッグ状態の定数です。
 */
@Getter
public class DebugConstants
{
    /**
     * KPM がデバッグモードが有効かどうかを示します。これを {@code true} にすると、他の全てのデバッグ項目が有効になります。
     * VM プロパティの {@code kpm.enable-debug} が {@code true} である場合に有効化されます。
     */
    public static final boolean DEBUG_MODE;
    /**
     * 不必要な KPM のアップグレードを許可するかどうかを示します。
     * デバッグモードが有効か、VM プロパティの {@code kpm.debug.allow-unneeded-upgrade} が {@code true} である場合に有効化されます。
     */
    public static final boolean ALLOW_UNNEEDED_UPGRADE;
    /**
     * データベースのコネクションを出力するかどうかを示します。
     * デバッグモードが有効か、VM プロパティの {@code kpm.kpm.debug.db.trace-connection} が {@code true} である場合に有効化されます。
     */
    public static final boolean DB_CONNECTION_TRACE;
    /**
     * HTTP リクエストの詳細を出力するかどうかを示します。
     * デバッグモードが有効か、VM プロパティの {@code kpm.debug.http.trace-request} が {@code true} である場合に有効化されます。
     */
    public static final boolean HTTP_REQUEST_TRACE;
    /**
     * HTTP リクエストのリダイレクトを出力するかどうかを示します。
     * デバッグモードが有効か、{@link #HTTP_REQUEST_TRACE} が有効か、VM プロパティの {@code kpm.debug.http.trace-redirect} が {@code true} である場合に有効化されます。
     */
    public static final boolean HTTP_REDIRECT_TRACE;
    /**
     * プラグインメタデータの操作を出力するかどうかを示します。
     * デバッグモードが有効か、VM プロパティの {@code kpm.debug.plugin-meta.trace-operation} が {@code true} である場合に有効化されます。
     */
    public static final boolean PLUGIN_META_OPERATION_TRACE;
    /**
     * 依存関係ツリーの操作を出力するかどうかを示します。
     * デバッグモードが有効か、VM プロパティの {@code kpm.debug.plugin-meta.trace-dependency-tree} が {@code true} である場合に有効化されます。
     */
    public static final boolean PLUGIN_META_DEPENDENCY_TREE_TRACE;
    /**
     * メッセージの出力時に、翻訳キーを出力するかどうかを示します。
     * デバッグモードが有効か、VM プロパティの {@code kpm.debug.raw-message} が {@code true} である場合に有効化されます。
     */
    public static final boolean RAW_MESSAGE;

    private static final Logger LOGGER;

    static
    {
        LOGGER = LoggerFactory.getLogger(DebugConstants.class);

        DEBUG_MODE = Boolean.getBoolean("kpm.enable-debug");

        ALLOW_UNNEEDED_UPGRADE = DEBUG_MODE || Boolean.getBoolean("kpm.debug.allow-unneeded-upgrade");

        DB_CONNECTION_TRACE = DEBUG_MODE || Boolean.getBoolean("kpm.debug.db.trace-connection");

        HTTP_REQUEST_TRACE = DEBUG_MODE || Boolean.getBoolean("kpm.debug.http.trace-request");
        HTTP_REDIRECT_TRACE = HTTP_REQUEST_TRACE || Boolean.getBoolean("kpm.debug.http.trace-redirect");

        PLUGIN_META_OPERATION_TRACE = DEBUG_MODE || Boolean.getBoolean("kpm.debug.plugin-meta.trace-operation");
        PLUGIN_META_DEPENDENCY_TREE_TRACE = DEBUG_MODE || Boolean.getBoolean("kpm.debug.plugin-meta.trace-dependency-tree");

        RAW_MESSAGE = DEBUG_MODE || Boolean.getBoolean("kpm.debug.raw-message");

    }

    public static Logger getLogger()
    {
        return LOGGER;
    }

    public static void debugLog(String message, boolean flag)
    {
        if (flag)
            LOGGER.info(message);
    }

    public static void debugLog(String message)
    {
        LOGGER.info(message);
    }

    public static void onException(Exception e)
    {
        LOGGER.error("An exception occurred", e);
    }
}
