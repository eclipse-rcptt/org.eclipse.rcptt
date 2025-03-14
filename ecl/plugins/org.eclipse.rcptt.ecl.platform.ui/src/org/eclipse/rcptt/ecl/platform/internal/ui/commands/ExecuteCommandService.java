package org.eclipse.rcptt.ecl.platform.internal.ui.commands;

import static org.eclipse.rcptt.ecl.platform.internal.ui.commands.Utils.error;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

public class ExecuteCommandService implements ICommandService {

	public ExecuteCommandService() {
	}

	@Override
	public IStatus service(Command command, IProcess context) throws InterruptedException, CoreException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IHandlerService service = workbench.getService(IHandlerService.class);
		if (service == null) {
			return Utils.error("Handler service is unavailable");
		}
		ExecuteCommand typed = (ExecuteCommand) command;
		String id = typed.getCommandId();
		CompletableFuture<IStatus> result = new CompletableFuture<>();
		workbench.getDisplay().syncExec(() -> {
			try {
				// If the command shows a dialog and does not return, continue execution
				workbench.getDisplay().asyncExec(() -> result.complete(Status.OK_STATUS));
				service.executeCommand(id, null);
				result.complete(Status.OK_STATUS);
			} catch( NotDefinedException | NotEnabledException | NotHandledException e) {
				result.complete(error("Command " + id + " is unavailable", e));
			} catch (Throwable e) {
				result.complete(error("Command " + id + " has failed", e));
			}
		});
		try {
			return result.get(1, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			return error("UI thread has timed out");
		} catch (java.util.concurrent.ExecutionException e) {
			return error("Unexpected completion result", e);
		}
	}
	

}
