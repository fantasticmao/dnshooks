package cn.fantasticmao.dnshooks.proxy.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ResolverUtil
 * <p>read the resolver configuration file</p>
 *
 * @author maomao
 * @since 2020/3/14
 */
public class ResolverUtil {
    private static String filePath = System.getProperty("resolv.conf", "/etc/resolv.conf");

    public static List<String> nameServer() {
        Path path = Paths.get(filePath);
        try {
            List<String> lines = Files.readAllLines(path);
            return lines.stream()
                .map(String::trim)
                .filter(line -> line.startsWith("nameserver"))
                .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
