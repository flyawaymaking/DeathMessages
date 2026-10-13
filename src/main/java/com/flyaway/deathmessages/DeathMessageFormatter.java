package com.flyaway.deathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders a MiniMessage template whose {@code {name}} placeholders are inserted as components, so their
 * colors, hover and click events survive and their text is never parsed as MiniMessage.
 */
final class DeathMessageFormatter {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-z_]+)}");
    /** Keeps placeholder tags apart from the standard MiniMessage tags. */
    private static final String TAG_PREFIX = "dm_";

    private DeathMessageFormatter() {
    }

    static Component format(MiniMessage miniMessage, String template, Map<String, Component> placeholders) {
        TagResolver.Builder resolvers = TagResolver.builder();
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            Component value = placeholders.get(key);
            if (value == null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                continue;
            }
            String tag = TAG_PREFIX + key;
            resolvers.resolver(Placeholder.component(tag, value));
            matcher.appendReplacement(result, Matcher.quoteReplacement("<" + tag + ">"));
        }
        matcher.appendTail(result);
        return miniMessage.deserialize(result.toString(), resolvers.build());
    }
}
