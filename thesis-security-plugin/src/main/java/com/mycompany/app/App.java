package com.mycompany.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.pss.model.CveItem;
import com.pss.util.NvdDataFetcher;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        List<String> artifactIds = Arrays.asList("core", "portainer", "salt-netapi-client");
        Map<String, List<CveItem>> objects = NvdDataFetcher.fetchData(artifactIds);
        List<CveItem> test = objects.get("salt-netapi-client");
        test.forEach(item -> System.out.println(item.getCveId()));
    }
}
