package com.hundsun.hep.studio.inspection;

import com.hundsun.hep.studio.HsBaseInspection;
import com.hundsun.hep.studio.IHsDelegateInspection;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.inheritance.MissingOverrideAnnotationInspection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangxh
 * @date
 */
public class HsMissingOverrideInspection extends MissingOverrideAnnotationInspection implements HsBaseInspection {

    public  HsMissingOverrideInspection(){
        super();
    }

    public HsMissingOverrideInspection(Object obj){
        this();
        if (obj instanceof IHsDelegateInspection){
            ((IHsDelegateInspection) obj).init(this);
        }
    }

    @Override
    public String ruleName() {
        return "MissingOverride";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "覆写方法必须加override注解";
    }

    @Nullable
    @Override
    public String getStaticDescription() {
        return getDisplayName();
    }

    @NotNull
    @Override
    public String getShortName() {
        return ruleName();
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    public LocalQuickFix manualBuildFix(PsiElement psiElement, Boolean isOnTheFly) {
        return null;
    }

    @Override
    public boolean runForWholeFile() {
        return true;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return super.getGroupDisplayName();
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new BaseInspectionVisitor() {
            @Override
            public void visitMethod(PsiMethod method) {
                if (method.getNameIdentifier() == null) {
                    return;
                }
                if (method.isConstructor()) {
                    return;
                }
                if (method.hasModifierProperty(PsiModifier.PRIVATE) || method.hasModifierProperty(PsiModifier.STATIC)) {
                    return;
                }
                PsiClass methodClass = method.getContainingClass();
                if (methodClass == null) {
                    return;
                }

                PsiMethod[] methods = method.findSuperMethods();

                if (!isMethodOverride(method, methodClass)){
                    return;
                }

                if (hasOverrideAnnotation(method)) {
                    return;
                }
                holder.registerProblem(method, getDisplayName());
            }
        };
    }

        private boolean isMethodOverride(PsiMethod method, PsiClass methodClass) {
            PsiMethod[] superMethods = method.findSuperMethods();
            boolean hasSupers = false;
            for (PsiMethod superMethod : superMethods) {
                PsiClass superClass = superMethod.getContainingClass();
                if (!InheritanceUtil.isInheritorOrSelf(methodClass, superClass, true)) {
                    continue;
                }
                hasSupers = true;
                if (!superMethod.hasModifierProperty(PsiModifier.PROTECTED)) {
                    return true;
                }
            }
            return hasSupers && !methodClass.isInterface();
        }

    private boolean hasOverrideAnnotation(PsiModifierListOwner element) {
        PsiModifierList modifierList = element.getModifierList();
        if (modifierList == null){
            return false;
        }
        return modifierList.findAnnotation(CommonClassNames.JAVA_LANG_OVERRIDE) != null;
    }

}
