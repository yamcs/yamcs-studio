/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.runmode;

import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Perspective for display runtime
 *
 * <p>
 * Allows opening views in "Left", "Right", ... location. .. but RCP API doesn't really allow opening views in a
 * specific location. Instead, this perspective has folders / part stacks named org.csstudio.opibuilder.opiViewLEFT:*,
 * org.csstudio.opibuilder.opiViewRIGHT:*, etc. and views with that ID will then appear in there.
 *
 * <p>
 * As long as this perspective is not dramatically rearranged, all is fine, but the user is of course free to for
 * example move the part stack named org.csstudio.opibuilder.opiViewLEFT:* to the RIGHT, and then this compromise
 * breaks.
 *
 * @author Xihui Chen - Original author
 * @author Kay Kasemir
 */
public class OPIRunnerPerspective implements IPerspectiveFactory {

    public enum Position {
        // Would like DEFAULT as, well, default, showing first in config UI etc.,
        // but config files store by option's index.
        // Changed order impacts existing display files.
        LEFT("Left", OPIView.ID + "LEFT"),
        RIGHT("Right", OPIView.ID + "RIGHT"),
        TOP("Top", OPIView.ID + "TOP"),
        BOTTOM("Bottom", OPIView.ID + "BOTTOM"),
        DETACHED("Detached", OPIView.ID),
        DEFAULT_VIEW("Default", OPIView.ID);

        private String description;
        private String view_id;

        private Position(String description, String view_id) {
            this.description = description;
            this.view_id = view_id;
        }

        /** @return ID of view to be used for displaying in this location */
        public String getOPIViewID() {
            return view_id;
        }

        public static String[] stringValues() {
            final String[] sv = new String[values().length];
            int i = 0;
            for (Position p : values())
                sv[i++] = p.toString();
            return sv;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private static final String SECOND_ID = ":*";

    public final static String ID = "org.csstudio.opibuilder.OPIRuntime.perspective";

    private static final String ID_CONSOLE_VIEW = "org.eclipse.ui.console.ConsoleView";

    static final String ID_EXPLORER = "org.yamcs.studio.explorer.view";

    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editor = layout.getEditorArea();

        IFolderLayout left = layout.createFolder(Position.LEFT.name(), IPageLayout.LEFT, 0.2f, editor);
        left.addPlaceholder(Position.LEFT.getOPIViewID() + SECOND_ID);

        IFolderLayout right = layout.createFolder(Position.RIGHT.name(), IPageLayout.RIGHT, 0.75f, editor);
        right.addPlaceholder(Position.RIGHT.getOPIViewID() + SECOND_ID);

        IFolderLayout top = layout.createFolder(Position.TOP.name(), IPageLayout.TOP, 0.25f, editor);
        top.addPlaceholder(Position.TOP.getOPIViewID() + SECOND_ID);

        IFolderLayout bottom = layout.createFolder(Position.BOTTOM.name(), IPageLayout.BOTTOM, 0.75f, editor);
        bottom.addPlaceholder(Position.BOTTOM.getOPIViewID() + SECOND_ID);

        // Create ordinary view stack for 'DEFAULT_VIEW' close to editor area
        // Alternative hack using internal API:
        // Adds view stack in the editor area, so 'DEFAULT_VIEW' appears
        // similar to editor
        //
        // ModeledPageLayout real_layout = (ModeledPageLayout) layout;
        // real_layout.stackView(OPIView.ID + SECOND_ID, editor, false);
        //
        // .. but such OPIViews are then in the IPageLayout.ID_EDITOR_AREA="org.eclipse.ui.editorss"(!) part,
        // which is linked to Shared Elements/Area, ignored by the perspective,
        // since it's meant for "Editors".
        IFolderLayout center = layout.createFolder(Position.DEFAULT_VIEW.name(), IPageLayout.RIGHT, 0.5f, editor);
        center.addPlaceholder(OPIView.ID + SECOND_ID);

        layout.setEditorAreaVisible(false);

        // Placeholder views show location of folders
        if (PreferencesHelper.showOpiRuntimeStacks()) {
            center.addView(PlaceHolderView.ID + ":CENTER");
            left.addView(PlaceHolderView.ID + ":LEFT");
            right.addView(PlaceHolderView.ID + ":RIGHT");
            top.addView(PlaceHolderView.ID + ":TOP");
            bottom.addView(PlaceHolderView.ID + ":BOTTOM");
        }

        bottom.addView(ID_CONSOLE_VIEW);
        layout.addShowViewShortcut(ID_CONSOLE_VIEW);
        left.addView(ID_EXPLORER);
        layout.addShowViewShortcut(ID_EXPLORER);
    }
}
