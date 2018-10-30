package edu.cmu.cs.cs313;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.bcel.generic.InstructionHandle;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.ba.BasicBlock;
import edu.umd.cs.findbugs.ba.CFG;
import edu.umd.cs.findbugs.ba.BasicBlock.InstructionIterator;
import edu.umd.cs.findbugs.bcel.CFGDetector;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;

public class ControlFlowPrinter extends CFGDetector{

	private final BugReporter bugReporter;

	//saves a bug report instance so our plugin can throw a findbugs error
	public ControlFlowPrinter(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
		System.out.println("Loading control flow detector detector");
	}

	@Override
	protected void visitMethodCFG(MethodDescriptor methodDescriptor, CFG cfg) throws CheckedAnalysisException {
		//used to show the boundary between the different methods
		System.out.println("--------------------------------" + methodDescriptor);
		printCFG(cfg);
	}

	private void printCFG(CFG cfg){
		HashMap<BasicBlock,Integer> blockRename = new HashMap<BasicBlock,Integer>();
		Iterator<BasicBlock> bbIter = cfg.blockIterator();
		//iterate through the blocks in the control flow graph
		// note that this simply iterates over the list of blocks provided by the cfg;
		// it does not follow paths in any interesting way.
		while(bbIter.hasNext()){
			//print the identification information for each block
			BasicBlock currentBlock = bbIter.next();
			if(currentBlock.getLabel()==cfg.getEntry().getLabel()){
				System.out.println("Starting Block!");
			}
			System.out.println("Current Block Label: "+currentBlock.getLabel());
			blockRename.put(currentBlock, currentBlock.getLabel());
			//print out the java byte code instructions in each block
			System.out.println("instructions for block "+blockRename.get(currentBlock)+":");
			InstructionIterator ii = currentBlock.instructionIterator();
			while(ii.hasNext()){
				InstructionHandle ih = ii.next();
				System.out.println(ih.toString());
			}
			//print out the list of blocks which come before this block in the control flow graph
			Iterator<BasicBlock> predIterator = cfg.predecessorIterator(currentBlock);
			System.out.print("predecessor blocks: ");
			while(predIterator.hasNext()){
				int blockName = predIterator.next().getLabel();
				System.out.print(blockName+", ");
			}
			System.out.println(""); //finished the current line
			//print out the list of blocks which come after this block in the control flow graph
			Iterator<BasicBlock> successorIterator = cfg.successorIterator(currentBlock);
			System.out.print("successor blocks: ");
			while(successorIterator.hasNext()){
				int blockName = successorIterator.next().getLabel();
				System.out.print(blockName+", ");
			}
			System.out.println(""); //finished the current line
			System.out.println("");
		}
	}
}
