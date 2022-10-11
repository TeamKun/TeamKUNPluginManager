package net.kunmc.lab.teamkunpluginmanager.common.http;

import lombok.Value;

@Value
public class DownloadProgress
{
    long totalSize;
    long downloaded;

    double percentage;
}
