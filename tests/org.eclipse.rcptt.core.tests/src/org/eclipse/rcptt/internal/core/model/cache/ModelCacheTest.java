/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.internal.core.model.cache;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.rcptt.core.workspace.RcpttCore;
import org.eclipse.rcptt.internal.core.model.Q7TestCase;
import org.junit.Assert;
import org.junit.Test;

public class ModelCacheTest {
	private static final IWorkspace WORKSPACE = ResourcesPlugin.getWorkspace();
	private static final IProject PROJECT = WORKSPACE.getRoot().getProject("TEST");
	private static final IFile TESTCASE_FILE = PROJECT.getFile("testcase.test");

	private static class Data implements Closeable {
		public boolean closed = false;

		public synchronized void assertOpen() {
			Assert.assertFalse(closed);
		}

		@Override
		public synchronized void close() throws IOException {
			closed = true;
		}

		public synchronized void assertClosed() {
			Assert.assertTrue(closed);

		}

	}

	@Test
	public void processLoadedItemEvenIfItIsImmediatelyClosed() throws InterruptedException, IOException {
		ModelCache subject = new ModelCache(0);
		Q7TestCase testcase = (Q7TestCase) RcpttCore.create(TESTCASE_FILE);
		try (Data result = subject.accessInfo(testcase, Data.class, Data::new, i -> {
			i.assertOpen();
			return i;
		})) {
			result.assertClosed(); // the cache size is too small to keep this data open
		}
	}
	
	@Test
	public void closeAfterException() throws InterruptedException, IOException {
		ModelCache subject = new ModelCache(0);
		Data data = new Data();
		Q7TestCase testcase = (Q7TestCase) RcpttCore.create(TESTCASE_FILE);
		try {
			subject.accessInfo(testcase, Data.class, () -> data, i -> {data.assertOpen(); throw new RuntimeException();});
			Assert.fail("RuntimeException is expected");
		} catch (RuntimeException e) {
			// Expected
		}
		data.assertClosed();
	}
	
	@Test
	public void allowNullReturnValue() throws InterruptedException, IOException {
		ModelCache subject = new ModelCache(0);
		Q7TestCase testcase = (Q7TestCase) RcpttCore.create(TESTCASE_FILE);
		Object result = subject.accessInfo(testcase, Data.class, Data::new, i -> {
			i.assertOpen();
			return null;
		}); // should not throw
		Assert.assertNull(result);
	}

}
