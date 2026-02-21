package mips;

import java.io.PrintWriter;
import java.util.*;
import temp.*;

public class MipsGenerator {
	private PrintWriter fileWriter;
	private Map<Integer, String> tempRegisterMap = new HashMap<>();
	private int saturationCounter = 0;
	private int stringOpCounter = 0;

	private String reg(Temp t) {
		String r = tempRegisterMap.get(t.getSerialNumber());
		if (r != null)
			return r;
		return "Temp_" + t.getSerialNumber();
	}

	public void setTempRegisterMap(Map<Integer, String> map) {
		this.tempRegisterMap = map;
	}

	/* ========== Data Section ========== */

	public void allocate(String varName) {
		fileWriter.format(".data\n\tglobal_%s: .word 0\n", varName);
	}

	public void allocateWithInit(String varName, int value) {
		fileWriter.format(".data\n\tglobal_%s: .word %d\n", varName, value);
	}

	public void allocateWithLabel(String varName, String label) {
		fileWriter.format(".data\n\tglobal_%s: .word %s\n", varName, label);
	}

	public void allocateString(String label, String value) {
		fileWriter.format(".data\n%s: .asciiz \"%s\"\n", label, escapeString(value));
	}

	public void allocateVtable(String label, List<String> methodLabels) {
		fileWriter.format(".data\n%s:\n", label);
		for (String ml : methodLabels) {
			fileWriter.format("\t.word %s\n", ml);
		}
	}

	private String escapeString(String s) {
		return s.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\t", "\\t");
	}

	/* ========== Function Prologue / Epilogue ========== */

	public void functionPrologue(String name, int numLocals) {
		int frameSize = 48 + numLocals * 4;
		fileWriter.format(".text\n%s:\n", name);
		fileWriter.format("\tsw $ra,-4($sp)\n");
		fileWriter.format("\tsw $fp,-8($sp)\n");
		fileWriter.format("\tsubu $fp,$sp,8\n");
		fileWriter.format("\tsubu $sp,$sp,%d\n", frameSize);
		for (int i = 0; i < 10; i++) {
			fileWriter.format("\tsw $t%d,%d($fp)\n", i, -(4 + i * 4));
		}
	}

	public void functionEpilogue(String name) {
		fileWriter.format("%s_epilogue:\n", name);
		for (int i = 0; i < 10; i++) {
			fileWriter.format("\tlw $t%d,%d($fp)\n", i, -(4 + i * 4));
		}
		fileWriter.format("\taddu $sp,$fp,8\n");
		fileWriter.format("\tlw $ra,4($fp)\n");
		fileWriter.format("\tlw $fp,0($fp)\n");
		fileWriter.format("\tjr $ra\n");
	}

