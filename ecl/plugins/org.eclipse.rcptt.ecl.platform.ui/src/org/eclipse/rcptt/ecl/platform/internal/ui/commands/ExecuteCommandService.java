package org.eclipse.rcptt.ecl.platform.internal.ui.commands;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.platform.ui.commands.ExecuteCommand;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

public class ExecuteCommandService implements ICommandService {

	public ExecuteCommandService() {
	}

	@Override
	public IStatus service(Command command, IProcess context) throws InterruptedException, CoreException {
		IHandlerService service = PlatformUI.getWorkbench().getService(IHandlerService.class);
		if (service == null) {
			return Status.error("Handler service is unavailable");
		}
		ExecuteCommand typed = (ExecuteCommand) command;
		String id = typed.getCommandId();
		try {
			service.executeCommand(id, null);
		} catch (ExecutionException e) {
			return Status.error("Command " + id + " has failed", e);
		} catch(NotDefinedException | NotEnabledException | NotHandledException e) {
			return Status.error("Command " + id + " is unavailable", e);
		}
		return Status.OK_STATUS;
	}

}
