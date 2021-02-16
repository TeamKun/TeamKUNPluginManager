package net.kunmc.lab.teamkunpluginmanager.common.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;

public class FileUploadUtil
{
    private static final String PROVIDER_ = "https://file.io/?expires=1d";
    private static final String PARAM_ = "file";
    private static final String METHOD_ = "POST";

    public static Optional<String> uploadFile(File f)
    {
        if (!f.exists())
            return Optional.empty();

        try(FileInputStream stream = new FileInputStream(f))
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(PROVIDER_).openConnection();
            connection.setRequestMethod(METHOD_);
            connection.setRequestProperty("User-Agent", "Mozilla/8.10; Safari/Chrome/Opera/Edge/KungleBot-Peyang; Mobile-Desktop");
            final UUID boundary = UUID.randomUUID();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setDoOutput(true);
            connection.connect();
            try(OutputStream out = connection.getOutputStream())
            {
                out.write(("--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"" + PARAM_ + "\"; " +
                        "filename=\"" + f.getName() + "\"\r\n" +
                        "Content-Type: application/octet-stream" + "\r\n\r\n")
                        .getBytes(StandardCharsets.UTF_8)
                );

                byte[] buffer = new byte[1024];
                int size = -1;
                while((size = stream.read(buffer)) != -1)
                    out.write(buffer, 0, size);
                out.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
                if (connection.getResponseCode() != 200)
                    return Optional.empty();
                return Optional.of(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static String getFileSizeString(long bytes)
    {
        String suffix = "B";

        BigDecimal dec = new BigDecimal(String.valueOf(bytes));
        BigDecimal div = new BigDecimal(1024);
        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "KiB";
        }

        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "MiB";
        }

        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "GiB";
        }

        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "TiB";
        }


        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "PiB";
        }


        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "EiB";
        }


        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "ZiB";
        }


        if (dec.compareTo(div) >= 0)
        {
            dec = dec.divide(div);
            suffix = "YiB";
        }

        dec = dec.setScale(3, BigDecimal.ROUND_HALF_UP);


        return new DecimalFormat("#,###.##;#,###.##").format(dec) + suffix;
    }
}
