package pl.edu.mimuw.nesc.plugin.projects.util;

import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.nesc.ContextRef;
import pl.edu.mimuw.nesc.FileData;
import pl.edu.mimuw.nesc.ProjectData;

/**
 *
 * @author Grzegorz Kołakowski <gk291583@students.mimuw.edu.pl>
 *
 */
public final class ProjectCache {

	private final ContextRef contextRef;
	private final Map<String, FileData> filesMap;
	private ProjectData projectData;

	public ProjectCache(ContextRef contextRef) {
		this.contextRef = contextRef;
		this.filesMap = new HashMap<>();
	}

	public ContextRef getContextRef() {
		return contextRef;
	}

	public ProjectData getProjectData() {
		return projectData;
	}

	public void setProjectData(ProjectData projectData) {
		this.projectData = projectData;
	}

	public Map<String, FileData> getFilesMap() {
		return filesMap;
	}

}
