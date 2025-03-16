package org.eclipse.rcptt.ecl.client.tcp.tests;


import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.runtime.ICommandService;
import org.eclipse.rcptt.ecl.runtime.IProcess;

public class EclInjectedCommandService implements ICommandService {
	public static BiFunction<Command, IProcess, IStatus> delegate = new BiFunction<Command, IProcess, IStatus>() {
		@Override
		public IStatus apply(Command ignored, IProcess ignored2) {
			return Status.OK_STATUS;
		}
	};

	public EclInjectedCommandService() {
	}

	@Override
	public IStatus service(Command command, IProcess context) throws InterruptedException, CoreException {
		return delegate.apply(command, context);
	}
	
	
	public static void inject(Function<Command, IStatus> injection) {
		delegate = (command, process) -> injection.apply(command);
	}
	
	public static void inject(BiFunction<Command, IProcess, IStatus> injection) {
		delegate = injection;
	}

}
