package org.yamcs.studio.css.core;

import java.text.DecimalFormat;
import java.time.Instant;

import org.csstudio.simplepv.IPV;
import org.csstudio.simplepv.VTypeHelper;
import org.diirt.vtype.Display;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.yamcs.protobuf.Mdb.AlarmInfo;
import org.yamcs.protobuf.Mdb.AlarmRange;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Mdb.ParameterTypeInfo;
import org.yamcs.protobuf.Mdb.UnitInfo;
import org.yamcs.protobuf.Pvalue.ParameterData;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.StringConverter;
import org.yamcs.studio.core.model.ParameterCatalogue;
import org.yamcs.studio.core.model.ParameterListener;

public class PVComposite extends Composite implements ParameterListener {

    private PVInfo pvInfo;

    private StyledText gentimeField;
    private StyledText rectimeField;
    private StyledText statusField;
    private StyledText engValueField;
    private StyledText engTypeField;
    private StyledText rawValueField;
    private StyledText rawTypeField;

    public PVComposite(Composite parent, int style, PVInfo pvInfo) {
        super(parent, style);
        this.pvInfo = pvInfo;

        if (pvInfo.getParameterInfo() != null) {
            createKeyValueTextPair("PV Type", pvInfo.getPVType());
            createSeparator();
            createYamcsProperties(pvInfo.getParameterInfo());
        } else if (pvInfo.getParameterInfoException() != null) {
            createKeyValueTextPair("PV Type", "Yamcs Parameter");
            createSeparator();
            createKeyValueTextPair("Error", pvInfo.getParameterInfoException());
        } else {
            createKeyValueTextPair("PV Type", "PV");
            createSeparator();
            createPVProperties(pvInfo);
        }

        ParameterCatalogue.getInstance().addParameterListener(this);
    }

    private void createYamcsProperties(ParameterInfo pinfo) {
        createKeyValueTextPair("Yamcs Data Source", capitalize(pinfo.getDataSource().toString()));
        createKeyValueTextPair("Qualified Name", pinfo.getQualifiedName());
        for (int i = 0; i < pinfo.getAliasCount(); i++) {
            NamedObjectId alias = pinfo.getAlias(i);
            String key = (i == 0) ? "Aliases" : null;
            createKeyValueTextPair(key, alias.getNamespace() + "; " + alias.getName());
        }

        if (pinfo.hasShortDescription()) {
            createKeyValueTextPair("Short Description", pinfo.getShortDescription());
        }
        if (pinfo.hasLongDescription()) {
            createKeyValueTextPair("Long Description", pinfo.getLongDescription());
        }

        createSeparator();
        createHeader("MDB");

        ParameterTypeInfo type = pinfo.getType();
        createKeyValueTextPair("Data Encoding", capitalize(type.getDataEncoding().getType().toString()));
        createKeyValueTextPair("Engineering Type", capitalize(type.getEngType()));
        if (type.getUnitSetCount() > 0) {
            String units = "";
            for (UnitInfo unit : type.getUnitSetList()) {
                units += unit.getUnit() + " ";
            }
            createKeyValueTextPair("Units", units);
        }

        if (type.getDefaultAlarm() != null && type.getDefaultAlarm().getStaticAlarmRangeCount() > 0) {
            createSeparator();
            createHeader("Default Alarm");
            AlarmInfo defaultAlarm = type.getDefaultAlarm();
            createKeyValueTextPair("Min. Violations", "" + defaultAlarm.getMinViolations());

            // Backwards for lower limits
            for (int i = defaultAlarm.getStaticAlarmRangeCount() - 1; i >= 0; i--) {
                AlarmRange range = defaultAlarm.getStaticAlarmRange(i);
                if (range.hasMinInclusive()) {
                    String label = capitalize(range.getLevel().toString()) + " Low";
                    String limit = new DecimalFormat("#.############").format(range.getMinInclusive());
                    createKeyValueTextPair(label, limit);
                }
            }

            // Now forwards for upper limits
            for (AlarmRange range : defaultAlarm.getStaticAlarmRangeList()) {
                if (range.hasMaxInclusive()) {
                    String label = capitalize(range.getLevel().toString()) + " High";
                    String limit = new DecimalFormat("#.############").format(range.getMaxInclusive());
                    createKeyValueTextPair(label, limit);
                }
            }
        }

        createSeparator();
        createHeader("Last Received Value");
        // Anybody knows better way to reserve space??
        gentimeField = createKeyValueTextPair("Generation Time", "---                                             ");
        rectimeField = createKeyValueTextPair("Reception Time", "---                                             ");
        statusField = createKeyValueTextPair("Status", "---                                             ");
        engValueField = createKeyValueTextPair("Engineering Value", "---                                             ");
        engTypeField = createKeyValueTextPair("Engineering Type", "---                                             ");
        rawValueField = createKeyValueTextPair("Raw Value", "---                                             ");
        rawTypeField = createKeyValueTextPair("Raw Type", "---                                             ");
    }

