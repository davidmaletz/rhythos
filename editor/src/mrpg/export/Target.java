package mrpg.export;

import mrpg.editor.resource.Project;

public interface Target {
	public void build(Project p);
	public void run(Project p);
}
