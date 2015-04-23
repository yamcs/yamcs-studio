package org.yamcs.studio.ycl.dsl.ui.contentassist.antlr.internal; 

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
import org.yamcs.studio.ycl.dsl.services.YCLGrammarAccess;



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
    public String getGrammarFileName() { return "../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g"; }


     
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:60:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:61:1: ( ruleModel EOF )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:62:1: ruleModel EOF
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:69:1: ruleModel : ( ( rule__Model__CommandsAssignment )* ) ;
    public final void ruleModel() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:73:2: ( ( ( rule__Model__CommandsAssignment )* ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:74:1: ( ( rule__Model__CommandsAssignment )* )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:74:1: ( ( rule__Model__CommandsAssignment )* )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:75:1: ( rule__Model__CommandsAssignment )*
            {
             before(grammarAccess.getModelAccess().getCommandsAssignment()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:76:1: ( rule__Model__CommandsAssignment )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==RULE_ID) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:76:2: rule__Model__CommandsAssignment
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:88:1: entryRuleREAL : ruleREAL EOF ;
    public final void entryRuleREAL() throws RecognitionException {

        	HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();

        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:92:1: ( ruleREAL EOF )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:93:1: ruleREAL EOF
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:103:1: ruleREAL : ( ( rule__REAL__Group__0 ) ) ;
    public final void ruleREAL() throws RecognitionException {

        		HiddenTokens myHiddenTokenState = ((XtextTokenStream)input).setHiddenTokens();
        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:108:2: ( ( ( rule__REAL__Group__0 ) ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:109:1: ( ( rule__REAL__Group__0 ) )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:109:1: ( ( rule__REAL__Group__0 ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:110:1: ( rule__REAL__Group__0 )
            {
             before(grammarAccess.getREALAccess().getGroup()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:111:1: ( rule__REAL__Group__0 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:111:2: rule__REAL__Group__0
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:124:1: entryRuleCommand : ruleCommand EOF ;
    public final void entryRuleCommand() throws RecognitionException {
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:125:1: ( ruleCommand EOF )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:126:1: ruleCommand EOF
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:133:1: ruleCommand : ( ( rule__Command__Group__0 ) ) ;
    public final void ruleCommand() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:137:2: ( ( ( rule__Command__Group__0 ) ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:138:1: ( ( rule__Command__Group__0 ) )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:138:1: ( ( rule__Command__Group__0 ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:139:1: ( rule__Command__Group__0 )
            {
             before(grammarAccess.getCommandAccess().getGroup()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:140:1: ( rule__Command__Group__0 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:140:2: rule__Command__Group__0
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


    // $ANTLR start "entryRuleCommandId"
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:152:1: entryRuleCommandId : ruleCommandId EOF ;
    public final void entryRuleCommandId() throws RecognitionException {
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:153:1: ( ruleCommandId EOF )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:154:1: ruleCommandId EOF
            {
             before(grammarAccess.getCommandIdRule()); 
            pushFollow(FOLLOW_ruleCommandId_in_entryRuleCommandId251);
            ruleCommandId();

            state._fsp--;

             after(grammarAccess.getCommandIdRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleCommandId258); 

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
    // $ANTLR end "entryRuleCommandId"


    // $ANTLR start "ruleCommandId"
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:161:1: ruleCommandId : ( ( rule__CommandId__IdAssignment ) ) ;
    public final void ruleCommandId() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:165:2: ( ( ( rule__CommandId__IdAssignment ) ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:166:1: ( ( rule__CommandId__IdAssignment ) )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:166:1: ( ( rule__CommandId__IdAssignment ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:167:1: ( rule__CommandId__IdAssignment )
            {
             before(grammarAccess.getCommandIdAccess().getIdAssignment()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:168:1: ( rule__CommandId__IdAssignment )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:168:2: rule__CommandId__IdAssignment
            {
            pushFollow(FOLLOW_rule__CommandId__IdAssignment_in_ruleCommandId284);
            rule__CommandId__IdAssignment();

            state._fsp--;


            }

             after(grammarAccess.getCommandIdAccess().getIdAssignment()); 

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
    // $ANTLR end "ruleCommandId"


    // $ANTLR start "entryRuleArgumentAssignment"
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:180:1: entryRuleArgumentAssignment : ruleArgumentAssignment EOF ;
    public final void entryRuleArgumentAssignment() throws RecognitionException {
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:181:1: ( ruleArgumentAssignment EOF )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:182:1: ruleArgumentAssignment EOF
            {
             before(grammarAccess.getArgumentAssignmentRule()); 
            pushFollow(FOLLOW_ruleArgumentAssignment_in_entryRuleArgumentAssignment311);
            ruleArgumentAssignment();

            state._fsp--;

             after(grammarAccess.getArgumentAssignmentRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleArgumentAssignment318); 

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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:189:1: ruleArgumentAssignment : ( ( rule__ArgumentAssignment__Group__0 ) ) ;
    public final void ruleArgumentAssignment() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:193:2: ( ( ( rule__ArgumentAssignment__Group__0 ) ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:194:1: ( ( rule__ArgumentAssignment__Group__0 ) )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:194:1: ( ( rule__ArgumentAssignment__Group__0 ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:195:1: ( rule__ArgumentAssignment__Group__0 )
            {
             before(grammarAccess.getArgumentAssignmentAccess().getGroup()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:196:1: ( rule__ArgumentAssignment__Group__0 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:196:2: rule__ArgumentAssignment__Group__0
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__0_in_ruleArgumentAssignment344);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:208:1: entryRuleArgumentAssignmentValue : ruleArgumentAssignmentValue EOF ;
    public final void entryRuleArgumentAssignmentValue() throws RecognitionException {
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:209:1: ( ruleArgumentAssignmentValue EOF )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:210:1: ruleArgumentAssignmentValue EOF
            {
             before(grammarAccess.getArgumentAssignmentValueRule()); 
            pushFollow(FOLLOW_ruleArgumentAssignmentValue_in_entryRuleArgumentAssignmentValue371);
            ruleArgumentAssignmentValue();

            state._fsp--;

             after(grammarAccess.getArgumentAssignmentValueRule()); 
            match(input,EOF,FOLLOW_EOF_in_entryRuleArgumentAssignmentValue378); 

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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:217:1: ruleArgumentAssignmentValue : ( ( rule__ArgumentAssignmentValue__Alternatives ) ) ;
    public final void ruleArgumentAssignmentValue() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:221:2: ( ( ( rule__ArgumentAssignmentValue__Alternatives ) ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:222:1: ( ( rule__ArgumentAssignmentValue__Alternatives ) )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:222:1: ( ( rule__ArgumentAssignmentValue__Alternatives ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:223:1: ( rule__ArgumentAssignmentValue__Alternatives )
            {
             before(grammarAccess.getArgumentAssignmentValueAccess().getAlternatives()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:224:1: ( rule__ArgumentAssignmentValue__Alternatives )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:224:2: rule__ArgumentAssignmentValue__Alternatives
            {
            pushFollow(FOLLOW_rule__ArgumentAssignmentValue__Alternatives_in_ruleArgumentAssignmentValue404);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:236:1: rule__REAL__Alternatives_2 : ( ( RULE_EXT_INT ) | ( RULE_INT ) );
    public final void rule__REAL__Alternatives_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:240:1: ( ( RULE_EXT_INT ) | ( RULE_INT ) )
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
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:241:1: ( RULE_EXT_INT )
                    {
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:241:1: ( RULE_EXT_INT )
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:242:1: RULE_EXT_INT
                    {
                     before(grammarAccess.getREALAccess().getEXT_INTTerminalRuleCall_2_0()); 
                    match(input,RULE_EXT_INT,FOLLOW_RULE_EXT_INT_in_rule__REAL__Alternatives_2440); 
                     after(grammarAccess.getREALAccess().getEXT_INTTerminalRuleCall_2_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:247:6: ( RULE_INT )
                    {
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:247:6: ( RULE_INT )
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:248:1: RULE_INT
                    {
                     before(grammarAccess.getREALAccess().getINTTerminalRuleCall_2_1()); 
                    match(input,RULE_INT,FOLLOW_RULE_INT_in_rule__REAL__Alternatives_2457); 
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:258:1: rule__ArgumentAssignmentValue__Alternatives : ( ( RULE_STRING ) | ( RULE_INT ) | ( RULE_HEX ) | ( ruleREAL ) );
    public final void rule__ArgumentAssignmentValue__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:262:1: ( ( RULE_STRING ) | ( RULE_INT ) | ( RULE_HEX ) | ( ruleREAL ) )
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

                if ( (LA3_2==EOF||LA3_2==RULE_ID||LA3_2==13) ) {
                    alt3=2;
                }
                else if ( (LA3_2==11) ) {
                    alt3=4;
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
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:263:1: ( RULE_STRING )
                    {
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:263:1: ( RULE_STRING )
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:264:1: RULE_STRING
                    {
                     before(grammarAccess.getArgumentAssignmentValueAccess().getSTRINGTerminalRuleCall_0()); 
                    match(input,RULE_STRING,FOLLOW_RULE_STRING_in_rule__ArgumentAssignmentValue__Alternatives489); 
                     after(grammarAccess.getArgumentAssignmentValueAccess().getSTRINGTerminalRuleCall_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:269:6: ( RULE_INT )
                    {
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:269:6: ( RULE_INT )
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:270:1: RULE_INT
                    {
                     before(grammarAccess.getArgumentAssignmentValueAccess().getINTTerminalRuleCall_1()); 
                    match(input,RULE_INT,FOLLOW_RULE_INT_in_rule__ArgumentAssignmentValue__Alternatives506); 
                     after(grammarAccess.getArgumentAssignmentValueAccess().getINTTerminalRuleCall_1()); 

                    }


                    }
                    break;
                case 3 :
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:275:6: ( RULE_HEX )
                    {
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:275:6: ( RULE_HEX )
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:276:1: RULE_HEX
                    {
                     before(grammarAccess.getArgumentAssignmentValueAccess().getHEXTerminalRuleCall_2()); 
                    match(input,RULE_HEX,FOLLOW_RULE_HEX_in_rule__ArgumentAssignmentValue__Alternatives523); 
                     after(grammarAccess.getArgumentAssignmentValueAccess().getHEXTerminalRuleCall_2()); 

                    }


                    }
                    break;
                case 4 :
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:281:6: ( ruleREAL )
                    {
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:281:6: ( ruleREAL )
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:282:1: ruleREAL
                    {
                     before(grammarAccess.getArgumentAssignmentValueAccess().getREALParserRuleCall_3()); 
                    pushFollow(FOLLOW_ruleREAL_in_rule__ArgumentAssignmentValue__Alternatives540);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:294:1: rule__REAL__Group__0 : rule__REAL__Group__0__Impl rule__REAL__Group__1 ;
    public final void rule__REAL__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:298:1: ( rule__REAL__Group__0__Impl rule__REAL__Group__1 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:299:2: rule__REAL__Group__0__Impl rule__REAL__Group__1
            {
            pushFollow(FOLLOW_rule__REAL__Group__0__Impl_in_rule__REAL__Group__0570);
            rule__REAL__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__REAL__Group__1_in_rule__REAL__Group__0573);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:306:1: rule__REAL__Group__0__Impl : ( RULE_INT ) ;
    public final void rule__REAL__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:310:1: ( ( RULE_INT ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:311:1: ( RULE_INT )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:311:1: ( RULE_INT )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:312:1: RULE_INT
            {
             before(grammarAccess.getREALAccess().getINTTerminalRuleCall_0()); 
            match(input,RULE_INT,FOLLOW_RULE_INT_in_rule__REAL__Group__0__Impl600); 
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:323:1: rule__REAL__Group__1 : rule__REAL__Group__1__Impl rule__REAL__Group__2 ;
    public final void rule__REAL__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:327:1: ( rule__REAL__Group__1__Impl rule__REAL__Group__2 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:328:2: rule__REAL__Group__1__Impl rule__REAL__Group__2
            {
            pushFollow(FOLLOW_rule__REAL__Group__1__Impl_in_rule__REAL__Group__1629);
            rule__REAL__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__REAL__Group__2_in_rule__REAL__Group__1632);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:335:1: rule__REAL__Group__1__Impl : ( '.' ) ;
    public final void rule__REAL__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:339:1: ( ( '.' ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:340:1: ( '.' )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:340:1: ( '.' )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:341:1: '.'
            {
             before(grammarAccess.getREALAccess().getFullStopKeyword_1()); 
            match(input,11,FOLLOW_11_in_rule__REAL__Group__1__Impl660); 
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:354:1: rule__REAL__Group__2 : rule__REAL__Group__2__Impl ;
    public final void rule__REAL__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:358:1: ( rule__REAL__Group__2__Impl )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:359:2: rule__REAL__Group__2__Impl
            {
            pushFollow(FOLLOW_rule__REAL__Group__2__Impl_in_rule__REAL__Group__2691);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:365:1: rule__REAL__Group__2__Impl : ( ( rule__REAL__Alternatives_2 ) ) ;
    public final void rule__REAL__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:369:1: ( ( ( rule__REAL__Alternatives_2 ) ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:370:1: ( ( rule__REAL__Alternatives_2 ) )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:370:1: ( ( rule__REAL__Alternatives_2 ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:371:1: ( rule__REAL__Alternatives_2 )
            {
             before(grammarAccess.getREALAccess().getAlternatives_2()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:372:1: ( rule__REAL__Alternatives_2 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:372:2: rule__REAL__Alternatives_2
            {
            pushFollow(FOLLOW_rule__REAL__Alternatives_2_in_rule__REAL__Group__2__Impl718);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:388:1: rule__Command__Group__0 : rule__Command__Group__0__Impl rule__Command__Group__1 ;
    public final void rule__Command__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:392:1: ( rule__Command__Group__0__Impl rule__Command__Group__1 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:393:2: rule__Command__Group__0__Impl rule__Command__Group__1
            {
            pushFollow(FOLLOW_rule__Command__Group__0__Impl_in_rule__Command__Group__0754);
            rule__Command__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Command__Group__1_in_rule__Command__Group__0757);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:400:1: rule__Command__Group__0__Impl : ( ( rule__Command__NameAssignment_0 ) ) ;
    public final void rule__Command__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:404:1: ( ( ( rule__Command__NameAssignment_0 ) ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:405:1: ( ( rule__Command__NameAssignment_0 ) )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:405:1: ( ( rule__Command__NameAssignment_0 ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:406:1: ( rule__Command__NameAssignment_0 )
            {
             before(grammarAccess.getCommandAccess().getNameAssignment_0()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:407:1: ( rule__Command__NameAssignment_0 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:407:2: rule__Command__NameAssignment_0
            {
            pushFollow(FOLLOW_rule__Command__NameAssignment_0_in_rule__Command__Group__0__Impl784);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:417:1: rule__Command__Group__1 : rule__Command__Group__1__Impl ;
    public final void rule__Command__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:421:1: ( rule__Command__Group__1__Impl )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:422:2: rule__Command__Group__1__Impl
            {
            pushFollow(FOLLOW_rule__Command__Group__1__Impl_in_rule__Command__Group__1814);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:428:1: rule__Command__Group__1__Impl : ( ( rule__Command__Group_1__0 )? ) ;
    public final void rule__Command__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:432:1: ( ( ( rule__Command__Group_1__0 )? ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:433:1: ( ( rule__Command__Group_1__0 )? )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:433:1: ( ( rule__Command__Group_1__0 )? )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:434:1: ( rule__Command__Group_1__0 )?
            {
             before(grammarAccess.getCommandAccess().getGroup_1()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:435:1: ( rule__Command__Group_1__0 )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==12) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:435:2: rule__Command__Group_1__0
                    {
                    pushFollow(FOLLOW_rule__Command__Group_1__0_in_rule__Command__Group__1__Impl841);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:449:1: rule__Command__Group_1__0 : rule__Command__Group_1__0__Impl rule__Command__Group_1__1 ;
    public final void rule__Command__Group_1__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:453:1: ( rule__Command__Group_1__0__Impl rule__Command__Group_1__1 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:454:2: rule__Command__Group_1__0__Impl rule__Command__Group_1__1
            {
            pushFollow(FOLLOW_rule__Command__Group_1__0__Impl_in_rule__Command__Group_1__0876);
            rule__Command__Group_1__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Command__Group_1__1_in_rule__Command__Group_1__0879);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:461:1: rule__Command__Group_1__0__Impl : ( '(' ) ;
    public final void rule__Command__Group_1__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:465:1: ( ( '(' ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:466:1: ( '(' )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:466:1: ( '(' )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:467:1: '('
            {
             before(grammarAccess.getCommandAccess().getLeftParenthesisKeyword_1_0()); 
            match(input,12,FOLLOW_12_in_rule__Command__Group_1__0__Impl907); 
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:480:1: rule__Command__Group_1__1 : rule__Command__Group_1__1__Impl rule__Command__Group_1__2 ;
    public final void rule__Command__Group_1__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:484:1: ( rule__Command__Group_1__1__Impl rule__Command__Group_1__2 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:485:2: rule__Command__Group_1__1__Impl rule__Command__Group_1__2
            {
            pushFollow(FOLLOW_rule__Command__Group_1__1__Impl_in_rule__Command__Group_1__1938);
            rule__Command__Group_1__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__Command__Group_1__2_in_rule__Command__Group_1__1941);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:492:1: rule__Command__Group_1__1__Impl : ( ( rule__Command__AssignmentsAssignment_1_1 )* ) ;
    public final void rule__Command__Group_1__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:496:1: ( ( ( rule__Command__AssignmentsAssignment_1_1 )* ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:497:1: ( ( rule__Command__AssignmentsAssignment_1_1 )* )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:497:1: ( ( rule__Command__AssignmentsAssignment_1_1 )* )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:498:1: ( rule__Command__AssignmentsAssignment_1_1 )*
            {
             before(grammarAccess.getCommandAccess().getAssignmentsAssignment_1_1()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:499:1: ( rule__Command__AssignmentsAssignment_1_1 )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==RULE_ID) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:499:2: rule__Command__AssignmentsAssignment_1_1
            	    {
            	    pushFollow(FOLLOW_rule__Command__AssignmentsAssignment_1_1_in_rule__Command__Group_1__1__Impl968);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:509:1: rule__Command__Group_1__2 : rule__Command__Group_1__2__Impl ;
    public final void rule__Command__Group_1__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:513:1: ( rule__Command__Group_1__2__Impl )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:514:2: rule__Command__Group_1__2__Impl
            {
            pushFollow(FOLLOW_rule__Command__Group_1__2__Impl_in_rule__Command__Group_1__2999);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:520:1: rule__Command__Group_1__2__Impl : ( ')' ) ;
    public final void rule__Command__Group_1__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:524:1: ( ( ')' ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:525:1: ( ')' )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:525:1: ( ')' )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:526:1: ')'
            {
             before(grammarAccess.getCommandAccess().getRightParenthesisKeyword_1_2()); 
            match(input,13,FOLLOW_13_in_rule__Command__Group_1__2__Impl1027); 
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:545:1: rule__ArgumentAssignment__Group__0 : rule__ArgumentAssignment__Group__0__Impl rule__ArgumentAssignment__Group__1 ;
    public final void rule__ArgumentAssignment__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:549:1: ( rule__ArgumentAssignment__Group__0__Impl rule__ArgumentAssignment__Group__1 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:550:2: rule__ArgumentAssignment__Group__0__Impl rule__ArgumentAssignment__Group__1
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__0__Impl_in_rule__ArgumentAssignment__Group__01064);
            rule__ArgumentAssignment__Group__0__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__1_in_rule__ArgumentAssignment__Group__01067);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:557:1: rule__ArgumentAssignment__Group__0__Impl : ( ( rule__ArgumentAssignment__NameAssignment_0 ) ) ;
    public final void rule__ArgumentAssignment__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:561:1: ( ( ( rule__ArgumentAssignment__NameAssignment_0 ) ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:562:1: ( ( rule__ArgumentAssignment__NameAssignment_0 ) )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:562:1: ( ( rule__ArgumentAssignment__NameAssignment_0 ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:563:1: ( rule__ArgumentAssignment__NameAssignment_0 )
            {
             before(grammarAccess.getArgumentAssignmentAccess().getNameAssignment_0()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:564:1: ( rule__ArgumentAssignment__NameAssignment_0 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:564:2: rule__ArgumentAssignment__NameAssignment_0
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__NameAssignment_0_in_rule__ArgumentAssignment__Group__0__Impl1094);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:574:1: rule__ArgumentAssignment__Group__1 : rule__ArgumentAssignment__Group__1__Impl rule__ArgumentAssignment__Group__2 ;
    public final void rule__ArgumentAssignment__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:578:1: ( rule__ArgumentAssignment__Group__1__Impl rule__ArgumentAssignment__Group__2 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:579:2: rule__ArgumentAssignment__Group__1__Impl rule__ArgumentAssignment__Group__2
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__1__Impl_in_rule__ArgumentAssignment__Group__11124);
            rule__ArgumentAssignment__Group__1__Impl();

            state._fsp--;

            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__2_in_rule__ArgumentAssignment__Group__11127);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:586:1: rule__ArgumentAssignment__Group__1__Impl : ( '=' ) ;
    public final void rule__ArgumentAssignment__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:590:1: ( ( '=' ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:591:1: ( '=' )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:591:1: ( '=' )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:592:1: '='
            {
             before(grammarAccess.getArgumentAssignmentAccess().getEqualsSignKeyword_1()); 
            match(input,14,FOLLOW_14_in_rule__ArgumentAssignment__Group__1__Impl1155); 
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:605:1: rule__ArgumentAssignment__Group__2 : rule__ArgumentAssignment__Group__2__Impl ;
    public final void rule__ArgumentAssignment__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:609:1: ( rule__ArgumentAssignment__Group__2__Impl )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:610:2: rule__ArgumentAssignment__Group__2__Impl
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__Group__2__Impl_in_rule__ArgumentAssignment__Group__21186);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:616:1: rule__ArgumentAssignment__Group__2__Impl : ( ( rule__ArgumentAssignment__ValueAssignment_2 ) ) ;
    public final void rule__ArgumentAssignment__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:620:1: ( ( ( rule__ArgumentAssignment__ValueAssignment_2 ) ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:621:1: ( ( rule__ArgumentAssignment__ValueAssignment_2 ) )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:621:1: ( ( rule__ArgumentAssignment__ValueAssignment_2 ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:622:1: ( rule__ArgumentAssignment__ValueAssignment_2 )
            {
             before(grammarAccess.getArgumentAssignmentAccess().getValueAssignment_2()); 
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:623:1: ( rule__ArgumentAssignment__ValueAssignment_2 )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:623:2: rule__ArgumentAssignment__ValueAssignment_2
            {
            pushFollow(FOLLOW_rule__ArgumentAssignment__ValueAssignment_2_in_rule__ArgumentAssignment__Group__2__Impl1213);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:640:1: rule__Model__CommandsAssignment : ( ruleCommand ) ;
    public final void rule__Model__CommandsAssignment() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:644:1: ( ( ruleCommand ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:645:1: ( ruleCommand )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:645:1: ( ruleCommand )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:646:1: ruleCommand
            {
             before(grammarAccess.getModelAccess().getCommandsCommandParserRuleCall_0()); 
            pushFollow(FOLLOW_ruleCommand_in_rule__Model__CommandsAssignment1254);
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:655:1: rule__Command__NameAssignment_0 : ( ruleCommandId ) ;
    public final void rule__Command__NameAssignment_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:659:1: ( ( ruleCommandId ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:660:1: ( ruleCommandId )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:660:1: ( ruleCommandId )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:661:1: ruleCommandId
            {
             before(grammarAccess.getCommandAccess().getNameCommandIdParserRuleCall_0_0()); 
            pushFollow(FOLLOW_ruleCommandId_in_rule__Command__NameAssignment_01285);
            ruleCommandId();

            state._fsp--;

             after(grammarAccess.getCommandAccess().getNameCommandIdParserRuleCall_0_0()); 

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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:670:1: rule__Command__AssignmentsAssignment_1_1 : ( ruleArgumentAssignment ) ;
    public final void rule__Command__AssignmentsAssignment_1_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:674:1: ( ( ruleArgumentAssignment ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:675:1: ( ruleArgumentAssignment )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:675:1: ( ruleArgumentAssignment )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:676:1: ruleArgumentAssignment
            {
             before(grammarAccess.getCommandAccess().getAssignmentsArgumentAssignmentParserRuleCall_1_1_0()); 
            pushFollow(FOLLOW_ruleArgumentAssignment_in_rule__Command__AssignmentsAssignment_1_11316);
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


    // $ANTLR start "rule__CommandId__IdAssignment"
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:685:1: rule__CommandId__IdAssignment : ( RULE_ID ) ;
    public final void rule__CommandId__IdAssignment() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:689:1: ( ( RULE_ID ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:690:1: ( RULE_ID )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:690:1: ( RULE_ID )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:691:1: RULE_ID
            {
             before(grammarAccess.getCommandIdAccess().getIdIDTerminalRuleCall_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__CommandId__IdAssignment1347); 
             after(grammarAccess.getCommandIdAccess().getIdIDTerminalRuleCall_0()); 

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
    // $ANTLR end "rule__CommandId__IdAssignment"


    // $ANTLR start "rule__ArgumentAssignment__NameAssignment_0"
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:700:1: rule__ArgumentAssignment__NameAssignment_0 : ( RULE_ID ) ;
    public final void rule__ArgumentAssignment__NameAssignment_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:704:1: ( ( RULE_ID ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:705:1: ( RULE_ID )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:705:1: ( RULE_ID )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:706:1: RULE_ID
            {
             before(grammarAccess.getArgumentAssignmentAccess().getNameIDTerminalRuleCall_0_0()); 
            match(input,RULE_ID,FOLLOW_RULE_ID_in_rule__ArgumentAssignment__NameAssignment_01378); 
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
    // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:715:1: rule__ArgumentAssignment__ValueAssignment_2 : ( ruleArgumentAssignmentValue ) ;
    public final void rule__ArgumentAssignment__ValueAssignment_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:719:1: ( ( ruleArgumentAssignmentValue ) )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:720:1: ( ruleArgumentAssignmentValue )
            {
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:720:1: ( ruleArgumentAssignmentValue )
            // ../org.yamcs.studio.ycl.ui/src-gen/org/yamcs/studio/ycl/dsl/ui/contentassist/antlr/internal/InternalYCL.g:721:1: ruleArgumentAssignmentValue
            {
             before(grammarAccess.getArgumentAssignmentAccess().getValueArgumentAssignmentValueParserRuleCall_2_0()); 
            pushFollow(FOLLOW_ruleArgumentAssignmentValue_in_rule__ArgumentAssignment__ValueAssignment_21409);
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
    public static final BitSet FOLLOW_ruleCommandId_in_entryRuleCommandId251 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleCommandId258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__CommandId__IdAssignment_in_ruleCommandId284 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignment_in_entryRuleArgumentAssignment311 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleArgumentAssignment318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__0_in_ruleArgumentAssignment344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignmentValue_in_entryRuleArgumentAssignmentValue371 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_entryRuleArgumentAssignmentValue378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignmentValue__Alternatives_in_ruleArgumentAssignmentValue404 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_EXT_INT_in_rule__REAL__Alternatives_2440 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_rule__REAL__Alternatives_2457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_STRING_in_rule__ArgumentAssignmentValue__Alternatives489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_rule__ArgumentAssignmentValue__Alternatives506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_HEX_in_rule__ArgumentAssignmentValue__Alternatives523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleREAL_in_rule__ArgumentAssignmentValue__Alternatives540 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__REAL__Group__0__Impl_in_rule__REAL__Group__0570 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_rule__REAL__Group__1_in_rule__REAL__Group__0573 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_INT_in_rule__REAL__Group__0__Impl600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__REAL__Group__1__Impl_in_rule__REAL__Group__1629 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_rule__REAL__Group__2_in_rule__REAL__Group__1632 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_11_in_rule__REAL__Group__1__Impl660 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__REAL__Group__2__Impl_in_rule__REAL__Group__2691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__REAL__Alternatives_2_in_rule__REAL__Group__2__Impl718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group__0__Impl_in_rule__Command__Group__0754 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_rule__Command__Group__1_in_rule__Command__Group__0757 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__NameAssignment_0_in_rule__Command__Group__0__Impl784 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group__1__Impl_in_rule__Command__Group__1814 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group_1__0_in_rule__Command__Group__1__Impl841 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group_1__0__Impl_in_rule__Command__Group_1__0876 = new BitSet(new long[]{0x0000000000002100L});
    public static final BitSet FOLLOW_rule__Command__Group_1__1_in_rule__Command__Group_1__0879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_12_in_rule__Command__Group_1__0__Impl907 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__Group_1__1__Impl_in_rule__Command__Group_1__1938 = new BitSet(new long[]{0x0000000000002100L});
    public static final BitSet FOLLOW_rule__Command__Group_1__2_in_rule__Command__Group_1__1941 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__Command__AssignmentsAssignment_1_1_in_rule__Command__Group_1__1__Impl968 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_rule__Command__Group_1__2__Impl_in_rule__Command__Group_1__2999 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_13_in_rule__Command__Group_1__2__Impl1027 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__0__Impl_in_rule__ArgumentAssignment__Group__01064 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__1_in_rule__ArgumentAssignment__Group__01067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__NameAssignment_0_in_rule__ArgumentAssignment__Group__0__Impl1094 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__1__Impl_in_rule__ArgumentAssignment__Group__11124 = new BitSet(new long[]{0x00000000000000E0L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__2_in_rule__ArgumentAssignment__Group__11127 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_14_in_rule__ArgumentAssignment__Group__1__Impl1155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__Group__2__Impl_in_rule__ArgumentAssignment__Group__21186 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule__ArgumentAssignment__ValueAssignment_2_in_rule__ArgumentAssignment__Group__2__Impl1213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleCommand_in_rule__Model__CommandsAssignment1254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleCommandId_in_rule__Command__NameAssignment_01285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignment_in_rule__Command__AssignmentsAssignment_1_11316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__CommandId__IdAssignment1347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_ID_in_rule__ArgumentAssignment__NameAssignment_01378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleArgumentAssignmentValue_in_rule__ArgumentAssignment__ValueAssignment_21409 = new BitSet(new long[]{0x0000000000000002L});

}