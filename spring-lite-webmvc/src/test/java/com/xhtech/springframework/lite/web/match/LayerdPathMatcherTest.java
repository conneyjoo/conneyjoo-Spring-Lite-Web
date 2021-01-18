package com.xhtech.springframework.lite.web.match;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class LayerdPathMatcherTest
{
    @Test
    public void matchTest()
    {
        List<String> patterns = new LinkedList();
        List<String> paths = new LinkedList();

        patterns.add("/box/server/{userId}/download");
        patterns.add("/box/server/*/download1");
        patterns.add("/box/server/*/{userId}");
        patterns.add("/box/server/{appId}/{userId}");
        patterns.add("/box/server/download");
        patterns.add("/box/server/{value}");
        patterns.add("/box/server/**/file/download");
        patterns.add("/box/server/aaa/file/**/download/test");

        paths.add("/box/server/321/download");
        paths.add("/box/server/unkown/download1");
        paths.add("/box/server/unkown_123456/{userId}");
        paths.add("/box/server/SB103013/{userId}");
        paths.add("/box/server/download");
        paths.add("/box/server/{value}");
        paths.add("/box/server/aa/bb/cc/file/download");
        paths.add("/box/server/aaa/file/dd/ff/download/test");

        LayerdPathMatcher layerdPathMatcher = new LayerdPathMatcher();

        for (int i = 0, len = patterns.size(); i < len; i++)
        {
            Assert.assertTrue(layerdPathMatcher.match(patterns.get(i), paths.get(i)));
        }

        Assert.assertFalse(layerdPathMatcher.match(patterns.get(0), "/box/other/321/download"));
    }

    @Test
    public void matchTest2()
    {
        List<String> patterns = new LinkedList();
        List<String> paths = new LinkedList();

        patterns.add("/box/server/{userId}/download/kaka");

        paths.add("/box/server/321/download/kaka");

        LayerdPathMatcher layerdPathMatcher = new LayerdPathMatcher();

        for (int i = 0, len = patterns.size(); i < len; i++)
        {
            Assert.assertTrue(layerdPathMatcher.match(patterns.get(i), paths.get(i)));
        }

        Assert.assertFalse(layerdPathMatcher.match(null, "/box/server/123/download"));
    }
}
