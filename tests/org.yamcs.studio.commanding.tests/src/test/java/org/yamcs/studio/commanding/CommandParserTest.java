package org.yamcs.studio.commanding;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.yamcs.protobuf.Rest.IssueCommandRequest.Assignment;
import org.yamcs.studio.commanding.CommandParser.ParseResult;

public class CommandParserTest {

    @Test
    public void testIntegerArguments() {
        ParseResult commandType = CommandParser.parseCommand("/YSS/SIMULATOR/SWITCH_VOLTAGE_ON(voltage_num: 1)");

        assertEquals("/YSS/SIMULATOR/SWITCH_VOLTAGE_ON", commandType.getQualifiedName());
        assertEquals(1, commandType.getAssignments().size());

        Assignment arg0 = commandType.getAssignments().get(0);
        assertEquals("voltage_num", arg0.getName());
        assertEquals("1", arg0.getValue());
    }

    @Test
    public void testNoArguments() {
        ParseResult commandType = CommandParser.parseCommand("/YSS/SIMULATOR/SWITCH_VOLTAGE_ON()");

        assertEquals("/YSS/SIMULATOR/SWITCH_VOLTAGE_ON", commandType.getQualifiedName());
        assertEquals(0, commandType.getAssignments().size());
    }

    @Test
    public void testDoubleQuotedStringArguments() {
        ParseResult commandType = CommandParser.parseCommand("/YSS/SIMULATOR/TestCmd(Param: \"Hello world\")");

        assertEquals("/YSS/SIMULATOR/TestCmd", commandType.getQualifiedName());
        assertEquals(1, commandType.getAssignments().size());

        Assignment arg0 = commandType.getAssignments().get(0);
        assertEquals("Param", arg0.getName());
        assertEquals("Hello world", arg0.getValue());
    }

    @Test
    public void testSingleQuotedStringArguments() {
        ParseResult commandType = CommandParser.parseCommand("/YSS/SIMULATOR/TestCmd(Param: 'Hello world')");

        assertEquals("/YSS/SIMULATOR/TestCmd", commandType.getQualifiedName());
        assertEquals(1, commandType.getAssignments().size());

        Assignment arg0 = commandType.getAssignments().get(0);
        assertEquals("Param", arg0.getName());
        assertEquals("Hello world", arg0.getValue());
    }

    @Test
    public void testStringArgumentEscaping() {
        // Actual test string after java interpets it: '\'Hello\' \"world\"'
        // which our parser should interpet then as: 'Hello' "world"
        ParseResult commandType = CommandParser.parseCommand("/YSS/SIMULATOR/TestCmd(Param: '\\'Hello\\' \\\"world\\\"')");

        assertEquals("/YSS/SIMULATOR/TestCmd", commandType.getQualifiedName());
        assertEquals(1, commandType.getAssignments().size());

        Assignment arg0 = commandType.getAssignments().get(0);
        assertEquals("Param", arg0.getName());
        assertEquals("'Hello' \"world\"", arg0.getValue());
    }
}
