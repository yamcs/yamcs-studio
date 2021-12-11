var Button = Java.type("org.eclipse.swt.widgets.Button");
var GridData = Java.type("org.eclipse.swt.layout.GridData");
var GridLayout = Java.type("org.eclipse.swt.layout.GridLayout");
var SelectionListener = Java.type("org.eclipse.swt.events.SelectionListener");
var Shell = Java.type("org.eclipse.swt.widgets.Shell");
var SWT = Java.type("org.eclipse.swt.SWT");
var Text = Java.type("org.eclipse.swt.widgets.Text");

if (PVUtil.getDouble(pvs[0]) == 100) {
	var shell = new Shell(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	shell.setSize(465, 200);
	shell.setText("MessageDialog");
	shell.setLayout(new GridLayout(5, false));
	text = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	var data = new GridData(GridData.FILL_BOTH);
	data.horizontalSpan = 5;
	text.setLayoutData(data);
	
	confirm = new Button(shell, SWT.NONE);
	confirm.setText("Confirm");
	gridconfirm = new GridData();
	gridconfirm.widthHint = 85;
	gridconfirm.heightHint = 25;
	confirm.setLayoutData(gridconfirm);
	var information = new Button(shell, SWT.NONE);
	information.setText("Information");
	var gridinformation = new GridData();
	gridinformation.widthHint = 85;
	gridinformation.heightHint = 25;
	information.setLayoutData(gridinformation);

	information.addSelectionListener(new SelectionListener({
		widgetSelected: function(event) {
			text.setText("hello");
			GUIUtil.openInformationDialog(text.getText());
		}
	}));

	shell.open();
	shell.layout();
}
