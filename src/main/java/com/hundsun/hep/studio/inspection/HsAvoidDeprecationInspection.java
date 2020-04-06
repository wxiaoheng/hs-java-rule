package com.hundsun.hep.studio.inspection;

import com.hundsun.hep.studio.HsBaseInspection;
import com.hundsun.hep.studio.IHsDelegateInspection;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.deprecation.DeprecationInspection;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangxh
 * @date 2020年3月29日
 */
public class HsAvoidDeprecationInspection extends DeprecationInspection implements HsBaseInspection {

    public HsAvoidDeprecationInspection(){
        super();
    }

    public  HsAvoidDeprecationInspection(Object obj){
        this();
        if (obj instanceof IHsDelegateInspection){
            ((IHsDelegateInspection) obj).init(this);
        }
    }

    @Override
    public String ruleName() {
        return "HsAvoidDeprecation";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "不要使用过时的类或方法";
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

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return super.buildVisitor(new HsAvoidDeprecationInspectionHolder(holder, isOnTheFly), isOnTheFly);
    }

    private class HsAvoidDeprecationInspectionHolder extends ProblemsHolder{

        private ProblemsHolder holder;

        public HsAvoidDeprecationInspectionHolder(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
            super(holder.getManager(), holder.getFile(), isOnTheFly);
            this.holder = holder;
        }

        @Override
        public void registerProblem(PsiElement psiElement,
                                    @Nls String descriptionTemplate,
                                    LocalQuickFix... fixes) {
            holder.registerProblem(psiElement, getMessage(descriptionTemplate), fixes);
        }

        @Override
        public void registerProblem(PsiElement psiElement,
                                    @Nls String descriptionTemplate,
                                    ProblemHighlightType highlightType, LocalQuickFix... fixes) {
            holder.registerProblem(psiElement, getMessage(descriptionTemplate), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixes);
        }

        @Override
        public void registerProblem(PsiReference reference, String descriptionTemplate,
                                    ProblemHighlightType highlightType) {
            holder.registerProblem(reference, getMessage(descriptionTemplate), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }

        @Override
        public void registerProblemForReference(PsiReference reference,
                                                ProblemHighlightType highlightType, String descriptionTemplate,
                                                LocalQuickFix... fixes) {
            holder.registerProblemForReference(reference, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, getMessage(descriptionTemplate), fixes);
        }

        @Override
        public void registerProblem(PsiElement psiElement, TextRange rangeInElement,
                                    String message, LocalQuickFix... fixes) {
            holder.registerProblem(psiElement, getTextRange(psiElement, rangeInElement), getMessage(message), fixes);
        }

        @Override
        public void registerProblem(PsiElement psiElement, String message,
                                    ProblemHighlightType highlightType, TextRange rangeInElement,
                                    LocalQuickFix... fixes) {
            holder.registerProblem(psiElement, getMessage(message), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, getTextRange(psiElement, rangeInElement), fixes);
        }

        private String getMessage(String msg) {
            return msg.replace("is deprecated", "已经过时了").replace("Default constructor in", "默认构造函数")
                    .replace("Overrides deprecated method in", "重写了过时的方法") + " #loc";
        }

        private TextRange getTextRange(PsiElement psiElement, TextRange range){

            if (range != null){
                return range;
            }
            if (psiElement != null){
                return new TextRange(0, psiElement.getTextLength());
            }
            return null;

        }
    }
}
