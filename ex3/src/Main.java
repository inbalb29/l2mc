
import java.io.*;

import Exceptions.*;
import ast.*;

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

			/*************************/
			/* [8] Close output file */
			/*************************/
			fileWriter.print("OK");
			fileWriter.close();

			/*************************************/
			/* [9] Finalize AST GRAPHIZ DOT file */
			/*************************************/
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
