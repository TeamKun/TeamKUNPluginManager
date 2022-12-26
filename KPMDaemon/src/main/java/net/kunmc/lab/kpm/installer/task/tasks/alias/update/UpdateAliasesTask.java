package net.kunmc.lab.kpm.installer.task.tasks.alias.update;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import net.kunmc.lab.kpm.KPMDaemon;
import net.kunmc.lab.kpm.alias.AliasUpdater;
import net.kunmc.lab.kpm.installer.AbstractInstaller;
import net.kunmc.lab.kpm.installer.task.InstallTask;
import net.kunmc.lab.kpm.installer.task.tasks.alias.update.signals.AliasUpdateSignal;
import net.kunmc.lab.kpm.installer.task.tasks.alias.update.signals.InvalidSourceSignal;
import net.kunmc.lab.kpm.installer.task.tasks.alias.update.signals.SourcePreparedSignal;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
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

    public UpdateAliasesTask(@NotNull AbstractInstaller<?, ?, ?> installer)
    {
        super(installer.getProgress(), installer.getProgress().getSignalHandler());
        this.daemon = installer.getDaemon();

        this.status = UpdateAliasesState.INITIALIZED;
    }

    @Override
    public @NotNull UpdateAliasesResult runTask(@NotNull UpdateAliasesArgument arguments)
    {
        HashMap<String, Pair<URI, Path>> sources = arguments.getSources();

        HashMap<String, Long> aliasesOfSources = new HashMap<>();
        long total = 0;

        boolean isWarn = false;
        boolean anySuccess = false;
        this.status = UpdateAliasesState.UPDATING;
        for (Map.Entry<String, Pair<URI, Path>> source : sources.entrySet())
        {
            String aliasName = source.getKey();
            URI uri = source.getValue().getLeft();
            Path path = source.getValue().getRight();

            long statusOrError = this.updateAliasesFromSource(aliasName, uri, path);

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

    private long updateAliasesFromSource(String sourceName, URI uri, Path source)
    {
        AliasUpdater updater = this.daemon.getAliasProvider().createUpdater(
                sourceName, uri.toString()
        );

        SourcePreparedSignal signal = new SourcePreparedSignal(sourceName, source, uri.toString());
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
                        sourceName, uri, aliasName, alias
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
                    uri.toString(), InvalidSourceSignal.ErrorCause.SOURCE_FILE_MALFORMED
            ));
            return -1;
        }
        catch (Exception e)
        {
            this.postSignal(new InvalidSourceSignal(sourceName, source,
                    uri.toString(), InvalidSourceSignal.ErrorCause.IO_ERROR
            ));
            e.printStackTrace();
            updater.cancel();
            return -1;
        }

        return updater.getAliasesCount();
    }

}
