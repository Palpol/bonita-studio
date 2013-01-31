/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.expression.editor.comparison;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.RepositoryManager;
import org.bonitasoft.studio.condition.ui.internal.ConditionModelActivator;
import org.bonitasoft.studio.expression.editor.ExpressionEditorPlugin;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.resource.XtextResourceSetProvider;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.inject.Injector;

/**
 * @author Romain Bioteau
 *
 */
public class ComparisonExpressionValidator implements IValidator {

	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
	 */
	@Override
	public IStatus validate(Object value) {
		if(value == null || value.toString().isEmpty()){
			return ValidationStatus.ok();
		}
		final Injector injector = ConditionModelActivator.getInstance().getInjector(ConditionModelActivator.ORG_BONITASOFT_STUDIO_CONDITION_CONDITIONMODEL);
		final IResourceValidator xtextResourceChecker =	injector.getInstance(IResourceValidator.class);
		final XtextResourceSetProvider xtextResourceSetProvider = injector.getInstance(XtextResourceSetProvider.class);
		final ResourceSet resourceSet = xtextResourceSetProvider.get(RepositoryManager.getInstance().getCurrentRepository().getProject());
		final XtextResource resource = (XtextResource) resourceSet.createResource(URI.createURI("somefile.cmodel"));
		try {
			resource.load(new StringInputStream(value.toString(), "UTF-8"), Collections.emptyMap());
		} catch (UnsupportedEncodingException e1) {
			BonitaStudioLog.error(e1, ExpressionEditorPlugin.PLUGIN_ID);
		} catch (IOException e1) {
			BonitaStudioLog.error(e1, ExpressionEditorPlugin.PLUGIN_ID);
		}

		final MultiStatus status = new MultiStatus(ExpressionEditorPlugin.PLUGIN_ID, 0, "", null);
		final List<Issue> issues = xtextResourceChecker.validate(resource, CheckMode.FAST_ONLY, null);

		for(Issue issue : issues){
			int severity = IStatus.ERROR;
			Severity issueSeverity = issue.getSeverity();
			if(issueSeverity == Severity.WARNING){
				severity = IStatus.WARNING;
			}
			status.add(new Status(severity, ExpressionEditorPlugin.PLUGIN_ID, issue.getMessage()));
		}

		return status;
	}

}
