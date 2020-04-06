package com.hundsun.hep.studio;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiFile;

/**
 * @author wangxh
 * @date
 */
public interface HsBaseInspection {

    /**
     * ruleName

     * @return ruleName
     */
    String ruleName();

    /**
     * display info for inspection

     * @return display
     */
    String getDisplayName();

    /**
     * group display info for inspection

     * @return group display
     */
    String getGroupDisplayName();

    /**
     * inspection enable by default

     * @return true -> enable
     */
    boolean isEnabledByDefault();

    /**
     * default inspection level

     * @return level
     */
    HighlightDisplayLevel getDefaultLevel();

    /**
     * inspection short name

     * @return shor name
     */
    String getShortName();

    /**
     * quickfix
     * @param psiElement
     * @param isOnTheFly
     * @return
     */
    LocalQuickFix manualBuildFix(com.intellij.psi.PsiElement psiElement, Boolean isOnTheFly);

    /**
     * 解析文件
     * @param psiFile
     * @param manager
     * @param start
     * @param end
     * @return
     */
    default com.intellij.psi.PsiElement manualParsePsiElement(PsiFile psiFile, InspectionManager manager,
                                                              int start, int end) {
        return psiFile.findElementAt(start);
    }

    String GROUP_NAME = "Ali-Check";
}
