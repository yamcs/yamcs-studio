package org.csstudio.yamcs.commanding;

import java.util.Date;

import org.csstudio.platform.libs.yamcs.CommandHistoryListener;
import org.csstudio.platform.libs.yamcs.YRegistrar;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.yamcs.protostuff.CommandHistoryEntry;

/**
 * TODO show a friendly message when the thing is still loading
 */
public class TelecommandView extends ViewPart {
    
    private LocalResourceManager resourceManager;
    private Action newCommandAction;
    private Image errorImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);

    @Override
    public void createPartControl(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);
        
        TableColumnLayout tcl = new TableColumnLayout();
        parent.setLayout(tcl);
        TableViewer tableViewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);
        
        TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        nameColumn.getColumn().setText("Command");
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Telecommand) element).name;
            }
            
            @Override
            public Image getImage(Object element) {
                return errorImage;
            }
        });
        tcl.setColumnData(nameColumn.getColumn(), new ColumnWeightData(300));
        
        TableViewerColumn sentColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        sentColumn.getColumn().setText("Sent");
        sentColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Telecommand) element).sent.toString();
            }
        });
        tcl.setColumnData(sentColumn.getColumn(), new ColumnWeightData(100));
        
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        tableViewer.setInput(new Telecommand[]{ /*new Telecommand(new Date(), "Switch_ON")*/ });
        
        newCommandAction = new Action("Add command") {
            @Override
            public void run() {
                int returnCode = new AddTelecommandDialog(parent.getShell()).open();
                
            }
        };
        
        Bundle bundle = FrameworkUtil.getBundle(TelecommandView.class);
        ImageDescriptor desc = ImageDescriptor.createFromURL(FileLocator.find(bundle, new Path("icons/tc_add.png"), null));
        newCommandAction.setImageDescriptor(desc);
        initializeToolBar();
        
        subscribeToUpdates();
    }
    
    private void subscribeToUpdates() {
        YRegistrar.getInstance().addCommandHistoryListener(new CommandHistoryListener() {
            @Override
            public void signalYamcsDisconnected() {
            }
            
            @Override
            public void signalYamcsConnected() {
            }
            
            @Override
            public void processCommandHistoryEntry(CommandHistoryEntry cmdhistEntry) {
                System.out.println("got che in view " + cmdhistEntry);
            }
        });
    }
    
    private void initializeToolBar() {
        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
        toolbarManager.add(newCommandAction);
    }

    @Override
    public void setFocus() {
    }
    
    @Override
    public void dispose() {
        super.dispose();
        resourceManager.dispose();
    }
    
    private static class Telecommand {
        Date sent;
        String name;
        public Telecommand(Date sent, String name) {
            this.sent = sent;
            this.name = name;
        }
    }
}
