package org.kunlab.kpm.task.tasks.alias.update.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * ソースファイルが不正だった場合に送信されるシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class InvalidSourceSignal extends SourceSignal
{
    /**
     * エラーの原因です。
     */
    ErrorCause errorCause;

    public InvalidSourceSignal(@NotNull String sourceName, @NotNull Path sourcePath, @Nullable String sourceURL, ErrorCause errorCause)
    {
        super(sourceName, sourcePath, sourceURL);
        this.errorCause = errorCause;
    }

    /**
     * エラーの理由です。
     */
    public enum ErrorCause
    {
        /**
         * ソースファイルが不正な形式です。
         */
        SOURCE_FILE_MALFORMED,
        /**
         * I/Oエラーが発生しました。
         */
        IO_ERROR,
    }
}
