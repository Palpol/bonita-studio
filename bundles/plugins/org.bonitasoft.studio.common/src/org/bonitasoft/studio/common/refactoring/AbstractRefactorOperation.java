/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.common.refactoring;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.model.expression.Expression;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * @author Romain Bioteau
 * 
 */
public abstract class AbstractRefactorOperation implements IRunnableWithProgress {

    public static final String EMPTY_VALUE = "     ";

    protected EditingDomain domain;

    protected CompoundCommand compoundCommand;

    private boolean canExecute = true;

    private boolean isCancelled = false;

    private RefactoringOperationType operationType;

    private boolean askConfirmation;

    protected String newValue;

    public AbstractRefactorOperation(RefactoringOperationType operationType) {
        this.operationType = operationType;
    }

    @Override
    public final void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        Assert.isNotNull(domain);
        if (compoundCommand == null) {
            compoundCommand = new CompoundCommand();
        }
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        updateReferencesInScripts();
        if (canExecute()) {
            doExecute(monitor);
        }
        if (canExecute()) {
            domain.getCommandStack().execute(compoundCommand);
            compoundCommand.dispose();
            compoundCommand = null;
        }
        monitor.done();
    }

    protected abstract void doExecute(IProgressMonitor monitor);

    protected void updateReferencesInScripts() {
        List<Expression> scriptExpressions = ModelHelper.findAllScriptAndConditionsExpressionWithReferencedElement(getContainer(), getOldValue());
        List<Expression> refactoredScriptExpression = performRefactoringForAllScripts(getOldValueName(), getNewValueName(), scriptExpressions);
        if (!scriptExpressions.isEmpty() && !getOldValueName().equals(getNewValue())) {
            AbstractScriptExpressionRefactoringAction action = getScriptExpressionRefactoringAction(getNewValue(), getOldValueName(), getNewValueName(),
                    scriptExpressions, refactoredScriptExpression, compoundCommand, domain, operationType);
            if (action != null) {
                action.setEditingDomain(domain);
                action.setAskConfirmation(askConfirmation());
                action.run(null);
                setCanExecute(!action.isCancelled());
                setCancelled(action.isCancelled());
            } else {
                setCanExecute(true);
            }
        } else {
            setCanExecute(true);
        }
    }

    protected abstract AbstractScriptExpressionRefactoringAction getScriptExpressionRefactoringAction(EObject newValue, String oldName, String newName,
            List<Expression> scriptExpressions,
            List<Expression> refactoredScriptExpression, CompoundCommand compoundCommand, EditingDomain domain, RefactoringOperationType operationType);

    protected List<Expression> performRefactoringForAllScripts(String elementNameToUpdate, String newName, List<Expression> groovyScriptExpressions) {
        List<Expression> newExpressions = new ArrayList<Expression>(groovyScriptExpressions.size());
        for (Expression expr : groovyScriptExpressions) {
            Expression newExpr = EcoreUtil.copy(expr);
            newExpr.setContent(performRefactoring(elementNameToUpdate, newName, expr.getContent()));
            newExpressions.add(newExpr);
        }
        return newExpressions;
    }

    private String performRefactoring(String elementToRefactorName, String newElementName, String script) {
        String contextRegex = "[\\W^_]";
        Pattern p = Pattern.compile(elementToRefactorName);
        Matcher m = p.matcher(script);
        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            String prefix = null;
            String suffix = null;
            if (m.start() > 0) {
                prefix = script.substring(m.start() - 1, m.start());
            }
            if (m.end() < script.length()) {
                suffix = script.substring(m.end(), m.end() + 1);
            }
            if (prefix == null && suffix == null) {
                m.appendReplacement(buf, newElementName);
            } else {
                if (prefix != null && prefix.matches(contextRegex) && suffix == null) {
                    m.appendReplacement(buf, newElementName);
                } else {
                    if (prefix == null && suffix != null && suffix.matches(contextRegex)) {
                        m.appendReplacement(buf, newElementName);
                    } else {
                        if (prefix != null && suffix != null && prefix.matches(contextRegex) && suffix.matches(contextRegex)) {
                            m.appendReplacement(buf, newElementName);
                        }
                    }
                }
            }

        }
        m.appendTail(buf);
        return buf.toString();
    }

    public void setEditingDomain(EditingDomain domain) {
        this.domain = domain;
    }

    public void setCompoundCommand(CompoundCommand compoundCommand) {
        this.compoundCommand = compoundCommand;
    }

    public void setAskConfirmation(boolean askConfirmation) {
        this.askConfirmation = askConfirmation;
    }

    protected boolean askConfirmation() {
        return askConfirmation;
    }

    public boolean canExecute() {
        return canExecute;
    }

    protected void setCanExecute(boolean canExecute) {
        this.canExecute = canExecute;
    }

    public void setNewValueName(String newValue) {
        this.newValue = newValue;
    }

    protected abstract EObject getContainer();

    protected abstract EObject getOldValue();

    protected abstract String getOldValueName();

    protected abstract EObject getNewValue();

    protected abstract String getNewValueName();

    public boolean isCancelled() {
        return isCancelled;
    }

    protected void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

}
