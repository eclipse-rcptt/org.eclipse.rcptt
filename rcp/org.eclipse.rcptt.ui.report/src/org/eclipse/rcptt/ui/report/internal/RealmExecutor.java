/*******************************************************************************
 * Copyright (c) 2026 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.ui.report.internal;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;

public class RealmExecutor implements Executor {

	private Realm realm;

	public static RealmExecutor toExecutor(AbstractObservable observable) {
		return new RealmExecutor(observable.getRealm());
	}
	
	public static <T> T syncGetValue(AbstractObservableValue<T> observable) {
		return CompletableFuture.supplyAsync(observable::getValue, toExecutor(observable)).join();
	}
	
	public RealmExecutor(Realm realm) {
		this.realm = requireNonNull(realm);
	}


	@Override
	public void execute(Runnable command) {
		realm.exec(command);
	}

}
