package com.hundsun.hep.studio;

import com.google.common.base.Throwables;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiFile;
import net.sourceforge.pmd.*;
import org.apache.log4j.Logger;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author wangxh
 * @date
 */
public class HsPmdProcessor {

    private PMDConfiguration configuration = new PMDConfiguration();
    private RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(configuration);
    private Rule rule;
    public HsPmdProcessor(Rule rule) {
        this.rule = rule;
    }

    public List<RuleViolation> processFile(PsiFile psiFile) {
        List<RuleViolation> violations = new ArrayList<>();
        configuration.setSourceEncoding(psiFile.getVirtualFile().getCharset().name());
        configuration.setInputPaths(psiFile.getVirtualFile().getCanonicalPath());
        Document document = FileDocumentManager.getInstance().getDocument(psiFile.getVirtualFile());
        if (document == null){
            return violations;
        }
        int maxLine = 10000;
        if (document.getLineCount() > maxLine) {
            return violations;
        }
        RuleContext ctx = new RuleContext();
        SourceCodeProcessor processor = new SourceCodeProcessor(configuration);
        String niceFileName = psiFile.getVirtualFile().getCanonicalPath();
        Report report = Report.createReport(ctx, niceFileName);
        RuleSets ruleSets = new RuleSets();

        try {
            RuleSet ruleSet = RuleSet.createFor(rule.getName(), rule);
            ruleSets.addRuleSet(ruleSet);
            LOG.debug("Processing " + ctx.getSourceCodeFilename());
            ctx.setLanguageVersion(null);
            processor.processSourceCode(new StringReader(document.getText()), ruleSets, ctx);
        } catch (PMDException pmde) {
            LOG.debug("Error while processing file: $niceFileName", pmde.getCause());
            report.addError(new Report.ProcessingError(pmde.getMessage(), niceFileName));
        } catch (RuntimeException re) {
            Throwable root = Throwables.getRootCause(re);
            if (!(root instanceof ApplicationUtil.CannotRunReadActionException)){
                LOG.error("RuntimeException while processing file: $niceFileName", re);
            }
        }
        Iterator<RuleViolation> it = ctx.getReport().iterator();
        while (it.hasNext()){
            violations.add(it.next());
        }
        return violations;
    }

    private static Logger LOG = Logger.getLogger(HsPmdProcessor.class);
}
