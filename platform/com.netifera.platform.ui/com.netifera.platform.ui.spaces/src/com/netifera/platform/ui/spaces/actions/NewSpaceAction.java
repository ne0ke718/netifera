package com.netifera.platform.ui.spaces.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IViewPart;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.ui.internal.spaces.Activator;

public class NewSpaceAction extends Action {

	private final StructuredViewer viewer;
	private final SpaceCreator creator;
	
	public NewSpaceAction(IViewPart view, StructuredViewer viewer) {
		this.viewer = viewer;
		this.creator = new SpaceCreator(view.getSite().getWorkbenchWindow());
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/add_space.png"));
		setText("New Space");
	}
	
	public void run() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		Object element = selection.getFirstElement();
		if(element instanceof IProbe) {
			IProbe probe = (IProbe) element;
			creator.openNewSpace(null, probe, false);
		} else if(element instanceof ISpace) {
			ISpace space = (ISpace) element;
			creator.openNewSpace(null, space, false);
		}
	}
}
