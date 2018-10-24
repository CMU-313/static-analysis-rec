/*
 * FindBugs - Find Bugs in Java programs
 * Copyright (C) 2003-2008 University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.cmu.cs.cs313;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.INVOKEDYNAMIC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.ReferenceType;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.BasicBlock.InstructionIterator;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.Edge;
import edu.umd.cs.findbugs.bcel.CFGDetector;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * This detector will issue an error if there is any control-flow path from a
 * source statement to a sink statement.
 * 
 * In this particular case, we issue an error if there is ever a call to
 * Math.random() followed by a call to Math.sin(), even though it makes not
 * sense ;)
 */
public class MethodCallLogger extends CFGDetector {

	private final BugReporter bugReporter;

	public MethodCallLogger(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
		System.out.println("Loading MethodCallLogger detector");
	}

	/**
	 * called by FindBugs for every method in the system; contains the actual
	 * analysis
	 */
	protected void visitMethodCFG(MethodDescriptor methodDescriptor, CFG cfg)
			throws CheckedAnalysisException {
		Iterator<BasicBlock> blockIterator = cfg.blockIterator();
		while (blockIterator.hasNext()) {
			BasicBlock block = blockIterator.next();
			InstructionIterator instructionIterator = block
					.instructionIterator();
			while (instructionIterator.hasNext()) {
				InstructionHandle instruction = instructionIterator.next();

				logMethodCallInstruction(instruction);
			}
		}
	}

	/**
	 * detects static invocations to Math.sin
	 */
	private void logMethodCallInstruction(InstructionHandle instrHandle) {
		// we care about normal method calls on classes and interfaces (invoke
		// virtual and interface), but not about static method calls (invoke
		// static) and constructor calls (invoke special)
		if (instrHandle.getInstruction() instanceof INVOKEVIRTUAL
				|| instrHandle.getInstruction() instanceof INVOKEINTERFACE) {
			InvokeInstruction inv = (InvokeInstruction) instrHandle
					.getInstruction();

			ReferenceType staticTypeOfTarget = inv
					.getReferenceType(classContext.getConstantPoolGen());
			
			String methodName = inv.getMethodName(classContext.getConstantPoolGen());

			//String methodName = inv.getName(classContext.getConstantPoolGen());

			String targetObject = findTargetName(instrHandle);

			System.out.println("calling " + methodName + " on " + targetObject
					+ " of type " + staticTypeOfTarget);
		}

	}

	/**
	 * Find the name of the target object of a method invocation
	 * 
	 * Will identify variable name "a" in "a.foo()" and field name "f" in
	 * "this.f.foo()". Field names are qualified by the class they occur in
	 * (that is they contain at least one "."). This ensures that all names are
	 * unique within a method.
	 * 
	 * The method will identify the target only in simple cases. Especially
	 * calls on computed targets, such as "foo().bar()", are not supported. The
	 * function returns null if the the target cannot be resolved.
	 * 
	 * 
	 * Technically, this works by going backward in the source code to find a
	 * ALOAD instruction (then we are calling a method on a local variable) or a
	 * FIELDLOAD instruction (then we are calling a method on a field). It
	 * proceeds only one step backward and relies on common patterns in the
	 * compiler.
	 */
	private String findTargetName(InstructionHandle inv) {
		InstructionHandle priorInstruction = inv.getPrev();
		if (priorInstruction == null)
			return null;

		// load the value of a local variable on the stack
		if (priorInstruction.getInstruction() instanceof ALOAD) {
			ALOAD loadInstruction = (ALOAD) priorInstruction.getInstruction();
			int variableIndex = loadInstruction.getIndex();
			LocalVariable variable = method.getLocalVariableTable()
					.getLocalVariable(variableIndex,
							priorInstruction.getPosition());
			return variable.getName();
		}
		// load the value of a field on the stack
		if (priorInstruction.getInstruction() instanceof GETFIELD) {
			GETFIELD getFieldInstruction = (GETFIELD) priorInstruction
					.getInstruction();
			return getFieldInstruction.getReferenceType(classContext
					.getConstantPoolGen())
					+ "."
					+ getFieldInstruction.getFieldName(classContext
							.getConstantPoolGen());
		}

		return null;
	}
}