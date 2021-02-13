package net.kunmc.lab.teamkunpluginmanager.console.commands;

import com.g00fy2.versioncompare.Version;
import net.kunmc.lab.teamkunpluginmanager.common.known.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.common.utils.GitHubURLBuilder;
import net.kunmc.lab.teamkunpluginmanager.common.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.common.utils.URLUtils;
import net.kunmc.lab.teamkunpluginmanager.console.PluginManagerConsole;
import net.kunmc.lab.teamkunpluginmanager.console.Progress;
import net.kunmc.lab.teamkunpluginmanager.console.commands.stracture.CommandBase;
import net.kunmc.lab.teamkunpluginmanager.console.utils.PluginYamlParser;
import net.kunmc.lab.teamkunpluginmanager.spigot.plugin.DependencyTree;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class CommandInstall implements CommandBase
{

    private boolean withoutRemove;

    @Override
    public String getName()
    {
        return "install";
    }

    @Override
    public String[] getAliases()
    {
        return new String[]{"i"};
    }

    @Override
    public int run(String... args)
    {
        if (args.length == 0)
        {
            printHelp();
            return -1;
        }

        switch (args[0])
        {
            case "-h":
            case "--help":
                printHelp();
                break;
            default:
                for (String url : args)
                {
                    install(url);
                }
                break;
        }

        return 0;
    }

    @Override
    public void printHelp()
    {

    }

    private void install(String url)
    {
        int install = 0;
        int uninstall = 0;
        int change = 0;

        Progress progress = new Progress("既知プラグインデータベースの読み取り中").start();
        KnownPlugins.initialize(PluginManagerConsole.dataFolder.toFile(),
                PluginManagerConsole.config.getString("resolvePath"));
        progress.stop();

        if (!UrlValidator.getInstance().isValid(url))
        {
            // User/Repository 形式であれば先頭に GitHub のドメインを追加する。
            if (StringUtils.split(url, "/").length == 2)
            {
                url = "https://github.com/" + url;
                url = GitHubURLBuilder.urlValidate(url);

                // URL の指定が間違っていると終了。
                if (url.startsWith("ERROR "))
                {
                    System.out.println(Color.RED.format( "E: " + url.substring(6)));
                    System.out.println(getResultMessage(install, change, uninstall));
                    return;
                }
            }
            else if (KnownPlugins.isKnown(url))
            {
                url = KnownPlugins.getKnown(url).url;
            }
            else
            {
                System.out.println(Color.RED.format("E: " + url + "が見つかりません。"));
                System.out.println(getResultMessage(install, change, uninstall));
                return;
            }
        }

        // ダウンロード開始時間（ミリ秒）
        long downloadStartTime = System.currentTimeMillis();
        System.out.println(Color.PURPLE.format("ファイルのダウンロード中..."));

        Pair<Boolean, String> result = URLUtils.downloadFile(url);
        if (result.getValue().equals(""))
        {
            System.out.println(Color.RED.format("E: ファイルのダウンロードに失敗しました。"));
            System.out.println(getResultMessage(install, change, uninstall));
            return;
        }

        System.out.println(getChangeMessage(ChangeType.INSTALL, result.getValue()));
        install++;

        System.out.println(Color.GREEN.format((System.currentTimeMillis() - downloadStartTime) / 1000L + "秒で取得しました。"));
        System.out.println(Color.PURPLE.format("情報を読み込み中..."));

        PluginYamlParser description;

        try
        {
            description = new PluginYamlParser(new File("plugins/" + result.getValue()));
        }
        catch (NoSuchFileException e)
        {
            System.out.println(Color.RED.format("E: plugin.yml が見つかりませんでした。"));
            System.out.println(getResultMessage(install, change, uninstall));
            return;
        }
        catch (IOException e)
        {
            System.out.println(Color.RED.format("E: 情報を読み込めませんでした。"));
            System.out.println(getResultMessage(install, change, uninstall));
            return;
        }

        // プラグイン保護するとこ飛ばします。

        DependencyTree.Info info = DependencyTree.getInfo(description.name, false);

        if (info != null && new Version(info.version).isHigherThan(description.version))
        {
            install--;
            System.out.println(Color.YELLOW.format("W: 既に同じプラグインが存在します。"));
            if (!withoutRemove && new File("plugins/" + result.getValue()).exists())
            {
                try
                {
                    File f = new File("plugins/" + result.getValue());
                    f.setWritable(true);
                    f.delete();
                }
                catch (Exception e)
                { //getName => PeyaPeyaPlugin getFullName => PeyaPeyaPlugin:1.0
                    System.out.println(Color.RED.format("E: ファイルの削除に失敗しました: " + result.getValue()));
                    System.out.println(getResultMessage(install, change, uninstall));
                    return;
                }
            }
            System.out.println(Color.GREEN.format("S: " + description.name + ":" + description.version + "を正常にインストールされました"));
            System.out.println(getResultMessage(install, change, uninstall));
            return;
        }

        //added.add(new InstallResult(downloadResult.getValue(), description.getName(), add, remove, modify, true));

    }

    private Ansi getChangeMessage(ChangeType type, String name)
    {
        switch (type)
        {
            case INSTALL:
                return Color.GREEN.format("+ " + name);
            case CHANGE:
                return Color.YELLOW.format("~ " + name);
            case uninstall:
                return Color.RED.format("- " + name);
            default:
                return Color.PINK.format("? " + name);
        }
    }

    private Ansi getResultMessage(int install, int change, int uninstall)
    {
        return Color.GREEN.format("|@" + Color.GREEN.toString() + install + "追加|@ " + Color.YELLOW + change + "変更|@ " + Color.RED + uninstall + "削除");
    }

    private enum ChangeType
    {
        INSTALL,
        CHANGE,
        uninstall
    }

}
