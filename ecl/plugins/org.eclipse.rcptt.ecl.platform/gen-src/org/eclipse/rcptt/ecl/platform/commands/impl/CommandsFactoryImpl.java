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
package org.eclipse.rcptt.ecl.platform.commands.impl;


import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.rcptt.ecl.platform.commands.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class CommandsFactoryImpl extends EFactoryImpl implements CommandsFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static CommandsFactory init() {
		try {
			CommandsFactory theCommandsFactory = (CommandsFactory)EPackage.Registry.INSTANCE.getEFactory(CommandsPackage.eNS_URI);
			if (theCommandsFactory != null) {
				return theCommandsFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new CommandsFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CommandsFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case CommandsPackage.LIST_PLUGINS: return createListPlugins();
			case CommandsPackage.LIST_FEATURES: return createListFeatures();
			case CommandsPackage.LIST_REPOSITORIES: return createListRepositories();
			case CommandsPackage.ADD_REPOSITORY: return createAddRepository();
			case CommandsPackage.REMOVE_REPOSITORY: return createRemoveRepository();
			case CommandsPackage.UPDATE_FEATURE: return createUpdateFeature();
			case CommandsPackage.UPDATE_ALL: return createUpdateAll();
			case CommandsPackage.SORT_BY: return createSortBy();
			case CommandsPackage.LIST_INSTALL_UNITS: return createListInstallUnits();
			case CommandsPackage.GET_LOG: return createGetLog();
			case CommandsPackage.LOG: return createLog();
			case CommandsPackage.ECHO: return createEcho();
			case CommandsPackage.CLEAR_LOG: return createClearLog();
			case CommandsPackage.LIST_LAUNCH_CONFIGURATIONS: return createListLaunchConfigurations();
			case CommandsPackage.LAUNCH: return createLaunch();
			case CommandsPackage.SUBSTITUTE_VARIABLES: return createSubstituteVariables();
			case CommandsPackage.GET_WORKSPACE_LOCATION: return createGetWorkspaceLocation();
			case CommandsPackage.FIND_IN_WORKSPACE: return createFindInWorkspace();
			case CommandsPackage.GET_STATUS_MESSAGE: return createGetStatusMessage();
			case CommandsPackage.GET_STATUS_TRACE: return createGetStatusTrace();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ListPlugins createListPlugins() {
		ListPluginsImpl listPlugins = new ListPluginsImpl();
		return listPlugins;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ListFeatures createListFeatures() {
		ListFeaturesImpl listFeatures = new ListFeaturesImpl();
		return listFeatures;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ListRepositories createListRepositories() {
		ListRepositoriesImpl listRepositories = new ListRepositoriesImpl();
		return listRepositories;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AddRepository createAddRepository() {
		AddRepositoryImpl addRepository = new AddRepositoryImpl();
		return addRepository;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RemoveRepository createRemoveRepository() {
		RemoveRepositoryImpl removeRepository = new RemoveRepositoryImpl();
		return removeRepository;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public UpdateFeature createUpdateFeature() {
		UpdateFeatureImpl updateFeature = new UpdateFeatureImpl();
		return updateFeature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public UpdateAll createUpdateAll() {
		UpdateAllImpl updateAll = new UpdateAllImpl();
		return updateAll;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SortBy createSortBy() {
		SortByImpl sortBy = new SortByImpl();
		return sortBy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ListInstallUnits createListInstallUnits() {
		ListInstallUnitsImpl listInstallUnits = new ListInstallUnitsImpl();
		return listInstallUnits;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetLog createGetLog() {
		GetLogImpl getLog = new GetLogImpl();
		return getLog;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Log createLog() {
		LogImpl log = new LogImpl();
		return log;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Echo createEcho() {
		EchoImpl echo = new EchoImpl();
		return echo;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ClearLog createClearLog() {
		ClearLogImpl clearLog = new ClearLogImpl();
		return clearLog;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ListLaunchConfigurations createListLaunchConfigurations() {
		ListLaunchConfigurationsImpl listLaunchConfigurations = new ListLaunchConfigurationsImpl();
		return listLaunchConfigurations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Launch createLaunch() {
		LaunchImpl launch = new LaunchImpl();
		return launch;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SubstituteVariables createSubstituteVariables() {
		SubstituteVariablesImpl substituteVariables = new SubstituteVariablesImpl();
		return substituteVariables;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetWorkspaceLocation createGetWorkspaceLocation() {
		GetWorkspaceLocationImpl getWorkspaceLocation = new GetWorkspaceLocationImpl();
		return getWorkspaceLocation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FindInWorkspace createFindInWorkspace() {
		FindInWorkspaceImpl findInWorkspace = new FindInWorkspaceImpl();
		return findInWorkspace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetStatusMessage createGetStatusMessage() {
		GetStatusMessageImpl getStatusMessage = new GetStatusMessageImpl();
		return getStatusMessage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GetStatusTrace createGetStatusTrace() {
		GetStatusTraceImpl getStatusTrace = new GetStatusTraceImpl();
		return getStatusTrace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CommandsPackage getCommandsPackage() {
		return (CommandsPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static CommandsPackage getPackage() {
		return CommandsPackage.eINSTANCE;
	}

} //CommandsFactoryImpl
