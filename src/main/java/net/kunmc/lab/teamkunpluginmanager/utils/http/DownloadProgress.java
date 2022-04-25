package net.kunmc.lab.teamkunpluginmanager.utils.http;

import lombok.Value;

@Value
public class DownloadProgress
{
    long totalSize;
    long downloaded;

    double percentage;
}
