package net.kunmc.lab.teamkunpluginmanager.console.utils;

import net.kunmc.lab.teamkunpluginmanager.common.DependencyTree;

import java.io.File;

public class Installer {

    public static void uninstall(String pluginName) {
        uninstall(pluginName, false);
    }

    public static void uninstall(String pluginName, boolean force) {
        System.out.println(Color.PURPLE + "依存関係ツリーを読み込み中...");

        DependencyTree.Info info = DependencyTree.getInfo(pluginName, false);
        if (info == null) {
            System.out.println(Color.RED + "E: プラグインが見つかりませんでした。");
            return;
        }

        if (new File(pluginName).exists()) {
            System.out.println(Color.RED + "E: プラグインが見つかりませんでした。");
            return;
        }

    }

}
