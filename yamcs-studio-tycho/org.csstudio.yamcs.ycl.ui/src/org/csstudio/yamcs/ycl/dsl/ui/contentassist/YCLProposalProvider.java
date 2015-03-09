package org.csstudio.yamcs.ycl.dsl.ui.contentassist;

import java.util.Collection;
import java.util.Collections;

import org.csstudio.platform.libs.yamcs.MDBContextListener;
import org.csstudio.platform.libs.yamcs.YamcsPlugin;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.yamcs.xtce.MetaCommand;

/**
 * see http://www.eclipse.org/Xtext/documentation.html#contentAssist on how to customize content assistant
 */
public class YCLProposalProvider extends AbstractYCLProposalProvider {
    
    private Collection<MetaCommand> commands = Collections.emptyList();
    
    public YCLProposalProvider() {
        YamcsPlugin.getDefault().addMdbListener(new MDBContextListener() {
            @Override
            public void onCommandsChanged(Collection<MetaCommand> commandIds) {
                commands = commandIds;
            }
        });
    }
    
    @Override
    public void complete_CommandId(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
        for (MetaCommand cmd : commands) {
            if (!cmd.isAbstract()) {
                ICompletionProposal proposal = createCompletionProposal(cmd.getName() + "()", cmd.getName(), null, context);
                acceptor.accept(proposal);
            }
        }
    }
}
