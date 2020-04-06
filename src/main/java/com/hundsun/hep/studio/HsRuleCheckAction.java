package com.hundsun.hep.studio;

import com.google.common.collect.Sets;
import com.hundsun.jres.studio.lang.java.util.NumberConstants;
import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.AnalysisUIOptions;
import com.intellij.analysis.BaseAnalysisActionDialog;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.codeInspection.ex.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.BaseInspectionProfileManager;
import com.intellij.profile.codeInspection.InspectionProfileManager;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author wangxh
 * @date 2020年3月30日
 */
public class HsRuleCheckAction extends AnAction {

    private Logger logger = Logger.getLogger(HsRuleCheckAction.class);
     @Override
    public void actionPerformed(AnActionEvent e) {
        com.intellij.openapi.project.Project project = e.getProject();
        if (project == null){
            return;
        }
        AnalysisUIOptions analysisUiOptions = ServiceManager.getService(project, AnalysisUIOptions.class);
        analysisUiOptions.GROUP_BY_SEVERITY = true;
        InspectionManagerEx managerEx = (InspectionManagerEx) InspectionManager.getInstance(project);
         InspectionProfileImpl profile = InspectionProjectProfileManager.getInstance(project).getCurrentProfile();
         List<ScopeToolState> tools = profile.getAllTools();
         List<InspectionToolWrapper> toolWrappers = new ArrayList<>();
         for (ScopeToolState state : tools){
             InspectionToolWrapper wapper = state.getTool();
             InspectionProfileEntry tool = wapper.getTool();
             if (tool instanceof HsBaseInspection){
                 toolWrappers.add(wapper);
             }
         }

        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        VirtualFile[] virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        AnalysisScope analysisScope = null;
        boolean projectDir = false;
        if (psiFile != null) {
            analysisScope = new AnalysisScope(psiFile);
            projectDir = isBaseDir(psiFile.getVirtualFile(), project);
        } else if (virtualFiles != null && virtualFiles.length > NumberConstants.INTEGER_SIZE_OR_LENGTH_0) {
            analysisScope = new AnalysisScope(project, Arrays.asList(virtualFiles));
            for (VirtualFile it : virtualFiles){
                if (isBaseDir(it, project)){
                    projectDir = true;
                    break;
                }
            }
        } else {
            if (virtualFile != null && virtualFile.isDirectory()) {
                PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(virtualFile);
                if (psiDirectory != null) {
                    analysisScope = new AnalysisScope(psiDirectory);
                    projectDir = isBaseDir(virtualFile, project);
                }
            }
            if (analysisScope == null && virtualFile != null) {
                analysisScope = new AnalysisScope(project, Arrays.asList(virtualFile));
                projectDir = isBaseDir(virtualFile, project);
            }
            if (analysisScope == null) {
                projectDir = true;
                analysisScope = new AnalysisScope(project);
            }
        }
        if (e.getInputEvent() instanceof KeyEvent) {
            inspectForKeyEvent(project, managerEx, toolWrappers, psiElement, psiFile, virtualFile, analysisScope);
            return;
        }
        PsiElement element = psiFile !=null ? psiFile : psiElement;
        analysisScope.setIncludeTestSource(false);
        analysisScope.setSearchInLibraries(true);
        createContext(toolWrappers, managerEx, element, projectDir).doInspections(analysisScope);
    }

    private boolean isBaseDir(VirtualFile file, com.intellij.openapi.project.Project project){
        if (file.getCanonicalPath() == null || project.getBasePath() == null) {
            return false;
        }
        return project.getBasePath().equals(file.getCanonicalPath());
    }

    private void inspectForKeyEvent(com.intellij.openapi.project.Project project, InspectionManagerEx managerEx,
                                    List<InspectionToolWrapper> toolWrappers, PsiElement psiElement, PsiFile psiFile,
                                    VirtualFile virtualFile, AnalysisScope analysisScope) {
        com.intellij.openapi.module.Module module= null;
        if (virtualFile != null && project.getBasePath().equals(virtualFile.getCanonicalPath())) {
            module = ModuleUtilCore.findModuleForFile(virtualFile, project);
        }

        AnalysisUIOptions uiOptions = AnalysisUIOptions.getInstance(project);
        uiOptions.ANALYZE_TEST_SOURCES = false;
        String name = null;
        if (module != null){
            name = module.getName();
        }
        BaseAnalysisActionDialog dialog = new BaseAnalysisActionDialog("Select Analyze Scope", "Analyze Scope", project, analysisScope,
        name, true, uiOptions, psiElement);

        if (!dialog.showAndGet()) {
            return;
        }
        AnalysisScope scope = dialog.getScope(uiOptions, analysisScope, project, module);
        scope.setSearchInLibraries(true);
        PsiElement element = psiFile !=null ? psiFile : psiElement;
        createContext(toolWrappers, managerEx, element,
                dialog.isProjectScopeSelected()).doInspections(scope);
    }




    @Override
    public void update(@NotNull AnActionEvent e){
        e.getPresentation().setText("恒生规约检查");
    }

    private GlobalInspectionContextImpl createContext(List<InspectionToolWrapper> toolWrapperList,
                                                      InspectionManagerEx managerEx, PsiElement psiElement, Boolean projectScopeSelected)
     {
         InspectionProfileImpl currentProfile = InspectionProjectProfileManager.getInstance(managerEx.getProject()).getCurrentProfile();
         LinkedHashSet<InspectionToolWrapper> allWrappers = Sets.newLinkedHashSet();
         allWrappers.addAll(toolWrapperList);
         LinkedHashSet<InspectionToolWrapper> forCompile = allWrappers;
         for (InspectionToolWrapper toolWrapper : allWrappers) {
             currentProfile.collectDependentInspections(toolWrapper, forCompile, managerEx.getProject());
         }

         InspectionProfileImpl profile = new InspectionProfileImpl("hsRule", new InspectionToolsSupplier.Simple(toolWrapperList), (BaseInspectionProfileManager) InspectionProfileManager.getInstance());
         for (InspectionToolWrapper wrapper : toolWrapperList){
             profile.enableTool(wrapper.getShortName(), managerEx.getProject());
         }

         try {
             Element element = new Element("toCopy");
             for (InspectionToolWrapper wrapper : toolWrapperList) {
                 wrapper.getTool().writeSettings(element);
                 InspectionToolWrapper tw;
                 if (psiElement == null) {
                     tw = profile.getInspectionTool(wrapper.getShortName(), managerEx.getProject());
                 } else {
                     tw = profile.getInspectionTool(wrapper.getShortName(), psiElement);
                 }
                 if (tw != null){
                     tw.getTool().readSettings(element);
                 }
             }
         } catch (WriteExternalException ignored) {
             logger.error(ignored, ignored);
     } catch (InvalidDataException ignored) {
             logger.error(ignored, ignored);
     }

         GlobalInspectionContextImpl inspectionContext = managerEx.createNewGlobalContext(false);
         for (InspectionToolWrapper wrapper : toolWrapperList) {
             wrapper.initialize(inspectionContext);
         }
         inspectionContext.setExternalProfile(profile);
        return inspectionContext;
    }

        private GlobalInspectionContextImpl createNewGlobalContext(InspectionManagerEx managerEx, Boolean projectScopeSelected) {
            return new GlobalInspectionContextImpl(managerEx.getProject(), managerEx.getContentManager());
    }

}
