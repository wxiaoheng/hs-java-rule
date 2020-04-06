package com.hundsun.hep.studio;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;

/**
 * @author wangxh
 * @date
 */
public class DeleteLocalInspection extends LocalInspectionTool implements HsBaseInspection,IHsDelegateInspection{

    private LocalInspectionTool forJavassist = null;

    private LocalInspectionTool localInspectionTool;

    @Override
    public void init(LocalInspectionTool forJavassist){
        this.forJavassist = forJavassist;
        if (this.forJavassist != null){
            localInspectionTool = this.forJavassist;
        }else {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean runForWholeFile() {
        return localInspectionTool.runForWholeFile();
    }

    @Override
    public ProblemDescriptor[] checkFile(PsiFile file, InspectionManager manager, boolean isOnTheFly) {
        return localInspectionTool.checkFile(file, manager, isOnTheFly);
    }

    @Override
    public String getStaticDescription(){
        return localInspectionTool.getStaticDescription();
    }

    @Override
    public String ruleName() {
        return ((HsBaseInspection)localInspectionTool).ruleName();
    }

    @Override
    public String getDisplayName() {
        return localInspectionTool.getDisplayName();
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    @Nls
    public String getGroupDisplayName(){
        return "恒生规约";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public String getShortName() {
        return localInspectionTool.getShortName();
    }

    @Override
    public LocalQuickFix manualBuildFix(PsiElement psiElement, Boolean isOnTheFly) {
        return null;
    }

    @Override
    public boolean isSuppressedFor(PsiElement element) {
        return false;
    }

    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder holder, boolean isOnTheFly, LocalInspectionToolSession session) {
        return localInspectionTool.buildVisitor(holder, isOnTheFly, session);
    }
}
