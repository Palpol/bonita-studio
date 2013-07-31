/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.studio.application.coolbar;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.bonitasoft.studio.application.i18n.Messages;
import org.bonitasoft.studio.common.extension.BonitaStudioExtensionRegistryManager;
import org.bonitasoft.studio.common.extension.IBonitaContributionItem;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.pics.Pics;
import org.bonitasoft.studio.preferences.BonitaCoolBarPreferenceConstant;
import org.bonitasoft.studio.preferences.BonitaStudioPreferencesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimmedPartLayout;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ActivityManagerEvent;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.internal.handlers.DirtyStateTracker;

/**
 * @author Romain Bioteau
 *
 */
public class CoolbarToolControl implements INullSelectionListener,IActivityManagerListener {

	private static final int ICON_SIZE = 24;

	private enum CoolbarSize {SMALL,NORMAL}

	private static final String COOLBAR_PNG =  "/bg-coolbar-repeat.png";
	private static final String CLASS = "class";
	private static final String POSITION = "toolbarPosition";
	private static final String PRIORITY = "priority";
	private static final int MAX_CONTRIBUTION_SIZE = 25;
	private CoolbarSize size;
	private final Map<Integer,IBonitaContributionItem> contributions = new HashMap<Integer, IBonitaContributionItem>();
	private ToolBar toolbar;
	private Image image;
	private Composite toolbarContainer;