    private void createPVProperties(PVInfo pvInfo) {
        IPV pv = pvInfo.getPV();
        StringBuilder stateInfo = new StringBuilder();
        if (!pv.isStarted()) {
            stateInfo.append("Not started");
        } else if (pv.isConnected()) {
            stateInfo.append("Connected");
            if (pv.isPaused()) {
                stateInfo.append(" Paused");
            } else {
                stateInfo.append(" Running");
            }
        } else {
            stateInfo.append("Connecting");
        }

        createKeyValueTextPair("Name", pv.getName());
        createKeyValueTextPair("State", stateInfo.toString());
        createSeparator();

        if (pv.getValue() != null) {
            createKeyValueTextPair("Last Received Value", pv.getValue().toString());
            Display displayInfo = VTypeHelper.getDisplayInfo(pv.getValue());
            if (displayInfo != null) {
                createKeyValueTextPair("Units", displayInfo.getUnits());
                createKeyValueTextPair("Precision", "" + displayInfo.getFormat().getMaximumFractionDigits());
                createKeyValueTextPair("Display Low", "" + displayInfo.getLowerDisplayLimit());
                createKeyValueTextPair("Display High", "" + displayInfo.getUpperDisplayLimit());
                createKeyValueTextPair("Alarm Low", "" + displayInfo.getLowerAlarmLimit());
                createKeyValueTextPair("Warning Low", "" + displayInfo.getLowerWarningLimit());
                createKeyValueTextPair("Warning High", "" + displayInfo.getUpperWarningLimit());
                createKeyValueTextPair("Alarm High", "" + displayInfo.getUpperAlarmLimit());
            }
        }
    }

    private StyledText createKeyValueTextPair(String key, String value) {
        Label lbl = new Label(this, SWT.NONE);
        if (key != null) {
            lbl.setText(key + ":");
        }
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.END;
        gd.verticalAlignment = SWT.BEGINNING;
        lbl.setLayoutData(gd);

        // StyledText instead of Label, so that text is selectable
        // TODO should wrap, but can't get it to work right now
        StyledText txt = new StyledText(this, SWT.WRAP);
        txt.setBackground(getBackground());
        txt.setEditable(false);
        txt.setCaret(null);
        txt.setText(value);
        txt.setWordWrap(true);
        return txt;
    }

    private void createSeparator() {
        Label divider = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        divider.setLayoutData(gd);
    }

    private void createHeader(String title) {
        Label header = new Label(this, SWT.NONE);
        header.setText(title);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        header.setLayoutData(gd);
        header.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
    }

    private static String capitalize(String string) {
        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
                found = false;
            }
        }
        return String.valueOf(chars);
    }

    @Override
    public void mdbUpdated() {
    }

    // TODO move this onto a digest thread. We shouldn't update GUI for _every_ value.
    @Override
    public void onParameterData(ParameterData pdata) {
        if (isDisposed()) {
            return;
        }
        getDisplay().asyncExec(() -> {
            if (isDisposed()) {
                return;
            }
            for (ParameterValue pval : pdata.getParameterList()) {
                if (pval.getId().getName().equals(pvInfo.getDisplayName())) {
                    gentimeField.setText(Instant
                            .ofEpochSecond(pval.getGenerationTime().getSeconds(), pval.getGenerationTime().getNanos())
                            .toString());
                    rectimeField.setText(Instant
                            .ofEpochSecond(pval.getAcquisitionTime().getSeconds(), pval.getAcquisitionTime().getNanos())
                            .toString());

                    String engValue = StringConverter.toString(pval.getEngValue());
                    if (pvInfo.getParameterInfo().hasType()) {
                        ParameterTypeInfo ptype = pvInfo.getParameterInfo().getType();
                        if (ptype.getUnitSetCount() > 0) {
                            for (UnitInfo unitInfo : ptype.getUnitSetList()) {
                                engValue += " " + unitInfo.getUnit();
                            }
                        }
                    }
                    engValueField.setText(engValue);
                    engTypeField.setText(capitalize(pval.getEngValue().getType().toString()));

                    if (pval.hasAcquisitionStatus()) {
                        statusField.setText(pval.getAcquisitionStatus().toString());
                    } else {
                        statusField.setText("---");
                    }

                    if (pval.hasRawValue()) {
                        rawValueField.setText(StringConverter.toString(pval.getRawValue()));
                        rawTypeField.setText(capitalize(pval.getRawValue().getType().toString()));
                    } else {
                        rawValueField.setText("---");
                        rawTypeField.setText("---");
                    }
                }
            }
        });
    }

    @Override
    public void dispose() {
        ParameterCatalogue.getInstance().removeParameterListener(this);
        super.dispose();
    }
}
