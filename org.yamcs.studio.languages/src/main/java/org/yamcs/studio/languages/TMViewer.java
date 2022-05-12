/*******************************************************************************
 * Copyright (c) 2022 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.languages;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.registry.IGrammarRegistryManager;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.registry.WorkingCopyGrammarRegistryManager;
import org.eclipse.tm4e.ui.text.TMPresentationReconciler;
import org.eclipse.tm4e.ui.themes.ITheme;

public class TMViewer extends SourceViewer {

    private final TMPresentationReconciler reconciler = new TMPresentationReconciler();

    private IGrammarRegistryManager grammarRegistryManager = new WorkingCopyGrammarRegistryManager(
            TMEclipseRegistryPlugin.getGrammarRegistryManager());

    public TMViewer(Composite parent, IVerticalRuler ruler, int styles) {
        this(parent, ruler, null, false, styles);
    }

    public TMViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler,
            boolean showAnnotationsOverview, int styles) {
        super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
        configure(new JavaScriptSourceViewerConfiguration());
    }

    public void loadJavaScriptGrammar() {
        setGrammar(grammarRegistryManager.getGrammarForScope("source.js"));
    }

    public void loadPythonGrammar() {
        setGrammar(grammarRegistryManager.getGrammarForScope("source.py"));
    }

    public void setGrammar(IGrammar grammar) {
        reconciler.setGrammar(grammar);
        if (getDocument() == null) {
            super.setDocument(new Document());
        }
    }

    public void setTheme(ITheme theme) {
        reconciler.setTheme(theme);
        var styledText = getTextWidget();
        styledText.setForeground(null);
        styledText.setBackground(null);
        theme.initializeViewerColors(styledText);
        getTextWidget().setFont(JFaceResources.getTextFont());
    }

    public void setText(String text) {
        if (getDocument() == null) {
            super.setDocument(new Document());
        }
        getDocument().set(text);
    }

    public String getText() {
        if (getDocument() == null) {
            return "";
        }
        return getDocument().get();
    }

    private final class JavaScriptSourceViewerConfiguration extends SourceViewerConfiguration {
        @Override
        public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
            return reconciler;
        }
    }
}
