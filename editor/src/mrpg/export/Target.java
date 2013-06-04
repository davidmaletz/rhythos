package mrpg.export;

import mrpg.editor.resource.Project;

public interface Target {
	public void build(Project p) throws Exception;
	public void run(Project p) throws Exception;
}
