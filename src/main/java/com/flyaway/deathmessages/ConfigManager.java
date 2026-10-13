package com.flyaway.deathmessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConfigManager {
    private final DeathMessages plugin;
    private FileConfiguration config;
    private final Random random = new Random();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, List<String>> deathMessages = new HashMap<>();
    private String prefix;
    private String deathPrefix;

    public ConfigManager(DeathMessages plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        FileConfiguration defaultConfig = loadDefaultConfig();
        int defaultVersion = defaultConfig.getInt("config-version", 0);
        int currentVersion = config.getInt("config-version", 0);

        if (currentVersion < defaultVersion) {
            plugin.getLogger().info("Updating config.yml from version " + currentVersion + " to " + defaultVersion);

            mergeSection(defaultConfig, config);
            config.set("config-version", defaultVersion);
            plugin.saveConfig();
        }

        this.prefix = config.getString("messages.prefix", "<gray>[<red>DeathMessages</red>]</gray>");
        this.deathPrefix = config.getString("death-messages.prefix", "<gray>");
        loadDeathMessages();
    }

    private FileConfiguration loadDefaultConfig() {
        YamlConfiguration defaultConfig = new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(plugin.getResource("config.yml")),
                StandardCharsets.UTF_8)) {
            defaultConfig.load(reader);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load default config.yml");
        }
        return defaultConfig;
    }

    private void mergeSection(ConfigurationSection source, ConfigurationSection target) {
        for (String key : source.getKeys(false)) {
            Object sourceValue = source.get(key);

            if (source.isConfigurationSection(key)) {
                ConfigurationSection sourceSub = source.getConfigurationSection(key);
                if (sourceSub == null) continue;

                ConfigurationSection targetSub = target.getConfigurationSection(key);
                if (targetSub == null) {
                    targetSub = target.createSection(key);
                }

                mergeSection(sourceSub, targetSub);

            } else {
                if (!target.isSet(key)) {
                    target.set(key, sourceValue);
                }
            }
        }
    }

    private void loadDeathMessages() {
        deathMessages.clear();
        ConfigurationSection deathSection = config.getConfigurationSection("death-messages");

        if (deathSection != null) {
            for (String key : deathSection.getKeys(false)) {
                List<String> messages = deathSection.getStringList(key);
                deathMessages.put(key, messages);
            }
        }

        plugin.getLogger().info("Loaded " + deathMessages.size() + " death messages types");
    }

    public @NotNull String getMessage(String key, Map<String, String> placeholders) {
        String message = plugin.getConfig().getString("messages." + key, "<red>message." + key + " not-found");
        return formatMessage(message, placeholders);
    }

    private @NotNull String formatMessage(String message, Map<String, String> placeholders) {
        if (message == null || message.isEmpty()) return "";
        message = message.replaceAll("\\s+$", "");

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return message;
    }

    public void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        sendRawMessage(sender, message);
    }

    public void sendMessage(CommandSender sender, String key) {
        sendMessage(sender, key, null);
    }

    public void sendRawMessage(CommandSender sender, String message) {
        if (message.isEmpty()) return;
        sender.sendMessage(miniMessage.deserialize(prefix + " » " + message));
    }

    public boolean hasDeathMessage(String deathType) {
        return deathMessages.containsKey(deathType) && !deathMessages.get(deathType).isEmpty();
    }

    private Component getPlayerNameComponent(Player player) {
        return switch (config.getString("settings.player-name-format", "name").toLowerCase(Locale.ROOT)) {
            case "display-name" -> player.displayName();
            case "team-display-name" -> player.teamDisplayName();
            default -> Component.text(player.getName());
        };
    }

    private boolean isWeapon(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        Material type = item.getType();
        String name = type.name();

        return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_SPEAR") || switch (type) {
            case BOW, CROSSBOW, TRIDENT, MACE -> true;
            default -> false;
        };
    }

    private boolean hasCustomName(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        return meta != null && meta.hasCustomName();
    }

    public Component getRandomDeathMessage(String deathType, Player player, Player killer) {
        ItemStack weapon = killer != null ? killer.getInventory().getItemInMainHand() : null;

        boolean weaponDeath = killer != null && (isWeapon(weapon) || hasCustomName(weapon));

        if (weaponDeath && deathType.equals("player") && hasDeathMessage("player-weapon")) {
            deathType = "player-weapon";
        }

        List<String> messages = deathMessages.get(deathType);
        String message = messages == null || messages.isEmpty() ? "<player> died" : messages.get(
                random.nextInt(messages.size()));

        message = message.replace("{player}", "<player>");

        List<TagResolver.Single> placeholders = new ArrayList<>();

        placeholders.add(Placeholder.component("player", getPlayerNameComponent(player)));

        if (killer != null) {
            message = message.replace("{killer}", "<killer>").replace("{killer_health}", "<killer_health>");

            placeholders.add(Placeholder.component("killer", getPlayerNameComponent(killer)));
            placeholders.add(
                    Placeholder.unparsed("killer_health", String.format(Locale.ROOT, "%.1f", killer.getHealth())));

            if (weaponDeath) {
                message = message.replace("{weapon}", "<weapon>");
                placeholders.add(Placeholder.component("weapon", weapon.displayName()));
            }
        }

        return miniMessage.deserialize(deathPrefix + message, placeholders.toArray(new TagResolver[0]));
    }

    public boolean showDeathCoordinates() {
        return config.getBoolean("settings.show-death-coordinates", true);
    }

    public boolean showBackButton() {
        return config.getBoolean("settings.show-back-button", true);
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadConfig();
    }
}
