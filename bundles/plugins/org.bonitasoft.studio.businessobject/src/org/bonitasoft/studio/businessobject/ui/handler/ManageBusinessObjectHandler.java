/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.businessobject.ui.handler;

import org.bonitasoft.studio.businessobject.BusinessObjectPlugin;
import org.bonitasoft.studio.businessobject.core.repository.BusinessObjectModelFileStore;
import org.bonitasoft.studio.businessobject.core.repository.BusinessObjectModelRepositoryStore;
import org.bonitasoft.studio.businessobject.i18n.Messages;
import org.bonitasoft.studio.businessobject.ui.wizard.ManageBusinessDataModelWizard;
import org.bonitasoft.studio.common.jface.CustomWizardDialog;
import org.bonitasoft.studio.common.jface.MessageDialogWithPrompt;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Romain Bioteau
 */
public class ManageBusinessObjectHandler {

    private static final String DO_NOT_SHOW_INSTALL_MESSAGE_DIALOG = "DO_NOT_SHOW_INSTALL_MESSAGE_DIALOG";
    private Shell shell;
    private BusinessObjectModelRepositoryStore<BusinessObjectModelFileStore> repositoryStore;

    @Execute
    public Object execute(RepositoryAccessor repositoryAccessor, Shell shell) throws ExecutionException {
        this.repositoryStore = repositoryAccessor.getRepositoryStore(BusinessObjectModelRepositoryStore.class);
        this.shell = shell;
        final ManageBusinessDataModelWizard newBusinessDataModelWizard = createWizard();
        final CustomWizardDialog dialog = createWizardDialog(newBusinessDataModelWizard, IDialogConstants.FINISH_LABEL);
        if (dialog.open() == IDialogConstants.OK_ID) {
            openSuccessDialog();
            return IDialogConstants.OK_ID;
        }
        return IDialogConstants.CANCEL_ID;
    }

    protected void openSuccessDialog() {
        final IPreferenceStore preferenceStore = BusinessObjectPlugin.getDefault().getPreferenceStore();
        if (!preferenceStore.getBoolean(DO_NOT_SHOW_INSTALL_MESSAGE_DIALOG)) {
            MessageDialogWithPrompt.openWithDetails(MessageDialog.INFORMATION,
                    shell,
                    Messages.bdmDeployedTitle,
                    Messages.bdmDeployedMessage,
                    Messages.doNotShowMeAgain,
                    Messages.bdmDeployDetails,
                    false,
                    preferenceStore,
                    DO_NOT_SHOW_INSTALL_MESSAGE_DIALOG,
                    SWT.NONE);
        }
    }

    protected CustomWizardDialog createWizardDialog(final IWizard wizard, final String finishLabel) {
        final CustomWizardDialog dialog = new CustomWizardDialog(shell, wizard, finishLabel) {

            @Override
            protected Control createHelpControl(final Composite parent) {
                final Control helpControl = super.createHelpControl(parent);
                if (helpControl instanceof ToolBar) {
                    final ToolItem toolItem = ((ToolBar) helpControl).getItem(0);
                    toolItem.setToolTipText(Messages.howToUseBusinessObjects);
                }
                return helpControl;

            }
        };
        dialog.setHelpAvailable(true);
        return dialog;
    }

    protected ManageBusinessDataModelWizard createWizard() {
        BusinessObjectModelFileStore fileStore = repositoryStore.getChild(BusinessObjectModelFileStore.BOM_FILENAME);
        if (fileStore == null) {
            fileStore = (BusinessObjectModelFileStore) repositoryStore
                    .createRepositoryFileStore(BusinessObjectModelFileStore.BOM_FILENAME);
        }
        return new ManageBusinessDataModelWizard(fileStore);
    }

}
