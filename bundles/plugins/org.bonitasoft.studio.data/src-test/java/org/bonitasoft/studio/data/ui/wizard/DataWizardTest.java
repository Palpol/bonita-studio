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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.data.ui.wizard;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bonitasoft.studio.data.operation.RefactorDataOperation;
import org.bonitasoft.studio.model.process.Data;
import org.bonitasoft.studio.model.process.Pool;
import org.bonitasoft.studio.model.process.ProcessFactory;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.model.process.util.ProcessAdapterFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.impl.TransactionalCommandStackImpl;
import org.eclipse.emf.transaction.impl.TransactionalEditingDomainImpl;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Romain Bioteau
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Display.class)
public class DataWizardTest {

    private DataWizard wizard;

    @Mock
    private RefactorDataOperation refactorOperation;

    private TransactionalEditingDomain editingDomain;

    @Mock
    private Display display;

    @Mock
    private IWizardContainer wizardContainer;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Display.class);
        PowerMockito.when(Display.getCurrent()).thenReturn(display);
        when(refactorOperation.canExecute()).thenReturn(true);
        editingDomain = new TransactionalEditingDomainImpl(new ProcessAdapterFactory(), new TransactionalCommandStackImpl());
        Data data = ProcessFactory.eINSTANCE.createData();
        wizard = spy(new DataWizard(editingDomain, data, ProcessPackage.Literals.DATA_AWARE__DATA, Collections.<EStructuralFeature> emptySet(), true));
        doReturn(refactorOperation).when(wizard).createRefactorOperation(eq(editingDomain), any(Data.class));
        doReturn(EcoreUtil.copy(data)).when(wizard).getWorkingCopy();
        doReturn(wizardContainer).when(wizard).getContainer();
        doNothing().when(wizard).refreshXtextReferences();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void should_performFinish_execute_refactor_operation() throws Exception {
        wizard.performFinish();
        verify(wizardContainer).run(true, false, refactorOperation);
    }

}
