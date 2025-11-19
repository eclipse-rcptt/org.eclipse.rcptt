package org.eclipse.rcptt.internal.launching;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.launching.AutLaunch;

public class RetryExecutable extends Executable {
	private final ThrowingFunction<String, Executable> delegateSupplier;
	private Executable delegate;
	private ArrayList<Executable> children = new ArrayList<>();

	public interface ThrowingFunction<T, R> {
		R apply(T t) throws CoreException;
	}
	public RetryExecutable(ThrowingFunction<String, Executable> delegate) throws CoreException {
		super(false);
		this.delegateSupplier = delegate;
		this.delegate = requireNonNull(delegate.apply("attempt 1"));
		children.add(this.delegate);
	}

	@Override
	public String getName() {
		return "Retry " + this.delegate.getName();
	}

	@Override
	public AutLaunch getAut() {
		return delegate.getAut();
	}

	@Override
	public IQ7NamedElement getActualElement() {
		return delegate.getActualElement();
	}

	@Override
	public int getType() {
		return delegate.getType();
	}

	@Override
	public Executable[] getChildren() {
		return children.toArray(Executable[]::new);
	}

	@Override
	protected final IStatus execute() throws InterruptedException {
		int attempts = Math.max(1, Q7Features.getInstance().getIntValue(Q7Features.RETRY_TEST));
		
		IStatus error = null;
		for (int i = 1; i < attempts; i++) {
			IStatus temp  = executeChild(delegate);
			if (temp.matches(IStatus.CANCEL)) {
				return temp;
			}
			// Remember if error, return on success 
			if (error == null) {
				error = temp;
			}
			if (!temp.matches(IStatus.ERROR)) {
				return error;
			}
			try {
				delegate = requireNonNull(delegateSupplier.apply("attempt " + (i + 1)));
				children.add(delegate);
			} catch (CoreException e) {
				return e.getStatus();
			}
		}
		executeChild(delegate);
		return error;
	}

}
