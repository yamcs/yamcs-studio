/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 * Copyright (c) 2022 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.persistence;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractContainerModel;
import org.csstudio.opibuilder.model.AbstractLinkingContainerModel;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.model.ConnectionModel;
import org.csstudio.opibuilder.model.DisplayModel;
import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.MacroUtil;
import org.csstudio.opibuilder.util.MacrosInput;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.opibuilder.util.WidgetsService;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.osgi.framework.Version;

/**
 * The utility class for XML related operation.
 */
public class XMLUtil {

    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    public static String XMLTAG_DISPLAY = "display";

    public static String XMLTAG_WIDGET = "widget";

    public static String XMLTAG_CONNECTION = "connection";

    public static Set<String> WIDGET_TAGS = new HashSet<>(
            Arrays.asList(XMLTAG_DISPLAY, XMLTAG_WIDGET, XMLTAG_CONNECTION));

    public static String XMLATTR_TYPEID = "typeId";

    public static String XMLATTR_PROPID = "id";
    public static String XMLATTR_VERSION = "version";

    public static String XMLTAG_WIDGET_UID = AbstractWidgetModel.PROP_WIDGET_UID;
    public static String XMLTAG_OPI_FILE = AbstractLinkingContainerModel.PROP_OPI_FILE;

    /**
     * Flatten a widget to XML element.
     *
     * @param widgetModel
     *            model of the widget
     * @return the XML element
     */
    public static Element widgetToXMLElement(AbstractWidgetModel widgetModel) {

        var result = new Element((widgetModel instanceof DisplayModel) ? XMLTAG_DISPLAY
                : (widgetModel instanceof ConnectionModel) ? XMLTAG_CONNECTION : XMLTAG_WIDGET);
        result.setAttribute(XMLATTR_TYPEID, widgetModel.getTypeID());
        result.setAttribute(XMLATTR_VERSION, widgetModel.getVersion().toString());
        List<String> propIds = new ArrayList<>(widgetModel.getAllPropertyIDs());
        Collections.sort(propIds);
        for (var propId : propIds) {
            if (widgetModel.getProperty(propId).isSavable()) {
                var propElement = new Element(propId);
                widgetModel.getProperty(propId).writeToXML(propElement);
                result.addContent(propElement);
            }
        }

        if (widgetModel instanceof AbstractContainerModel && !(widgetModel instanceof AbstractLinkingContainerModel)) {
            var containerModel = (AbstractContainerModel) widgetModel;
            for (var child : containerModel.getChildren()) {
                result.addContent(widgetToXMLElement(child));
            }
        }

        // convert connections on this displayModel to xml element
        if (widgetModel instanceof DisplayModel && ((DisplayModel) widgetModel).getConnectionList() != null) {
            for (var connectionModel : ((DisplayModel) widgetModel).getConnectionList()) {
                if (!connectionModel.isLoadedFromLinkedOpi()) {
                    var connElement = widgetToXMLElement(connectionModel);
                    result.addContent(connElement);
                }
            }
        }

        return result;
    }

    /**
     * Create and configure an XMLOutputter object.
     *
     * @param prettyFormat
     * @return the XMLOutputter
     */
    private static XMLOutputter getXMLOutputter(boolean prettyFormat) {
        var format = prettyFormat ? Format.getPrettyFormat() : Format.getRawFormat();
        format.setLineSeparator("\n"); // Always use Unix-style line endings.
        var xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(format);
        return xmlOutputter;
    }

    /**
     * Flatten a widget to XML String.
     *
     * @param widgetModel
     *            model of the widget.
     * @param prettyFormat
     *            true if the string is in pretty format
     * @return the XML String
     */
    public static String widgetToXMLString(AbstractWidgetModel widgetModel, boolean prettyFormat) {
        var xmlOutputter = getXMLOutputter(prettyFormat);
        return xmlOutputter.outputString(widgetToXMLElement(widgetModel));
    }

