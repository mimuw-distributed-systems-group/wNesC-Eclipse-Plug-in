package pl.edu.mimuw.nesc.plugin.natures;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import pl.edu.mimuw.nesc.plugin.builder.NescProjectBuilder;

/**
 * A project nature describes project behavior. Project can have more than one
 * nature.
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public class NescProjectNature implements IProjectNature {

	public static final String NATURE_ID = "pl.edu.mimuw.nesc.plugin.natures.NescProjectNature";

	private IProject project;

	@Override
	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		boolean found = false;

		// Do not add the same builder twice.
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(NescProjectBuilder.BUILDER_ID)) {
				found = true;
				break;
			}
		}
		if (!found) {
			// Add builder to project.
			ICommand command = desc.newCommand();
			command.setBuilderName(NescProjectBuilder.BUILDER_ID);
			ICommand[] newCommands = new ICommand[commands.length + 1];

			// Add it before other builders.
			System.arraycopy(commands, 0, newCommands, 1, commands.length);
			newCommands[0] = command;
			desc.setBuildSpec(newCommands);
			project.setDescription(desc, null);
		}
	}

	@Override
	public void deconfigure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();
		int index = -1;

		// Check if the command list contains our builder.
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(NescProjectBuilder.BUILDER_ID)) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			// Nothing to do.
			return;
		}

		// Remove builder.
		ICommand[] newCommands = new ICommand[commands.length - 1];
		int pos = 0;
		for (int i = 0; i < commands.length; ++i) {
			if (i != index) {
				newCommands[pos] = commands[i];
				pos ++;
			}
		}
		desc.setBuildSpec(newCommands);
		project.setDescription(desc, null);
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
