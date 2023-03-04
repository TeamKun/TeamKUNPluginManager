package org.kunlab.kpm.installer.impls.update.signals;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.kpm.signal.Signal;

import java.util.Map;

/**
 * エイリアスのアップデートが完了したことを示すシグナルです。
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class UpdateFinishedSignal extends Signal
{
    /**
     * エイリアスの数です。
     */
    long aliases;
    /**
     * エイリアスとソースのペアの数です。
     */
    Map<String, Long> aliasesBySources;
}