    /**
     * Write widget to an output stream.
     *
     * @param widgetModel
     *            model of the widget
     * @param out
     *            output stream
     * @param prettyFormat
     *            true if in pretty format
     * @throws IOException
     */
    public static void widgetToOutputStream(AbstractWidgetModel widgetModel, OutputStream out, boolean prettyFormat)
            throws IOException {
        var xmlOutputter = getXMLOutputter(prettyFormat);
        out.write(XML_HEADER.getBytes("UTF-8"));
        xmlOutputter.output(widgetToXMLElement(widgetModel), out);
    }

    /**
     * Convert an XML String to widget model
     *
     * @param xmlString
     * @return the widget model
     * @throws Exception
     */
    public static AbstractWidgetModel XMLStringToWidget(String xmlString) throws Exception {
        return XMLElementToWidget(stringToXML(xmlString));
    }

    /**
     * Convert an XML element to widget.
     *
     * @param element
     *            the element
     * @return model of the widget.
     * @throws Exception
     */
    public static AbstractWidgetModel XMLElementToWidget(Element element) throws Exception {
        return XMLElementToWidget(element, null);
    }

    /**
     * Fill the DisplayModel from an OPI file inputstream
     *
     * @param inputStream
     *            the inputstream will be closed in this method before return.
     * @param displayModel.
     *            The {@link DisplayModel} to be filled.
     * @param display
     *            the display in UI Thread.
     * @throws Exception
     */
    public static void fillDisplayModelFromInputStream(InputStream inputStream, DisplayModel displayModel,
            Display display) throws Exception {
        fillDisplayModelFromInputStreamSub(inputStream, displayModel, display, new ArrayList<IPath>(), null);
    }

    /**
     * Fill the DisplayModel from an OPI file inputstream
     *
     * @param inputStream
     *            the inputstream will be closed in this method before return.
     * @param displayModel.
     *            The {@link DisplayModel} to be filled.
     * @param display
     *            the display in UI Thread.
     * @throws Exception
     */
    public static void fillDisplayModelFromInputStream(InputStream inputStream, DisplayModel displayModel,
            Display display, MacrosInput macrosInput_) throws Exception {
        fillDisplayModelFromInputStreamSub(inputStream, displayModel, display, new ArrayList<IPath>(), macrosInput_);
    }

    private static void fillDisplayModelFromInputStreamSub(InputStream inputStream, DisplayModel displayModel,
            Display display, List<IPath> trace, MacrosInput macrosInput_) throws Exception {
        var root = inputStreamToXML(inputStream);
        if (root != null) {
            XMLElementToWidgetSub(root, displayModel, trace, macrosInput_);
        }
        inputStream.close();
    }

    /**
     * Fill the DisplayModel from an OPI file inputstream. In RAP, it must be called in UI Thread.
     *
     * @param inputStream
     *            the inputstream will be closed in this method before return.
     * @param displayModel
     * @throws Exception
     */
    public static void fillDisplayModelFromInputStream(InputStream inputStream, DisplayModel displayModel)
            throws Exception {
        fillDisplayModelFromInputStream(inputStream, displayModel, null);
    }

    /**
     * Construct widget model from XML element. Sometimes it includes filling LinkingContainer and/or construct
     * Connection model between widgets.
     *
     * @param element
     * @param displayModel
     *            the root display model. If root of the element is a display, use this display model as root model
     *            instead of creating a new one. If this is null, a new one will be created.
     * @return the root widget model
     * @throws Exception
     */
    public static AbstractWidgetModel XMLElementToWidget(Element element, DisplayModel displayModel) throws Exception {
        return XMLElementToWidgetSub(element, displayModel, new ArrayList<IPath>(), null);
    }

