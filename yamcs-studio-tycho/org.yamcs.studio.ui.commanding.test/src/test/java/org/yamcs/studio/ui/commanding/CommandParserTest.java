package org.yamcs.studio.ui.commanding;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.yamcs.protobuf.Rest.RestArgumentType;
import org.yamcs.protobuf.Rest.RestCommandType;
import org.yamcs.studio.core.model.CommandingCatalogue;

public class CommandParserTest {
    
    @Test
    public void testIntegerArguments() {
        CommandingCatalogue mockCatalogue = mock(CommandingCatalogue.class);
        when(mockCatalogue.getCommandOrigin()).thenReturn("an-origin");
        when(mockCatalogue.getNextCommandClientId()).thenReturn(123);
        RestCommandType commandType = CommandParser.toCommand("/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)", mockCatalogue);
        
        assertEquals("/YSS/SIMULATOR/SWITCH_VOLTAGE_ON", commandType.getId().getName());
        assertEquals(1, commandType.getArgumentsCount());
        
        RestArgumentType arg0 = commandType.getArguments(0);
        assertEquals("voltage_num", arg0.getName());
        assertEquals("1", arg0.getValue());
    }
    
    @Test
    public void testNoArguments() {
        CommandingCatalogue mockCatalogue = mock(CommandingCatalogue.class);
        when(mockCatalogue.getCommandOrigin()).thenReturn("an-origin");
        when(mockCatalogue.getNextCommandClientId()).thenReturn(123);
        RestCommandType commandType = CommandParser.toCommand("/YSS/SIMULATOR/SWITCH_VOLTAGE_ON()", mockCatalogue);
        
        assertEquals("/YSS/SIMULATOR/SWITCH_VOLTAGE_ON", commandType.getId().getName());
        assertEquals(0, commandType.getArgumentsCount());
    }
    
    @Test
    public void testDoubleQuotedStringArguments() {
        CommandingCatalogue mockCatalogue = mock(CommandingCatalogue.class);
        when(mockCatalogue.getCommandOrigin()).thenReturn("an-origin");
        when(mockCatalogue.getNextCommandClientId()).thenReturn(123);
        RestCommandType commandType = CommandParser.toCommand("/YSS/SIMULATOR/TestCmd(Param: \"Hello world\")", mockCatalogue);
        
        assertEquals("/YSS/SIMULATOR/TestCmd", commandType.getId().getName());
        assertEquals(1, commandType.getArgumentsCount());
        
        RestArgumentType arg0 = commandType.getArguments(0);
        assertEquals("Param", arg0.getName());
        assertEquals("Hello world", arg0.getValue());
    }
    
    @Test
    public void testSingleQuotedStringArguments() {
        CommandingCatalogue mockCatalogue = mock(CommandingCatalogue.class);
        when(mockCatalogue.getCommandOrigin()).thenReturn("an-origin");
        when(mockCatalogue.getNextCommandClientId()).thenReturn(123);
        RestCommandType commandType = CommandParser.toCommand("/YSS/SIMULATOR/TestCmd(Param: 'Hello world')", mockCatalogue);
        
        assertEquals("/YSS/SIMULATOR/TestCmd", commandType.getId().getName());
        assertEquals(1, commandType.getArgumentsCount());
        
        RestArgumentType arg0 = commandType.getArguments(0);
        assertEquals("Param", arg0.getName());
        assertEquals("Hello world", arg0.getValue());
    }
    
    @Test
    public void testStringArgumentEscaping() {
        CommandingCatalogue mockCatalogue = mock(CommandingCatalogue.class);
        when(mockCatalogue.getCommandOrigin()).thenReturn("an-origin");
        when(mockCatalogue.getNextCommandClientId()).thenReturn(123);
        // Actual test string after java interpets it: '\'Hello\' \"world\"'
        // which our parser should interpet then as: 'Hello' "world"
        System.out.println("Testing " + "/YSS/SIMULATOR/TestCmd(Param: '\\'Hello\\' \\\"world\\\"')");
        RestCommandType commandType = CommandParser.toCommand("/YSS/SIMULATOR/TestCmd(Param: '\\'Hello\\' \\\"world\\\"')", mockCatalogue);
        
        assertEquals("/YSS/SIMULATOR/TestCmd", commandType.getId().getName());
        assertEquals(1, commandType.getArgumentsCount());
        
        RestArgumentType arg0 = commandType.getArguments(0);
        assertEquals("Param", arg0.getName());
        assertEquals("'Hello' \"world\"", arg0.getValue());
    }
}