	public void mainEntryPoint() {
		fileWriter.format(".text\nmain:\n");
		fileWriter.format("\tjal user_main\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
		emitErrorHandlers();
	}

	private void emitErrorHandlers() {
		fileWriter.format("__access_violation:\n");
		fileWriter.format("\tla $a0,string_access_violation\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");

		fileWriter.format("__div_by_zero_error:\n");
		fileWriter.format("\tla $a0,string_illegal_div_by_0\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");

		fileWriter.format("__null_ptr_error:\n");
		fileWriter.format("\tla $a0,string_invalid_ptr_dref\n");
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $v0,10\n");
		fileWriter.format("\tsyscall\n");
	}

	/* ========== Arithmetic (with saturation) ========== */

	private void saturate(String register) {
		int n = saturationCounter++;
		fileWriter.format("\tli $v1,32767\n");
		fileWriter.format("\tbgt %s,$v1,__sat_high_%d\n", register, n);
		fileWriter.format("\tli $v1,-32768\n");
		fileWriter.format("\tblt %s,$v1,__sat_low_%d\n", register, n);
		fileWriter.format("\tj __sat_done_%d\n", n);
		fileWriter.format("__sat_high_%d:\n", n);
		fileWriter.format("\tli %s,32767\n", register);
		fileWriter.format("\tj __sat_done_%d\n", n);
		fileWriter.format("__sat_low_%d:\n", n);
		fileWriter.format("\tli %s,-32768\n", register);
		fileWriter.format("__sat_done_%d:\n", n);
	}

	public void li(Temp t, int value) {
		fileWriter.format("\tli %s,%d\n", reg(t), value);
	}

	public void add(Temp dst, Temp oprnd1, Temp oprnd2) {
		fileWriter.format("\tadd %s,%s,%s\n", reg(dst), reg(oprnd1), reg(oprnd2));
		saturate(reg(dst));
	}

	public void sub(Temp dst, Temp oprnd1, Temp oprnd2) {
		fileWriter.format("\tsub %s,%s,%s\n", reg(dst), reg(oprnd1), reg(oprnd2));
		saturate(reg(dst));
	}

	public void mul(Temp dst, Temp oprnd1, Temp oprnd2) {
		fileWriter.format("\tmul %s,%s,%s\n", reg(dst), reg(oprnd1), reg(oprnd2));
		saturate(reg(dst));
	}

	public void divFloor(Temp dst, Temp oprnd1, Temp oprnd2) {
		int n = saturationCounter++;
		String d = reg(dst), r1 = reg(oprnd1), r2 = reg(oprnd2);
		fileWriter.format("\tbeq %s,$zero,__div_by_zero_error\n", r2);
		fileWriter.format("\tdiv %s,%s\n", r1, r2);
		fileWriter.format("\tmflo %s\n", d);
		fileWriter.format("\tmfhi $v1\n");
		fileWriter.format("\tbeq $v1,$zero,__div_done_%d\n", n);
		fileWriter.format("\txor $v1,%s,%s\n", r1, r2);
		fileWriter.format("\tbgez $v1,__div_done_%d\n", n);
		fileWriter.format("\taddi %s,%s,-1\n", d, d);
		fileWriter.format("__div_done_%d:\n", n);
		saturate(d);
	}

	/* ========== Global Variable Access ========== */

	public void load(Temp dst, String varName) {
		fileWriter.format("\tlw %s,global_%s\n", reg(dst), varName);
	}

	public void store(String varName, Temp src) {
		fileWriter.format("\tsw %s,global_%s\n", reg(src), varName);
	}

	/* ========== Frame Variable Access ========== */

	public void loadFrame(Temp dst, int offset) {
		fileWriter.format("\tlw %s,%d($fp)\n", reg(dst), offset);
	}

	public void storeFrame(int offset, Temp src) {
		fileWriter.format("\tsw %s,%d($fp)\n", reg(src), offset);
	}

	/* ========== Object Field Access ========== */

	public void fieldGet(Temp dst, Temp obj, int fieldOffset) {
		fileWriter.format("\tlw %s,%d(%s)\n", reg(dst), fieldOffset, reg(obj));
	}

	public void fieldSet(Temp obj, int fieldOffset, Temp src) {
		fileWriter.format("\tsw %s,%d(%s)\n", reg(src), fieldOffset, reg(obj));
	}

	/* ========== Array Access (with bounds check) ========== */

	public void arrayGet(Temp dst, Temp arr, Temp idx) {
		String a = reg(arr), i = reg(idx), d = reg(dst);
		fileWriter.format("\tlw $v1,0(%s)\n", a);
		fileWriter.format("\tblt %s,$zero,__access_violation\n", i);
		fileWriter.format("\tbge %s,$v1,__access_violation\n", i);
		fileWriter.format("\tsll $v1,%s,2\n", i);
		fileWriter.format("\tadd $v1,%s,$v1\n", a);
		fileWriter.format("\tlw %s,4($v1)\n", d);
	}

	public void arraySet(Temp arr, Temp idx, Temp src) {
		String a = reg(arr), i = reg(idx), s = reg(src);
		fileWriter.format("\tlw $v1,0(%s)\n", a);
		fileWriter.format("\tblt %s,$zero,__access_violation\n", i);
		fileWriter.format("\tbge %s,$v1,__access_violation\n", i);
		fileWriter.format("\tsll $v1,%s,2\n", i);
		fileWriter.format("\tadd $v1,%s,$v1\n", a);
		fileWriter.format("\tsw %s,4($v1)\n", s);
	}

	/* ========== Function Calls ========== */

	public void callFunc(Temp dst, String funcLabel, List<Temp> args) {
		for (int i = args.size() - 1; i >= 0; i--) {
			fileWriter.format("\tsubu $sp,$sp,4\n");
			fileWriter.format("\tsw %s,0($sp)\n", reg(args.get(i)));
		}
		fileWriter.format("\tjal %s\n", funcLabel);
		if (!args.isEmpty()) {
			fileWriter.format("\taddu $sp,$sp,%d\n", args.size() * 4);
		}
		if (dst != null) {
			fileWriter.format("\tmove %s,$v0\n", reg(dst));
		}
	}

	public void virtualCall(Temp dst, Temp obj, int vtableOffset, List<Temp> args) {
		for (int i = args.size() - 1; i >= 0; i--) {
			fileWriter.format("\tsubu $sp,$sp,4\n");
			fileWriter.format("\tsw %s,0($sp)\n", reg(args.get(i)));
		}
		fileWriter.format("\tsubu $sp,$sp,4\n");
		fileWriter.format("\tsw %s,0($sp)\n", reg(obj));
		fileWriter.format("\tlw $v1,0(%s)\n", reg(obj));
		fileWriter.format("\tlw $v1,%d($v1)\n", vtableOffset);
		fileWriter.format("\tjalr $v1\n");
		fileWriter.format("\taddu $sp,$sp,%d\n", (args.size() + 1) * 4);
		if (dst != null) {
			fileWriter.format("\tmove %s,$v0\n", reg(dst));
		}
	}

	public void moveToReturn(Temp value) {
		fileWriter.format("\tmove $v0,%s\n", reg(value));
	}

	/* ========== Heap Allocation ========== */

	public void mallocDynamic(Temp dst, Temp size) {
		fileWriter.format("\tmove $a0,%s\n", reg(size));
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tmove %s,$v0\n", reg(dst));
	}

	public void mallocConst(Temp dst, int size) {
		fileWriter.format("\tli $a0,%d\n", size);
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tmove %s,$v0\n", reg(dst));
	}

	/* ========== Runtime Checks ========== */

	public void checkNullPtr(Temp ptr) {
		fileWriter.format("\tbeq %s,$zero,__null_ptr_error\n", reg(ptr));
	}

	/* ========== I/O ========== */

	public void printInt(Temp t) {
		fileWriter.format("\tmove $a0,%s\n", reg(t));
		fileWriter.format("\tli $v0,1\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tli $a0,32\n");
		fileWriter.format("\tli $v0,11\n");
		fileWriter.format("\tsyscall\n");
	}

	public void printString(Temp t) {
		fileWriter.format("\tmove $a0,%s\n", reg(t));
		fileWriter.format("\tli $v0,4\n");
		fileWriter.format("\tsyscall\n");
	}

	/* ========== String Operations ========== */

	public void stringConcat(Temp dst, Temp s1, Temp s2) {
		int n = stringOpCounter++;
		String r1 = reg(s1), r2 = reg(s2), rd = reg(dst);
		// Save s1 and s2 on stack (r1/r2/rd may alias the same register)
		fileWriter.format("\tsubu $sp,$sp,8\n");
		fileWriter.format("\tsw %s,0($sp)\n", r1);
		fileWriter.format("\tsw %s,4($sp)\n", r2);
		// Count s1 length
		fileWriter.format("\tmove $a0,%s\n", r1);
		fileWriter.format("\tli $a1,0\n");
		fileWriter.format("__strcat_len1_%d:\n", n);
		fileWriter.format("\tlb $v0,0($a0)\n");
		fileWriter.format("\tbeq $v0,$zero,__strcat_len1d_%d\n", n);
		fileWriter.format("\taddi $a1,$a1,1\n");
		fileWriter.format("\taddi $a0,$a0,1\n");
		fileWriter.format("\tj __strcat_len1_%d\n", n);
		fileWriter.format("__strcat_len1d_%d:\n", n);
		// Count s2 length
		fileWriter.format("\tmove $a0,%s\n", r2);
		fileWriter.format("\tli $a2,0\n");
		fileWriter.format("__strcat_len2_%d:\n", n);
		fileWriter.format("\tlb $v0,0($a0)\n");
		fileWriter.format("\tbeq $v0,$zero,__strcat_len2d_%d\n", n);
		fileWriter.format("\taddi $a2,$a2,1\n");
		fileWriter.format("\taddi $a0,$a0,1\n");
		fileWriter.format("\tj __strcat_len2_%d\n", n);
		fileWriter.format("__strcat_len2d_%d:\n", n);
		// Allocate len1 + len2 + 1
		fileWriter.format("\tadd $a0,$a1,$a2\n");
		fileWriter.format("\taddi $a0,$a0,1\n");
		fileWriter.format("\tli $v0,9\n");
		fileWriter.format("\tsyscall\n");
		fileWriter.format("\tmove %s,$v0\n", rd);
		// Copy s1 (reload original s1 from stack in case rd clobbered r1)
		fileWriter.format("\tlw $a0,0($sp)\n");
		fileWriter.format("\tmove $a3,%s\n", rd);
		fileWriter.format("__strcat_cp1_%d:\n", n);
		fileWriter.format("\tlb $v1,0($a0)\n");
		fileWriter.format("\tbeq $v1,$zero,__strcat_cp1d_%d\n", n);
		fileWriter.format("\tsb $v1,0($a3)\n");
		fileWriter.format("\taddi $a0,$a0,1\n");
		fileWriter.format("\taddi $a3,$a3,1\n");
		fileWriter.format("\tj __strcat_cp1_%d\n", n);
		fileWriter.format("__strcat_cp1d_%d:\n", n);
		// Copy s2 (reload original s2 from stack in case rd clobbered r2)
		fileWriter.format("\tlw $a0,4($sp)\n");
		fileWriter.format("__strcat_cp2_%d:\n", n);
		fileWriter.format("\tlb $v1,0($a0)\n");
		fileWriter.format("\tbeq $v1,$zero,__strcat_cp2d_%d\n", n);
		fileWriter.format("\tsb $v1,0($a3)\n");
		fileWriter.format("\taddi $a0,$a0,1\n");
		fileWriter.format("\taddi $a3,$a3,1\n");
		fileWriter.format("\tj __strcat_cp2_%d\n", n);
		fileWriter.format("__strcat_cp2d_%d:\n", n);
		// Null-terminate and restore stack
		fileWriter.format("\tsb $zero,0($a3)\n");
		fileWriter.format("\taddu $sp,$sp,8\n");
	}

	public void stringEq(Temp dst, Temp s1, Temp s2) {
		int n = stringOpCounter++;
		String r1 = reg(s1), r2 = reg(s2), rd = reg(dst);
		fileWriter.format("\tmove $a0,%s\n", r1);
		fileWriter.format("\tmove $a1,%s\n", r2);
		fileWriter.format("__streq_loop_%d:\n", n);
		fileWriter.format("\tlb $v0,0($a0)\n");
		fileWriter.format("\tlb $v1,0($a1)\n");
		fileWriter.format("\tbne $v0,$v1,__streq_ne_%d\n", n);
		fileWriter.format("\tbeq $v0,$zero,__streq_eq_%d\n", n);
		fileWriter.format("\taddi $a0,$a0,1\n");
		fileWriter.format("\taddi $a1,$a1,1\n");
		fileWriter.format("\tj __streq_loop_%d\n", n);
		fileWriter.format("__streq_eq_%d:\n", n);
		fileWriter.format("\tli %s,1\n", rd);
		fileWriter.format("\tj __streq_done_%d\n", n);
		fileWriter.format("__streq_ne_%d:\n", n);
		fileWriter.format("\tli %s,0\n", rd);
		fileWriter.format("__streq_done_%d:\n", n);
	}

	public void loadAddress(Temp dst, String label) {
		fileWriter.format("\tla %s,%s\n", reg(dst), label);
	}

	/* ========== Control Flow ========== */

	public void label(String inlabel) {
		fileWriter.format("%s:\n", inlabel);
	}

	public void jump(String inlabel) {
		fileWriter.format("\tj %s\n", inlabel);
	}

	public void blt(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tblt %s,%s,%s\n", reg(oprnd1), reg(oprnd2), label);
	}

	public void bge(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tbge %s,%s,%s\n", reg(oprnd1), reg(oprnd2), label);
	}

	public void bne(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tbne %s,%s,%s\n", reg(oprnd1), reg(oprnd2), label);
	}

	public void beq(Temp oprnd1, Temp oprnd2, String label) {
		fileWriter.format("\tbeq %s,%s,%s\n", reg(oprnd1), reg(oprnd2), label);
	}

	public void beqz(Temp oprnd1, String label) {
		fileWriter.format("\tbeq %s,$zero,%s\n", reg(oprnd1), label);
	}

	/* ========== Array Initialization ========== */

	public void zeroInitArray(Temp arr, Temp size) {
		int n = stringOpCounter++;
		String a = reg(arr), s = reg(size);
		fileWriter.format("\tli $v1,0\n");
		fileWriter.format("__zero_arr_%d:\n", n);
		fileWriter.format("\tbge $v1,%s,__zero_arr_done_%d\n", s, n);
		fileWriter.format("\tsll $v0,$v1,2\n");
		fileWriter.format("\tadd $v0,%s,$v0\n", a);
		fileWriter.format("\tsw $zero,4($v0)\n");
		fileWriter.format("\taddi $v1,$v1,1\n");
		fileWriter.format("\tj __zero_arr_%d\n", n);
		fileWriter.format("__zero_arr_done_%d:\n", n);
	}

	public void finalizeFile() {
		fileWriter.close();
	}

	/* ========== Singleton ========== */

	private static MipsGenerator instance = null;

	protected MipsGenerator() {
	}

	public static void init(String outputPath) {
		instance = new MipsGenerator();
		try {
			instance.fileWriter = new PrintWriter(outputPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		instance.fileWriter.print(".data\n");
		instance.fileWriter.print("string_access_violation: .asciiz \"Access Violation\"\n");
		instance.fileWriter.print("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
		instance.fileWriter.print("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");
	}

	public static MipsGenerator getInstance() {
		return instance;
	}

	public static void reset() {
		instance = null;
	}
}
