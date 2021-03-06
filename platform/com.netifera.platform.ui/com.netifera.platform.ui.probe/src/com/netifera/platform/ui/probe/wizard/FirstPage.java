package com.netifera.platform.ui.probe.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FirstPage extends WizardPage {
	private final static String[] CHANNEL_TYPES = {"TCP Listen Channel", "TCP Connect-Back Channel"};
	private Text probeNameText;
	private Button[] radioButtons;
	boolean finished;
	
	public FirstPage() {
		super("firstPage");
		setTitle("New Probe");
		setDescription("Create a new Probe.");
		
		setPageComplete(false);
	}
	
	public void createControl(Composite parent) {
		final Composite container = createComposite(parent);
//	    createBanner(container, "Select a name for your new probe");
	
	    probeNameText = createProbeName(container);
	    final Group group = createGroup(container, "Probe Channel Type");
	    
	    radioButtons = createRadioButtons(group);
	    radioButtons[1].setEnabled(false); //FIXME disable connect-back for now
	}
	
	@Override
	public String getName() {
		return probeNameText.getText();
	}
	
	private Text createProbeName(Composite container) {
		createLabel(container, "Probe Name:");
		final Text t = createText(container, 16);
		t.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(t.getText().length() > 0) {
					setErrorMessage(null);
					setPageComplete(true);
				} else {
					setErrorMessage("Probe name is empty.");
					setPageComplete(false);
				}
			}
		});
		return t;
	}
	
	private void createLabel(Composite container, String text) {
		final Label label = new Label(container, SWT.NONE);
		final GridData gd = new GridData(SWT.END, SWT.CENTER, false, false);
		label.setLayoutData(gd);
		label.setText(text);
	}
	
	private Composite createComposite(Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
	    final GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 2;
	    gridLayout.verticalSpacing = 20;
	    c.setLayout(gridLayout);
	    setControl(c);
	    return c;
	}
	private Text createText(Composite container, int limit) {
		final Text text = new Text(container, SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
//		gd.widthHint = 10 * limit;
		text.setLayoutData(gd);
		text.setTextLimit(limit);
		return text;
	}
	
	private Group createGroup(Composite container, String name) {
		final Group group = new Group(container, SWT.SHADOW_ETCHED_OUT);
	    final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
	    gd.horizontalSpan = 2;
	    group.setLayoutData(gd);
	    group.setLayout(new GridLayout());
	    group.setText(name);
		return group;
	}
	
	private Button[] createRadioButtons(Composite parent) {
		final Button[] radios = new Button[CHANNEL_TYPES.length];
		for(int i = 0; i < CHANNEL_TYPES.length; i++) {
			radios[i] = createChannelButton(parent, CHANNEL_TYPES[i]);
		}
		
		if(radios.length > 0) 
			radios[0].setSelection(true);
		
		return radios;
	}
	
	private Button createChannelButton(Composite parent, String text) {
		final Button b = new Button(parent, SWT.RADIO);
		b.setText(text);
		return b;
	}
}
