//Changes call convention from __this call to __fast call, you need to provide what type of "_this" should be set
//then script will add unused _edx. Rest params will be not touched.
//Ghidra do not allow to use __thiscall without classes definition and classed are hard to rename and use, so this plugin solves it.
//@author h0x91b
//@category _NEW_
//@keybinding  F5
//@menupath 
//@toolbar 

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ghidra.app.script.GhidraScript;
import ghidra.app.util.datatype.DataTypeSelectionDialog;
import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.util.*;
import ghidra.util.data.DataTypeParser.AllowedDataTypes;
import ghidra.util.exception.DuplicateNameException;
import ghidra.util.exception.InvalidInputException;
import ghidra.program.model.reloc.*;
import ghidra.program.model.data.*;
import ghidra.program.model.block.*;
import ghidra.program.model.symbol.*;
import ghidra.program.model.scalar.*;
import ghidra.program.model.mem.*;
import ghidra.program.model.listing.*;
import ghidra.program.model.listing.Function.FunctionUpdateType;
import ghidra.program.model.lang.*;
import ghidra.program.model.pcode.*;
import ghidra.program.model.address.*;

public class changeCallConvenstionFromThisToFast extends GhidraScript {

    public void run() throws Exception {
		Function fn = getFunctionContaining(currentAddress);
		if(fn == null) {
			println("no function found");
			return;
		}
		
		println("fn: " + fn);
		
		if(!fn.getCallingConventionName().equals("__thiscall")) {
			println("calling convention isn't __thiscall, do nothing");
			return;
		}
		
		PluginTool tool = state.getTool();
		DataTypeManager dtm = currentProgram.getDataTypeManager();
		DataTypeSelectionDialog selectionDialog =
			new DataTypeSelectionDialog(tool, dtm, -1, AllowedDataTypes.FIXED_LENGTH);
		tool.showDialog(selectionDialog);
		DataType dataType = selectionDialog.getUserChosenDataType();

		if (dataType != null) {
			println("Chosen data type: " + dataType);
		}
		
		int cnt = fn.getParameterCount();
		println("Function has: "+cnt+" params");
		List<Parameter> list = new ArrayList<>();
		for(int i=1;i<cnt;i++) {
			Parameter p = fn.getParameter(i);
			println("Processing param " + i + " " + p);
			list.add(p);
		}
		
		Parameter paramEdx = new ParameterImpl("edx", DataType.DEFAULT, getCurrentProgram());
		list.add(0, paramEdx);
		
		Parameter paramThis = new ParameterImpl("_this", dataType, getCurrentProgram());
		list.add(0, paramThis);
		
		fn.updateFunction("__fastcall", fn.getReturn(), list, FunctionUpdateType.DYNAMIC_STORAGE_ALL_PARAMS, false, SourceType.DEFAULT); 
		
    }

}
