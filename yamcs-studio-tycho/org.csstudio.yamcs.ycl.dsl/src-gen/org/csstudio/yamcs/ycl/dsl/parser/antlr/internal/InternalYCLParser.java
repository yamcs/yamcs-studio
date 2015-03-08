package org.csstudio.yamcs.ycl.dsl.parser.antlr.internal; 

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import org.csstudio.yamcs.ycl.dsl.services.YCLGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalYCLParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_INT", "RULE_EXT_INT", "RULE_ID", "RULE_STRING", "RULE_HEX", "RULE_SL_COMMENT", "RULE_WS", "'.'", "'('", "')'", "'='"
    };
    public static final int RULE_HEX=8;
    public static final int RULE_ID=6;
    public static final int RULE_WS=10;
    public static final int RULE_EXT_INT=5;
    public static final int RULE_STRING=7;
    public static final int RULE_SL_COMMENT=9;
    public static final int RULE_INT=4;
    public static final int T__11=11;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int EOF=-1;

    // delegates
    // delegators


        public InternalYCLParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalYCLParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalYCLParser.tokenNames; }
    public String getGrammarFileName() { return "../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g"; }



     	private YCLGrammarAccess grammarAccess;
     	
        public InternalYCLParser(TokenStream input, YCLGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }
        
        @Override
        protected String getFirstRuleName() {
        	return "Model";	
       	}
       	
       	@Override
       	protected YCLGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}



    // $ANTLR start "entryRuleModel"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:67:1: entryRuleModel returns [EObject current=null] : iv_ruleModel= ruleModel EOF ;
    public final EObject entryRuleModel() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModel = null;


        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:68:2: (iv_ruleModel= ruleModel EOF )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:69:2: iv_ruleModel= ruleModel EOF
            {
             newCompositeNode(grammarAccess.getModelRule()); 
            pushFollow(FOLLOW_ruleModel_in_entryRuleModel75);
            iv_ruleModel=ruleModel();

            state._fsp--;

             current =iv_ruleModel; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleModel85); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleModel"


    // $ANTLR start "ruleModel"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:76:1: ruleModel returns [EObject current=null] : ( (lv_commands_0_0= ruleCommand ) )* ;
    public final EObject ruleModel() throws RecognitionException {
        EObject current = null;

        EObject lv_commands_0_0 = null;


         enterRule(); 
            
        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:79:28: ( ( (lv_commands_0_0= ruleCommand ) )* )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:80:1: ( (lv_commands_0_0= ruleCommand ) )*
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:80:1: ( (lv_commands_0_0= ruleCommand ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==RULE_ID) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:81:1: (lv_commands_0_0= ruleCommand )
            	    {
            	    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:81:1: (lv_commands_0_0= ruleCommand )
            	    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:82:3: lv_commands_0_0= ruleCommand
            	    {
            	     
            	    	        newCompositeNode(grammarAccess.getModelAccess().getCommandsCommandParserRuleCall_0()); 
            	    	    
            	    pushFollow(FOLLOW_ruleCommand_in_ruleModel130);
            	    lv_commands_0_0=ruleCommand();

            	    state._fsp--;


            	    	        if (current==null) {
            	    	            current = createModelElementForParent(grammarAccess.getModelRule());
            	    	        }
            	           		add(
            	           			current, 
            	           			"commands",
            	            		lv_commands_0_0, 
            	            		"Command");
            	    	        afterParserOrEnumRuleCall();
            	    	    

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleModel"


    // $ANTLR start "entryRuleREAL"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:106:1: entryRuleREAL returns [String current=null] : iv_ruleREAL= ruleREAL EOF ;
    public final String entryRuleREAL() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleREAL = null;


         
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
        	
        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:110:2: (iv_ruleREAL= ruleREAL EOF )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:111:2: iv_ruleREAL= ruleREAL EOF
            {
             newCompositeNode(grammarAccess.getREALRule()); 
            pushFollow(FOLLOW_ruleREAL_in_entryRuleREAL173);
            iv_ruleREAL=ruleREAL();

            state._fsp--;

             current =iv_ruleREAL.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleREAL184); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "entryRuleREAL"


    // $ANTLR start "ruleREAL"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:121:1: ruleREAL returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_INT_0= RULE_INT kw= '.' (this_EXT_INT_2= RULE_EXT_INT | this_INT_3= RULE_INT ) ) ;
    public final AntlrDatatypeRuleToken ruleREAL() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_INT_0=null;
        Token kw=null;
        Token this_EXT_INT_2=null;
        Token this_INT_3=null;

         enterRule(); 
        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
            
        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:125:28: ( (this_INT_0= RULE_INT kw= '.' (this_EXT_INT_2= RULE_EXT_INT | this_INT_3= RULE_INT ) ) )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:126:1: (this_INT_0= RULE_INT kw= '.' (this_EXT_INT_2= RULE_EXT_INT | this_INT_3= RULE_INT ) )
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:126:1: (this_INT_0= RULE_INT kw= '.' (this_EXT_INT_2= RULE_EXT_INT | this_INT_3= RULE_INT ) )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:126:6: this_INT_0= RULE_INT kw= '.' (this_EXT_INT_2= RULE_EXT_INT | this_INT_3= RULE_INT )
            {
            this_INT_0=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleREAL228); 

            		current.merge(this_INT_0);
                
             
                newLeafNode(this_INT_0, grammarAccess.getREALAccess().getINTTerminalRuleCall_0()); 
                
            kw=(Token)match(input,11,FOLLOW_11_in_ruleREAL246); 

                    current.merge(kw);
                    newLeafNode(kw, grammarAccess.getREALAccess().getFullStopKeyword_1()); 
                
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:139:1: (this_EXT_INT_2= RULE_EXT_INT | this_INT_3= RULE_INT )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==RULE_EXT_INT) ) {
                alt2=1;
            }
            else if ( (LA2_0==RULE_INT) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:139:6: this_EXT_INT_2= RULE_EXT_INT
                    {
                    this_EXT_INT_2=(Token)match(input,RULE_EXT_INT,FOLLOW_RULE_EXT_INT_in_ruleREAL262); 

                    		current.merge(this_EXT_INT_2);
                        
                     
                        newLeafNode(this_EXT_INT_2, grammarAccess.getREALAccess().getEXT_INTTerminalRuleCall_2_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:147:10: this_INT_3= RULE_INT
                    {
                    this_INT_3=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleREAL288); 

                    		current.merge(this_INT_3);
                        
                     
                        newLeafNode(this_INT_3, grammarAccess.getREALAccess().getINTTerminalRuleCall_2_1()); 
                        

                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {

            	myHiddenTokenState.restore();

        }
        return current;
    }
    // $ANTLR end "ruleREAL"


    // $ANTLR start "entryRuleCommand"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:165:1: entryRuleCommand returns [EObject current=null] : iv_ruleCommand= ruleCommand EOF ;
    public final EObject entryRuleCommand() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCommand = null;


        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:166:2: (iv_ruleCommand= ruleCommand EOF )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:167:2: iv_ruleCommand= ruleCommand EOF
            {
             newCompositeNode(grammarAccess.getCommandRule()); 
            pushFollow(FOLLOW_ruleCommand_in_entryRuleCommand338);
            iv_ruleCommand=ruleCommand();

            state._fsp--;

             current =iv_ruleCommand; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleCommand348); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCommand"


    // $ANTLR start "ruleCommand"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:174:1: ruleCommand returns [EObject current=null] : ( ( (lv_name_0_0= RULE_ID ) ) (otherlv_1= '(' ( (lv_assignments_2_0= ruleArgumentAssignment ) )* otherlv_3= ')' )? ) ;
    public final EObject ruleCommand() throws RecognitionException {
        EObject current = null;

        Token lv_name_0_0=null;
        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_assignments_2_0 = null;


         enterRule(); 
            
        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:177:28: ( ( ( (lv_name_0_0= RULE_ID ) ) (otherlv_1= '(' ( (lv_assignments_2_0= ruleArgumentAssignment ) )* otherlv_3= ')' )? ) )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:178:1: ( ( (lv_name_0_0= RULE_ID ) ) (otherlv_1= '(' ( (lv_assignments_2_0= ruleArgumentAssignment ) )* otherlv_3= ')' )? )
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:178:1: ( ( (lv_name_0_0= RULE_ID ) ) (otherlv_1= '(' ( (lv_assignments_2_0= ruleArgumentAssignment ) )* otherlv_3= ')' )? )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:178:2: ( (lv_name_0_0= RULE_ID ) ) (otherlv_1= '(' ( (lv_assignments_2_0= ruleArgumentAssignment ) )* otherlv_3= ')' )?
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:178:2: ( (lv_name_0_0= RULE_ID ) )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:179:1: (lv_name_0_0= RULE_ID )
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:179:1: (lv_name_0_0= RULE_ID )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:180:3: lv_name_0_0= RULE_ID
            {
            lv_name_0_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleCommand390); 

            			newLeafNode(lv_name_0_0, grammarAccess.getCommandAccess().getNameIDTerminalRuleCall_0_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getCommandRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_0_0, 
                    		"ID");
            	    

            }


            }

            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:196:2: (otherlv_1= '(' ( (lv_assignments_2_0= ruleArgumentAssignment ) )* otherlv_3= ')' )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==12) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:196:4: otherlv_1= '(' ( (lv_assignments_2_0= ruleArgumentAssignment ) )* otherlv_3= ')'
                    {
                    otherlv_1=(Token)match(input,12,FOLLOW_12_in_ruleCommand408); 

                        	newLeafNode(otherlv_1, grammarAccess.getCommandAccess().getLeftParenthesisKeyword_1_0());
                        
                    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:200:1: ( (lv_assignments_2_0= ruleArgumentAssignment ) )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==RULE_ID) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:201:1: (lv_assignments_2_0= ruleArgumentAssignment )
                    	    {
                    	    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:201:1: (lv_assignments_2_0= ruleArgumentAssignment )
                    	    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:202:3: lv_assignments_2_0= ruleArgumentAssignment
                    	    {
                    	     
                    	    	        newCompositeNode(grammarAccess.getCommandAccess().getAssignmentsArgumentAssignmentParserRuleCall_1_1_0()); 
                    	    	    
                    	    pushFollow(FOLLOW_ruleArgumentAssignment_in_ruleCommand429);
                    	    lv_assignments_2_0=ruleArgumentAssignment();

                    	    state._fsp--;


                    	    	        if (current==null) {
                    	    	            current = createModelElementForParent(grammarAccess.getCommandRule());
                    	    	        }
                    	           		add(
                    	           			current, 
                    	           			"assignments",
                    	            		lv_assignments_2_0, 
                    	            		"ArgumentAssignment");
                    	    	        afterParserOrEnumRuleCall();
                    	    	    

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);

                    otherlv_3=(Token)match(input,13,FOLLOW_13_in_ruleCommand442); 

                        	newLeafNode(otherlv_3, grammarAccess.getCommandAccess().getRightParenthesisKeyword_1_2());
                        

                    }
                    break;

            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCommand"


    // $ANTLR start "entryRuleArgumentAssignment"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:230:1: entryRuleArgumentAssignment returns [EObject current=null] : iv_ruleArgumentAssignment= ruleArgumentAssignment EOF ;
    public final EObject entryRuleArgumentAssignment() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleArgumentAssignment = null;


        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:231:2: (iv_ruleArgumentAssignment= ruleArgumentAssignment EOF )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:232:2: iv_ruleArgumentAssignment= ruleArgumentAssignment EOF
            {
             newCompositeNode(grammarAccess.getArgumentAssignmentRule()); 
            pushFollow(FOLLOW_ruleArgumentAssignment_in_entryRuleArgumentAssignment480);
            iv_ruleArgumentAssignment=ruleArgumentAssignment();

            state._fsp--;

             current =iv_ruleArgumentAssignment; 
            match(input,EOF,FOLLOW_EOF_in_entryRuleArgumentAssignment490); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleArgumentAssignment"


    // $ANTLR start "ruleArgumentAssignment"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:239:1: ruleArgumentAssignment returns [EObject current=null] : ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '=' ( (lv_value_2_0= ruleArgumentAssignmentValue ) ) ) ;
    public final EObject ruleArgumentAssignment() throws RecognitionException {
        EObject current = null;

        Token lv_name_0_0=null;
        Token otherlv_1=null;
        AntlrDatatypeRuleToken lv_value_2_0 = null;


         enterRule(); 
            
        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:242:28: ( ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '=' ( (lv_value_2_0= ruleArgumentAssignmentValue ) ) ) )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:243:1: ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '=' ( (lv_value_2_0= ruleArgumentAssignmentValue ) ) )
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:243:1: ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '=' ( (lv_value_2_0= ruleArgumentAssignmentValue ) ) )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:243:2: ( (lv_name_0_0= RULE_ID ) ) otherlv_1= '=' ( (lv_value_2_0= ruleArgumentAssignmentValue ) )
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:243:2: ( (lv_name_0_0= RULE_ID ) )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:244:1: (lv_name_0_0= RULE_ID )
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:244:1: (lv_name_0_0= RULE_ID )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:245:3: lv_name_0_0= RULE_ID
            {
            lv_name_0_0=(Token)match(input,RULE_ID,FOLLOW_RULE_ID_in_ruleArgumentAssignment532); 

            			newLeafNode(lv_name_0_0, grammarAccess.getArgumentAssignmentAccess().getNameIDTerminalRuleCall_0_0()); 
            		

            	        if (current==null) {
            	            current = createModelElement(grammarAccess.getArgumentAssignmentRule());
            	        }
                   		setWithLastConsumed(
                   			current, 
                   			"name",
                    		lv_name_0_0, 
                    		"ID");
            	    

            }


            }

            otherlv_1=(Token)match(input,14,FOLLOW_14_in_ruleArgumentAssignment549); 

                	newLeafNode(otherlv_1, grammarAccess.getArgumentAssignmentAccess().getEqualsSignKeyword_1());
                
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:265:1: ( (lv_value_2_0= ruleArgumentAssignmentValue ) )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:266:1: (lv_value_2_0= ruleArgumentAssignmentValue )
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:266:1: (lv_value_2_0= ruleArgumentAssignmentValue )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:267:3: lv_value_2_0= ruleArgumentAssignmentValue
            {
             
            	        newCompositeNode(grammarAccess.getArgumentAssignmentAccess().getValueArgumentAssignmentValueParserRuleCall_2_0()); 
            	    
            pushFollow(FOLLOW_ruleArgumentAssignmentValue_in_ruleArgumentAssignment570);
            lv_value_2_0=ruleArgumentAssignmentValue();

            state._fsp--;


            	        if (current==null) {
            	            current = createModelElementForParent(grammarAccess.getArgumentAssignmentRule());
            	        }
                   		set(
                   			current, 
                   			"value",
                    		lv_value_2_0, 
                    		"ArgumentAssignmentValue");
            	        afterParserOrEnumRuleCall();
            	    

            }


            }


            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleArgumentAssignment"


    // $ANTLR start "entryRuleArgumentAssignmentValue"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:291:1: entryRuleArgumentAssignmentValue returns [String current=null] : iv_ruleArgumentAssignmentValue= ruleArgumentAssignmentValue EOF ;
    public final String entryRuleArgumentAssignmentValue() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleArgumentAssignmentValue = null;


        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:292:2: (iv_ruleArgumentAssignmentValue= ruleArgumentAssignmentValue EOF )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:293:2: iv_ruleArgumentAssignmentValue= ruleArgumentAssignmentValue EOF
            {
             newCompositeNode(grammarAccess.getArgumentAssignmentValueRule()); 
            pushFollow(FOLLOW_ruleArgumentAssignmentValue_in_entryRuleArgumentAssignmentValue607);
            iv_ruleArgumentAssignmentValue=ruleArgumentAssignmentValue();

            state._fsp--;

             current =iv_ruleArgumentAssignmentValue.getText(); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleArgumentAssignmentValue618); 

            }

        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleArgumentAssignmentValue"


    // $ANTLR start "ruleArgumentAssignmentValue"
    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:300:1: ruleArgumentAssignmentValue returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (this_STRING_0= RULE_STRING | this_INT_1= RULE_INT | this_HEX_2= RULE_HEX | this_REAL_3= ruleREAL ) ;
    public final AntlrDatatypeRuleToken ruleArgumentAssignmentValue() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_STRING_0=null;
        Token this_INT_1=null;
        Token this_HEX_2=null;
        AntlrDatatypeRuleToken this_REAL_3 = null;


         enterRule(); 
            
        try {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:303:28: ( (this_STRING_0= RULE_STRING | this_INT_1= RULE_INT | this_HEX_2= RULE_HEX | this_REAL_3= ruleREAL ) )
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:304:1: (this_STRING_0= RULE_STRING | this_INT_1= RULE_INT | this_HEX_2= RULE_HEX | this_REAL_3= ruleREAL )
            {
            // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:304:1: (this_STRING_0= RULE_STRING | this_INT_1= RULE_INT | this_HEX_2= RULE_HEX | this_REAL_3= ruleREAL )
            int alt5=4;
            switch ( input.LA(1) ) {
            case RULE_STRING:
                {
                alt5=1;
                }
                break;
            case RULE_INT:
                {
                int LA5_2 = input.LA(2);

                if ( (LA5_2==EOF||LA5_2==RULE_ID||LA5_2==13) ) {
                    alt5=2;
                }
                else if ( (LA5_2==11) ) {
                    alt5=4;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 2, input);

                    throw nvae;
                }
                }
                break;
            case RULE_HEX:
                {
                alt5=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:304:6: this_STRING_0= RULE_STRING
                    {
                    this_STRING_0=(Token)match(input,RULE_STRING,FOLLOW_RULE_STRING_in_ruleArgumentAssignmentValue658); 

                    		current.merge(this_STRING_0);
                        
                     
                        newLeafNode(this_STRING_0, grammarAccess.getArgumentAssignmentValueAccess().getSTRINGTerminalRuleCall_0()); 
                        

                    }
                    break;
                case 2 :
                    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:312:10: this_INT_1= RULE_INT
                    {
                    this_INT_1=(Token)match(input,RULE_INT,FOLLOW_RULE_INT_in_ruleArgumentAssignmentValue684); 

                    		current.merge(this_INT_1);
                        
                     
                        newLeafNode(this_INT_1, grammarAccess.getArgumentAssignmentValueAccess().getINTTerminalRuleCall_1()); 
                        

                    }
                    break;
                case 3 :
                    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:320:10: this_HEX_2= RULE_HEX
                    {
                    this_HEX_2=(Token)match(input,RULE_HEX,FOLLOW_RULE_HEX_in_ruleArgumentAssignmentValue710); 

                    		current.merge(this_HEX_2);
                        
                     
                        newLeafNode(this_HEX_2, grammarAccess.getArgumentAssignmentValueAccess().getHEXTerminalRuleCall_2()); 
                        

                    }
                    break;
                case 4 :
                    // ../org.csstudio.yamcs.ycl.dsl/src-gen/org/csstudio/yamcs/ycl/dsl/parser/antlr/internal/InternalYCL.g:329:5: this_REAL_3= ruleREAL
                    {
                     
                            newCompositeNode(grammarAccess.getArgumentAssignmentValueAccess().getREALParserRuleCall_3()); 
                        
                    pushFollow(FOLLOW_ruleREAL_in_ruleArgumentAssignmentValue743);
                    this_REAL_3=ruleREAL();

                    state._fsp--;


                    		current.merge(this_REAL_3);
                        
                     
                            afterParserOrEnumRuleCall();
                        

                    }
                    break;

            }


            }

             leaveRule(); 
        }
         
            catch (RecognitionException re) { 
                recover(input,re); 
                appendSkippedTokens();
            } 
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleArgumentAssignmentValue"

    // Delegated rules


 

    public static final BitSet FOLLOW_ruleModel_in_entryRuleModel75 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleModel85 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleCommand_in_ruleModel130 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_ruleREAL_in_entryRuleREAL173 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleREAL184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleREAL228 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_ruleREAL246 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_RULE_EXT_INT_in_ruleREAL262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleREAL288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleCommand_in_entryRuleCommand338 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleCommand348 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleCommand390 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_12_in_ruleCommand408 = new BitSet(new long[]{0x0000000000002040L});
    public static final BitSet FOLLOW_ruleArgumentAssignment_in_ruleCommand429 = new BitSet(new long[]{0x0000000000002040L});
    public static final BitSet FOLLOW_13_in_ruleCommand442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignment_in_entryRuleArgumentAssignment480 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleArgumentAssignment490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_ruleArgumentAssignment532 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_ruleArgumentAssignment549 = new BitSet(new long[]{0x0000000000000190L});
    public static final BitSet FOLLOW_ruleArgumentAssignmentValue_in_ruleArgumentAssignment570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignmentValue_in_entryRuleArgumentAssignmentValue607 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleArgumentAssignmentValue618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_STRING_in_ruleArgumentAssignmentValue658 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_ruleArgumentAssignmentValue684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_HEX_in_ruleArgumentAssignmentValue710 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleREAL_in_ruleArgumentAssignmentValue743 = new BitSet(new long[]{0x0000000000000002L});

}