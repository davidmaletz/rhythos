package mrpg.export;

import java.io.File;

import mrpg.editor.resource.Project;

public interface Target {
	public void publish(Project p, File f);
}
