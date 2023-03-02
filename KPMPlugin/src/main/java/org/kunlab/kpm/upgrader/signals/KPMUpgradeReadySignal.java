package org.kunlab.kpm.upgrader.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kunlab.kpm.signal.Signal;
import org.kunlab.kpm.versioning.Version;

@Data
@EqualsAndHashCode(callSuper = true)
public class KPMUpgradeReadySignal extends Signal
{
    private final Version currentKPMVersion;
    private final Version toKPMVersion;

    private boolean continueUpgrade;

}
