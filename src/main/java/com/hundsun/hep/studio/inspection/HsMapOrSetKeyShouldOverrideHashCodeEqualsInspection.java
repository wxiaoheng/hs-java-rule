package com.hundsun.hep.studio.inspection;

import com.hundsun.hep.studio.HsBaseInspection;
import com.hundsun.hep.studio.IHsDelegateInspection;
import com.hundsun.jres.studio.lang.java.util.NumberConstants;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspection;
import com.siyeh.ig.BaseInspectionVisitor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wangxh
 * @date
 */
public class HsMapOrSetKeyShouldOverrideHashCodeEqualsInspection extends BaseInspection implements HsBaseInspection {

    public  HsMapOrSetKeyShouldOverrideHashCodeEqualsInspection(){
        super();
    }

    public  HsMapOrSetKeyShouldOverrideHashCodeEqualsInspection(Object obj){
        this();
        if (obj instanceof IHsDelegateInspection){
            ((IHsDelegateInspection) obj).init(this);
        }
    }

    @Override
    public String ruleName() {
        return "HsMapOrSetKeyShouldOverrideHashCodeEquals";
    }

    @Override
    public String getDisplayName() {
    return "自定义类作为Map键或者Set中的元素时，必须重写HashCode和Equals方法";
}

    @NotNull
    @Override
    protected String buildErrorString(Object... objects) {
        return getDisplayName();
    }

    @Override
    public BaseInspectionVisitor buildVisitor() {
        return new MapOrSetKeyVisitor();
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

    enum ClassType {
        /**
         * parameter type is Set
         */
        SET,
        MAP, OTHER;

        @Override
        public String toString() {
            String string = super.toString();
            return string.charAt(0) + string.substring(1).toLowerCase();
        }
    }

    private class MapOrSetKeyVisitor extends BaseInspectionVisitor {

        private ClassType getClassType(PsiClass aClass) {
            return isMapOrSet(aClass, new HashSet<PsiClass>());
        }

        private ClassType isMapOrSet(PsiClass aClass, Set<PsiClass> visitedClasses) {
            if (aClass == null) {
                return ClassType.OTHER;
            }
            if (!visitedClasses.add(aClass)) {
                return ClassType.OTHER;
            }
            String className = aClass.getQualifiedName();
            if (CommonClassNames.JAVA_UTIL_SET .equals(className)) {
                return ClassType.SET;
            }
            if (CommonClassNames.JAVA_UTIL_MAP.equals(className)) {
                return ClassType.MAP;
            }
            PsiClass[] supers = aClass.getSupers();
            for (PsiClass clz : supers){
                ClassType it = isMapOrSet(clz, visitedClasses);
                if (it == ClassType.OTHER){
                    return it;
                }
            }
            return ClassType.OTHER;
        }

        @Override
        public void visitVariable(PsiVariable variable) {
            super.visitVariable(variable);
            PsiTypeElement typeElement = variable.getTypeElement();
            if (typeElement == null){
                return;
            }
            PsiType type = typeElement.getType();
            if (!(type instanceof PsiClassType)){
                return;
            }
            PsiJavaCodeReferenceElement referenceElement = typeElement.getInnermostComponentReferenceElement();
            if (referenceElement == null){
                return;
            }
            PsiClass aClass = ((PsiClassType) type).resolve();

            ClassType collectionType = getClassType(aClass);
            if (collectionType == ClassType.OTHER) {
                return;
            }
            PsiReferenceParameterList parameterList = referenceElement.getParameterList();
            if (parameterList == null || parameterList.getTypeParameterElements().length== NumberConstants.INTEGER_SIZE_OR_LENGTH_0) {
                return;
            }
            PsiType psiType = parameterList.getTypeArguments()[0];
            if (!redefineHashCodeEquals(psiType)) {
                registerError(parameterList.getTypeParameterElements()[0], psiType);
            }
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            PsiReferenceExpression methodExpression = expression.getMethodExpression();
            PsiExpression qualifierExpression = methodExpression.getQualifierExpression();
            if (qualifierExpression == null){
                return;
            }
            PsiType type = qualifierExpression.getType();
            if (!(type instanceof  PsiClassType)){
                return;
            }
            PsiClass aClass = ((PsiClassType) type).resolve();

            ClassType collectionType = getClassType(aClass);
            if (collectionType == ClassType.OTHER) {
                return;
            }
            @NonNls
            String methodName = methodExpression.getReferenceName();
            String funcName = "add";
            if (collectionType == ClassType.SET && !funcName.equals(methodName)) {
                return;
            }
            funcName = "put";
            if (collectionType == ClassType.MAP && !funcName.equals(methodName)) {
                return;
            }
            PsiExpressionList argumentList = expression.getArgumentList();
            PsiExpression[] arguments = argumentList.getExpressions();
            if (collectionType == ClassType.SET && arguments.length != NumberConstants.INTEGER_SIZE_OR_LENGTH_1) {
                return;
            }
            if (collectionType == ClassType.MAP && arguments.length != NumberConstants.INTEGER_SIZE_OR_LENGTH_2) {
                return;
            }
            PsiExpression argument = arguments[0];
            PsiType argumentType = argument.getType();
            if (argumentType == null || redefineHashCodeEquals(argumentType)) {
                return;
            }
            registerMethodCallError(expression, argumentType);
        }
    }


    private static String  skipJdkPackageJava = "java.";
    private static String skipJdkPackageJavax = "javax.";

    private static boolean redefineHashCodeEquals(PsiType psiType) {
        if (!(psiType instanceof PsiClassType)) {
            return true;
        }
        PsiClass psiClass = ((PsiClassType) psiType).resolve();
        if (psiClass == null){
            return false;
        }
        String name = psiClass.getQualifiedName();
        boolean skip = psiClass.getContainingFile() == null || psiClass instanceof PsiTypeParameter
                || psiClass.isEnum() || psiClass.isInterface()
                || !(psiClass.getContainingFile().getFileType() instanceof JavaFileType)
                || name !=null && name.startsWith(skipJdkPackageJava) ? true : false
                || name != null && name.startsWith(skipJdkPackageJavax) ? true : false;
        if (skip) {
            return true;
        }
        PsiMethod[] hashCodeMethods = psiClass.findMethodsByName("hashCode", false);
        if (hashCodeMethods.length == NumberConstants.INTEGER_SIZE_OR_LENGTH_0) {
            return false;
        }
        PsiMethod[] equalsMethods = psiClass.findMethodsByName("equals", false);
        return equalsMethods.length > NumberConstants.INTEGER_SIZE_OR_LENGTH_0;
    }
}
