package org.eclipse.rcptt.internal.launching;

import java.util.Objects;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.launching.AutLaunch;

public class RetryExecutable extends Executable {
	private final Executable delegate;
	private int attempt = 1;

	public RetryExecutable(Executable delegate) {
		super(delegate.isDebug());
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	public String getName() {
		return this.delegate.getName() + " attempt " + attempt;
	}

	@Override
	public AutLaunch getAut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQ7NamedElement getActualElement() {
		return delegate.getActualElement();
	}

	@Override
	public int getType() {
		return TYPE_SCENARIO;
	}

	@Override
	public Executable[] getChildren() {
		return new Executable[] {delegate};
	}

	@Override
	protected final IStatus execute() throws InterruptedException {
		int attempts = Math.max(1, Q7Features.getInstance().getIntValue(Q7Features.RETRY_TEST));
		MultiStatus status = new MultiStatus(getClass(), 0, "Execution result for " + getName());
		boolean first = true;
		for (int i = 0; i < attempts; i++) {
			this.attempt = i + 1;
			IStatus temp  = delegate.execute();
			if (temp.matches(IStatus.CANCEL)) {
				return temp;
			}
			status.add(temp);
			if (!temp.matches(IStatus.ERROR)) {
				if (first) {
					return temp;
				} else {
					return status;
				}
			}
			first = false;
		}
		return status;
	}

}
