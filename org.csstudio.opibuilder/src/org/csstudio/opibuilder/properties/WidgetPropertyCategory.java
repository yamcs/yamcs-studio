/********************************************************************************
 * Copyright (c) 2006, 2021 DESY and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.csstudio.opibuilder.properties;

/**
 * Categories of widget properties.
 */
public interface WidgetPropertyCategory {

    public final static WidgetPropertyCategory Image = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Image";
        }
    };

    public final static WidgetPropertyCategory Behavior = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Behavior";
        }
    };

    public final static WidgetPropertyCategory Display = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Display";
        }
    };

    public final static WidgetPropertyCategory Position = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Position";
        }
    };

    public final static WidgetPropertyCategory Misc = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Misc";
        }
    };

    public final static WidgetPropertyCategory Border = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Border";
        }
    };

    public final static WidgetPropertyCategory Basic = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Basic";
        }
    };
}
