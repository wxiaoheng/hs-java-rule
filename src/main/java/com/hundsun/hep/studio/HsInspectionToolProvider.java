package com.hundsun.hep.studio;

import com.hundsun.hep.studio.inspection.HsAccessStaticViaInstanceInspection;
import com.hundsun.hep.studio.inspection.HsAvoidDeprecationInspection;
import com.hundsun.hep.studio.inspection.HsMapOrSetKeyShouldOverrideHashCodeEqualsInspection;
import com.hundsun.hep.studio.inspection.HsMissingOverrideInspection;
import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import javassist.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author wangxh
 * @date
 */
public class HsInspectionToolProvider implements InspectionToolProvider {

    private List<Class> classList = new ArrayList<Class>();

    private List<Class> local = new ArrayList<>();

    private static final Logger LOGGER = Logger.getLogger(HsInspectionToolProvider.class);

    public HsInspectionToolProvider(){
        local.add(HsMissingOverrideInspection.class);
        local.add(HsAccessStaticViaInstanceInspection.class);
        local.add(HsAvoidDeprecationInspection.class);
        local.add(HsMapOrSetKeyShouldOverrideHashCodeEqualsInspection.class);
    }

    @NotNull
    @Override
    public Class[] getInspectionClasses() {
        initPmdInspection();
        initLocalInspection();
        return classList.toArray(new Class[]{});
    }

    private void initLocalInspection() {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(DeleteLocalInspection.class));
        try {
            for (Class it : local){
                CtClass cc = pool.get(DeleteLocalInspection.class.getName());
                cc.setName("Delegate" + it.getSimpleName());
                CtField ctField = cc.getField("forJavassist");
                cc.removeField(ctField);
                CtClass itClass = pool.get(it.getName());
                CtClass toolClass = pool.get(LocalInspectionTool.class.getName());
                CtField newField = new CtField(toolClass, "forJavassist", cc);
                cc.addField(newField, CtField.Initializer.byNew(itClass));
                cc.writeFile("F://");
                classList.add(cc.toClass());
            }
        } catch (NotFoundException e) {
            LOGGER.error(e);
        } catch (CannotCompileException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initPmdInspection() {
        Set<String> rules = HsPmdRuleManager.getManager().getRules();
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(DelegatePmdInspection.class));
        try {
            for (String ruleName : rules) {
                CtClass cc = pool.get(DelegatePmdInspection.class.getName());
                cc.setName(ruleName+ DelegatePmdInspection.SUFFIX);
                CtField ctField = cc.getField("ruleName");
                cc.removeField(ctField);
                String value = "\"" + ruleName + "\"";
                CtField newField = CtField.make(String.format("private String ruleName = %s;", value), cc);
                cc.addField(newField, value);
                classList.add(cc.toClass());
            }

        } catch (NotFoundException e) {
            LOGGER.error(e);
        } catch (CannotCompileException e) {
            LOGGER.error(e);
        }
    }
}
