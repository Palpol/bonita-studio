/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.studio.properties.form.sections.actions.contributions;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.studio.common.ExpressionConstants;
import org.bonitasoft.studio.common.emf.tools.ExpressionHelper;
import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.expression.ExpressionFactory;
import org.bonitasoft.studio.model.expression.Operation;
import org.bonitasoft.studio.model.form.Form;
import org.bonitasoft.studio.model.form.FormFactory;
import org.bonitasoft.studio.model.form.TextFormField;
import org.bonitasoft.studio.model.form.Widget;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.workspace.WorkspaceEditingDomainFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Romain Bioteau
 *
 */
public class WidgetModifierContributionTest {

    private TextFormField textFormField;
    private WidgetModifierContribution widgetModifierContribution;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Form form = FormFactory.eINSTANCE.createForm();
        textFormField = FormFactory.eINSTANCE.createTextFormField();
        textFormField.setName("name");
        textFormField.setReturnTypeModifier(String.class.getName());
        form.getWidgets().add(textFormField);
        widgetModifierContribution = new WidgetModifierContribution();
        widgetModifierContribution.setEditingDomain(WorkspaceEditingDomainFactory.INSTANCE.createEditingDomain());
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldUpdateWidgetReferences_Update_Widget_References_WithNewModifierType_ForFormFieldExpressionType() throws Exception {
        Operation operation= ExpressionFactory.eINSTANCE.createOperation();
        Expression actionExp = ExpressionFactory.eINSTANCE.createExpression();
        actionExp.setReturnType(String.class.getName());
        actionExp.setType(ExpressionConstants.FORM_FIELD_TYPE);
        actionExp.setContent("field_name");
        operation.setRightOperand(actionExp);
        textFormField.setAction(operation);
        widgetModifierContribution.updateWidgetReferences(textFormField, Integer.class.getName());
        ExpressionAssert.assertThat(actionExp).hasReturnType(Integer.class.getName());
    }
    
    @Test
    public void shouldUpdateWidgetReferences_Update_Widget_References_WithNewModifierType_ForGroovyExpressionDependencies() throws Exception {
        Operation operation= ExpressionFactory.eINSTANCE.createOperation();
        Expression actionExp = ExpressionFactory.eINSTANCE.createExpression();
        actionExp.setReturnType(String.class.getName());
        actionExp.setType(ExpressionConstants.SCRIPT_TYPE);
        actionExp.setInterpreter(ExpressionConstants.GROOVY);
        actionExp.setContent("field_name");
        EObject dependencyFromEObject = ExpressionHelper.createDependencyFromEObject(textFormField);
        assertThat(((Widget)dependencyFromEObject).getReturnTypeModifier()).isEqualTo(String.class.getName());
        actionExp.getReferencedElements().add(dependencyFromEObject);
        operation.setRightOperand(actionExp);
        textFormField.setAction(operation);
        widgetModifierContribution.updateWidgetReferences(textFormField, Integer.class.getName());
        assertThat(dependencyFromEObject).isInstanceOf(Widget.class);
        assertThat(((Widget)dependencyFromEObject).getReturnTypeModifier()).isEqualTo(Integer.class.getName());
    }

}
