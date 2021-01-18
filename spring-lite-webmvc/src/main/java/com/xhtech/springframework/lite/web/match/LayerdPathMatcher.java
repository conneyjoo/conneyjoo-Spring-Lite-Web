/*
 * Copyright 2019-2029 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xhtech.springframework.lite.web.match;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LayerdPathMatcher extends AntPathMatcher
{
    private static final Pattern VARIABLES_PATTERN = Pattern.compile("\\{([\\w\\-.]+.)\\}");

    private final Map<LayerPath, PatternLayer> patternLayers = new LinkedHashMap<>();

    private final Map<String, PatternLayer> patternLookup = new LinkedHashMap<>();

    private final List<MatchLayer> matches = new LinkedList<>();

    private PathSeparatorPatternCache pathSeparatorPatternCache;

    public LayerdPathMatcher()
    {
        matches.add(new PathMatchLayer());
        matches.add(new VariableMatchLayer());
        matches.add(new AsteriskMatchLayer());
        matches.add(new Asterisk2MatchLayer());

        pathSeparatorPatternCache = new PathSeparatorPatternCache(DEFAULT_PATH_SEPARATOR);
    }

    @Override
    public boolean match(String pattern, String path)
    {
        return doMatch(pattern, path, null) ? true : pattern != null && super.match(pattern, path);
    }

    public String match(String path, Map<String, String> variables)
    {
        PatternLayer patternLayer = getPattern(null, path, variables);
        return patternLayer != null ? patternLayer.pattern : null;
    }

    @Override
    public Map<String, String> extractUriTemplateVariables(String pattern, String path)
    {
        Map<String, String> variables = new LinkedHashMap<>();
        if (!doMatch(pattern, path, variables))
        {
            throw new IllegalStateException("Pattern \"" + pattern + "\" is not a match for \"" + path + "\"");
        }
        return variables;
    }

    protected boolean doMatch(String pattern, String path, Map<String, String> variables)
    {
        return getPattern(pattern, path, variables) != null;
    }

    @Override
    public String combine(String pattern1, String pattern2)
    {
        if (!StringUtils.hasText(pattern1) && !StringUtils.hasText(pattern2))
        {
            return "";
        }
        if (!StringUtils.hasText(pattern1))
        {
            return pattern2;
        }
        if (!StringUtils.hasText(pattern2))
        {
            return pattern1;
        }

        boolean pattern1ContainsUriVar = (pattern1.indexOf('{') != -1);
        if (!pattern1.equals(pattern2) && !pattern1ContainsUriVar && super.match(pattern1, pattern2))
        {
            return pattern2;
        }

        if (pattern1.endsWith(pathSeparatorPatternCache.getEndsOnWildCard()))
        {
            return concat(pattern1.substring(0, pattern1.length() - 2), pattern2);
        }

        if (pattern1.endsWith(pathSeparatorPatternCache.getEndsOnDoubleWildCard()))
        {
            return concat(pattern1, pattern2);
        }

        int starDotPos1 = pattern1.indexOf("*.");
        if (pattern1ContainsUriVar || starDotPos1 == -1) {
            return concat(pattern1, pattern2);
        }

        String ext1 = pattern1.substring(starDotPos1 + 1);
        int dotPos2 = pattern2.indexOf('.');
        String file2 = (dotPos2 == -1 ? pattern2 : pattern2.substring(0, dotPos2));
        String ext2 = (dotPos2 == -1 ? "" : pattern2.substring(dotPos2));
        boolean ext1All = (ext1.equals(".*") || ext1.equals(""));
        boolean ext2All = (ext2.equals(".*") || ext2.equals(""));
        if (!ext1All && !ext2All)
        {
            throw new IllegalArgumentException("Cannot combine patterns: " + pattern1 + " vs " + pattern2);
        }
        String ext = (ext1All ? ext2 : ext1);
        return file2 + ext;
    }

    private String concat(String path1, String path2)
    {
        boolean path1EndsWithSeparator = path1.endsWith(DEFAULT_PATH_SEPARATOR);
        boolean path2StartsWithSeparator = path2.startsWith(DEFAULT_PATH_SEPARATOR);

        if (path1EndsWithSeparator && path2StartsWithSeparator)
        {
            return path1 + path2.substring(1);
        }
        else if (path1EndsWithSeparator || path2StartsWithSeparator)
        {
            return path1 + path2;
        }
        else
        {
            return path1 + DEFAULT_PATH_SEPARATOR + path2;
        }
    }

    public PatternLayer getPattern(String pattern, String path)
    {
        return getPattern(pattern, path, null);
    }

    public PatternLayer getPattern(String pattern, String path, Map<String, String> variables)
    {
        if (pattern != null)
        {
            patternLookup.computeIfAbsent(pattern, (k) -> addPattern(k));
        }

        PatternLayer layer = patternLookup.get(path);
        LayerPath lookupPath = LayerPath.create(path);
        LayerPath lp = lookupPath;

        if (layer == null)
        {
            Map<LayerPath, PatternLayer> layers = patternLayers;

            for (; lp != null; lp = lp.next(), layers = (layer != null ? layer.subLayers : null))
            {
                if (layers == null || (layer = matching(layers, lp)) == null)
                {
                    return null;
                }

                if (lp.isLeaf())
                {
                    layer = layer.pattern != null ? layer : layer.subLayers.get(lp.asterisk2());
                }
            }
        }

        if (variables != null && layer != null && layer.pattern != null)
        {
            extractUriTemplateVariables(layer.variables, layer.path, lookupPath, variables);
        }

        return layer;
    }

    public PatternLayer addPattern(String pattern)
    {
        PatternLayer patternLayer = null;
        Map<LayerPath, PatternLayer> root = patternLayers;
        LayerPath layer = LayerPath.create(pattern);

        for (; layer != null; layer = layer.next())
        {
            patternLayer = root.computeIfAbsent(layer, k -> new PatternLayer(k));
            if (layer.isLeaf())
            {
                patternLayer.pattern = pattern;
                patternLayer.variables = new LinkedList<>();
                extractPatternVariables(pattern, patternLayer.variables);
            }
            root = patternLayer.subLayers;
        }

        return patternLayer;
    }

    private void extractPatternVariables(String pattern, List<String> variables)
    {
        Matcher matcher = VARIABLES_PATTERN.matcher(pattern);
        String name;

        while (matcher.find()) {
            name = matcher.group();
            variables.add(name.substring(1, name.length() - 1));
        }
    }

    private Map<String, String> extractUriTemplateVariables(List<String> variables, LayerPath patternPath, LayerPath lookupPath, Map<String, String> uriVariables)
    {
        if (variables.size() > 0)
        {
            LayerPath lp = patternPath.first();
            int index = 0;

            for (lp = lp.first(); lp != null && lookupPath != null; lp = lp.next(), lookupPath = lookupPath.next())
            {
                if (lp.isVariable())
                {
                    uriVariables.put(variables.get(index++), lookupPath.getPattern());
                }
            }

            return uriVariables;
        }

        return null;
    }

    private PatternLayer matching(Map<LayerPath, PatternLayer> patternLayers, LayerPath path)
    {
        PatternLayer patternLayer;
        for (MatchLayer m : matches)
        {
            if ((patternLayer = m.match(patternLayers, path)) != null)
            {
                return patternLayer;
            }
        }
        return null;
    }

    public static class PatternLayer
    {
        final LayerPath path;

        final Map<LayerPath, PatternLayer> subLayers;

        String pattern;

        List<String> variables;

        public PatternLayer(LayerPath path)
        {
            Assert.notNull(path, "Mapping must not be null");
            this.path = path;
            this.subLayers = new HashMap<>();
        }
    }

    interface MatchLayer
    {
        PatternLayer match(Map<LayerPath, PatternLayer> patternLookup, LayerPath path);
    }

    class PathMatchLayer implements MatchLayer
    {
        @Override
        public PatternLayer match(Map<LayerPath, PatternLayer> patternLookup, LayerPath path)
        {
            return patternLookup.get(path);
        }
    }

    class VariableMatchLayer implements MatchLayer
    {
        @Override
        public PatternLayer match(Map<LayerPath, PatternLayer> patternLookup, LayerPath path)
        {
            PatternLayer patternLayer;
            return (patternLayer = patternLookup.get(path.variable())) != null && conformWith(path, patternLayer) ? patternLayer : null;
        }

        public boolean conformWith(LayerPath lp, PatternLayer pl)
        {
            return ((lp.isLeaf() && pl.pattern != null) || (lp.next() != null && pl.subLayers.size() > 0 && matching(pl.subLayers, lp.next()) != null));
        }
    }

    class AsteriskMatchLayer extends VariableMatchLayer
    {
        @Override
        public PatternLayer match(Map<LayerPath, PatternLayer> patternLookup, LayerPath path)
        {
            PatternLayer patternLayer;
            return (patternLayer = patternLookup.get(path.asterisk())) != null && conformWith(path, patternLayer) ? patternLayer : null;
        }
    }

    class Asterisk2MatchLayer implements MatchLayer
    {
        @Override
        public PatternLayer match(Map<LayerPath, PatternLayer> patternLookup, LayerPath path)
        {
            PatternLayer patternLayer = patternLookup.get(path.asterisk2());
            LayerPath lp = path;

            if (patternLayer != null)
            {
                if (lp.isLeaf())
                {
                    return patternLayer;
                }

                for (; lp != null; lp = lp.next())
                {
                    if (matching(patternLayer.subLayers, lp) != null)
                    {
                        lp.next(lp.clone());
                        return patternLayer;
                    }
                }

                path.end();
                return patternLayer;
            }

            return null;
        }
    }

    private static class PathSeparatorPatternCache
    {
        private final String endsOnWildCard;

        private final String endsOnDoubleWildCard;

        public PathSeparatorPatternCache(String pathSeparator)
        {
            this.endsOnWildCard = pathSeparator + "*";
            this.endsOnDoubleWildCard = pathSeparator + "**";
        }

        public String getEndsOnWildCard() {
            return this.endsOnWildCard;
        }

        public String getEndsOnDoubleWildCard() {
            return this.endsOnDoubleWildCard;
        }
    }
}
