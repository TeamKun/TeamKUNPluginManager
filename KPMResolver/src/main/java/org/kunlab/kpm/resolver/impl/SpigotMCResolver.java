package org.kunlab.kpm.resolver.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.http.HTTPResponse;
import org.kunlab.kpm.http.RequestContext;
import org.kunlab.kpm.http.Requests;
import org.kunlab.kpm.resolver.ErrorCause;
import org.kunlab.kpm.resolver.interfaces.QueryContext;
import org.kunlab.kpm.resolver.interfaces.URLResolver;
import org.kunlab.kpm.resolver.interfaces.result.ErrorResult;
import org.kunlab.kpm.resolver.interfaces.result.MultiResult;
import org.kunlab.kpm.resolver.interfaces.result.ResolveResult;
import org.kunlab.kpm.resolver.result.ErrorResultImpl;
import org.kunlab.kpm.resolver.result.MultiResultImpl;
import org.kunlab.kpm.resolver.utils.URLResolveUtil;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SpigotMCResolver implements URLResolver
{
    private static final Pattern PATTERN = Pattern.compile("/resources/([\\w-]+\\.?(?<resourceId>\\d+))(?:/|/updates/?|/downloads/?|(?:update/?\\?update=|download/?\\?version=)(?<version>\\d+))?");

    @Override
    public ResolveResult resolve(QueryContext query)
    {
        Matcher matcher = this.urlMatcher(PATTERN, query.getQuery());

        if (matcher == null)
            return new ErrorResultImpl(this, ErrorCause.INVALID_QUERY, ResolveResult.Source.SPIGOT_MC);

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
            return new ErrorResultImpl(this, ErrorCause.INVALID_QUERY, ResolveResult.Source.SPIGOT_MC);

        String spigotAPIUrl = "https://api.spiget.org/v2/resources/" + id;

        HTTPResponse data = Requests.request(RequestContext.builder()
                .url(spigotAPIUrl)
                .build());

        ErrorResult mayError = URLResolveUtil.processErrorResponse(this, data, ResolveResult.Source.SPIGOT_MC);

        if (mayError != null)
            return mayError;

        return this.buildResult(data.getAsJson().getAsJsonObject(), version);
    }

    private ResolveResult buildResult(JsonObject jsonObject, @Nullable String version)
    {
        boolean external = jsonObject.get("external").getAsBoolean();
        if (external)
            return new ErrorResultImpl(this, ErrorCause.ASSET_NOT_FOUND,
                    ResolveResult.Source.SPIGOT_MC
            );

        String description = this.b64Decode(jsonObject.get("description").getAsString());
        String name = jsonObject.get("name").getAsString();
        List<String> testedVersions = StreamSupport.stream(jsonObject.get("testedVersions").getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsString)
                .collect(Collectors.toList());
        long id = jsonObject.get("id").getAsLong();

        boolean premium = jsonObject.get("premium").getAsBoolean();
        if (premium)
            return new ErrorResultImpl(this, ErrorCause.ASSET_NOT_FOUND,
                    ResolveResult.Source.SPIGOT_MC,
                    "This plugin is marked as premium plugin."
            );

        long[] versions = StreamSupport.stream(jsonObject.get("versions").getAsJsonArray().spliterator(), false)
                .mapToLong(e -> e.getAsJsonObject().get("id").getAsLong())
                .toArray();


        if (versions.length == 0)
            return new ErrorResultImpl(this, ErrorCause.ASSET_NOT_FOUND, ResolveResult.Source.SPIGOT_MC);


        if (version == null)
        {
            return new MultiResultImpl(this, Arrays.stream(versions)
                    .mapToObj(v -> new SpigotMCSuccessResult(
                            this,
                            String.valueOf(v),
                            name,
                            id,
                            description,
                            testedVersions
                    ))
                    .toArray(SpigotMCSuccessResult[]::new)
            );
        }

        for (long v : versions)
            if (String.valueOf(v).equals(version))
                return new SpigotMCSuccessResult(this, version, name, id, description, testedVersions);

        return new ErrorResultImpl(this, ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.SPIGOT_MC,
                "Version " + version + " not found."
        );
    }

    private String b64Decode(String str)
    {
        try
        {
            return new String(Base64.getDecoder().decode(str));
        }
        catch (IllegalArgumentException e)
        {
            LoggerFactory.getLogger(SpigotMCResolver.class).error("Failed to decode Base64 string.", e);
            return "Failed to decode Base64 string.";
        }
    }

    @Override
    public ResolveResult autoPickOnePlugin(MultiResult multiResult)
    {
        ResolveResult[] results = multiResult.getResults();

        if (results.length == 0)
            return new ErrorResultImpl(this, ErrorCause.PLUGIN_NOT_FOUND, ResolveResult.Source.SPIGOT_MC);

        ResolveResult result = results[0];
        if (result instanceof MultiResult)
            return this.autoPickOnePlugin((MultiResult) result);

        return result;
    }

    @Override
    public String[] getHosts()
    {
        return new String[]{"spigotmc.org", "www.spigotmc.org"};
    }
}
