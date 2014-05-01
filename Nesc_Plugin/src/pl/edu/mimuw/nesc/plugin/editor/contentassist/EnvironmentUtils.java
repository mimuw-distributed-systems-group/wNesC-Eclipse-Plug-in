package pl.edu.mimuw.nesc.plugin.editor.contentassist;

import java.util.Map;

import pl.edu.mimuw.nesc.ast.Location;
import pl.edu.mimuw.nesc.declaration.object.ObjectDeclaration;
import pl.edu.mimuw.nesc.environment.DefaultEnvironment;
import pl.edu.mimuw.nesc.environment.Environment;
import pl.edu.mimuw.nesc.environment.ScopeType;

/**
 * Utility class that contains common methods for managing environments.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class EnvironmentUtils {

	/**
	 * Flattens given environment.
	 *
	 * @param environment
	 *            hierarchical environment
	 * @return flattened environment
	 */
	public static Environment flattenEnvironment(Environment environment) {
		final Environment flattenedEnvironment = new DefaultEnvironment();
		flattenEnvironment(flattenedEnvironment, environment);
		return flattenedEnvironment;
	}

	/**
	 * Inserts all declarations from <code>currentEnvironment</code> and from
	 * its ancestors into <code>flattenedEnvironment</code>.
	 *
	 * @param flattenedEnvironment
	 *            flattened environment
	 * @param currentEnvironment
	 *            hierarchical environment
	 */
	public static void flattenEnvironment(Environment flattenedEnvironment, Environment currentEnvironment) {
		for (Map.Entry<String, ObjectDeclaration> entry : currentEnvironment.getObjects().getAll()) {
			entry.getValue().setEnvironment(currentEnvironment);
			flattenedEnvironment.getObjects().add(entry.getKey(), entry.getValue());
		}
		// TODO: tags
		/*
		 * Add declarations from parent scope (they will not override
		 * declarations that are already added).
		 */
		if (currentEnvironment.getParent().isPresent()) {
			flattenEnvironment(flattenedEnvironment, currentEnvironment.getParent().get());
		}
	}

	/**
	 * Returns the most nested environment in given location.
	 *
	 * @param environment
	 *            current environment
	 * @param location
	 *            current location
	 * @return the most nested environment in given location
	 */
	public static Environment getEnvironment(Environment environment, Location location) {
		for (Environment nested : environment.getEnclosedEnvironments()) {
			if (nested.getScopeType().equals(ScopeType.OTHER)) {
				continue;
			}
			if (isInRange(location, nested.getStartLocation().get(), nested.getEndLocation().get())) {
				return getEnvironment(nested, location);
			}
		}
		/* This is the most nested environment */
		return environment;
	}

	/**
	 * Checks if given line and column fit between given start and end location.
	 *
	 * @param currentLocation
	 *            current location
	 * @param startLocation
	 *            start location
	 * @param endLocation
	 *            end location
	 * @return <code>true</code> if given line and column fit between given
	 *         start and end location
	 */
	public static boolean isInRange(Location currentLocation, Location startLocation, Location endLocation) {
		final int line = currentLocation.getLine();
		final int column = currentLocation.getColumn();
		if (line < startLocation.getLine() || line > endLocation.getLine()) {
			return false;
		}
		if (line == startLocation.getLine() && column < startLocation.getColumn()) {
			return false;
		}
		if (line == endLocation.getLine() && column > endLocation.getColumn()) {
			return false;
		}
		return true;
	}
}
