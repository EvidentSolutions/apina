package fi.evident.apina.utils;

import java.io.*;
import java.nio.charset.Charset;

public final class ResourceUtils {

    private ResourceUtils() { }

    public static String readResourceAsString(String path, Charset charset) throws IOException {
        try (Reader reader = openResourceAsReader(path, charset)) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int n;

            while ((n = reader.read(buffer)) != -1)
                sb.append(buffer, 0, n);

            return sb.toString();
        }
    }

    public static Reader openResourceAsReader(String path, Charset charset) throws FileNotFoundException {
        return new InputStreamReader(openResourceAsStream(path), charset);
    }

    public static InputStream openResourceAsStream(String path) throws FileNotFoundException {
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        InputStream in = classLoader.getResourceAsStream(path);
        if (in != null)
            return in;
        else
            throw new FileNotFoundException("could not find classpath resource: " + path);
    }
}
