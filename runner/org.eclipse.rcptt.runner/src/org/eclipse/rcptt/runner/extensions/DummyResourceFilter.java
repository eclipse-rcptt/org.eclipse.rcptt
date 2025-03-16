package org.eclipse.rcptt.runner.extensions;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.filtermatchers.AbstractFileInfoMatcher;
import org.eclipse.core.text.StringMatcher;

// Replaces org.eclipse.ui.internal.ide.misc.FileInfoAttributesMatcher in non-UI context
public class DummyResourceFilter extends AbstractFileInfoMatcher {
	StringMatcher matcher = null;

	@Override
	public void initialize(IProject project, Object arguments) {
		if ((arguments instanceof String) && ((String) arguments).length() > 0)
			matcher = new StringMatcher((String) arguments, true, false);
	}

	@Override
	public boolean matches(IContainer parent, IFileInfo fileInfo) {
		if (matcher != null)
			return matcher.match(fileInfo.getName());
		return false;
	}
}
