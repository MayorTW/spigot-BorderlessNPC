package tw.mayortw.blnpc.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class TargetRule {

    private static class Rule {

        public static final String TYPE = "type";
        public static final String NAME = "name";
        public static final String PERM = "perm";

        @SuppressWarnings("deprecation")
        public static boolean isValidRule(String type, String rule) {
            switch(type.toLowerCase()){
                case TYPE:
                    return rule.equals("player") ||
                        EntityType.fromName(rule) != null;
                case NAME:
                case PERM:
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

    private static ConfigurationSection config;

    public static void loadConfig(ConfigurationSection config) {
        TargetRule.config = config;
    }

    // Return true on sucess
    public static boolean addTarget(String rule) {
        String[] t = rule.split(":");
        if(Rule.isValidRule(t[0], t[1])) {
            ConfigurationSection section =
                config.createSection(TARGET_PATH + "." + rule.replaceAll("[\\.:]", "_"));

            section.set(TYPE_PATH, t[0]);
            section.set(RULE_PATH, t[1]);
            return true;
        }
        return false;
    }

    // Return true on sucess
    public static boolean addExclude(String rule) {
        String[] t = rule.split(":");
        if(Rule.isValidRule(t[0], t[1])) {
            ConfigurationSection section =
                config.createSection(EXCLUDE_PATH + "." + rule.replaceAll("[\\.:]", "_"));

            section.set(TYPE_PATH, t[0]);
            section.set(RULE_PATH, t[1]);
            return true;
        }
        return false;
    }

    // Return true on sucess
    public static boolean delTarget(String rule) {
        String path = TARGET_PATH + "." + rule.replaceAll("[\\.:]", "_");
        if(config.contains(path)) {
            config.set(path, null);
            return true;
        }
        return false;
    }

    // Return true on sucess
    public static boolean delExclude(String rule) {
        String path = EXCLUDE_PATH + "." + rule.replaceAll("[\\.:]", "_");
        if(config.contains(path)) {
            config.set(path, null);
            return true;
        }
        return false;
    }

    public static List<String> getTargets() {
        List<String> targets = new ArrayList<>();
        ConfigurationSection rules = (ConfigurationSection) config.get(TARGET_PATH);

        if(rules != null) {
            for(String key : rules.getKeys(false)) {
                targets.add(rules.getString(key + ".type") + ":" + rules.getString(key + ".rule"));
            }
        }
        return targets;
    }

    public static List<String> getExcludes() {
        List<String> excludes = new ArrayList<>();
        ConfigurationSection rules = (ConfigurationSection) config.get(EXCLUDE_PATH);

        if(rules != null) {
            for(String key : rules.getKeys(false)) {
                excludes.add(rules.getString(key + ".type") + ":" + rules.getString(key + ".rule"));
            }
        }
        return excludes;
    }

    public static boolean isTarget(Entity entity) {
        ConfigurationSection rules;

        for(String check : new String[] {EXCLUDE_PATH, TARGET_PATH}) {
            rules = (ConfigurationSection) config.get(check);
            if(rules != null) {
                for(String key : rules.getKeys(false)) {
                    if(checkRule(entity, rules.getString(key + ".type"), rules.getString(key + ".rule")))
                        return check.equals(TARGET_PATH);
                }
            }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private static boolean checkRule(Entity entity, String type, String rule) {

        if(type == null || rule == null)
            return false;

        switch(type.toLowerCase()) {
            case Rule.TYPE:
                return entity.getType() == EntityType.fromName(rule) ||
                    rule.equals("player") && entity.getType() == EntityType.PLAYER;
            case Rule.NAME:
                return entity.getName().equals(rule);
            case Rule.PERM:
                return entity.isPermissionSet(rule);
            default:
                return false;
        }
    }
}
