/***********/
/* PACKAGE */
/***********/
package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class Ir {
	private List<IrCommand> commands = new ArrayList<>();

	// Scope tracking for var name disambiguation
	private int scopeCounter = 0;
	private int currentScope = 0;

	// original var name -> scope-qualified name
	private Map<String, String> varNameMap = new HashMap<>();
	// var name -> scope level
	private Map<String, Integer> varScopeLevel = new HashMap<>();

	/******************/
	/* Add Ir command */
	/******************/
	public void AddIrCommand(IrCommand cmd) {
		commands.add(cmd);
	}

	public List<IrCommand> getCommands() {
		return commands;
	}

	public void beginScope() {
		currentScope = ++scopeCounter;
	}

	public void endScope() {
		// Remove vars defined at the current scope level
		varNameMap.entrySet().removeIf(e -> {
			Integer level = varScopeLevel.get(e.getValue());
			return level != null && level == currentScope;
		});
	}

	/**
	 * Register a new var in the current scope and return its qualified name.
	 */
	public String registerVariable(String originalName) {
		String qualifiedName;
		if (currentScope == 0) {
			qualifiedName = originalName;
		} else {
			qualifiedName = originalName + "@" + currentScope;
		}
		varNameMap.put(originalName, qualifiedName);
		varScopeLevel.put(qualifiedName, currentScope);
		return qualifiedName;
	}

	/**
	 * Get the qualified name for a variable reference.
	 * Returns the most recently defined version of this variable.
	 */
	public String getQualifiedName(String originalName) {
		String qualified = varNameMap.get(originalName);
		return qualified != null ? qualified : originalName;
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static Ir instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected Ir() {
	}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static Ir getInstance() {
		if (instance == null) {
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new Ir();
		}
		return instance;
	}

	public static void reset() {
		instance = null;
	}
}
