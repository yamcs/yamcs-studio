/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.commanding.cmdhist;

import org.yamcs.client.Acknowledgment;

public class AckTableRecord {

    Acknowledgment acknowledgment;
    CommandHistoryRecord rec;

    AckTableRecord(Acknowledgment acknowledgment, CommandHistoryRecord rec) {
        this.acknowledgment = acknowledgment;
        this.rec = rec;
    }
}
