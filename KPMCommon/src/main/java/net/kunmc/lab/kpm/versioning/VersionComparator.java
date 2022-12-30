package net.kunmc.lab.kpm.versioning;

import java.util.Comparator;

public class VersionComparator implements Comparator<Version>
{
    @Override
    public int compare(Version o1, Version o2)
    {
        return o1.compareTo(o2);
    }
}
