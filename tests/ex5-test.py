#!/usr/bin/env python3

import os
import subprocess
import sys
from pathlib import Path

# Get the project root directory (ex5)
PROJECT_ROOT = Path(__file__).parent.parent.resolve() / "ex5"
INPUT_DIR = Path(__file__).parent / "ex5-input"
EXPECTED_OUTPUT_DIR = Path(__file__).parent / "ex5-expected_output"
TEMP_OUTPUT_DIR = Path(__file__).parent / "output"
ERROR_OUTPUT_DIR = Path(__file__).parent / "ex5-error_output"

SPIM_HEADER = """SPIM Version 8.0 of January 8, 2010
Copyright 1990-2010, James R. Larus.
All Rights Reserved.
See the file README for a full copyright notice.
Loaded: /usr/lib/spim/exceptions.s
"""

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
    result = run_command(["make"], cwd=PROJECT_ROOT)
    if result.returncode != 0:
        print(f"Build failed!")
        print(result.stderr)
        sys.exit(1)
    print("    ✓ Build successful")
    
    # Step 2: Create output directory
    TEMP_OUTPUT_DIR.mkdir(exist_ok=True)

    # Step 3: Run compiler + SPIM for all input files
    input_files = sorted(INPUT_DIR.glob("*.txt"))
    
    if not input_files:
        print(f"\nNo input files found in {INPUT_DIR}")
        sys.exit(1)

    print(f"\n[2] Running compiler + SPIM on {len(input_files)} test files...")

    passed = 0
    failed = 0
    
    for input_file in input_files:
        # Determine expected output file name
        # TEST_1.txt -> TEST_1_Expected_Output.txt
        expected_output_file = EXPECTED_OUTPUT_DIR / f"{input_file.stem}_Expected_Output.txt"
        temp_mips_file = TEMP_OUTPUT_DIR / f"{input_file.stem}_mips.s"
        temp_output_file = TEMP_OUTPUT_DIR / f"{input_file.stem}_Output.txt"
        
        if not expected_output_file.exists():
            print(f"    ⚠ Skipping {input_file.name} - no expected output file")
            continue

        # Read expected output
        with open(expected_output_file, 'r') as f:
            expected_output = f.read()

        # Run the compiler
        compiler_result = subprocess.run(
            ["java", "-jar", "COMPILER", str(input_file), str(temp_mips_file)],
            cwd=PROJECT_ROOT,
            capture_output=True,
            text=True
        )

        # Read compiler output file
        try:
            with open(temp_mips_file, 'r') as f:
                compiler_output = f.read()
        except FileNotFoundError:
            compiler_output = ""

        # Check if compiler produced an error or "Register Allocation Failed"
        # (i.e., not MIPS assembly)
        if compiler_output.startswith("ERROR") or compiler_output == "Register Allocation Failed":
            # Direct comparison - no SPIM needed
            actual_output = compiler_output
        else:
            # Run SPIM on the generated MIPS code
            spim_result = subprocess.run(
                ["spim", "-f", str(temp_mips_file)],
                capture_output=True,
                text=True
            )
            actual_output = spim_result.stdout

        # Save actual output
        with open(temp_output_file, 'w') as f:
            f.write(actual_output)

        # Compare outputs
        if actual_output == expected_output:
            print(f"    ✓ {input_file.name}")
            passed += 1
        else:
            print(f"    ✗ {input_file.name}")
            # Show meaningful diff
            if expected_output.startswith(SPIM_HEADER.rstrip()):
                exp_payload = expected_output[len(SPIM_HEADER):]
            else:
                exp_payload = expected_output
            if actual_output.startswith(SPIM_HEADER.rstrip()):
                act_payload = actual_output[len(SPIM_HEADER):]
            else:
                act_payload = actual_output
            print(f"      - Expected: {repr(exp_payload)}")
            print(f"      - Got:      {repr(act_payload)}")
            err_base = input_file.stem
            stdout_text = compiler_result.stdout or ""
            stderr_text = compiler_result.stderr or ""
            if stdout_text or stderr_text or compiler_result.returncode != 0:
                ERROR_OUTPUT_DIR.mkdir(exist_ok=True)
                if stdout_text:
                    stdout_path = ERROR_OUTPUT_DIR / f"{err_base}_stdout.txt"
                    stdout_path.write_text(stdout_text)
                    print(f"      - Saved stdout to {stdout_path.relative_to(Path(__file__).parent)}")
                if stderr_text:
                    stderr_path = ERROR_OUTPUT_DIR / f"{err_base}_stderr.txt"
                    stderr_path.write_text(stderr_text)
                    print(f"      - Saved stderr to {stderr_path.relative_to(Path(__file__).parent)}")
            # Also save the MIPS file for debugging
            if not compiler_output.startswith("ERROR") and compiler_output != "Register Allocation Failed":
                ERROR_OUTPUT_DIR.mkdir(exist_ok=True)
                mips_path = ERROR_OUTPUT_DIR / f"{err_base}_mips.s"
                mips_path.write_text(compiler_output)
                print(f"      - Saved MIPS to {mips_path.relative_to(Path(__file__).parent)}")
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
