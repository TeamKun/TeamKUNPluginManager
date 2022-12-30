package net.kunmc.lab.kpm.upgrader.signals;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kunmc.lab.kpm.signal.Signal;
import net.kunmc.lab.kpm.versioning.Version;

@Data
@EqualsAndHashCode(callSuper = true)
public class KPMUpgradeReadySignal extends Signal
{
    private final Version currentKPMVersion;
    private final Version toKPMVersion;

    private boolean continueUpgrade;

}
