package com.hundsun.hep.studio;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiFile;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleViolation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author wangxh
 * @date 2020年4月3日
 */
public class DelegatePmdInspection extends LocalInspectionTool implements HsBaseInspection {

    private String ruleName = "";

    public static final String SUFFIX = "Inspection";

    private Rule getRule(){
        if (org.apache.commons.lang.StringUtils.isBlank(ruleName)){
            String name = getClass().getSimpleName();
            ruleName = org.apache.commons.lang.StringUtils.substring(name, 0, name.length()-SUFFIX.length());
        }
        return HsPmdRuleManager.getManager().getRule(ruleName);
    }

    @Override
    public boolean runForWholeFile(){
        return true;
    }

    @Override
    public ProblemDescriptor[] checkFile(PsiFile file, InspectionManager manager, boolean isOnTheFly){
        HsPmdProcessor processor = new HsPmdProcessor(getRule());
        java.util.List<RuleViolation> violations = processor.processFile(file);
        Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        java.util.List<ProblemDescriptor> problems = new ArrayList<>();
        for (RuleViolation violation : violations){
            int beginLine = violation.getBeginLine();
            int endLine = violation.getEndLine();

            com.intellij.openapi.util.TextRange textRange = new com.intellij.openapi.util.TextRange(document.getLineStartOffset(beginLine), document.getLineEndOffset(endLine));
            ProblemDescriptor problem = manager.createProblemDescriptor(file, textRange, violation.getDescription(),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    isOnTheFly);
            problems.add(problem);
        }
        return problems.toArray(new ProblemDescriptor[]{});
    }

    @Nullable
    @Override
    public String getMainToolId() {
        return super.getMainToolId();
    }

    @Override
    public String getStaticDescription(){
        String desc = getRule().getDescription();
        if (StringUtils.isNotBlank(desc)){
            return desc;
        }
        return getDisplayName();
    }

    @Override
    public String ruleName(){
        return getRule().getName();
    }

    @Override
    public String getDisplayName(){
        return getRule().getMessage();
    }

    @Override
    public HighlightDisplayLevel getDefaultLevel(){
        RulePriority priority = getRule().getPriority();
        if (priority.equals(RulePriority.HIGH)){
            return HighlightDisplayLevel.ERROR;
        }else if (priority.equals(RulePriority.MEDIUM_HIGH)){
            return HighlightDisplayLevel.WARNING;
        }
        return HighlightDisplayLevel.WEAK_WARNING;
    }

    @Override
    public String getGroupDisplayName() {
        return "恒生规约";
    }

    @Override
    public boolean isEnabledByDefault(){
        return true;
    }

    @Override
    public String getShortName(){
        return getRule().getName();
    }

    @Override
    public LocalQuickFix manualBuildFix(com.intellij.psi.PsiElement psiElement, Boolean isOnTheFly) {
        return null;
    }

    @Override
    public boolean isSuppressedFor(com.intellij.psi.PsiElement element) {
        return false;
    }



}
