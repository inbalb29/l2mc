#!/usr/bin/env python3

import os
import subprocess
import sys
from pathlib import Path

# Get the project root directory (ex4)
PROJECT_ROOT = Path(__file__).parent.parent.resolve() / "ex4"
INPUT_DIR = Path(__file__).parent / "ex4-input"
EXPECTED_OUTPUT_DIR = Path(__file__).parent / "ex4-expected_output"
TEMP_OUTPUT_DIR = Path(__file__).parent / "output"
ERROR_OUTPUT_DIR = Path(__file__).parent / "ex4-error_output"

def run_command(cmd, cwd=None, check=True):
    """Run a shell command and return the result."""
    result = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True)
    if check and result.returncode != 0:
        print(f"Error running command: {' '.join(cmd)}")
        print(f"stderr: {result.stderr}")
        sys.exit(1)
    return result

def main():
    # Step 1: Build the project using make
    print("\n[1] Building project with make...")
    result = run_command(["make", "compile"], cwd=PROJECT_ROOT)
    if result.returncode != 0:
        print(f"Build failed!")
        print(result.stderr)
        sys.exit(1)
    print("    ✓ Build successful")
    
    # Step 2: Create output directory
    TEMP_OUTPUT_DIR.mkdir(exist_ok=True)
    
    # Step 3: Run analyzer for all input files
    input_files = sorted(INPUT_DIR.glob("*.txt"))
    
    if not input_files:
        print(f"\nNo input files found in {INPUT_DIR}")
        sys.exit(1)
    
    print(f"\n[2] Running dataflow analyzer on {len(input_files)} test files...")
    
    passed = 0
    failed = 0
    
    for input_file in input_files:
        # Determine expected output file name
        # TEST_1.txt -> TEST_1_Expected_Output.txt
        expected_output_file = EXPECTED_OUTPUT_DIR / f"{input_file.stem}_Expected_Output.txt"
        temp_output_file = TEMP_OUTPUT_DIR / f"{input_file.stem}_Output.txt"
        
        if not expected_output_file.exists():
            print(f"    ⚠ Skipping {input_file.name} - no expected output file")
            continue
        
        # Run the dataflow analyzer
        result = subprocess.run(
            ["java", "-jar", "ANALYZER", str(input_file), str(temp_output_file)],
            cwd=PROJECT_ROOT,
            capture_output=True,
            text=True
        )
        
        # Read the actual output
        try:
            with open(temp_output_file, 'r') as f:
                actual_output = f.read()
        except FileNotFoundError:
            actual_output = ""
        
        # Read the expected output
        with open(expected_output_file, 'r') as f:
            expected_output = f.read()
        
        # Compare outputs
        if actual_output == expected_output:
            print(f"    ✓ {input_file.name}")
            passed += 1
        else:
            print(f"    ✗ {input_file.name}")
            print(f"      - Expected: {repr(expected_output)}")
            print(f"      - Got:      {repr(actual_output)}")
            err_base = input_file.stem
            stdout_text = result.stdout or ""
            stderr_text = result.stderr or ""
            # Save error outputs if any
            if stdout_text or stderr_text or result.returncode != 0:
                ERROR_OUTPUT_DIR.mkdir(exist_ok=True)
                if stdout_text:
                    stdout_path = ERROR_OUTPUT_DIR / f"{err_base}_stdout.txt"
                    stdout_path.write_text(stdout_text)
                    print(f"      - Saved stdout to {stdout_path.relative_to(Path(__file__).parent)}")
                if stderr_text:
                    stderr_path = ERROR_OUTPUT_DIR / f"{err_base}_stderr.txt"
                    stderr_path.write_text(stderr_text)
                    print(f"      - Saved stderr to {stderr_path.relative_to(Path(__file__).parent)}")
            failed += 1
    
    # Summary
    print(f"\nResults: {passed} passed, {failed} failed out of {passed + failed} tests")
    
    if failed == 0:
        print("\n✓ All tests passed!")
        sys.exit(0)
    else:
        print(f"\n✗ {failed} test(s) failed")
        sys.exit(1)

if __name__ == "__main__":
    main()
