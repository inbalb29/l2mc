
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import Exceptions.*;
import analysis.DataFlowAnalysis;
import ast.*;
import ir.Ir;

public class Main {
	static public void main(String argv[]) {
		Lexer l;
		Parser p;
		AstProg ast;
		FileReader fileReader;
		PrintWriter fileWriter;
		String inputFileName = argv[0];
		String outputFileName = argv[1];

		try {
			/********************************/
			/* [1] Initialize a file reader */
			/********************************/
			fileReader = new FileReader(inputFileName);

			/********************************/
			/* [2] Initialize a file writer */
			/********************************/
			fileWriter = new PrintWriter(outputFileName);

			/******************************/
			/* [3] Initialize a new lexer */
			/******************************/
			l = new Lexer(fileReader);

			/*******************************/
			/* [4] Initialize a new parser */
			/*******************************/
			p = new Parser(l);

			/***********************************/
			/* [5] 3 ... 2 ... 1 ... Parse !!! */
			/***********************************/
			ast = (AstProg) p.parse().value;

			/*************************/
			/* [6] Print the AST ... */
			/*************************/
			ast.printMe();

			/**************************/
			/* [7] Semant the AST ... */
			/**************************/
			ast.semantMe();

			/**********************/
			/* [8] IR the AST ... */
			/**********************/
			ast.irMe();

			/*******************************************/
			/* [9] Run uninitialized variable analysis */
			/*******************************************/
			DataFlowAnalysis analysis = new DataFlowAnalysis();
			Set<String> uninitVars = analysis.analyze(Ir.getInstance().getCommands());

			/*********************/
			/* [10] Write output */
			/*********************/
			if (uninitVars.isEmpty()) {
				fileWriter.print("!OK");
			} else {
				// Output sorted variable names, one per line
				List<String> sortedVars = new ArrayList<>(uninitVars);
				Collections.sort(sortedVars);
				for (int i = 0; i < sortedVars.size(); i++) {
					fileWriter.print(sortedVars.get(i));
					if (i < sortedVars.size() - 1) {
						fileWriter.println();
					}
				}
			}
			fileWriter.close();

			/**************************************/
			/* [10] Finalize AST GRAPHIZ DOT file */
			/**************************************/
			AstGraphviz.getInstance().finalizeFile();
		} catch (LexerError e) {
			writeError(outputFileName, "ERROR", e);
		} catch (ParserError e) {
			writeError(outputFileName, "ERROR(" + e.getLine() + ")", e);
		} catch (SemanticError e) {
			writeError(outputFileName, "ERROR(" + e.getLine() + ")", e);
		} catch (Throwable e) {
			writeError(outputFileName, "ERROR", e);
		}
	}

	private static void writeError(String outputFileName, String errorMessage, Throwable e) {
		try (PrintWriter errorWriter = new PrintWriter(outputFileName)) {
			errorWriter.print(errorMessage);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		e.printStackTrace();
	}
}
