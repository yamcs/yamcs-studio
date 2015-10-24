package org.yamcs.studio.ycl.dsl.ui.contentassist;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.yamcs.protobuf.Mdb.CommandInfo;
import org.yamcs.studio.core.model.CommandingCatalogue;

/**
 * see http://www.eclipse.org/Xtext/documentation.html#contentAssist on how to customize content
 * assistant
 */
public class YCLProposalProvider extends AbstractYCLProposalProvider {

    @Override
    public void complete_CommandId(EObject model, RuleCall ruleCall, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
        for (CommandInfo cmd : CommandingCatalogue.getInstance().getMetaCommands()) {
            if (!cmd.getAbstract()) {
                ICompletionProposal proposal = createCompletionProposal(cmd.getDescription().getQualifiedName() + "()",
                        cmd.getDescription().getQualifiedName(), null, context);
                acceptor.accept(proposal);
            }
        }
    }
}
