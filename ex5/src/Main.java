import java.io.*;

import Exceptions.*;
import ast.*;
import codegen.RegisterAllocationFailed;
import ir.*;
import mips.*;

public class Main {
	static public void main(String argv[]) {
		Lexer l;
		Parser p;
		AstProg ast;
		FileReader fileReader;
		String inputFileName = argv[0];
		String outputFileName = argv[1];

		try {
			/********************************/
			/* [1] Initialize a file reader */
			/********************************/
			fileReader = new FileReader(inputFileName);

			/******************************/
			/* [2] Initialize a new lexer */
			/******************************/
			l = new Lexer(fileReader);

			/*******************************/
			/* [3] Initialize a new parser */
			/*******************************/
			p = new Parser(l);

			/***********************************/
			/* [4] 3 ... 2 ... 1 ... Parse !!! */
			/***********************************/
			ast = (AstProg) p.parse().value;

			/*************************/
			/* [5] Print the AST ... */
			/*************************/
			ast.printMe();

			/**************************/
			/* [6] Semant the AST ... */
			/**************************/
			ast.semantMe();

			/**********************/
			/* [7] IR the AST ... */
			/**********************/
			ast.irMe();

			/*****************************/
			/* [8] Register Allocation */
			/*****************************/
			Ir.getInstance().allocateRegisters();

			/*************************************/
			/* [9] Initialize MIPS output file */
			/*************************************/
			MipsGenerator.init(outputFileName);

			/***********************/
			/* [10] MIPS the Ir */
			/***********************/
			Ir.getInstance().mipsMe();

			/**************************************/
			/* [11] Finalize AST GRAPHIZ DOT file */
			/**************************************/
			AstGraphviz.getInstance().finalizeFile();

			/***************************/
			/* [12] Finalize MIPS file */
			/***************************/
			MipsGenerator.getInstance().finalizeFile();

		} catch (RegisterAllocationFailed e) {
			writeError(outputFileName, "Register Allocation Failed", e);
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
