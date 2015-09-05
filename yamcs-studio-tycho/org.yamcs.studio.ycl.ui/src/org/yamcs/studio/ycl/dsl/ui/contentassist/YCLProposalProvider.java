package org.yamcs.studio.ycl.dsl.ui.contentassist;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.yamcs.studio.core.model.CommandingCatalogue;
import org.yamcs.xtce.MetaCommand;

/**
 * see http://www.eclipse.org/Xtext/documentation.html#contentAssist on how to customize content
 * assistant
 */
public class YCLProposalProvider extends AbstractYCLProposalProvider {

    @Override
    public void complete_CommandId(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
        for (MetaCommand cmd : CommandingCatalogue.getInstance().getMetaCommands()) {
            if (!cmd.isAbstract()) {
                ICompletionProposal proposal = createCompletionProposal(cmd.getName() + "()", cmd.getName(), null, context);
                acceptor.accept(proposal);
            }
        }
    }
}
