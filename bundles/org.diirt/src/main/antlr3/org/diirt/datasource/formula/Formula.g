grammar Formula;

options {
    language = Java;
}

@header {
package org.diirt.datasource.formula;
import static org.diirt.datasource.formula.FormulaAst.*;
}

@lexer::header {
package org.diirt.datasource.formula;
}

@members {
@Override
public void reportError(RecognitionException e) {
    throw new RuntimeException(e);
}
}

singleChannel returns [FormulaAst result]
    :   channel EOF {result = $channel.result;}
    ;

formula returns [FormulaAst result]
    :   expression EOF {result = $expression.result;}
    ;

expression returns [FormulaAst result]
    :   conditionalExpression {result = $conditionalExpression.result;}
    ;

conditionalExpression returns [FormulaAst result]
    :   op1=conditionalOrExpression {result = $op1.result;}
        (   '?' op2=expression ':' op3=conditionalExpression {result = op("?:", $result, $op2.result, $op3.result);}
        )?
    ;

conditionalOrExpression returns [FormulaAst result]
    :   op1=conditionalAndExpression {result = $op1.result;}
        (   '||' op2=conditionalAndExpression {result = op("||", $result, $op2.result);}
        )*
    ;

conditionalAndExpression returns [FormulaAst result]
    :   op1=inclusiveOrExpression {result = $op1.result;}
        (   '&&' op2=inclusiveOrExpression {result = op("&&", $result, $op2.result);}
        )*
    ;

inclusiveOrExpression returns [FormulaAst result]
    :   op1=andExpression {result = $op1.result;}
        (   '|' op2=andExpression {result = op("|", $result, $op2.result);}
        )*
    ;

andExpression returns [FormulaAst result]
    :   op1=equalityExpression {result = $op1.result;}
        (   '&' op2=equalityExpression {result = op("&", $result, $op2.result);}
        )*
    ;

equalityExpression returns [FormulaAst result]
    :   op1=relationalExpression {result = $op1.result;}
        (   '==' op2=relationalExpression {result = op("==", $result, $op2.result);}
        |   '!=' op2=relationalExpression {result = op("!=", $result, $op2.result);}
        )*
    ;

relationalExpression returns [FormulaAst result]
    :   op1=additiveExpression {result = $op1.result;}
        (   '<' '=' op2=additiveExpression {result = op("<=", $result, $op2.result);}
        |   '>' '=' op2=additiveExpression {result = op(">=", $result, $op2.result);}
        |   '<' op2=additiveExpression {result = op("<", $result, $op2.result);}
        |   '>' op2=additiveExpression {result = op(">", $result, $op2.result);}
        )*
    ;

additiveExpression returns [FormulaAst result]
    :   op1=multiplicativeExpression {result = $op1.result;}
        (   '+' op2=multiplicativeExpression {result = op("+", $result, $op2.result);}
        |   '-' op2=multiplicativeExpression {result = op("-", $result, $op2.result);}
        )*
    ;

multiplicativeExpression returns [FormulaAst result]
    :   op1=exponentialExpression {result = $op1.result;}
        (   '*' op2=exponentialExpression {result = op("*", $result, $op2.result);}
        |   '/' op2=exponentialExpression {result = op("/", $result, $op2.result);}
        |   '%' op2=exponentialExpression {result = op("\%", $result, $op2.result);}
        )*
    ;

exponentialExpression returns [FormulaAst result]
    :   op1=unaryExpression {result = $op1.result;}
        (   '^' op2=unaryExpression {result = op("^", $result, $op2.result);}
        |   '**' op2=unaryExpression {result = op("^", $result, $op2.result);}
        )*
    ;

unaryExpression returns [FormulaAst result]
    :   '-' op=unaryExpression {result = op("-", $op.result);}
    |   op=unaryExpressionNotPlusMinus {result = $op.result;}
    ;

unaryExpressionNotPlusMinus returns [FormulaAst result]
    :   '!' op=unaryExpression {result = op("!", $op.result);}
    |   op=primary {result = $op.result;}
    ;

primary returns [FormulaAst result]
    :   functionExpression {result = $functionExpression.result;}
    |   parExpression {result = $parExpression.result;}
    |   channel {result = $channel.result;}
    |   numericLiteral {result = $numericLiteral.result;}
    |   stringLiteral {result = $stringLiteral.result;}
    |   constant {result = $constant.result;}
    ;

functionExpression returns [FormulaAst result]
    :   FUNCTION '(' op=expression {String name = $FUNCTION.text; List<FormulaAst> args = new ArrayList(); args.add($op.result);}
        (   ',' op2=expression {args.add($op2.result);}
        )* ')' {result = op(name, args);}
    ;

parExpression returns [FormulaAst result]
    :   '(' expression ')' {result = $expression.result;}
    ;

channel returns [FormulaAst result]
    :   CHANNEL {result = channelFromToken($CHANNEL.text);}
    ;

numericLiteral returns [FormulaAst result]
    :   INT {result = integerFromToken($INT.text);}
    |   FLOAT {result = floatingPointFromToken($FLOAT.text);}
    ;

stringLiteral returns [FormulaAst result]
    :	STRING {result = stringFromToken($STRING.text);}
    ;

constant returns [FormulaAst result]
    :	FUNCTION {result = id($FUNCTION.text);}
    ;


INT :	'0'..'9'+
    ;

FLOAT
    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT
    ;

FUNCTION  :	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9')*
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

CHANNEL
    :  '\'' ( ESC_SEQ | ~('\\'|'\'') )* '\''
    ;

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
