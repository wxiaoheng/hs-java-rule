package com.hundsun.hep.studio;

import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author wangxh
 * @date
 */
public class HsPmdRuleManager {
    private static HsPmdRuleManager manager;
    private static final Logger LOGGER = Logger.getLogger(HsPmdRuleManager.class);

    private Map<String, Rule> map = new HashMap<>();

    public static HsPmdRuleManager getManager() {
        if (manager == null){
            manager = new HsPmdRuleManager();
        }
        return manager;
    }

    private HsPmdRuleManager(){
        String ruleSetName = "java/hs-pmd";
        RuleSetFactory factory = new RuleSetFactory();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(HsPmdRuleManager.class.getClassLoader());
        try {
            RuleSet ruleSet = factory.createRuleSet(ruleSetName.replace("/", "-"));
            for (Rule rule : ruleSet.getRules()){
                map.put(rule.getName(), rule);
            }
        } catch (RuleSetNotFoundException e) {
            LOGGER.error(String.format("rule set %s not found for", ruleSetName));
        }finally {
            // Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public Rule getRule(String name){
        if (map.containsKey(name)){
            return map.get(name);
        }
        return null;
    }

    public Set<String> getRules(){
        return map.keySet();
    }
}
