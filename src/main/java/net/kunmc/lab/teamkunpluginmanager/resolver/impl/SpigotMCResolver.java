package net.kunmc.lab.teamkunpluginmanager.resolver.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kunmc.lab.teamkunpluginmanager.resolver.interfaces.URLResolver;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ErrorResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.MultiResult;
import net.kunmc.lab.teamkunpluginmanager.resolver.result.ResolveResult;
import net.kunmc.lab.teamkunpluginmanager.utils.Pair;
import net.kunmc.lab.teamkunpluginmanager.utils.URLUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SpigotMCResolver implements URLResolver
{
    private static final Pattern PATTERN = Pattern.compile("/resources/([\\w-]+\\.?(?<resourceId>[\\d]+))(?:/|/updates/?|/downloads/?|(?:update/?\\?update=|download/?\\?version=)(?<version>\\d+))?");

    @Override
    public ResolveResult resolve(String query)
    {
        Matcher matcher = urlMatcher(PATTERN, query);

        String id = null;
        String version = null;

        while (matcher.find())
        {
            String idGroup = matcher.group("resourceId");
            String versionGroup = matcher.group("version");

            if (idGroup != null && !idGroup.isEmpty())
                id = idGroup;
            if (versionGroup != null && !versionGroup.isEmpty())
                version = versionGroup;
        }

        if (id == null)
            return new ErrorResult(ErrorResult.ErrorCause.INVALID_QUERY, ResolveResult.Source.SPIGOT_MC);

        String spigotAPIUrl = "https://apple.api.spiget.org/v2/resources/" + id;

        Pair<Integer, String> data = URLUtils.getAsString(spigotAPIUrl);


        ErrorResult mayError = processErrorResponse(data.getLeft(), ResolveResult.Source.SPIGOT_MC);

        if (mayError != null)
            return mayError;

        String json = data.getRight();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        return buildResult(jsonObject, version);
    }

    private ResolveResult buildResult(JsonObject jsonObject, @Nullable String version)
    {
        boolean external = jsonObject.get("external").getAsBoolean();
        if (external)
            return new ErrorResult(ErrorResult.ErrorCause.ASSET_NOT_FOUND.value("SpigotMC ではホストされていません。"),
                    ResolveResult.Source.SPIGOT_MC);

        String description = b64Decode(jsonObject.get("description").getAsString());
        String name = jsonObject.get("name").getAsString();
        List<String> testedVersions = StreamSupport.stream(jsonObject.get("testedVersions").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .collect(Collectors.toList());
        long id = jsonObject.get("id").getAsLong();

        boolean premium = jsonObject.get("premium").getAsBoolean();
        if (premium)
            return new ErrorResult(ErrorResult.ErrorCause.MATCH_PLUGIN_NOT_FOUND.value("このプラグインはプレミアムプラグインです。"),
                    ResolveResult.Source.SPIGOT_MC);

        long[] versions = StreamSupport.stream(jsonObject.get("versions").getAsJsonArray().spliterator(), false)
                .mapToLong(e -> e.getAsJsonObject().get("id").getAsLong())
                .toArray();


        if (versions.length == 0)
            return new ErrorResult(ErrorResult.ErrorCause.ASSET_NOT_FOUND, ResolveResult.Source.SPIGOT_MC);


        if (version == null)
        {
            List<SpigotMCSuccessResult> results = new ArrayList<>();
            for (long v : versions)
                results.add(new SpigotMCSuccessResult(String.valueOf(v), name, id, description, testedVersions));

            return new MultiResult(results.toArray(new SpigotMCSuccessResult[0]));
        }

        for (long v : versions)
            if (String.valueOf(v).equals(version))
                return new SpigotMCSuccessResult(version, name, id, description, testedVersions);

        return new ErrorResult(ErrorResult.ErrorCause.ASSET_NOT_FOUND.value("指定されたバージョンのプラグインが見つかりませんでした。"),
                ResolveResult.Source.SPIGOT_MC);
    }

    private String b64Decode(String str)
    {
        try
        {
            return new String(Base64.getDecoder().decode(str));
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("[SpigotMCResolver] SpigotMCから無効なレスポンスが返答されました。: " + e.getMessage());
            e.printStackTrace();

            return "Failed to decode Base64 string.";
        }
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        return autoPickFirst(multiResult, ResolveResult.Source.SPIGOT_MC);
    }

    @Override
    public String[] getHosts()
    {
        return new String[]{"spigotmc.org", "www.spigotmc.org"};
    }
}
