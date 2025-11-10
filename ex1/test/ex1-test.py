#!/usr/bin/env python3

import os
import subprocess
import sys
from pathlib import Path

# Get the project root directory (ex1)
PROJECT_ROOT = Path(__file__).parent.parent.resolve()
SRC_DIR = PROJECT_ROOT / "src"
BIN_DIR = PROJECT_ROOT / "bin"
INPUT_DIR = PROJECT_ROOT / "input"
OUTPUT_DIR = PROJECT_ROOT / "output"
EXTERNAL_JARS_DIR = PROJECT_ROOT / "external_jars"
LEX_FILE = PROJECT_ROOT / "jflex" / "LEX_FILE.lex"
LEXER_JAVA = SRC_DIR / "Lexer.java"
JAVA_CUP_JAR = EXTERNAL_JARS_DIR / "java-cup-11b-runtime.jar"

def run_command(cmd, cwd=None):
    """Run a shell command and return the result."""
    print(f"Running: {' '.join(cmd)}")
    result = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error: {result.stderr}")
        sys.exit(1)
    return result

def main():
    # Step 1: Delete src/Lexer.java if present
    if LEXER_JAVA.exists():
        print(f"Deleting {LEXER_JAVA}")
        LEXER_JAVA.unlink()
    
    # Step 2: Regenerate Lexer.java using jflex
    print(f"\nGenerating Lexer.java from {LEX_FILE}")
    run_command(["jflex", "-d", str(SRC_DIR), str(LEX_FILE)])
    
    # Step 3: Create bin directory and compile using make
    print(f"\nCompiling Java sources")
    BIN_DIR.mkdir(exist_ok=True)
    OUTPUT_DIR.mkdir(exist_ok=True)
    
    # Compile using javac directly (make requires full rebuild)
    java_files = list(SRC_DIR.rglob("*.java"))
    if not java_files:
        print("No Java files found to compile!")
        sys.exit(1)
    
    # Try to find javac - it might be in WSL or need installation
    compile_cmd = ["javac", "-cp", str(JAVA_CUP_JAR), "-d", str(BIN_DIR)] + [str(f) for f in java_files]
    try:
        run_command(compile_cmd)
    except FileNotFoundError:
        print("\nERROR: javac not found. Please install JDK.")
        sys.exit(1)
    
    # Step 5: Run Main class for every file in input directory
    print(f"\nRunning Main class for all input files")
    input_files = sorted(INPUT_DIR.glob("*.txt"))
    
    for input_file in input_files:
        output_file = OUTPUT_DIR / f"{input_file.stem}_OUTPUT.txt"
        print(f"\n  Processing: {input_file.name} -> {output_file.name}")
        
        # Run the Main class with input and output files
        result = subprocess.run(
            ["java", "-cp", f"{BIN_DIR}:{JAVA_CUP_JAR}", "Main", str(input_file), str(output_file)],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.PIPE,
            text=True
        )

        stderr_output = result.stderr.strip()
        if stderr_output:
            print(f"    Error processing {input_file.name}: {stderr_output}")
        else:
            print(f"    Success: {output_file.name} created")
    
    print("\n:) All tests completed!")

if __name__ == "__main__":
    main()
