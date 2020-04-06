package com.hundsun.hep.studio.inspection;

import com.hundsun.hep.studio.HsBaseInspection;
import com.hundsun.hep.studio.IHsDelegateInspection;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.impl.quickfix.RemoveUnusedVariableUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.accessStaticViaInstance.AccessStaticViaInstance;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author wangxh
 * @date
 */
public class HsAccessStaticViaInstanceInspection extends AccessStaticViaInstance implements HsBaseInspection {

    public HsAccessStaticViaInstanceInspection(){
        super();
    }

    public HsAccessStaticViaInstanceInspection(Object obj){
        this();
        if (obj instanceof IHsDelegateInspection){
            ((IHsDelegateInspection) obj).init(this);
        }
    }


    @Override
    public String ruleName() {
        return "HsAccessStaticViaInstance";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "静态方法或静态属性直接使用类名来访问1";
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

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
                checkAccessStaticMemberViaInstanceReference(expression, holder, isOnTheFly);
            }
        };
    }

    private void checkAccessStaticMemberViaInstanceReference(PsiReferenceExpression expr, ProblemsHolder holder, boolean onTheFly) {
        JavaResolveResult result = expr.advancedResolve(false);
        PsiElement resolved = result.getElement();
        if (!(resolved instanceof  PsiMember)){
            return;
        }
        PsiExpression qualifierExpression = expr.getQualifierExpression();
        if (qualifierExpression == null){
            return;
        }

        if (qualifierExpression instanceof PsiReferenceExpression) {
            PsiElement qualifierResolved = ((PsiReferenceExpression) qualifierExpression).resolve();
            if (qualifierResolved instanceof PsiClass || qualifierResolved instanceof PsiPackage) {
                return;
            }
        }
        if (!((PsiMember)resolved).hasModifierProperty(PsiModifier.STATIC)) {
            return;
        }

        String description = "静态方法或静态属性直接使用类名来访问";
        if (!onTheFly) {
            if (RemoveUnusedVariableUtil.checkSideEffects(qualifierExpression, null, new ArrayList<PsiElement>())) {
                holder.registerProblem(expr, description);
                return;
            }
        }
        holder.registerProblem(expr, description, createAccessStaticViaInstanceFix(expr, onTheFly, result));
    }
}
