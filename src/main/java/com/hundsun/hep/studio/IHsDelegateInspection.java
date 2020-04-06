package com.hundsun.hep.studio;

import com.intellij.codeInspection.LocalInspectionTool;

/**
 * @author wangxh
 * @date
 */
public interface IHsDelegateInspection {
    /**
     * 通过javaassist初始化
     * @param forJavassist
     */
    void init(LocalInspectionTool forJavassist);
}
