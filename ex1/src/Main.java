import java.io.*;
import java.io.PrintWriter;

import java_cup.runtime.Symbol;

public class Main {
	static public void main(String argv[]) {
		Lexer l;
		Symbol s;
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

			/***********************/
			/* [4] Read next token */
			/***********************/
			s = l.next_token();

			/********************************/
			/* [5] Main reading tokens loop */
			/********************************/
			while (s.sym != TokenNames.EOF) {
				String tokenName = TokenNames.getTokenName(s.sym);
				StringBuilder sb = new StringBuilder(tokenName);

				if (s.sym == TokenNames.ID || s.sym == TokenNames.INT_LITERAL) {
					sb.append('(').append(s.value).append(')');
				} else if (s.sym == TokenNames.STRING_LITERAL) {
					sb.append("(\"").append(s.value).append("\")");
				}

				sb.append('[')
						.append(l.getLine())
						.append(',')
						.append(l.getTokenStartPosition())
						.append(']');
				String output = sb.toString();

				/************************/
				/* [6] Print to console */
				/************************/
				System.out.println(output);

				/*********************/
				/* [7] Print to file */
				/*********************/
				fileWriter.println(output);

				/***********************/
				/* [8] Read next token */
				/***********************/
				s = l.next_token();
			}

			/******************************/
			/* [9] Close lexer input file */
			/******************************/
			l.yyclose();

			/**************************/
			/* [10] Close output file */
			/**************************/
			fileWriter.close();
		} catch (Throwable e) {
			writeError(outputFileName);
			e.printStackTrace();
		}

	}

	private static void writeError(String outputFileName) {
		try (PrintWriter errorWriter = new PrintWriter(outputFileName)) {
			errorWriter.print("ERROR");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
