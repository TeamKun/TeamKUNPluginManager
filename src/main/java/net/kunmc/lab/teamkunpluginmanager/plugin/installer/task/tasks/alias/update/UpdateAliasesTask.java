package net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.alias.update;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import net.kunmc.lab.teamkunpluginmanager.KPMDaemon;
import net.kunmc.lab.teamkunpluginmanager.plugin.alias.AliasUpdater;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.InstallProgress;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.InstallTask;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.alias.update.signals.AliasUpdateSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.alias.update.signals.InvalidSourceSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.installer.task.tasks.alias.update.signals.SourcePreparedSignal;
import net.kunmc.lab.teamkunpluginmanager.plugin.signal.SignalHandleManager;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * エイリアスのアップデートを行うタスクです。
 */
public class UpdateAliasesTask extends InstallTask<UpdateAliasesArgument, UpdateAliasesResult>
{
    private final KPMDaemon daemon;

    private UpdateAliasesState status;

    public UpdateAliasesTask(@NotNull KPMDaemon daemon, @NotNull InstallProgress<?, ?> progress, @NotNull SignalHandleManager signalHandler)
    {
        super(progress, signalHandler);
        this.daemon = daemon;

        this.status = UpdateAliasesState.INITIALIZED;
    }

    @Override
    public @NotNull UpdateAliasesResult runTask(@NotNull UpdateAliasesArgument arguments)
    {
        HashMap<String, Pair<URL, Path>> sources = arguments.getSources();

        HashMap<String, Long> aliasesOfSources = new HashMap<>();
        long total = 0;

        boolean isWarn = false;
        boolean anySuccess = false;
        this.status = UpdateAliasesState.UPDATING;
        for (Map.Entry<String, Pair<URL, Path>> source : sources.entrySet())
        {
            String aliasName = source.getKey();
            URL url = source.getValue().getLeft();
            Path path = source.getValue().getRight();

            long statusOrError = this.updateAliasesFromSource(aliasName, url, path);

            if (statusOrError < 0)
            {
                isWarn = true;
                continue;
            }

            total += statusOrError;
            aliasesOfSources.put(aliasName, statusOrError);

            anySuccess = true;
        }


        return new UpdateAliasesResult(anySuccess, this.status,
                anySuccess ? null: UpdateAliasesErrorCause.ALL_UPDATE_FAILED, isWarn,
                total, aliasesOfSources
        );
    }

    private long updateAliasesFromSource(String sourceName, URL url, Path source)
    {
        AliasUpdater updater = this.daemon.getAliasProvider().createUpdater(
                sourceName, url.toString()
        );

        SourcePreparedSignal signal = new SourcePreparedSignal(sourceName, source, url.toString());
        this.postSignal(signal);
        if (!signal.isSkip())
            return -1;

        try (InputStream stream = Files.newInputStream(source);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
             JsonReader jsonReader = new JsonReader(reader))
        {
            jsonReader.beginObject();
            while (jsonReader.hasNext())
            {
                if (jsonReader.peek().equals(JsonToken.END_OBJECT))
                    break;

                String aliasName = jsonReader.nextName();
                String alias = jsonReader.nextString();

                AliasUpdateSignal updateSignal = new AliasUpdateSignal(
                        sourceName, url, aliasName, alias
                );
                this.postSignal(updateSignal);

                if (updateSignal.isSkip())
                    continue;

                updater.update(aliasName, alias);
            }

            updater.done();
        }
        catch (JsonSyntaxException e)
        {
            this.postSignal(new InvalidSourceSignal(sourceName, source,
                    url.toString(), InvalidSourceSignal.ErrorCause.SOURCE_FILE_MALFORMED
            ));
            return -1;
        }
        catch (Exception e)
        {
            this.postSignal(new InvalidSourceSignal(sourceName, source,
                    url.toString(), InvalidSourceSignal.ErrorCause.IO_ERROR
            ));
            e.printStackTrace();
            updater.cancel();
            return -1;
        }

        return updater.getAliasesCount();
    }

}
