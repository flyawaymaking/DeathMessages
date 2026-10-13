package com.flyaway.deathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeathMessageFormatterTest {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Test
    void insertsComponentsWithTheirStyleAndHover() {
        Component killer = Component.text("[E] ", NamedTextColor.GREEN).append(Component.text("Bob"));
        HoverEvent<Component> tooltip = HoverEvent.showText(Component.text("Sharpness I"));
        Component weapon = Component.text("[ScandiPvP]", NamedTextColor.AQUA).hoverEvent(tooltip);

        Component message = DeathMessageFormatter.format(MINI_MESSAGE,
                "<gray>{player} was slain by {killer_display} using {weapon}",
                Map.of("player", Component.text("Alice"), "killer_display", killer, "weapon", weapon));

        assertEquals("Alice was slain by [E] Bob using [ScandiPvP]", plain(message));
        assertEquals(NamedTextColor.GREEN, find(message, "[E] Bob").color(), "killer keeps the team prefix color");
        Component insertedWeapon = find(message, "[ScandiPvP]");
        assertEquals(NamedTextColor.AQUA, insertedWeapon.color());
        assertEquals(tooltip, insertedWeapon.hoverEvent(), "weapon keeps its tooltip");
    }

    @Test
    void neverParsesPlaceholderTextAsMiniMessage() {
        Component message = DeathMessageFormatter.format(MINI_MESSAGE, "{weapon}",
                Map.of("weapon", Component.text("<red><click:run_command:/op me>x")));

        assertEquals("<red><click:run_command:/op me>x", plain(message));
    }

    @Test
    void leavesUnknownPlaceholdersUntouched() {
        Component message = DeathMessageFormatter.format(MINI_MESSAGE, "{player} hit {killer}",
                Map.of("player", Component.text("Alice")));

        assertEquals("Alice hit {killer}", plain(message));
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static Component find(Component component, String content) {
        if (component instanceof TextComponent text && text.content().equals(content)) {
            return component;
        }
        return component.children().stream()
                .map(child -> find(child, content))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