	@PostConstruct
	public void createControls(Composite parent) {
		initCoolBarPreferredSize() ;
		Composite parentShell = parent.getParent();
		TrimmedPartLayout layout = (TrimmedPartLayout) parentShell.getLayout();
		toolbarContainer = new Composite(parentShell, SWT.INHERIT_FORCE) ;
		toolbarContainer.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).create()) ;
		toolbarContainer.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).spacing(0, 0).margins(0, 0).create()) ;
		layout.top = toolbarContainer;
		createToolbar(toolbarContainer);
		PlatformUI.getWorkbench().getActivitySupport().getActivityManager().addActivityManagerListener(this);
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		activePage.addSelectionListener(this);
		activePage.addPartListener(new IPartListener(){

			@Override
			public void partOpened(IWorkbenchPart wp) {
				if(wp instanceof ISaveablePart){
					for(IBonitaContributionItem bcItem : contributions.values()){
						if(bcItem instanceof SaveCoolbarItem){
							final DirtyStateTracker dirtyStateTracker = getDirtyStateTracker(bcItem);
							dirtyStateTracker.partOpened(wp);
						}
					}
				}
			}

			@Override
			public void partDeactivated(IWorkbenchPart wp) {
				if(wp instanceof ISaveablePart){
					for(IBonitaContributionItem bcItem : contributions.values()){
						if(bcItem instanceof SaveCoolbarItem){
							final DirtyStateTracker dirtyStateTracker = getDirtyStateTracker(bcItem);
							dirtyStateTracker.partDeactivated(wp);

						}
					}
				}
			}

			@Override
			public void partClosed(IWorkbenchPart wp) {
				if(wp instanceof ISaveablePart){
					for(IBonitaContributionItem bcItem : contributions.values()){
						if(bcItem instanceof SaveCoolbarItem){
							final DirtyStateTracker dirtyStateTracker = getDirtyStateTracker(bcItem);
							dirtyStateTracker.partClosed(wp);
						}
					}
				}
			}

			@Override
			public void partBroughtToTop(IWorkbenchPart wp) {
				if(wp instanceof ISaveablePart){
					for(IBonitaContributionItem bcItem : contributions.values()){
						if(bcItem instanceof SaveCoolbarItem){
							final DirtyStateTracker dirtyStateTracker = getDirtyStateTracker(bcItem);
							dirtyStateTracker.partBroughtToTop(wp);
						}
					}
				}
			}

			@Override
			public void partActivated(IWorkbenchPart wp) {
				if(wp instanceof ISaveablePart){
					for(IBonitaContributionItem bcItem : contributions.values()){
						if(bcItem instanceof SaveCoolbarItem){
							final DirtyStateTracker dirtyStateTracker = getDirtyStateTracker(bcItem);
							dirtyStateTracker.partActivated(wp);
						}
					}
				}
				if(wp instanceof DiagramEditor){
					for(IBonitaContributionItem bcItem : contributions.values()){
						if(bcItem instanceof ISelectionChangedListener){
							((DiagramEditor) wp).getDiagramGraphicalViewer().addSelectionChangedListener((ISelectionChangedListener)bcItem);
						}
					}
				}
			}

			/**
			 * @param bcItem
			 * @return
			 */
			 public DirtyStateTracker getDirtyStateTracker(
					 IBonitaContributionItem bcItem) {
				DirtyStateTracker dirtyStateTracker = ((SaveCoolbarItem) bcItem).getDirtyStateTracker();
				if(dirtyStateTracker == null){
					((SaveCoolbarItem) bcItem).createDirtyStateTracker();
					dirtyStateTracker = ((SaveCoolbarItem) bcItem).getDirtyStateTracker();
				}
				return dirtyStateTracker;
			}
		});
	}

	private void createToolbar(final Composite toolbarContainer) {
		toolbar = new ToolBar(toolbarContainer,SWT.FLAT) ;
		toolbar.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).create()) ;

		fillBonitaBar();

		ToolBar sizingToolbar = new ToolBar(toolbarContainer,SWT.FLAT | SWT.VERTICAL) ;
		sizingToolbar.setBackgroundMode(SWT.INHERIT_FORCE) ;
		sizingToolbar.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).spacing(0, 0).create()) ;
		sizingToolbar.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).create()) ;


		ToolItem minimizeButton = new ToolItem(sizingToolbar, SWT.FLAT) ;
		minimizeButton.setImage(Pics.getImage("arrow-up.png")) ;
		minimizeButton.setToolTipText(Messages.reduceCoolbarTooltip) ;
		minimizeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				minimizeCoolbar() ;
			}
		}) ;

		ToolItem maximizeButton = new ToolItem(sizingToolbar, SWT.FLAT ) ;
		maximizeButton.setToolTipText(Messages.maximizeCoolbarTooltip) ;
		maximizeButton.setImage(Pics.getImage("arrow-down.png")) ;
		maximizeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				maximizeCoolbar();
			}
		}) ;



		if(image != null){
			image.dispose() ;
		}

		if(size == CoolbarSize.SMALL){
			image = new Image(Display.getDefault(),Pics.getImage(COOLBAR_PNG).getImageData().scaledTo(Display.getDefault().getBounds().width, 30)) ;

		}else{
			image = new Image(Display.getDefault(),Pics.getImage(COOLBAR_PNG).getImageData().scaledTo(Display.getDefault().getBounds().width, 75)) ;

		}
		toolbarContainer.setBackgroundImage(image);
		toolbarContainer.setBackgroundMode(SWT.INHERIT_FORCE);
	}

	private void fillBonitaBar() {

		for(IBonitaContributionItem contribution : contributions.values()){
			contribution.dispose() ;
		}
		contributions.clear() ;

		IConfigurationElement[] elements = BonitaStudioExtensionRegistryManager.getInstance().getConfigurationElements("org.bonitasoft.studio.coolbarContributionItem") ;
		if(elements.length >= MAX_CONTRIBUTION_SIZE){
			throw new RuntimeException("Too many coolbar contributions defined") ;
		}
		for(int i = 0 ; i < MAX_CONTRIBUTION_SIZE ; i++){
			IConfigurationElement element = findContributionForPosition(i,elements) ;
			if(element != null){
				try {
					IBonitaContributionItem item = (IBonitaContributionItem) element.createExecutableExtension(CLASS) ;
					if(toolbar.getItemCount() > 1 && item instanceof SeparatorCoolbarItem){
						int index = toolbar.getItemCount()-1;
						ToolItem previousItem = toolbar.getItem(index);
						if((previousItem.getStyle() & SWT.SEPARATOR) != 0){
							item.setVisible(false);
						}
					}
					if(item.isVisible()){
						if(size == CoolbarSize.SMALL){
							item.fill(toolbar, i,ICON_SIZE) ;
						}else{
							item.fill(toolbar, i, -1) ;
						}
						contributions.put(toolbar.getItemCount()-1, item) ;
					}
				} catch (CoreException e) {
					BonitaStudioLog.error(e) ;
				}
			}
		}

		refreshCoolBarButtons();

	}

	public void maximizeCoolbar() {
		for(Control c : toolbarContainer.getChildren()){
			c.dispose() ;
		}
		size = CoolbarSize.NORMAL ;
		createToolbar(toolbarContainer) ;
		toolbarContainer.getParent().layout(true, true) ;
	}

	public void minimizeCoolbar() {
		for(Control c : toolbarContainer.getChildren()){
			c.dispose() ;
		}
		size = CoolbarSize.SMALL ;
		createToolbar(toolbarContainer) ;
		toolbarContainer.getParent().layout(true, true) ;
	}

	private void initCoolBarPreferredSize() {
		String value = BonitaStudioPreferencesPlugin.getDefault().getPreferenceStore().getString(BonitaCoolBarPreferenceConstant.COOLBAR_DEFAULT_SIZE) ;
		if (value.equals(BonitaCoolBarPreferenceConstant.SMALL)){
			size = CoolbarSize.SMALL;
		}else if(value.equals(BonitaCoolBarPreferenceConstant.NORMAL)){
			size = CoolbarSize.NORMAL;
		}
	}

	private void refreshCoolBarButtons() {
		if(toolbar !=null && !toolbar.isDisposed()){
			for(ToolItem item : toolbar.getItems()){
				if(!item.isDisposed()){
					IBonitaContributionItem iBonitaContributionItem = contributions.get(toolbar.indexOf(item));
					item.setEnabled(iBonitaContributionItem.isEnabled()) ;
				}
			}
		}
	}

	private IConfigurationElement findContributionForPosition(int position,IConfigurationElement[] elements) {
		List<IConfigurationElement> list = new ArrayList<IConfigurationElement>() ;
		for(IConfigurationElement element : elements){
			int pos = Integer.parseInt(element.getAttribute(POSITION)) ;
			if(pos == position){
				list.add(element) ;
			}
		}
		if(!list.isEmpty()){
			if(list.size() > 1){
				list = sortByPriority(list) ;
			}
			return list.get(0) ;
		}
		return null;
	}

	private List<IConfigurationElement> sortByPriority(List<IConfigurationElement> list) {
		List<IConfigurationElement> sortedConfigElems = new ArrayList<IConfigurationElement>() ;
		for(IConfigurationElement elem : list){
			sortedConfigElems.add(elem) ;
		}

		Collections.sort(sortedConfigElems, new Comparator<IConfigurationElement>() {

			@Override
			public int compare(IConfigurationElement e1, IConfigurationElement e2) {
				int	p1 = 0;
				int p2 = 0 ;
				try{
					p1 = Integer.parseInt(e1.getAttribute(PRIORITY));
				}catch (NumberFormatException e) {
					p1 = 0 ;
				}
				try{
					p2 = Integer.parseInt(e2.getAttribute(PRIORITY));
				}catch (NumberFormatException e) {
					p2 = 0 ;
				}
				return  p2 - p1  ; //Highest Priority first
			}

		}) ;
		return sortedConfigElems;

	}

	private void refreshCoolbar(){
		for(Control c : toolbarContainer.getChildren()){
			c.dispose() ;
		}
		createToolbar(toolbarContainer) ;
		for(IBonitaContributionItem bcItem : contributions.values()){
			if(bcItem instanceof SaveCoolbarItem){
				((SaveCoolbarItem) bcItem).createDirtyStateTracker();
			}
		}
		toolbar.getParent().layout(true,true) ;
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		refreshCoolBarButtons();
	}

	@Override
	public void activityManagerChanged(ActivityManagerEvent activityManagerEvent) {
		refreshCoolbar() ;
	}
}
