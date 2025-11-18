<div align="center">
  <h1>L to MIPS Compiler</h1>
  <a href="https://github.com/Neil-001/l2mc/blob/main/LICENSE"><img alt="License: GPL v3" src="https://img.shields.io/badge/License-GPLv3-blue.svg"></a>
</div>

\
Solutions for the **Compilation** course exercises at Tel Aviv University.

## Repository Structure

The compiler was built over the course of multiple exercises. Each exercise is contained within its own directory, structured as follows:

```bash
l2mc/
├── ex1/          # Lexical analysis
├── ex2/          # Syntax analysis
├── ex3/          # Semantic analysis
├── ex4/          # IR & data-flow analysis
├── ex5/          # Code generation
├── tests/        # Unit tests for various components
└── README.md
```

## Setup

1. **Install prerequisites** (JDK 17+, JFlex, Graphviz). On Ubuntu/Debian you can grab everything with APT:

	```bash
	sudo apt update
	sudo apt install default-jdk graphviz jflex
	```

    The `external_jars/` directory already contains the `java-cup` compiler and runtime jars.

2. **Build the project**. Navigate to the desired exercise directory and run `make`:

	```bash
	cd ex<num> # e.g., ex1
	make
	```

## Exercises

### Exercise 1: Lexical Analysis

Implements a lexical scanner based on JFlex to tokenize the L language. The input is a single L source file, and the output is a text file containing a tokenized representation of the source code.

### Exercise 2: Syntax Analysis

Extends the lexical scanner with a CUP-based parser to generate an Abstract Syntax Tree (AST) from the tokenized input. The output is a text file indicating whether the input is syntactically correct.

## License

This project is distributed under the GPL-3.0 License. See the
[LICENSE](LICENSE) file for more information.
