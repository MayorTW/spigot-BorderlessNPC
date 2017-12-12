package tw.mayortw.blnpc.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.citizensnpcs.api.CitizensAPI;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TargetRule {

    private static class Rule {

        public static final String TYPE = "type";
        public static final String NAME = "name";
        public static final String PERM = "perm";
        public static final String PLAYER = "player";

        @SuppressWarnings("deprecation")
        public static boolean isValidRule(String type, String rule) {
            switch(type.toLowerCase()){
                case TYPE:
                    return rule.equals("player") || rule.equals("npc") ||
                        EntityType.fromName(rule) != null;
                case NAME:
                case PERM:
                case PLAYER:
                    return true;
                default:
                    return false;
            }
        }
    }

    private static final String TARGET_PATH = "target";
    private static final String EXCLUDE_PATH = "exclude";
    private static final String TYPE_PATH = "type";
    private static final String RULE_PATH = "rule";
    private static final String DURA_PATH = "until";

    private static TargetRule instance;

    private ConfigurationSection config;

    private TargetRule(ConfigurationSection config) {
        this.config = config;
    }

    public static void init(ConfigurationSection config) {
        instance = new TargetRule(config);
    }

    public static TargetRule getInstance() {
        return instance;
    }

    // Return true on sucess
    // dura can be null
    @SuppressWarnings("deprecation") // Spigot just loves to spam deprecation
    public boolean addTarget(String rule, Duration dura) {
        String[] t = rule.split(":");
        if(t.length >= 2 && Rule.isValidRule(t[0], t[1])) {

            LocalDateTime until = null;
            if(dura != null) {
                until = LocalDateTime.now().plus(dura);
            }

            if(t[0].equals(Rule.PLAYER)) { // Convert player name to uuid
                Player player = Bukkit.getPlayer(t[1]);
                if(player == null)
                    return false;
                t[1] = player.getUniqueId().toString();
                rule = t[0] + ":" + t[1];
            }

            ConfigurationSection section =
                config.createSection(TARGET_PATH + "." + rule.replaceAll("[\\.:]", "_"));

            section.set(TYPE_PATH, t[0]);
            section.set(RULE_PATH, t[1]);
            section.set(DURA_PATH, until == null ? null : until.toString());

            return true;
        }
        return false;
    }

    // Return true on sucess
    // dura can be null
    @SuppressWarnings("deprecation")
    public boolean addExclude(String rule, Duration dura) {
        String[] t = rule.split(":");
        if(t.length >= 2 && Rule.isValidRule(t[0], t[1])) {

            LocalDateTime until = null;
            if(dura != null) {
                until = LocalDateTime.now().plus(dura);
            }

            if(t[0].equals(Rule.PLAYER)) { // Convert player name to uuid
                Player player = Bukkit.getPlayer(t[1]);
                if(player == null)
                    return false;
                t[1] = player.getUniqueId().toString();
                rule = t[0] + ":" + t[1];
            }

            ConfigurationSection section =
                config.createSection(EXCLUDE_PATH + "." + rule.replaceAll("[\\.:]", "_"));

            section.set(TYPE_PATH, t[0]);
            section.set(RULE_PATH, t[1]);
            section.set(DURA_PATH, until == null ? null : until.toString());

            return true;
        }
        return false;
    }

    // Return true on sucess
    @SuppressWarnings("deprecation")
    public boolean delTarget(String rule) {
        String[] t = rule.split(":");
        if(t.length >= 2 && t[0].equals(Rule.PLAYER)) { // Convert player name to uuid
            Player player = Bukkit.getPlayer(t[1]);
            if(player == null) {
                t[1] = Bukkit.getOfflinePlayer(t[1]).getUniqueId().toString();
            } else {
                t[1] = player.getUniqueId().toString();
            }
            rule = t[0] + ":" + t[1];
        }

        String path = TARGET_PATH + "." + rule.replaceAll("[\\.:]", "_");
        if(config.contains(path)) {
            config.set(path, null);
            return true;
        }
        return false;
    }

    // Return true on sucess
    @SuppressWarnings("deprecation")
    public boolean delExclude(String rule) {
        String[] t = rule.split(":");
        if(t.length >= 2 && t[0].equals(Rule.PLAYER)) { // Convert player name to uuid
            Player player = Bukkit.getPlayer(t[1]);
            if(player == null) {
                t[1] = Bukkit.getOfflinePlayer(t[1]).getUniqueId().toString();
            } else {
                t[1] = player.getUniqueId().toString();
            }
            rule = t[0] + ":" + t[1];
        }

        String path = EXCLUDE_PATH + "." + rule.replaceAll("[\\.:]", "_");
        if(config.contains(path)) {
            config.set(path, null);
            return true;
        }
        return false;
    }

    // Return number of rules deleted
    public int clearTarget() {
        if(!config.contains(TARGET_PATH))
            return 0;

        int count = config.getConfigurationSection(TARGET_PATH).getValues(false).size();
        config.set(TARGET_PATH, null);
        return count;
    }

    // Return number of rules deleted
    public int clearExclude() {
        if(!config.contains(EXCLUDE_PATH))
            return 0;

        int count = config.getConfigurationSection(EXCLUDE_PATH).getValues(false).size();
        config.set(EXCLUDE_PATH, null);
        return count;
    }

    @SuppressWarnings("deprecation")
    public Map<String, String> getTargets() {
        Map<String, String> targets = new HashMap<>();
        ConfigurationSection rules = config.getConfigurationSection(TARGET_PATH);

        if(rules != null) {
            for(String key : rules.getKeys(false)) {
                String type = rules.getString(key + "." + TYPE_PATH);
                String rule = rules.getString(key + "." + RULE_PATH);

                if(type.equals(Rule.PLAYER)) { // Convert uuid to player name
                    Player player = Bukkit.getPlayer(UUID.fromString(rule));
                    if(player == null) {
                        rule = Bukkit.getOfflinePlayer(UUID.fromString(rule)).getName();
                    } else {
                        rule = player.getName();
                    }
                }
                targets.put(type + ":" + rule,
                        rules.getString(key + "." + DURA_PATH));
            }
        }
        return targets;
    }

    @SuppressWarnings("deprecation")
    public Map<String, String> getExcludes() {
        Map<String, String> excludes = new HashMap<>();
        ConfigurationSection rules = config.getConfigurationSection(EXCLUDE_PATH);

        if(rules != null) {
            for(String key : rules.getKeys(false)) {
                String type = rules.getString(key + "." + TYPE_PATH);
                String rule = rules.getString(key + "." + RULE_PATH);

                if(type.equals(Rule.PLAYER)) { // Convert uuid to player name
                    Player player = Bukkit.getPlayer(UUID.fromString(rule));
                    if(player == null) {
                        rule = Bukkit.getOfflinePlayer(UUID.fromString(rule)).getName();
                    } else {
                        rule = player.getName();
                    }
                }
                excludes.put(type + ":" + rule,
                        rules.getString(key + "." + DURA_PATH));
            }
        }
        return excludes;
    }

    public boolean isTarget(Entity entity) {
        ConfigurationSection rules;

        for(String check : new String[] {EXCLUDE_PATH, TARGET_PATH}) {
            rules = config.getConfigurationSection(check);
            if(rules != null) {
                for(String key : rules.getKeys(false)) {
                    if(checkRule(entity, rules.getConfigurationSection(key)))
                        return check.equals(TARGET_PATH);
                }
            }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean checkRule(Entity entity, ConfigurationSection section) {

        String type = section.getString(TYPE_PATH);
        String rule = section.getString(RULE_PATH);
        String time = section.getString(DURA_PATH);

        if(type == null || rule == null)
            return false;

        try {
            if(!checkTime(time)) {
                section.getParent().set(section.getName(), null);
                return false;
            }
        } catch(DateTimeException exc) {
            section.set(DURA_PATH, null);
        }

        switch(type.toLowerCase()) {
            case Rule.TYPE:
                return entity.getType() == EntityType.fromName(rule) ||
                    rule.equals("player") && entity.getType() == EntityType.PLAYER ||
                    rule.equals("npc") && CitizensAPI.getNPCRegistry().isNPC(entity);
            case Rule.NAME:
                return entity.getName().equals(rule);
            case Rule.PERM:
                return entity.isPermissionSet(rule);
            case Rule.PLAYER:
                return entity.getType() == EntityType.PLAYER &&
                    entity.getUniqueId().toString().equals(rule);
            default:
                return false;
        }
    }

    // Return true when time has not been reached or time is null
    private boolean checkTime(String time) {

        if(time == null)
            return true;

        LocalDateTime until = LocalDateTime.parse(time);

        return until.isAfter(LocalDateTime.now());
    }
}
