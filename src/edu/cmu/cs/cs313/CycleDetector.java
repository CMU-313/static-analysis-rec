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

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.Edge;
import edu.umd.cs.findbugs.ba.EdgeTypes;
import edu.umd.cs.findbugs.bcel.CFGDetector;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

/**
 * This detector will issue an error if there is a cycle in the control flow graph.
 */
public class CycleDetector extends CFGDetector {

	private final BugReporter bugReporter;

	public CycleDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
		System.out.println("Loading CycleDetector detector");
	}

	/**
	 * by FindBugs for every method in the system; contains the actual analysis
	 */
	protected void visitMethodCFG(MethodDescriptor methodDescriptor, CFG cfg)
			throws CheckedAnalysisException {

		if (hasCycle(cfg)) {
			System.err.println("Found cycle in method "
					+ methodDescriptor.getName());
			bugReporter.reportBug(new BugInstance(this,
					"CS_RAND_BEFORE_SIN", HIGH_PRIORITY).addClassAndMethod(
					methodDescriptor));
		}

	}

	/**
	 * search for a cycle in the CFG between any node
	 * 
	 * @param cfg
	 *            the graph
	 * @return true if there is a path, false otherwise
	 */
	private boolean hasCycle(CFG cfg) {
		Set<BasicBlock> blocks = new HashSet<>();
		blocks.add(cfg.getEntry());
		Set<BasicBlock> visited = new HashSet<>();
		
		while (!blocks.isEmpty()) {
			Iterator<BasicBlock> it = blocks.iterator();
			BasicBlock block = it.next();
			it.remove();
			
			if(visited.contains(block)) {
				return true;
			}
			
			visited.add(block);
			Iterator<Edge> childrenIterator = cfg.outgoingEdgeIterator(block);
			
            while (childrenIterator.hasNext()) {
                Edge edge = childrenIterator.next();
                
                if (edge.getType() != EdgeTypes.UNHANDLED_EXCEPTION_EDGE) {
                    BasicBlock child = edge.getTarget();
                    blocks.add(child);
                }
            }

		}
		return false;
	}
	
}