/*******************************************************************************
 * Copyright (c) 2009, 2019 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.launching;

/**
 * Interface for listening to AUTs/launches life cycle events.
 * 
 * @author Yuri Strot
 * @see AutManager
 */
public interface AutListener {

	/**
	 * New AUT was registered
	 * 
	 * @param aut
	 */
	void autAdded(Aut aut);

	/**
	 * AUT was removed
	 * 
	 * @param aut
	 */
	void autRemoved(Aut aut);

	void autChanged(Aut aut);

	/**
	 * AUT was launched
	 * 
	 * @param launch
	 */
	void launchAdded(AutLaunch launch);

	/**
	 * AUT launch was removed
	 * 
	 * @param launch
	 */
	void launchRemoved(AutLaunch launch);
	
	public class AutAdapter implements AutListener {

		@Override
		public void autAdded(Aut aut) {
		}

		@Override
		public void autRemoved(Aut aut) {
		}

		@Override
		public void autChanged(Aut aut) {
		}

		@Override
		public void launchAdded(AutLaunch launch) {
		}

		@Override
		public void launchRemoved(AutLaunch launch) {
		}
		
	}

}
