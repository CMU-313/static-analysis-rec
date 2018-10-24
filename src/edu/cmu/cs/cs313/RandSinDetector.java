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

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.BasicBlock.InstructionIterator;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.bcel.CFGDetector;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * This detector will issue an error if there is any control-flow path from a
 * source statement to a sink statement.
 * 
 * In this particular case, we issue an error if there is ever a call to
 * Math.random() followed by a call to Math.sin(), even though it makes no
 * sense ;)
 */
public class RandSinDetector extends CFGDetector {

	private final BugReporter bugReporter;

	public RandSinDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
		System.out.println("Loading RandSinDetector detector");
	}

	/**
	 * by FindBugs for every method in the system; contains the actual analysis
	 * 
	 * first, we go through all blocks and all instructions in blocks to
	 * identify sources and sinks in the graph. Then we use a simple graph
	 * search to find if there is any reachable path from a source to a sink. If
	 * there is, we issue a bug warning.
	 */
	protected void visitMethodCFG(MethodDescriptor methodDescriptor, CFG cfg)
			throws CheckedAnalysisException {
		Set<BasicBlock> sources = new HashSet<>();
		Set<BasicBlock> sinks = new HashSet<>();

		Iterator<BasicBlock> blockIterator = cfg.blockIterator();
		while (blockIterator.hasNext()) {
			BasicBlock block = blockIterator.next();
			InstructionIterator instructionIterator = block
					.instructionIterator();
			while (instructionIterator.hasNext()) {
				InstructionHandle instruction = instructionIterator.next();
				if (isSourceInstruction(instruction.getInstruction(),
						classContext.getConstantPoolGen()))
					sources.add(block);
				if (isSinkInstruction(instruction.getInstruction(),
						classContext.getConstantPoolGen()))
					sinks.add(block);
			}
		}

		if (hasPath(cfg, sources, sinks)) {
			System.err.println("Found path in method "
					+ methodDescriptor.getName());
			bugReporter.reportBug(new BugInstance(this,
					"CS_RAND_BEFORE_SIN", HIGH_PRIORITY).addClassAndMethod(
					methodDescriptor));
		}

	}

	/**
	 * search for a path in the CFG between any of the source nodes and any of
	 * the target nodes
	 * 
	 * @param cfg
	 *            the graph
	 * @param sources
	 *            the potential source nodes
	 * @param sinks
	 *            the potential target nodes
	 * @return true if there is a path, false otherwise
	 */
	private boolean hasPath(CFG cfg, Set<BasicBlock> sources,
			Set<BasicBlock> sinks) {

		Set<BasicBlock> verticesTodo = new HashSet<>(sources);
		Set<BasicBlock> reachable = new HashSet<>(sources);
		while (!verticesTodo.isEmpty()) {
			Iterator<BasicBlock> it = verticesTodo.iterator();
			BasicBlock vertex = it.next();
			it.remove();
			if (sinks.contains(vertex))
				return true;
			reachable.add(vertex);

			// you can also operate on edges, if you'd prefer:
			//  Iterator<Edge> neighborIterator = cfg.outgoingEdgeIterator(vertex);
			
			Iterator<BasicBlock> neighborIterator = cfg.successorIterator(vertex);
			while (neighborIterator.hasNext()) {
				BasicBlock neighbor = neighborIterator.next();
				if (!reachable.contains(neighbor)) {
					verticesTodo.add(neighbor);
				}
			}
		}
		return false;
	}

	/**
	 * detects static invocations to Math.sin
	 */
	private boolean isSinkInstruction(Instruction instruction,
			ConstantPoolGen cpg) {
		if (instruction instanceof INVOKESTATIC) {
			INVOKESTATIC inv = (INVOKESTATIC) instruction;

			if ("java.lang.Math".equals(inv.getReferenceType(cpg).toString())
					&& "sin".equals(inv.getName(cpg)))
				return true;

		}

		return false;

	}

	/**
	 * detects static invocations to Math.random
	 */
	private boolean isSourceInstruction(Instruction instruction,
			ConstantPoolGen cpg) {
		if (instruction instanceof INVOKESTATIC) {
			INVOKESTATIC inv = (INVOKESTATIC) instruction;

			System.out.println("Call: " + inv.getReferenceType(cpg) + " - "
					+ inv.getName(cpg) + " - " + inv.getSignature(cpg));

			if ("java.lang.Math".equals(inv.getReferenceType(cpg).toString())
					&& "random".equals(inv.getName(cpg)))
				return true;

		}

		return false;
	}

	
}