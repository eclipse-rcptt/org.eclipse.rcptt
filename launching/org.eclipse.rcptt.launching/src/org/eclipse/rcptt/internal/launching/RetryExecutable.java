package org.eclipse.rcptt.internal.launching;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.rcptt.core.Q7Features;
import org.eclipse.rcptt.core.model.IQ7NamedElement;
import org.eclipse.rcptt.launching.AutLaunch;
import org.eclipse.rcptt.launching.IExecutable;
import org.eclipse.rcptt.launching.utils.TestSuiteUtils;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Node;
import org.eclipse.rcptt.sherlock.core.model.sherlock.report.Report;

import com.google.common.base.Preconditions;

public class RetryExecutable extends Executable {
	private final ThrowingFunction<String, Executable> delegateSupplier;
	private Executable delegate;
	private final ArrayList<Executable> children = new ArrayList<>();
	private final int attempts;
	private long startTime = System.currentTimeMillis();

	public interface ThrowingFunction<T, R> {
		R apply(T t) throws CoreException;
	}
	
	public static Executable  wrap(ThrowingFunction<String, Executable> delegate) throws CoreException {
		int attempts = Math.max(1, Q7Features.getInstance().getIntValue(Q7Features.RETRY_TEST));
		if (attempts == 1) {
			return delegate.apply(null);
		}
		if (attempts > 1) {
			return new RetryExecutable(attempts, delegate);
		}
		throw new IllegalStateException(String.format("attempts should be greater than 0, but found %d", attempts));
	}
	
	public RetryExecutable(int attempts, ThrowingFunction<String, Executable> delegate) throws CoreException {
		super(false);
		Preconditions.checkArgument(attempts > 1, "attempts should be greater than 1, but received %s", attempts);
		this.attempts = attempts;
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
	public Report getResultReport() {
		Report result = TestSuiteUtils.generateReport(getActualElement(), getResultStatus());
		Node root = result.getRoot();
		long end = currentTimeMillis();
		root.setEndTime(end);
		root.setStartTime(startTime);
		root.setDuration(end - startTime);
		int i = 0;
		for (IExecutable child: children) {
			Node root2 = child.getResultReport().getRoot();
			root2.setName("attempt " + i++);
			root.getChildren().add(root2);
		}
		return result;
	}

	@Override
	protected final IStatus execute() throws InterruptedException {
		IStatus error = null;
		startTime = currentTimeMillis();
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
		assert error != null; // at least one attempt has been made
		executeChild(delegate);
		return error;
	}

}
