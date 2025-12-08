import java.io.*;

import Exceptions.LexerError;
import Exceptions.ParserError;
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

			/*******************************/
			/* [7] Write OK to output file */
			/*******************************/
			fileWriter.print("OK");
			fileWriter.close();

			/*************************************/
			/* [8] Finalize AST GRAPHIZ DOT file */
			/*************************************/
			AstGraphviz.getInstance().finalizeFile();
		} catch (LexerError e) {
			writeError(outputFileName, "ERROR");
		} catch (ParserError e) {
			writeError(outputFileName, "ERROR(" + e.getLine() + ")");
		} catch (Throwable e) {
			writeError(outputFileName, "ERROR");
		}
	}

	private static void writeError(String outputFileName, String errorMessage) {
		try (PrintWriter errorWriter = new PrintWriter(outputFileName)) {
			errorWriter.print(errorMessage);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}
}
