/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.css.core;

import static org.yamcs.studio.data.vtype.NumberFormats.TO_STRING_FORMAT;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.yamcs.client.ParameterSubscription;
import org.yamcs.protobuf.Mdb.ParameterInfo;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.protobuf.SubscribeParametersRequest;
import org.yamcs.protobuf.Yamcs.NamedObjectId;
import org.yamcs.studio.core.YamcsAware;
import org.yamcs.studio.core.YamcsPlugin;
import org.yamcs.studio.data.VTypeHelper;
import org.yamcs.studio.data.yamcs.YamcsVType;

public class PVComposite extends Composite implements YamcsAware, ParameterSubscription.Listener {

    private PVInfo pvInfo;

    private StyledText gentimeField;
    private StyledText rectimeField;
    private StyledText statusField;
    private StyledText engValueField;
    private StyledText engTypeField;
    private StyledText rawValueField;
    private StyledText rawTypeField;

    private ParameterSubscription subscription;

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

        YamcsPlugin.addListener(this);
    }

    @Override
    public void changeProcessor(String instance, String processor) {
        if (subscription != null) {
            subscription.cancel(true);
            subscription = null;
        }

        if (processor != null && pvInfo.getParameterInfo() != null) {
            subscription = YamcsPlugin.getYamcsClient().createParameterSubscription();
            subscription.addListener(this);
            subscription.sendMessage(SubscribeParametersRequest.newBuilder()
                    .setInstance(instance)
                    .setProcessor(processor)
                    .setAbortOnInvalid(false)
                    .setUpdateOnExpiration(true)
                    .setSendFromCache(true)
                    .addId(NamedObjectId.newBuilder().setName(pvInfo.getParameterInfo().getQualifiedName()))
                    .build());
        }
    }

    private void createYamcsProperties(ParameterInfo pinfo) {
        createKeyValueTextPair("Yamcs Data Source", capitalize(pinfo.getDataSource().toString()));
        createKeyValueTextPair("Qualified Name", pinfo.getQualifiedName());
        for (var i = 0; i < pinfo.getAliasCount(); i++) {
            var alias = pinfo.getAlias(i);
            var key = (i == 0) ? "Aliases" : null;
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

        var type = pinfo.getType();
        createKeyValueTextPair("Data Encoding", capitalize(type.getDataEncoding().getType().toString()));
        createKeyValueTextPair("Engineering Type", capitalize(type.getEngType()));
        String units = null;
        if (type.getUnitSetCount() > 0) {
            units = "";
            for (var unit : type.getUnitSetList()) {
                units += unit.getUnit() + " ";
            }
            createKeyValueTextPair("Units", units);
        }

        if (type.getDefaultAlarm() != null && type.getDefaultAlarm().getStaticAlarmRangeCount() > 0) {
            createSeparator();
            createHeader("Default Alarm");
            var defaultAlarm = type.getDefaultAlarm();
            createKeyValueTextPair("Min. Violations", "" + defaultAlarm.getMinViolations());

            // Backwards for lower limits
            for (var i = defaultAlarm.getStaticAlarmRangeCount() - 1; i >= 0; i--) {
                var range = defaultAlarm.getStaticAlarmRange(i);
                if (range.hasMinInclusive()) {
                    var label = capitalize(range.getLevel().toString()) + " Low";
                    var limit = new DecimalFormat("#.############").format(range.getMinInclusive());
                    if (units != null) {
                        limit += " " + units;
                    }
                    createKeyValueTextPair(label, limit);
                }
            }

            // Now forwards for upper limits
            for (var range : defaultAlarm.getStaticAlarmRangeList()) {
                if (range.hasMaxInclusive()) {
                    var label = capitalize(range.getLevel().toString()) + " High";
                    var limit = new DecimalFormat("#.############").format(range.getMaxInclusive());
                    if (units != null) {
                        limit += " " + units;
                    }
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
        var pv = pvInfo.getPV();
        var stateInfo = new StringBuilder();
        if (!pv.isStarted()) {
            stateInfo.append("Not started");
        } else if (pv.isConnected()) {
            stateInfo.append("Connected");
        } else {
            stateInfo.append("Connecting");
        }

        createKeyValueTextPair("Name", pv.getName());
        createKeyValueTextPair("State", stateInfo.toString());
        createSeparator();

        if (pv.getValue() != null) {
            createKeyValueTextPair("Last Received Value", pv.getValue().toString());
            var displayInfo = VTypeHelper.getDisplayInfo(pv.getValue());
            if (displayInfo != null) {
                createKeyValueTextPair("Units", displayInfo.getUnits());
                createKeyValueTextPair("Precision", "" + displayInfo.getFormat().getMaximumFractionDigits());
                createKeyValueTextPair("Display Low", "" +
                        TO_STRING_FORMAT.format(displayInfo.getLowerDisplayLimit()));
                createKeyValueTextPair("Display High", "" +
                        TO_STRING_FORMAT.format(displayInfo.getUpperDisplayLimit()));
                createKeyValueTextPair("Alarm Low", "" +
                        TO_STRING_FORMAT.format(displayInfo.getLowerAlarmLimit()));
                createKeyValueTextPair("Warning Low", "" +
                        TO_STRING_FORMAT.format(displayInfo.getLowerWarningLimit()));
                createKeyValueTextPair("Warning High", "" +
                        TO_STRING_FORMAT.format(displayInfo.getUpperWarningLimit()));
                createKeyValueTextPair("Alarm High", "" +
                        TO_STRING_FORMAT.format(displayInfo.getUpperAlarmLimit()));
            }
        }
    }

    private StyledText createKeyValueTextPair(String key, String value) {
        var lbl = new Label(this, SWT.NONE);
        if (key != null) {
            lbl.setText(key + ":");
        }
        var gd = new GridData();
        gd.horizontalAlignment = SWT.END;
        gd.verticalAlignment = SWT.BEGINNING;
        lbl.setLayoutData(gd);

        // StyledText instead of Label, so that text is selectable
        // TODO should wrap, but can't get it to work right now
        var txt = new StyledText(this, SWT.WRAP);
        txt.setBackground(getBackground());
        txt.setEditable(false);
        txt.setCaret(null);
        txt.setText(value);
        txt.setWordWrap(true);
        return txt;
    }

    private void createSeparator() {
        var divider = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
        var gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        divider.setLayoutData(gd);
    }

    private void createHeader(String title) {
        var header = new Label(this, SWT.NONE);
        header.setText(title);
        var gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        header.setLayoutData(gd);
        header.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
    }

    private static String capitalize(String string) {
        var chars = string.toLowerCase().toCharArray();
        var found = false;
        for (var i = 0; i < chars.length; i++) {
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
    public void onData(List<ParameterValue> values) {
        if (isDisposed()) {
            return;
        }
        getDisplay().asyncExec(() -> {
            if (isDisposed()) {
                return;
            }
            for (var pval : values) {
                gentimeField.setText(Instant
                        .ofEpochSecond(pval.getGenerationTime().getSeconds(), pval.getGenerationTime().getNanos())
                        .toString());
                rectimeField.setText(Instant
                        .ofEpochSecond(pval.getAcquisitionTime().getSeconds(), pval.getAcquisitionTime().getNanos())
                        .toString());

                var engValue = YamcsVType.fromYamcs(pval, false);

                var text = engValue.toString();
                if (VTypeHelper.getDisplayInfo(engValue) != null) {
                    var units = VTypeHelper.getDisplayInfo(engValue).getUnits();
                    if (units != null && units.trim().length() > 0) {
                        text = text + " " + units;
                    }
                }
                engValueField.setText(text);
                engTypeField.setText(pval.getEngValue().getType().toString());

                if (pval.hasAcquisitionStatus()) {
                    statusField.setText(pval.getAcquisitionStatus().toString());
                } else {
                    statusField.setText("---");
                }

                if (pval.hasRawValue()) {
                    var rawValue = YamcsVType.fromYamcs(pval, true);
                    rawValueField.setText(rawValue.toString());
                    rawTypeField.setText(pval.getRawValue().getType().toString());
                } else {
                    rawValueField.setText("---");
                    rawTypeField.setText("---");
                }
            }
        });
    }

    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.cancel(true);
        }
        YamcsPlugin.removeListener(this);
        super.dispose();
    }
}
