package org.csstudio.yamcs.ycl.dsl.ui.contentassist.antlr.internal; 

import java.io.InputStream;
import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.AbstractInternalContentAssistParser;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.DFA;
import org.csstudio.yamcs.ycl.dsl.services.YCLGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalYCLParser extends AbstractInternalContentAssistParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_EXT_INT", "RULE_INT", "RULE_STRING", "RULE_HEX", "RULE_ID", "RULE_SL_COMMENT", "RULE_WS", "'.'", "'('", "')'", "'='"
    };
    public static final int RULE_HEX=7;
    public static final int RULE_ID=8;
    public static final int RULE_WS=10;
    public static final int RULE_EXT_INT=4;
    public static final int RULE_STRING=6;
    public static final int RULE_SL_COMMENT=9;
    public static final int RULE_INT=5;
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
    public String getGrammarFileName() { return "../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g"; }


     
     	private YCLGrammarAccess grammarAccess;
     	
        public void setGrammarAccess(YCLGrammarAccess grammarAccess) {
        	this.grammarAccess = grammarAccess;
        }
        
        @Override
        protected Grammar getGrammar() {
        	return grammarAccess.getGrammar();
        }
        
        @Override
        protected String getValueForTokenName(String tokenName) {
        	return tokenName;
        }




    // $ANTLR start "entryRuleModel"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:60:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:61:1: ( ruleModel EOF )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:62:1: ruleModel EOF
            {
             before(grammarAccess.getModelRule()); 
            pushFollow(FOLLOW_ruleModel_in_entryRuleModel61);
            ruleModel();

            state._fsp--;

             after(grammarAccess.getModelRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleModel68); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleModel"


    // $ANTLR start "ruleModel"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:69:1: ruleModel : ( ( rule__Model__CommandsAssignment )* ) ;
    public final void ruleModel() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:73:2: ( ( ( rule__Model__CommandsAssignment )* ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:74:1: ( ( rule__Model__CommandsAssignment )* )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:74:1: ( ( rule__Model__CommandsAssignment )* )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:75:1: ( rule__Model__CommandsAssignment )*
            {
             before(grammarAccess.getModelAccess().getCommandsAssignment()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:76:1: ( rule__Model__CommandsAssignment )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==RULE_ID) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:76:2: rule__Model__CommandsAssignment
            	    {
            	    pushFollow(FOLLOW_rule__Model__CommandsAssignment_in_ruleModel94);
            	    rule__Model__CommandsAssignment();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

             after(grammarAccess.getModelAccess().getCommandsAssignment()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleModel"


    // $ANTLR start "entryRuleREAL"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:88:1: entryRuleREAL : ruleREAL EOF ;
    public final void entryRuleREAL() throws RecognitionException {

        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:92:1: ( ruleREAL EOF )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:93:1: ruleREAL EOF
            {
             before(grammarAccess.getREALRule()); 
            pushFollow(FOLLOW_ruleREAL_in_entryRuleREAL127);
            ruleREAL();

            state._fsp--;

             after(grammarAccess.getREALRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleREAL134); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	myHiddenTokenState.restore();

        }
        return ;
    }
    // $ANTLR end "entryRuleREAL"


    // $ANTLR start "ruleREAL"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:103:1: ruleREAL : ( ( rule__REAL__Group__0 ) ) ;
    public final void ruleREAL() throws RecognitionException {

        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:108:2: ( ( ( rule__REAL__Group__0 ) ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:109:1: ( ( rule__REAL__Group__0 ) )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:109:1: ( ( rule__REAL__Group__0 ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:110:1: ( rule__REAL__Group__0 )
            {
             before(grammarAccess.getREALAccess().getGroup()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:111:1: ( rule__REAL__Group__0 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:111:2: rule__REAL__Group__0
            {
            pushFollow(FOLLOW_rule__REAL__Group__0_in_ruleREAL164);
            rule__REAL__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getREALAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);
            	myHiddenTokenState.restore();

        }
        return ;
    }
    // $ANTLR end "ruleREAL"


    // $ANTLR start "entryRuleCommand"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:124:1: entryRuleCommand : ruleCommand EOF ;
    public final void entryRuleCommand() throws RecognitionException {
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:125:1: ( ruleCommand EOF )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:126:1: ruleCommand EOF
            {
             before(grammarAccess.getCommandRule()); 
            pushFollow(FOLLOW_ruleCommand_in_entryRuleCommand191);
            ruleCommand();

            state._fsp--;

             after(grammarAccess.getCommandRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleCommand198); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleCommand"


    // $ANTLR start "ruleCommand"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:133:1: ruleCommand : ( ( rule__Command__Group__0 ) ) ;
    public final void ruleCommand() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:137:2: ( ( ( rule__Command__Group__0 ) ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:138:1: ( ( rule__Command__Group__0 ) )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:138:1: ( ( rule__Command__Group__0 ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:139:1: ( rule__Command__Group__0 )
            {
             before(grammarAccess.getCommandAccess().getGroup()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:140:1: ( rule__Command__Group__0 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:140:2: rule__Command__Group__0
            {
            pushFollow(FOLLOW_rule__Command__Group__0_in_ruleCommand224);
            rule__Command__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getCommandAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleCommand"


    // $ANTLR start "entryRuleArgumentAssignment"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:152:1: entryRuleArgumentAssignment : ruleArgumentAssignment EOF ;
    public final void entryRuleArgumentAssignment() throws RecognitionException {
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:153:1: ( ruleArgumentAssignment EOF )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:154:1: ruleArgumentAssignment EOF
            {
             before(grammarAccess.getArgumentAssignmentRule()); 
            pushFollow(FOLLOW_ruleArgumentAssignment_in_entryRuleArgumentAssignment251);
            ruleArgumentAssignment();

            state._fsp--;

             after(grammarAccess.getArgumentAssignmentRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleArgumentAssignment258); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleArgumentAssignment"


    // $ANTLR start "ruleArgumentAssignment"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:161:1: ruleArgumentAssignment : ( ( rule__ArgumentAssignment__Group__0 ) ) ;
    public final void ruleArgumentAssignment() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:165:2: ( ( ( rule__ArgumentAssignment__Group__0 ) ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:166:1: ( ( rule__ArgumentAssignment__Group__0 ) )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:166:1: ( ( rule__ArgumentAssignment__Group__0 ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:167:1: ( rule__ArgumentAssignment__Group__0 )
            {
             before(grammarAccess.getArgumentAssignmentAccess().getGroup()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:168:1: ( rule__ArgumentAssignment__Group__0 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:168:2: rule__ArgumentAssignment__Group__0
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__0_in_ruleArgumentAssignment284);
            rule__ArgumentAssignment__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getArgumentAssignmentAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleArgumentAssignment"


    // $ANTLR start "entryRuleArgumentAssignmentValue"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:180:1: entryRuleArgumentAssignmentValue : ruleArgumentAssignmentValue EOF ;
    public final void entryRuleArgumentAssignmentValue() throws RecognitionException {
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:181:1: ( ruleArgumentAssignmentValue EOF )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:182:1: ruleArgumentAssignmentValue EOF
            {
             before(grammarAccess.getArgumentAssignmentValueRule()); 
            pushFollow(FOLLOW_ruleArgumentAssignmentValue_in_entryRuleArgumentAssignmentValue311);
            ruleArgumentAssignmentValue();

            state._fsp--;

             after(grammarAccess.getArgumentAssignmentValueRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleArgumentAssignmentValue318); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleArgumentAssignmentValue"


    // $ANTLR start "ruleArgumentAssignmentValue"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:189:1: ruleArgumentAssignmentValue : ( ( rule__ArgumentAssignmentValue__Alternatives ) ) ;
    public final void ruleArgumentAssignmentValue() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:193:2: ( ( ( rule__ArgumentAssignmentValue__Alternatives ) ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:194:1: ( ( rule__ArgumentAssignmentValue__Alternatives ) )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:194:1: ( ( rule__ArgumentAssignmentValue__Alternatives ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:195:1: ( rule__ArgumentAssignmentValue__Alternatives )
            {
             before(grammarAccess.getArgumentAssignmentValueAccess().getAlternatives()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:196:1: ( rule__ArgumentAssignmentValue__Alternatives )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:196:2: rule__ArgumentAssignmentValue__Alternatives
            {
            pushFollow(FOLLOW_rule__ArgumentAssignmentValue__Alternatives_in_ruleArgumentAssignmentValue344);
            rule__ArgumentAssignmentValue__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getArgumentAssignmentValueAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleArgumentAssignmentValue"


    // $ANTLR start "rule__REAL__Alternatives_2"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:208:1: rule__REAL__Alternatives_2 : ( ( RULE_EXT_INT ) | ( RULE_INT ) );
    public final void rule__REAL__Alternatives_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:212:1: ( ( RULE_EXT_INT ) | ( RULE_INT ) )
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
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:213:1: ( RULE_EXT_INT )
                    {
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:213:1: ( RULE_EXT_INT )
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:214:1: RULE_EXT_INT
                    {
                     before(grammarAccess.getREALAccess().getEXT_INTTerminalRuleCall_2_0()); 
                    match(input,RULE_EXT_INT,FOLLOW_RULE_EXT_INT_in_rule__REAL__Alternatives_2380); 
                     after(grammarAccess.getREALAccess().getEXT_INTTerminalRuleCall_2_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:219:6: ( RULE_INT )
                    {
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:219:6: ( RULE_INT )
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:220:1: RULE_INT
                    {
                     before(grammarAccess.getREALAccess().getINTTerminalRuleCall_2_1()); 
                    match(input,RULE_INT,FOLLOW_RULE_INT_in_rule__REAL__Alternatives_2397); 
                     after(grammarAccess.getREALAccess().getINTTerminalRuleCall_2_1()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__REAL__Alternatives_2"


    // $ANTLR start "rule__ArgumentAssignmentValue__Alternatives"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:230:1: rule__ArgumentAssignmentValue__Alternatives : ( ( RULE_STRING ) | ( RULE_INT ) | ( RULE_HEX ) | ( ruleREAL ) );
    public final void rule__ArgumentAssignmentValue__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:234:1: ( ( RULE_STRING ) | ( RULE_INT ) | ( RULE_HEX ) | ( ruleREAL ) )
            int alt3=4;
            switch ( input.LA(1) ) {
            case RULE_STRING:
                {
                alt3=1;
                }
                break;
            case RULE_INT:
                {
                int LA3_2 = input.LA(2);

                if ( (LA3_2==11) ) {
                    alt3=4;
                }
                else if ( (LA3_2==EOF||LA3_2==RULE_ID||LA3_2==13) ) {
                    alt3=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 2, input);

                    throw nvae;
                }
                }
                break;
            case RULE_HEX:
                {
                alt3=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:235:1: ( RULE_STRING )
                    {
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:235:1: ( RULE_STRING )
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:236:1: RULE_STRING
                    {
                     before(grammarAccess.getArgumentAssignmentValueAccess().getSTRINGTerminalRuleCall_0()); 
                    match(input,RULE_STRING,FOLLOW_RULE_STRING_in_rule__ArgumentAssignmentValue__Alternatives429); 
                     after(grammarAccess.getArgumentAssignmentValueAccess().getSTRINGTerminalRuleCall_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:241:6: ( RULE_INT )
                    {
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:241:6: ( RULE_INT )
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:242:1: RULE_INT
                    {
                     before(grammarAccess.getArgumentAssignmentValueAccess().getINTTerminalRuleCall_1()); 
                    match(input,RULE_INT,FOLLOW_RULE_INT_in_rule__ArgumentAssignmentValue__Alternatives446); 
                     after(grammarAccess.getArgumentAssignmentValueAccess().getINTTerminalRuleCall_1()); 

                    }


                    }
                    break;
                case 3 :
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:247:6: ( RULE_HEX )
                    {
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:247:6: ( RULE_HEX )
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:248:1: RULE_HEX
                    {
                     before(grammarAccess.getArgumentAssignmentValueAccess().getHEXTerminalRuleCall_2()); 
                    match(input,RULE_HEX,FOLLOW_RULE_HEX_in_rule__ArgumentAssignmentValue__Alternatives463); 
                     after(grammarAccess.getArgumentAssignmentValueAccess().getHEXTerminalRuleCall_2()); 

                    }


                    }
                    break;
                case 4 :
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:253:6: ( ruleREAL )
                    {
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:253:6: ( ruleREAL )
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:254:1: ruleREAL
                    {
                     before(grammarAccess.getArgumentAssignmentValueAccess().getREALParserRuleCall_3()); 
                    pushFollow(FOLLOW_ruleREAL_in_rule__ArgumentAssignmentValue__Alternatives480);
                    ruleREAL();

                    state._fsp--;

                     after(grammarAccess.getArgumentAssignmentValueAccess().getREALParserRuleCall_3()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ArgumentAssignmentValue__Alternatives"


    // $ANTLR start "rule__REAL__Group__0"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:266:1: rule__REAL__Group__0 : rule__REAL__Group__0__Impl rule__REAL__Group__1 ;
    public final void rule__REAL__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:270:1: ( rule__REAL__Group__0__Impl rule__REAL__Group__1 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:271:2: rule__REAL__Group__0__Impl rule__REAL__Group__1
            {
            pushFollow(FOLLOW_rule__REAL__Group__0__Impl_in_rule__REAL__Group__0510);
            rule__REAL__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__REAL__Group__1_in_rule__REAL__Group__0513);
            rule__REAL__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__REAL__Group__0"


    // $ANTLR start "rule__REAL__Group__0__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:278:1: rule__REAL__Group__0__Impl : ( RULE_INT ) ;
    public final void rule__REAL__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:282:1: ( ( RULE_INT ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:283:1: ( RULE_INT )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:283:1: ( RULE_INT )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:284:1: RULE_INT
            {
             before(grammarAccess.getREALAccess().getINTTerminalRuleCall_0()); 
            match(input,RULE_INT,FOLLOW_RULE_INT_in_rule__REAL__Group__0__Impl540); 
             after(grammarAccess.getREALAccess().getINTTerminalRuleCall_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__REAL__Group__0__Impl"


    // $ANTLR start "rule__REAL__Group__1"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:295:1: rule__REAL__Group__1 : rule__REAL__Group__1__Impl rule__REAL__Group__2 ;
    public final void rule__REAL__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:299:1: ( rule__REAL__Group__1__Impl rule__REAL__Group__2 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:300:2: rule__REAL__Group__1__Impl rule__REAL__Group__2
            {
            pushFollow(FOLLOW_rule__REAL__Group__1__Impl_in_rule__REAL__Group__1569);
            rule__REAL__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__REAL__Group__2_in_rule__REAL__Group__1572);
            rule__REAL__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__REAL__Group__1"


    // $ANTLR start "rule__REAL__Group__1__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:307:1: rule__REAL__Group__1__Impl : ( '.' ) ;
    public final void rule__REAL__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:311:1: ( ( '.' ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:312:1: ( '.' )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:312:1: ( '.' )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:313:1: '.'
            {
             before(grammarAccess.getREALAccess().getFullStopKeyword_1()); 
            match(input,11,FOLLOW_11_in_rule__REAL__Group__1__Impl600); 
             after(grammarAccess.getREALAccess().getFullStopKeyword_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__REAL__Group__1__Impl"


    // $ANTLR start "rule__REAL__Group__2"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:326:1: rule__REAL__Group__2 : rule__REAL__Group__2__Impl ;
    public final void rule__REAL__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:330:1: ( rule__REAL__Group__2__Impl )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:331:2: rule__REAL__Group__2__Impl
            {
            pushFollow(FOLLOW_rule__REAL__Group__2__Impl_in_rule__REAL__Group__2631);
            rule__REAL__Group__2__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__REAL__Group__2"


    // $ANTLR start "rule__REAL__Group__2__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:337:1: rule__REAL__Group__2__Impl : ( ( rule__REAL__Alternatives_2 ) ) ;
    public final void rule__REAL__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:341:1: ( ( ( rule__REAL__Alternatives_2 ) ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:342:1: ( ( rule__REAL__Alternatives_2 ) )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:342:1: ( ( rule__REAL__Alternatives_2 ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:343:1: ( rule__REAL__Alternatives_2 )
            {
             before(grammarAccess.getREALAccess().getAlternatives_2()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:344:1: ( rule__REAL__Alternatives_2 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:344:2: rule__REAL__Alternatives_2
            {
            pushFollow(FOLLOW_rule__REAL__Alternatives_2_in_rule__REAL__Group__2__Impl658);
            rule__REAL__Alternatives_2();

            state._fsp--;


            }

             after(grammarAccess.getREALAccess().getAlternatives_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__REAL__Group__2__Impl"


    // $ANTLR start "rule__Command__Group__0"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:360:1: rule__Command__Group__0 : rule__Command__Group__0__Impl rule__Command__Group__1 ;
    public final void rule__Command__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:364:1: ( rule__Command__Group__0__Impl rule__Command__Group__1 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:365:2: rule__Command__Group__0__Impl rule__Command__Group__1
            {
            pushFollow(FOLLOW_rule__Command__Group__0__Impl_in_rule__Command__Group__0694);
            rule__Command__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Command__Group__1_in_rule__Command__Group__0697);
            rule__Command__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group__0"


    // $ANTLR start "rule__Command__Group__0__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:372:1: rule__Command__Group__0__Impl : ( ( rule__Command__NameAssignment_0 ) ) ;
    public final void rule__Command__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:376:1: ( ( ( rule__Command__NameAssignment_0 ) ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:377:1: ( ( rule__Command__NameAssignment_0 ) )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:377:1: ( ( rule__Command__NameAssignment_0 ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:378:1: ( rule__Command__NameAssignment_0 )
            {
             before(grammarAccess.getCommandAccess().getNameAssignment_0()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:379:1: ( rule__Command__NameAssignment_0 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:379:2: rule__Command__NameAssignment_0
            {
            pushFollow(FOLLOW_rule__Command__NameAssignment_0_in_rule__Command__Group__0__Impl724);
            rule__Command__NameAssignment_0();

            state._fsp--;


            }

             after(grammarAccess.getCommandAccess().getNameAssignment_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group__0__Impl"


    // $ANTLR start "rule__Command__Group__1"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:389:1: rule__Command__Group__1 : rule__Command__Group__1__Impl ;
    public final void rule__Command__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:393:1: ( rule__Command__Group__1__Impl )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:394:2: rule__Command__Group__1__Impl
            {
            pushFollow(FOLLOW_rule__Command__Group__1__Impl_in_rule__Command__Group__1754);
            rule__Command__Group__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group__1"


    // $ANTLR start "rule__Command__Group__1__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:400:1: rule__Command__Group__1__Impl : ( ( rule__Command__Group_1__0 )? ) ;
    public final void rule__Command__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:404:1: ( ( ( rule__Command__Group_1__0 )? ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:405:1: ( ( rule__Command__Group_1__0 )? )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:405:1: ( ( rule__Command__Group_1__0 )? )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:406:1: ( rule__Command__Group_1__0 )?
            {
             before(grammarAccess.getCommandAccess().getGroup_1()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:407:1: ( rule__Command__Group_1__0 )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==12) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:407:2: rule__Command__Group_1__0
                    {
                    pushFollow(FOLLOW_rule__Command__Group_1__0_in_rule__Command__Group__1__Impl781);
                    rule__Command__Group_1__0();

                    state._fsp--;


                    }
                    break;

            }

             after(grammarAccess.getCommandAccess().getGroup_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group__1__Impl"


    // $ANTLR start "rule__Command__Group_1__0"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:421:1: rule__Command__Group_1__0 : rule__Command__Group_1__0__Impl rule__Command__Group_1__1 ;
    public final void rule__Command__Group_1__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:425:1: ( rule__Command__Group_1__0__Impl rule__Command__Group_1__1 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:426:2: rule__Command__Group_1__0__Impl rule__Command__Group_1__1
            {
            pushFollow(FOLLOW_rule__Command__Group_1__0__Impl_in_rule__Command__Group_1__0816);
            rule__Command__Group_1__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Command__Group_1__1_in_rule__Command__Group_1__0819);
            rule__Command__Group_1__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group_1__0"


    // $ANTLR start "rule__Command__Group_1__0__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:433:1: rule__Command__Group_1__0__Impl : ( '(' ) ;
    public final void rule__Command__Group_1__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:437:1: ( ( '(' ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:438:1: ( '(' )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:438:1: ( '(' )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:439:1: '('
            {
             before(grammarAccess.getCommandAccess().getLeftParenthesisKeyword_1_0()); 
            match(input,12,FOLLOW_12_in_rule__Command__Group_1__0__Impl847); 
             after(grammarAccess.getCommandAccess().getLeftParenthesisKeyword_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group_1__0__Impl"


    // $ANTLR start "rule__Command__Group_1__1"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:452:1: rule__Command__Group_1__1 : rule__Command__Group_1__1__Impl rule__Command__Group_1__2 ;
    public final void rule__Command__Group_1__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:456:1: ( rule__Command__Group_1__1__Impl rule__Command__Group_1__2 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:457:2: rule__Command__Group_1__1__Impl rule__Command__Group_1__2
            {
            pushFollow(FOLLOW_rule__Command__Group_1__1__Impl_in_rule__Command__Group_1__1878);
            rule__Command__Group_1__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Command__Group_1__2_in_rule__Command__Group_1__1881);
            rule__Command__Group_1__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group_1__1"


    // $ANTLR start "rule__Command__Group_1__1__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:464:1: rule__Command__Group_1__1__Impl : ( ( rule__Command__AssignmentsAssignment_1_1 )* ) ;
    public final void rule__Command__Group_1__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:468:1: ( ( ( rule__Command__AssignmentsAssignment_1_1 )* ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:469:1: ( ( rule__Command__AssignmentsAssignment_1_1 )* )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:469:1: ( ( rule__Command__AssignmentsAssignment_1_1 )* )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:470:1: ( rule__Command__AssignmentsAssignment_1_1 )*
            {
             before(grammarAccess.getCommandAccess().getAssignmentsAssignment_1_1()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:471:1: ( rule__Command__AssignmentsAssignment_1_1 )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==RULE_ID) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:471:2: rule__Command__AssignmentsAssignment_1_1
            	    {
            	    pushFollow(FOLLOW_rule__Command__AssignmentsAssignment_1_1_in_rule__Command__Group_1__1__Impl908);
            	    rule__Command__AssignmentsAssignment_1_1();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

             after(grammarAccess.getCommandAccess().getAssignmentsAssignment_1_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group_1__1__Impl"


    // $ANTLR start "rule__Command__Group_1__2"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:481:1: rule__Command__Group_1__2 : rule__Command__Group_1__2__Impl ;
    public final void rule__Command__Group_1__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:485:1: ( rule__Command__Group_1__2__Impl )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:486:2: rule__Command__Group_1__2__Impl
            {
            pushFollow(FOLLOW_rule__Command__Group_1__2__Impl_in_rule__Command__Group_1__2939);
            rule__Command__Group_1__2__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group_1__2"


    // $ANTLR start "rule__Command__Group_1__2__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:492:1: rule__Command__Group_1__2__Impl : ( ')' ) ;
    public final void rule__Command__Group_1__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:496:1: ( ( ')' ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:497:1: ( ')' )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:497:1: ( ')' )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:498:1: ')'
            {
             before(grammarAccess.getCommandAccess().getRightParenthesisKeyword_1_2()); 
            match(input,13,FOLLOW_13_in_rule__Command__Group_1__2__Impl967); 
             after(grammarAccess.getCommandAccess().getRightParenthesisKeyword_1_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__Group_1__2__Impl"


    // $ANTLR start "rule__ArgumentAssignment__Group__0"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:517:1: rule__ArgumentAssignment__Group__0 : rule__ArgumentAssignment__Group__0__Impl rule__ArgumentAssignment__Group__1 ;
    public final void rule__ArgumentAssignment__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:521:1: ( rule__ArgumentAssignment__Group__0__Impl rule__ArgumentAssignment__Group__1 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:522:2: rule__ArgumentAssignment__Group__0__Impl rule__ArgumentAssignment__Group__1
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__0__Impl_in_rule__ArgumentAssignment__Group__01004);
            rule__ArgumentAssignment__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__1_in_rule__ArgumentAssignment__Group__01007);
            rule__ArgumentAssignment__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ArgumentAssignment__Group__0"


    // $ANTLR start "rule__ArgumentAssignment__Group__0__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:529:1: rule__ArgumentAssignment__Group__0__Impl : ( ( rule__ArgumentAssignment__NameAssignment_0 ) ) ;
    public final void rule__ArgumentAssignment__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:533:1: ( ( ( rule__ArgumentAssignment__NameAssignment_0 ) ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:534:1: ( ( rule__ArgumentAssignment__NameAssignment_0 ) )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:534:1: ( ( rule__ArgumentAssignment__NameAssignment_0 ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:535:1: ( rule__ArgumentAssignment__NameAssignment_0 )
            {
             before(grammarAccess.getArgumentAssignmentAccess().getNameAssignment_0()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:536:1: ( rule__ArgumentAssignment__NameAssignment_0 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:536:2: rule__ArgumentAssignment__NameAssignment_0
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__NameAssignment_0_in_rule__ArgumentAssignment__Group__0__Impl1034);
            rule__ArgumentAssignment__NameAssignment_0();

            state._fsp--;


            }

             after(grammarAccess.getArgumentAssignmentAccess().getNameAssignment_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ArgumentAssignment__Group__0__Impl"


    // $ANTLR start "rule__ArgumentAssignment__Group__1"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:546:1: rule__ArgumentAssignment__Group__1 : rule__ArgumentAssignment__Group__1__Impl rule__ArgumentAssignment__Group__2 ;
    public final void rule__ArgumentAssignment__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:550:1: ( rule__ArgumentAssignment__Group__1__Impl rule__ArgumentAssignment__Group__2 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:551:2: rule__ArgumentAssignment__Group__1__Impl rule__ArgumentAssignment__Group__2
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__1__Impl_in_rule__ArgumentAssignment__Group__11064);
            rule__ArgumentAssignment__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__2_in_rule__ArgumentAssignment__Group__11067);
            rule__ArgumentAssignment__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ArgumentAssignment__Group__1"


    // $ANTLR start "rule__ArgumentAssignment__Group__1__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:558:1: rule__ArgumentAssignment__Group__1__Impl : ( '=' ) ;
    public final void rule__ArgumentAssignment__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:562:1: ( ( '=' ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:563:1: ( '=' )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:563:1: ( '=' )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:564:1: '='
            {
             before(grammarAccess.getArgumentAssignmentAccess().getEqualsSignKeyword_1()); 
            match(input,14,FOLLOW_14_in_rule__ArgumentAssignment__Group__1__Impl1095); 
             after(grammarAccess.getArgumentAssignmentAccess().getEqualsSignKeyword_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ArgumentAssignment__Group__1__Impl"


    // $ANTLR start "rule__ArgumentAssignment__Group__2"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:577:1: rule__ArgumentAssignment__Group__2 : rule__ArgumentAssignment__Group__2__Impl ;
    public final void rule__ArgumentAssignment__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:581:1: ( rule__ArgumentAssignment__Group__2__Impl )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:582:2: rule__ArgumentAssignment__Group__2__Impl
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__2__Impl_in_rule__ArgumentAssignment__Group__21126);
            rule__ArgumentAssignment__Group__2__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ArgumentAssignment__Group__2"


    // $ANTLR start "rule__ArgumentAssignment__Group__2__Impl"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:588:1: rule__ArgumentAssignment__Group__2__Impl : ( ( rule__ArgumentAssignment__ValueAssignment_2 ) ) ;
    public final void rule__ArgumentAssignment__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:592:1: ( ( ( rule__ArgumentAssignment__ValueAssignment_2 ) ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:593:1: ( ( rule__ArgumentAssignment__ValueAssignment_2 ) )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:593:1: ( ( rule__ArgumentAssignment__ValueAssignment_2 ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:594:1: ( rule__ArgumentAssignment__ValueAssignment_2 )
            {
             before(grammarAccess.getArgumentAssignmentAccess().getValueAssignment_2()); 
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:595:1: ( rule__ArgumentAssignment__ValueAssignment_2 )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:595:2: rule__ArgumentAssignment__ValueAssignment_2
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__ValueAssignment_2_in_rule__ArgumentAssignment__Group__2__Impl1153);
            rule__ArgumentAssignment__ValueAssignment_2();

            state._fsp--;


            }

             after(grammarAccess.getArgumentAssignmentAccess().getValueAssignment_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ArgumentAssignment__Group__2__Impl"


    // $ANTLR start "rule__Model__CommandsAssignment"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:612:1: rule__Model__CommandsAssignment : ( ruleCommand ) ;
    public final void rule__Model__CommandsAssignment() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:616:1: ( ( ruleCommand ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:617:1: ( ruleCommand )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:617:1: ( ruleCommand )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:618:1: ruleCommand
            {
             before(grammarAccess.getModelAccess().getCommandsCommandParserRuleCall_0()); 
            pushFollow(FOLLOW_ruleCommand_in_rule__Model__CommandsAssignment1194);
            ruleCommand();

            state._fsp--;

             after(grammarAccess.getModelAccess().getCommandsCommandParserRuleCall_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Model__CommandsAssignment"


    // $ANTLR start "rule__Command__NameAssignment_0"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:627:1: rule__Command__NameAssignment_0 : ( RULE_ID ) ;
    public final void rule__Command__NameAssignment_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:631:1: ( ( RULE_ID ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:632:1: ( RULE_ID )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:632:1: ( RULE_ID )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:633:1: RULE_ID
            {
             before(grammarAccess.getCommandAccess().getNameIDTerminalRuleCall_0_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__Command__NameAssignment_01225); 
             after(grammarAccess.getCommandAccess().getNameIDTerminalRuleCall_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__NameAssignment_0"


    // $ANTLR start "rule__Command__AssignmentsAssignment_1_1"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:642:1: rule__Command__AssignmentsAssignment_1_1 : ( ruleArgumentAssignment ) ;
    public final void rule__Command__AssignmentsAssignment_1_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:646:1: ( ( ruleArgumentAssignment ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:647:1: ( ruleArgumentAssignment )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:647:1: ( ruleArgumentAssignment )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:648:1: ruleArgumentAssignment
            {
             before(grammarAccess.getCommandAccess().getAssignmentsArgumentAssignmentParserRuleCall_1_1_0()); 
            pushFollow(FOLLOW_ruleArgumentAssignment_in_rule__Command__AssignmentsAssignment_1_11256);
            ruleArgumentAssignment();

            state._fsp--;

             after(grammarAccess.getCommandAccess().getAssignmentsArgumentAssignmentParserRuleCall_1_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__Command__AssignmentsAssignment_1_1"


    // $ANTLR start "rule__ArgumentAssignment__NameAssignment_0"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:657:1: rule__ArgumentAssignment__NameAssignment_0 : ( RULE_ID ) ;
    public final void rule__ArgumentAssignment__NameAssignment_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:661:1: ( ( RULE_ID ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:662:1: ( RULE_ID )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:662:1: ( RULE_ID )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:663:1: RULE_ID
            {
             before(grammarAccess.getArgumentAssignmentAccess().getNameIDTerminalRuleCall_0_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__ArgumentAssignment__NameAssignment_01287); 
             after(grammarAccess.getArgumentAssignmentAccess().getNameIDTerminalRuleCall_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ArgumentAssignment__NameAssignment_0"


    // $ANTLR start "rule__ArgumentAssignment__ValueAssignment_2"
    // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:672:1: rule__ArgumentAssignment__ValueAssignment_2 : ( ruleArgumentAssignmentValue ) ;
    public final void rule__ArgumentAssignment__ValueAssignment_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:676:1: ( ( ruleArgumentAssignmentValue ) )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:677:1: ( ruleArgumentAssignmentValue )
            {
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:677:1: ( ruleArgumentAssignmentValue )
            // ../org.csstudio.yamcs.ycl.ui/src-gen/org/csstudio/yamcs/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:678:1: ruleArgumentAssignmentValue
            {
             before(grammarAccess.getArgumentAssignmentAccess().getValueArgumentAssignmentValueParserRuleCall_2_0()); 
            pushFollow(FOLLOW_ruleArgumentAssignmentValue_in_rule__ArgumentAssignment__ValueAssignment_21318);
            ruleArgumentAssignmentValue();

            state._fsp--;

             after(grammarAccess.getArgumentAssignmentAccess().getValueArgumentAssignmentValueParserRuleCall_2_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__ArgumentAssignment__ValueAssignment_2"

    // Delegated rules


 

    public static final BitSet FOLLOW_ruleModel_in_entryRuleModel61 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleModel68 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Model__CommandsAssignment_in_ruleModel94 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_ruleREAL_in_entryRuleREAL127 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleREAL134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__REAL__Group__0_in_ruleREAL164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleCommand_in_entryRuleCommand191 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleCommand198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group__0_in_ruleCommand224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignment_in_entryRuleArgumentAssignment251 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleArgumentAssignment258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__0_in_ruleArgumentAssignment284 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignmentValue_in_entryRuleArgumentAssignmentValue311 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleArgumentAssignmentValue318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignmentValue__Alternatives_in_ruleArgumentAssignmentValue344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_EXT_INT_in_rule__REAL__Alternatives_2380 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_rule__REAL__Alternatives_2397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_STRING_in_rule__ArgumentAssignmentValue__Alternatives429 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_rule__ArgumentAssignmentValue__Alternatives446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_HEX_in_rule__ArgumentAssignmentValue__Alternatives463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleREAL_in_rule__ArgumentAssignmentValue__Alternatives480 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__REAL__Group__0__Impl_in_rule__REAL__Group__0510 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_rule__REAL__Group__1_in_rule__REAL__Group__0513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_rule__REAL__Group__0__Impl540 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__REAL__Group__1__Impl_in_rule__REAL__Group__1569 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_rule__REAL__Group__2_in_rule__REAL__Group__1572 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_11_in_rule__REAL__Group__1__Impl600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__REAL__Group__2__Impl_in_rule__REAL__Group__2631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__REAL__Alternatives_2_in_rule__REAL__Group__2__Impl658 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group__0__Impl_in_rule__Command__Group__0694 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_rule__Command__Group__1_in_rule__Command__Group__0697 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__NameAssignment_0_in_rule__Command__Group__0__Impl724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group__1__Impl_in_rule__Command__Group__1754 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group_1__0_in_rule__Command__Group__1__Impl781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group_1__0__Impl_in_rule__Command__Group_1__0816 = new BitSet(new long[]{0x0000000000002100L});
    public static final BitSet FOLLOW_rule__Command__Group_1__1_in_rule__Command__Group_1__0819 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_12_in_rule__Command__Group_1__0__Impl847 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group_1__1__Impl_in_rule__Command__Group_1__1878 = new BitSet(new long[]{0x0000000000002100L});
    public static final BitSet FOLLOW_rule__Command__Group_1__2_in_rule__Command__Group_1__1881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__AssignmentsAssignment_1_1_in_rule__Command__Group_1__1__Impl908 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_rule__Command__Group_1__2__Impl_in_rule__Command__Group_1__2939 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_13_in_rule__Command__Group_1__2__Impl967 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__0__Impl_in_rule__ArgumentAssignment__Group__01004 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__1_in_rule__ArgumentAssignment__Group__01007 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__NameAssignment_0_in_rule__ArgumentAssignment__Group__0__Impl1034 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__1__Impl_in_rule__ArgumentAssignment__Group__11064 = new BitSet(new long[]{0x00000000000000E0L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__2_in_rule__ArgumentAssignment__Group__11067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_14_in_rule__ArgumentAssignment__Group__1__Impl1095 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__2__Impl_in_rule__ArgumentAssignment__Group__21126 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__ValueAssignment_2_in_rule__ArgumentAssignment__Group__2__Impl1153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleCommand_in_rule__Model__CommandsAssignment1194 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__Command__NameAssignment_01225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignment_in_rule__Command__AssignmentsAssignment_1_11256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__ArgumentAssignment__NameAssignment_01287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignmentValue_in_rule__ArgumentAssignment__ValueAssignment_21318 = new BitSet(new long[]{0x0000000000000002L});

}