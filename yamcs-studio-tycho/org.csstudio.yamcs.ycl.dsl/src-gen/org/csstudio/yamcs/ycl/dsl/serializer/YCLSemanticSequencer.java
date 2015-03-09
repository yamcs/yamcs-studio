package org.csstudio.yamcs.ycl.dsl.serializer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.csstudio.yamcs.ycl.dsl.services.YCLGrammarAccess;
import org.csstudio.yamcs.ycl.dsl.ycl.ArgumentAssignment;
import org.csstudio.yamcs.ycl.dsl.ycl.Command;
import org.csstudio.yamcs.ycl.dsl.ycl.CommandId;
import org.csstudio.yamcs.ycl.dsl.ycl.Model;
import org.csstudio.yamcs.ycl.dsl.ycl.YclPackage;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.serializer.acceptor.ISemanticSequenceAcceptor;
import org.eclipse.xtext.serializer.acceptor.SequenceFeeder;
import org.eclipse.xtext.serializer.diagnostic.ISemanticSequencerDiagnosticProvider;
import org.eclipse.xtext.serializer.diagnostic.ISerializationDiagnostic.Acceptor;
import org.eclipse.xtext.serializer.sequencer.AbstractDelegatingSemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.GenericSequencer;
import org.eclipse.xtext.serializer.sequencer.ISemanticNodeProvider.INodesForEObjectProvider;
import org.eclipse.xtext.serializer.sequencer.ISemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService.ValueTransient;

@SuppressWarnings("all")
public class YCLSemanticSequencer extends AbstractDelegatingSemanticSequencer {

	@Inject
	private YCLGrammarAccess grammarAccess;
	
	public void createSequence(EObject context, EObject semanticObject) {
		if(semanticObject.eClass().getEPackage() == YclPackage.eINSTANCE) switch(semanticObject.eClass().getClassifierID()) {
			case YclPackage.ARGUMENT_ASSIGNMENT:
				if(context == grammarAccess.getArgumentAssignmentRule()) {
					sequence_ArgumentAssignment(context, (ArgumentAssignment) semanticObject); 
					return; 
				}
				else break;
			case YclPackage.COMMAND:
				if(context == grammarAccess.getCommandRule()) {
					sequence_Command(context, (Command) semanticObject); 
					return; 
				}
				else break;
			case YclPackage.COMMAND_ID:
				if(context == grammarAccess.getCommandIdRule()) {
					sequence_CommandId(context, (CommandId) semanticObject); 
					return; 
				}
				else break;
			case YclPackage.MODEL:
				if(context == grammarAccess.getModelRule()) {
					sequence_Model(context, (Model) semanticObject); 
					return; 
				}
				else break;
			}
		if (errorAcceptor != null) errorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
	}
	
	/**
	 * Constraint:
	 *     (name=ID value=ArgumentAssignmentValue)
	 */
	protected void sequence_ArgumentAssignment(EObject context, ArgumentAssignment semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, YclPackage.Literals.ARGUMENT_ASSIGNMENT__NAME) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, YclPackage.Literals.ARGUMENT_ASSIGNMENT__NAME));
			if(transientValues.isValueTransient(semanticObject, YclPackage.Literals.ARGUMENT_ASSIGNMENT__VALUE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, YclPackage.Literals.ARGUMENT_ASSIGNMENT__VALUE));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getArgumentAssignmentAccess().getNameIDTerminalRuleCall_0_0(), semanticObject.getName());
		feeder.accept(grammarAccess.getArgumentAssignmentAccess().getValueArgumentAssignmentValueParserRuleCall_2_0(), semanticObject.getValue());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     id=ID
	 */
	protected void sequence_CommandId(EObject context, CommandId semanticObject) {
		if(errorAcceptor != null) {
			if(transientValues.isValueTransient(semanticObject, YclPackage.Literals.COMMAND_ID__ID) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, YclPackage.Literals.COMMAND_ID__ID));
		}
		INodesForEObjectProvider nodes = createNodeProvider(semanticObject);
		SequenceFeeder feeder = createSequencerFeeder(semanticObject, nodes);
		feeder.accept(grammarAccess.getCommandIdAccess().getIdIDTerminalRuleCall_0(), semanticObject.getId());
		feeder.finish();
	}
	
	
	/**
	 * Constraint:
	 *     (name=CommandId assignments+=ArgumentAssignment*)
	 */
	protected void sequence_Command(EObject context, Command semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     commands+=Command*
	 */
	protected void sequence_Model(EObject context, Model semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
}