    private static AbstractWidgetModel XMLElementToWidgetSub(Element element, DisplayModel displayModel,
            List<IPath> trace, MacrosInput macrosInput_) throws Exception {
        if (element == null) {
            return null;
        }

        AbstractWidgetModel result = null;

        if (WIDGET_TAGS.contains(element.getName())) {
            result = fillWidgets(element, displayModel);

            if (result instanceof AbstractContainerModel) {
                fillLinkingContainersSub((AbstractContainerModel) result, trace, macrosInput_);
            }
            fillConnections(element, displayModel);

            return result;
        } else {
            var errorMessage = "Unknown Tag: " + element.getName();
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, errorMessage);
            return null;
        }
    }

    /**
     * Convert XML String to a widget model.
     *
     * @param xmlString
     * @param displayModel
     *            the root display model. If root of the element is a display, use this display model as root model
     *            instead of creating a new one. If this is null, a new one will be created.
     * @throws Exception
     */
    public static AbstractWidgetModel fillWidgetsFromXMLString(String xmlString, DisplayModel displayModel)
            throws Exception {
        return fillWidgets(stringToXML(xmlString), displayModel);
    }

    /**
     * Convert XML Element to a widget model.
     *
     * @param element
     * @param displayModel
     *            the root display model. If root of the element is a display, use this display model as root model
     *            instead of creating a new one. If this is null, a new one will be created.
     * @throws Exception
     */
    public static AbstractWidgetModel fillWidgets(Element element, DisplayModel displayModel) throws Exception {
        if (element == null) {
            return null;
        }

        AbstractWidgetModel rootWidgetModel = null;

        // Determine root widget model
        if (element.getName().equals(XMLTAG_DISPLAY)) {
            if (displayModel != null) {
                rootWidgetModel = displayModel;
            } else {
                rootWidgetModel = new DisplayModel(null);
            }
        } else if (element.getName().equals(XMLTAG_WIDGET)) {
            var typeId = element.getAttributeValue(XMLATTR_TYPEID);
            var desc = WidgetsService.getInstance().getWidgetDescriptor(typeId);
            if (desc != null) {
                rootWidgetModel = desc.getWidgetModel();
            }
            if (rootWidgetModel == null) {
                var errorMessage = NLS.bind("Unknown widget: {0}", typeId);
                ErrorHandlerUtil.handleError(errorMessage, new Exception("Widget does not exist."));
                return null;
            }
        } else if (element.getName().equals(XMLTAG_CONNECTION)) {
            rootWidgetModel = new ConnectionModel(displayModel);
        } else {
            var errorMessage = "Unknown Tag: " + element.getName();
            OPIBuilderPlugin.getLogger().log(Level.SEVERE, errorMessage);
            return null;
        }

        setPropertiesFromXML(element, rootWidgetModel);

        if (rootWidgetModel instanceof AbstractContainerModel) {
            var container = (AbstractContainerModel) rootWidgetModel;
            var children = element.getChildren();
            var iterator = children.iterator();
            while (iterator.hasNext()) {
                var subElement = (Element) iterator.next();
                if (subElement.getName().equals(XMLTAG_WIDGET)) {
                    container.addChild(fillWidgets(subElement, displayModel));
                }
            }
        }

        if (displayModel != null) {
            rootWidgetModel.processVersionDifference(displayModel.getBOYVersion());
        }

        return rootWidgetModel;
    }

    /**
     * Fill all LinkingContainers under the model.
     *
     * @param container
     *            LinkingContainer to be filled.
     * @throws Exception
     */
    public static void fillLinkingContainers(AbstractContainerModel container) throws Exception {
        fillLinkingContainersSub(container, new ArrayList<IPath>(), null);
    }

    private static void fillLinkingContainersSub(AbstractContainerModel container, List<IPath> trace,
            MacrosInput macrosInput_) throws Exception {
        if (container instanceof AbstractLinkingContainerModel) {
            var linkingContainer = (AbstractLinkingContainerModel) container;
            List<IPath> tempTrace = new ArrayList<>();
            tempTrace.addAll(trace);
            fillLinkingContainerSub(linkingContainer, tempTrace, macrosInput_);
        }

        for (var w : container.getAllDescendants()) {
            if (w instanceof AbstractLinkingContainerModel) {
                var linkingContainer = (AbstractLinkingContainerModel) w;
                List<IPath> tempTrace = new ArrayList<>();
                tempTrace.addAll(trace);
                fillLinkingContainerSub(linkingContainer, tempTrace, macrosInput_);
            }
        }
    }

    private static void fillConnections(Element element, DisplayModel displayModel) throws Exception {
        if (element.getName().equals(XMLTAG_CONNECTION)) {
            var result = new ConnectionModel(displayModel);
            setPropertiesFromXML(element, result);
        } else if (element.getName().equals(XMLTAG_DISPLAY)) {
            var children = element.getChildren();
            var iterator = children.iterator();
            while (iterator.hasNext()) {
                var subElement = (Element) iterator.next();
                if (subElement.getName().equals(XMLTAG_CONNECTION)) {
                    fillConnections(subElement, displayModel);
                }
            }
        }
    }

    private static void setPropertiesFromXML(Element element, AbstractWidgetModel model) {
        if (model == null || element == null) {
            return;
        }

        var versionOnFile = element.getAttributeValue(XMLATTR_VERSION);
        model.setVersionOnFile(Version.parseVersion(versionOnFile));

        var children = element.getChildren();
        var iterator = children.iterator();
        var propIdSet = model.getAllPropertyIDs();
        while (iterator.hasNext()) {
            var subElement = (Element) iterator.next();
            // handle property
            if (propIdSet.contains(subElement.getName())) {
                var propId = subElement.getName();
                try {
                    model.setPropertyValue(propId, model.getProperty(propId).readValueFromXML(subElement));
                } catch (Exception e) {
                    var errorMessage = "Failed to read the " + propId + " property for " + model.getName() + ". "
                            + "The default property value will be set instead. \n" + e;
                    // MessageDialog.openError(null, "OPI File format error", errorMessage + "\n" + e.getMessage());
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, errorMessage, e);
                }
            }
        }
    }

    /**
     * Load opi file attached to LinkingContainer widget.
     *
     * @param container
     *            LinkingContainer to be filled.
     * @throws Exception
     */
    public static void fillLinkingContainer(AbstractLinkingContainerModel container) throws Exception {
        fillLinkingContainerSub(container, new ArrayList<IPath>(), null);
    }

    private static Map<String, String> buildMacroMap(AbstractContainerModel model) {
        Map<String, String> macros = new HashMap<>();
        if (model != null) {
            var input = model.getMacrosInput();
            if (input.isInclude_parent_macros()) {
                macros.putAll(buildMacroMap(model.getParent()));
            }
            macros.putAll(input.getMacrosMap());
        }
        return macros;
    }

    private static void fillLinkingContainerSub(AbstractLinkingContainerModel container, List<IPath> trace,
            MacrosInput macrosInput_) throws Exception {

        if (container == null) {
            return;
        }

        if (container.getRootDisplayModel() != null && container.getRootDisplayModel().getOpiFilePath() != null) {
            if (trace.contains(container.getRootDisplayModel().getOpiFilePath())) {
                container.setOPIFilePath("");
                throw new Exception("Opi link contains some loops.\n" + trace.toString());
            } else {
                trace.add(container.getRootDisplayModel().getOpiFilePath());
            }

            var path = container.getOPIFilePath();
            if (path != null && !path.isEmpty()) {
                Map<String, String> macroMap = PreferencesHelper.getMacros();
                if (macrosInput_ != null && macrosInput_.getMacrosMap() != null) {
                    macroMap.putAll(macrosInput_.getMacrosMap());
                }
                macroMap.putAll(buildMacroMap(container));
                var resolvedPath = MacroUtil.replaceMacros(path.toString(), s -> macroMap.get(s));
                path = ResourceUtil.getPathFromString(resolvedPath);

                var inside = new DisplayModel(path);
                inside.setDisplayID(container.getRootDisplayModel(false).getDisplayID());
                inside.setParentDisplayModel(container.getRootDisplayModel());

                try {
                    fillDisplayModelFromInputStreamSub(ResourceUtil.pathToInputStream(path), inside,
                            Display.getCurrent(), trace, macrosInput_);
                } catch (Exception ex) {
                    OPIBuilderPlugin.getLogger().log(Level.WARNING, "Failed to load LinkingContainer opi_file " + path,
                            ex);
                }

                // mark connection as it is loaded from linked opi
                for (var w : inside.getAllDescendants()) {
                    for (var conn : w.getSourceConnections()) {
                        conn.setLoadedFromLinkedOpi(true);
                    }
                }

                AbstractContainerModel loadTarget = inside;

                if (!container.getGroupName().trim().equals("")) {
                    var group = inside.getChildByName(container.getGroupName());
                    if (group != null && group instanceof AbstractContainerModel) {
                        loadTarget = (AbstractContainerModel) group;
                    }
                }

                // container.addChildren(loadTarget.getChildren(), true);

                container.setDisplayModel(inside);
            }
        }
    }

    /**
     * Return the wuid of the closest widget to offset char position in input stream
     *
     * @param in
     *            the OPI file input stream
     * @param offset
     *            the character offset
     * @return String wuid
     * @throws IOException
     */
    public static String findClosestWidgetUid(InputStream in, int offset) throws IOException {
        if (in == null) {
            return null;
        }
        var out = new StringBuffer();
        var br = new BufferedReader(new InputStreamReader(in));
        var buf = new char[1024];
        for (var len = br.read(buf); len > 0; len = br.read(buf)) {
            out.append(buf, 0, len);
        }
        br.close();
        if (offset + XMLUtil.XMLTAG_WIDGET.length() + 2 >= out.length()) {
            // The offset position is too close to the end of file
            // No widget will be found
            return null;
        }
        var widgetElementStart = offset;
        while (widgetElementStart >= 0 && !matchXMLTag(out, widgetElementStart, XMLUtil.XMLTAG_WIDGET)) {
            widgetElementStart--;
        }
        if (widgetElementStart > 0) {
            // corresponding widget element found
            var wuidAttrStart = widgetElementStart + 1;
            // looking for <wuid> before a <widget> or </widget
            var xmlEndTagWidget = "/" + XMLUtil.XMLTAG_WIDGET;
            while (!matchXMLTag(out, wuidAttrStart, XMLUtil.XMLTAG_WIDGET_UID)
                    && !matchXMLTag(out, wuidAttrStart, XMLUtil.XMLTAG_WIDGET)
                    && !matchXMLTag(out, wuidAttrStart, xmlEndTagWidget)) {
                wuidAttrStart++;
            }
            if (matchXMLTag(out, wuidAttrStart, XMLUtil.XMLTAG_WIDGET_UID)) {
                // <wuid> found
                wuidAttrStart = out.indexOf(">", wuidAttrStart);
                if (wuidAttrStart >= 0) {
                    wuidAttrStart++;
                    if (wuidAttrStart < out.length()) {
                        var wuidAttrEnd = out.indexOf("</" + XMLTAG_WIDGET_UID, wuidAttrStart);
                        if (wuidAttrEnd >= 0) {
                            return out.substring(wuidAttrStart, wuidAttrEnd);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Return true if the string starting at offset in sb matches with xmlTag.
     *
     * @param sb
     *            StringBuffer
     * @param offset
     *            int
     * @param xmlTag
     *            String The XML tag name to check without '&lt;' and '&gt;'
     * @return
     */
    private static boolean matchXMLTag(StringBuffer sb, int offset, String xmlTag) {
        if (offset >= sb.length()) {
            return false;
        }
        if (sb.charAt(offset) != '<') {
            return false;
        }
        var indexOfSpace = sb.indexOf(" ", offset);
        var indexOfGt = sb.indexOf(">", offset);
        var indexOfEndTag = Integer.MAX_VALUE;
        if (indexOfSpace >= 0) {
            indexOfEndTag = indexOfSpace;
        }
        if (indexOfGt >= 0 && indexOfGt < indexOfEndTag) {
            indexOfEndTag = indexOfGt;
        }
        if (indexOfEndTag == Integer.MAX_VALUE) {
            return false;
        }
        var potentialTag = sb.substring(offset + 1, indexOfEndTag);
        return potentialTag.equals(xmlTag);
    }

    private static Element inputStreamToXML(InputStream stream) throws JDOMException, IOException {
        var saxBuilder = new SAXBuilder();
        var doc = saxBuilder.build(stream);
        var root = doc.getRootElement();
        return root;
    }

    private static Element stringToXML(String xmlString) throws JDOMException, IOException {
        InputStream stream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
        return inputStreamToXML(stream);
    }
}
