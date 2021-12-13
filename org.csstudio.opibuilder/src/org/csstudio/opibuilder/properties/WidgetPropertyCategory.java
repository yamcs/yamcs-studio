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

    WidgetPropertyCategory Image = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Image";
        }
    };

    WidgetPropertyCategory Behavior = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Behavior";
        }
    };

    WidgetPropertyCategory Display = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Display";
        }
    };

    WidgetPropertyCategory Position = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Position";
        }
    };

    WidgetPropertyCategory Misc = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Misc";
        }
    };

    WidgetPropertyCategory Border = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Border";
        }
    };

    WidgetPropertyCategory Basic = new WidgetPropertyCategory() {
        @Override
        public String toString() {
            return "Basic";
        }
    };
}
