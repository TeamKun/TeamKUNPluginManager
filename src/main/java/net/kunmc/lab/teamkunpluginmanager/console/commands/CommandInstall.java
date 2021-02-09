package net.kunmc.lab.teamkunpluginmanager.console.commands;

import net.kunmc.lab.teamkunpluginmanager.common.known.KnownPlugins;
import net.kunmc.lab.teamkunpluginmanager.common.utils.GitHubURLBuilder;
import net.kunmc.lab.teamkunpluginmanager.common.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.common.utils.URLUtils;
import net.kunmc.lab.teamkunpluginmanager.console.commands.stracture.CommandBase;
import net.kunmc.lab.teamkunpluginmanager.console.utils.PluginYamlParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class CommandInstall implements CommandBase
{

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
        int delete = 0;
        int change = 0;

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
                    System.out.println(Color.RED + "E: " + url.substring(6));
                    System.out.println(getResultMessage(install, change, delete));
                    return;
                }
            }
            else if (KnownPlugins.isKnown(url))
            {
                url = KnownPlugins.getKnown(url).url;
            }
            else
            {
                System.out.println(Color.RED + "E: " + url + "が見つかりません。");
                System.out.println(getResultMessage(install, change, delete));
                return;
            }
        }

        // ダウンロード開始時間（ミリ秒）
        long downloadStartTime = System.currentTimeMillis();
        System.out.println(Color.PURPLE + "ファイルのダウンロード中...");

        Pair<Boolean, String> result = URLUtils.downloadFile(url);
        if (result.getValue().equals(""))
        {
            System.out.println(Color.RED + "E: ファイルのダウンロードに失敗しました。");
            System.out.println(getResultMessage(install, change, delete));
            return;
        }

        System.out.println(getChangeMessage(ChangeType.INSTALL, result.getValue()));
        install++;

        System.out.println(Color.GREEN.toString() + (System.currentTimeMillis() - downloadStartTime) / 1000L + "秒で取得しました。");
        System.out.println(Color.PURPLE + "情報を読み込み中...");

        PluginYamlParser description;

        try
        {
            description = new PluginYamlParser(new File("plugins/" + result.getValue()));
        }
        catch (NoSuchFileException e)
        {
            System.out.println(Color.RED + "E: plugin.yml が見つかりませんでした。");
            System.out.println(getResultMessage(install, change, delete));
            return;
        }
        catch (IOException e)
        {
            System.out.println(Color.RED + "E: 情報を呼び込めませんでした。");
            System.out.println(getResultMessage(install, change, delete));
            return;
        }

        // プラグイン保護するとこ飛ばします。


    }

    private String getChangeMessage(ChangeType type, String name)
    {
        switch (type)
        {
            case INSTALL:
                return Color.GREEN + "+ " + name;
            case CHANGE:
                return Color.YELLOW + "~ " + name;
            case DELETE:
                return Color.RED + "- " + name;
            default:
                return Color.PINK + "? " + name;
        }
    }

    private String getResultMessage(int install, int change, int delete)
    {
        return Color.GREEN.toString() + install + "追加" + Color.YELLOW + change + "変更" + Color.RED + delete + "削除";
    }

    private enum ChangeType
    {
        INSTALL,
        CHANGE,
        DELETE
    }

}
