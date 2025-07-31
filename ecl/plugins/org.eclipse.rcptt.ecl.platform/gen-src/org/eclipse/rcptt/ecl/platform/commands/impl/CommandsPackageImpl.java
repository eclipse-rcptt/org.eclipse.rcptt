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




import org.eclipse.emf.common.util.URI;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.rcptt.ecl.core.CorePackage;
import org.eclipse.rcptt.ecl.platform.commands.AddRepository;
import org.eclipse.rcptt.ecl.platform.commands.ClearLog;
import org.eclipse.rcptt.ecl.platform.commands.CommandsFactory;
import org.eclipse.rcptt.ecl.platform.commands.CommandsPackage;
import org.eclipse.rcptt.ecl.platform.commands.Echo;
import org.eclipse.rcptt.ecl.platform.commands.FindInWorkspace;
import org.eclipse.rcptt.ecl.platform.commands.GetLog;
import org.eclipse.rcptt.ecl.platform.commands.GetStatusMessage;
import org.eclipse.rcptt.ecl.platform.commands.GetStatusTrace;
import org.eclipse.rcptt.ecl.platform.commands.GetWorkspaceLocation;
import org.eclipse.rcptt.ecl.platform.commands.Launch;
import org.eclipse.rcptt.ecl.platform.commands.ListFeatures;
import org.eclipse.rcptt.ecl.platform.commands.ListInstallUnits;
import org.eclipse.rcptt.ecl.platform.commands.ListLaunchConfigurations;
import org.eclipse.rcptt.ecl.platform.commands.ListPlugins;
import org.eclipse.rcptt.ecl.platform.commands.ListRepositories;
import org.eclipse.rcptt.ecl.platform.commands.Log;
import org.eclipse.rcptt.ecl.platform.commands.RemoveRepository;
import org.eclipse.rcptt.ecl.platform.commands.SortBy;
import org.eclipse.rcptt.ecl.platform.commands.SubstituteVariables;
import org.eclipse.rcptt.ecl.platform.commands.UpdateAll;
import org.eclipse.rcptt.ecl.platform.commands.UpdateFeature;
import org.eclipse.rcptt.ecl.platform.objects.ObjectsPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class CommandsPackageImpl extends EPackageImpl implements CommandsPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass listPluginsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass listFeaturesEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass listRepositoriesEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass addRepositoryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass removeRepositoryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass updateFeatureEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass updateAllEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sortByEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass listInstallUnitsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getLogEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass logEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass echoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass clearLogEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass listLaunchConfigurationsEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass launchEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass substituteVariablesEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getWorkspaceLocationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass findInWorkspaceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getStatusMessageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getStatusTraceEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.rcptt.ecl.platform.commands.CommandsPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private CommandsPackageImpl() {
		super(eNS_URI, CommandsFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link CommandsPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static CommandsPackage init() {
		if (isInited) return (CommandsPackage)EPackage.Registry.INSTANCE.getEPackage(CommandsPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredCommandsPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		CommandsPackageImpl theCommandsPackage = registeredCommandsPackage instanceof CommandsPackageImpl ? (CommandsPackageImpl)registeredCommandsPackage : new CommandsPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		CorePackage.eINSTANCE.eClass();
		EcorePackage.eINSTANCE.eClass();
		ObjectsPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theCommandsPackage.createPackageContents();

		// Initialize created meta-data
		theCommandsPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCommandsPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(CommandsPackage.eNS_URI, theCommandsPackage);
		return theCommandsPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getListPlugins() {
		return listPluginsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getListPlugins_IncludeDependencies() {
		return (EAttribute)listPluginsEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getListFeatures() {
		return listFeaturesEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getListRepositories() {
		return listRepositoriesEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAddRepository() {
		return addRepositoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAddRepository_Uri() {
		return (EAttribute)addRepositoryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAddRepository_Name() {
		return (EAttribute)addRepositoryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRemoveRepository() {
		return removeRepositoryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRemoveRepository_Uri() {
		return (EAttribute)removeRepositoryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getUpdateFeature() {
		return updateFeatureEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getUpdateFeature_Id() {
		return (EAttribute)updateFeatureEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getUpdateAll() {
		return updateAllEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSortBy() {
		return sortByEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSortBy_Field() {
		return (EAttribute)sortByEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getListInstallUnits() {
		return listInstallUnitsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetLog() {
		return getLogEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetLog_Levels() {
		return (EAttribute)getLogEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetLog_Limit() {
		return (EAttribute)getLogEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getLog() {
		return logEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLog_Message() {
		return (EAttribute)logEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLog_Severity() {
		return (EAttribute)logEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLog_Plugin() {
		return (EAttribute)logEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEcho() {
		return echoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEcho_Str() {
		return (EAttribute)echoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClearLog() {
		return clearLogEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getListLaunchConfigurations() {
		return listLaunchConfigurationsEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getLaunch() {
		return launchEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLaunch_Mode() {
		return (EAttribute)launchEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLaunch_Name() {
		return (EAttribute)launchEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSubstituteVariables() {
		return substituteVariablesEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSubstituteVariables_Expression() {
		return (EAttribute)substituteVariablesEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSubstituteVariables_IgnoreUndefined() {
		return (EAttribute)substituteVariablesEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetWorkspaceLocation() {
		return getWorkspaceLocationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFindInWorkspace() {
		return findInWorkspaceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFindInWorkspace_Path() {
		return (EAttribute)findInWorkspaceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFindInWorkspace_All() {
		return (EAttribute)findInWorkspaceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetStatusMessage() {
		return getStatusMessageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGetStatusMessage_Status() {
		return (EReference)getStatusMessageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetStatusTrace() {
		return getStatusTraceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGetStatusTrace_Status() {
		return (EReference)getStatusTraceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CommandsFactory getCommandsFactory() {
		return (CommandsFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		listPluginsEClass = createEClass(LIST_PLUGINS);
		createEAttribute(listPluginsEClass, LIST_PLUGINS__INCLUDE_DEPENDENCIES);

		listFeaturesEClass = createEClass(LIST_FEATURES);

		listRepositoriesEClass = createEClass(LIST_REPOSITORIES);

		addRepositoryEClass = createEClass(ADD_REPOSITORY);
		createEAttribute(addRepositoryEClass, ADD_REPOSITORY__URI);
		createEAttribute(addRepositoryEClass, ADD_REPOSITORY__NAME);

		removeRepositoryEClass = createEClass(REMOVE_REPOSITORY);
		createEAttribute(removeRepositoryEClass, REMOVE_REPOSITORY__URI);

		updateFeatureEClass = createEClass(UPDATE_FEATURE);
		createEAttribute(updateFeatureEClass, UPDATE_FEATURE__ID);

		updateAllEClass = createEClass(UPDATE_ALL);

		sortByEClass = createEClass(SORT_BY);
		createEAttribute(sortByEClass, SORT_BY__FIELD);

		listInstallUnitsEClass = createEClass(LIST_INSTALL_UNITS);

		getLogEClass = createEClass(GET_LOG);
		createEAttribute(getLogEClass, GET_LOG__LEVELS);
		createEAttribute(getLogEClass, GET_LOG__LIMIT);

		logEClass = createEClass(LOG);
		createEAttribute(logEClass, LOG__MESSAGE);
		createEAttribute(logEClass, LOG__SEVERITY);
		createEAttribute(logEClass, LOG__PLUGIN);

		echoEClass = createEClass(ECHO);
		createEAttribute(echoEClass, ECHO__STR);

		clearLogEClass = createEClass(CLEAR_LOG);

		listLaunchConfigurationsEClass = createEClass(LIST_LAUNCH_CONFIGURATIONS);

		launchEClass = createEClass(LAUNCH);
		createEAttribute(launchEClass, LAUNCH__MODE);
		createEAttribute(launchEClass, LAUNCH__NAME);

		substituteVariablesEClass = createEClass(SUBSTITUTE_VARIABLES);
		createEAttribute(substituteVariablesEClass, SUBSTITUTE_VARIABLES__EXPRESSION);
		createEAttribute(substituteVariablesEClass, SUBSTITUTE_VARIABLES__IGNORE_UNDEFINED);

		getWorkspaceLocationEClass = createEClass(GET_WORKSPACE_LOCATION);

		findInWorkspaceEClass = createEClass(FIND_IN_WORKSPACE);
		createEAttribute(findInWorkspaceEClass, FIND_IN_WORKSPACE__PATH);
		createEAttribute(findInWorkspaceEClass, FIND_IN_WORKSPACE__ALL);

		getStatusMessageEClass = createEClass(GET_STATUS_MESSAGE);
		createEReference(getStatusMessageEClass, GET_STATUS_MESSAGE__STATUS);

		getStatusTraceEClass = createEClass(GET_STATUS_TRACE);
		createEReference(getStatusTraceEClass, GET_STATUS_TRACE__STATUS);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		CorePackage theCorePackage = (CorePackage)EPackage.Registry.INSTANCE.getEPackage(CorePackage.eNS_URI);
		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		listPluginsEClass.getESuperTypes().add(theCorePackage.getCommand());
		listFeaturesEClass.getESuperTypes().add(theCorePackage.getCommand());
		listRepositoriesEClass.getESuperTypes().add(theCorePackage.getCommand());
		addRepositoryEClass.getESuperTypes().add(theCorePackage.getCommand());
		removeRepositoryEClass.getESuperTypes().add(theCorePackage.getCommand());
		updateFeatureEClass.getESuperTypes().add(theCorePackage.getCommand());
		updateAllEClass.getESuperTypes().add(theCorePackage.getCommand());
		sortByEClass.getESuperTypes().add(theCorePackage.getCommand());
		listInstallUnitsEClass.getESuperTypes().add(theCorePackage.getCommand());
		getLogEClass.getESuperTypes().add(theCorePackage.getCommand());
		logEClass.getESuperTypes().add(theCorePackage.getCommand());
		echoEClass.getESuperTypes().add(theCorePackage.getCommand());
		clearLogEClass.getESuperTypes().add(theCorePackage.getCommand());
		listLaunchConfigurationsEClass.getESuperTypes().add(theCorePackage.getCommand());
		launchEClass.getESuperTypes().add(theCorePackage.getCommand());
		substituteVariablesEClass.getESuperTypes().add(theCorePackage.getCommand());
		getWorkspaceLocationEClass.getESuperTypes().add(theCorePackage.getCommand());
		findInWorkspaceEClass.getESuperTypes().add(theCorePackage.getCommand());
		getStatusMessageEClass.getESuperTypes().add(theCorePackage.getCommand());
		getStatusTraceEClass.getESuperTypes().add(theCorePackage.getCommand());

		// Initialize classes and features; add operations and parameters
		initEClass(listPluginsEClass, ListPlugins.class, "ListPlugins", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getListPlugins_IncludeDependencies(), theEcorePackage.getEBoolean(), "includeDependencies", "false", 0, 1, ListPlugins.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(listFeaturesEClass, ListFeatures.class, "ListFeatures", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(listRepositoriesEClass, ListRepositories.class, "ListRepositories", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(addRepositoryEClass, AddRepository.class, "AddRepository", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAddRepository_Uri(), theEcorePackage.getEString(), "uri", null, 0, 1, AddRepository.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAddRepository_Name(), theEcorePackage.getEString(), "name", null, 0, 1, AddRepository.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(removeRepositoryEClass, RemoveRepository.class, "RemoveRepository", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRemoveRepository_Uri(), theEcorePackage.getEString(), "uri", null, 0, 1, RemoveRepository.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(updateFeatureEClass, UpdateFeature.class, "UpdateFeature", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getUpdateFeature_Id(), theEcorePackage.getEString(), "id", null, 0, 1, UpdateFeature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(updateAllEClass, UpdateAll.class, "UpdateAll", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(sortByEClass, SortBy.class, "SortBy", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSortBy_Field(), theEcorePackage.getEString(), "field", null, 0, 1, SortBy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(listInstallUnitsEClass, ListInstallUnits.class, "ListInstallUnits", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(getLogEClass, GetLog.class, "GetLog", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetLog_Levels(), theEcorePackage.getEString(), "levels", "false", 0, -1, GetLog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGetLog_Limit(), theEcorePackage.getEInt(), "limit", "100", 0, 1, GetLog.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(logEClass, Log.class, "Log", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getLog_Message(), theEcorePackage.getEString(), "message", null, 0, 1, Log.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getLog_Severity(), theEcorePackage.getEString(), "severity", "info", 0, 1, Log.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getLog_Plugin(), theEcorePackage.getEString(), "plugin", "org.eclipse.rcptt.ecl.platform", 0, 1, Log.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(echoEClass, Echo.class, "Echo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEcho_Str(), theEcorePackage.getEString(), "str", "", 0, 1, Echo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(clearLogEClass, ClearLog.class, "ClearLog", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(listLaunchConfigurationsEClass, ListLaunchConfigurations.class, "ListLaunchConfigurations", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(launchEClass, Launch.class, "Launch", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getLaunch_Mode(), theEcorePackage.getEString(), "mode", null, 1, 1, Launch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getLaunch_Name(), theEcorePackage.getEString(), "name", null, 1, 1, Launch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(substituteVariablesEClass, SubstituteVariables.class, "SubstituteVariables", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSubstituteVariables_Expression(), theEcorePackage.getEString(), "expression", null, 0, 1, SubstituteVariables.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSubstituteVariables_IgnoreUndefined(), theEcorePackage.getEBoolean(), "ignoreUndefined", "false", 0, 1, SubstituteVariables.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getWorkspaceLocationEClass, GetWorkspaceLocation.class, "GetWorkspaceLocation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(findInWorkspaceEClass, FindInWorkspace.class, "FindInWorkspace", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFindInWorkspace_Path(), theEcorePackage.getEString(), "path", "", 1, 1, FindInWorkspace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFindInWorkspace_All(), theEcorePackage.getEBoolean(), "all", "false", 0, 1, FindInWorkspace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getStatusMessageEClass, GetStatusMessage.class, "GetStatusMessage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGetStatusMessage_Status(), theCorePackage.getProcessStatus(), null, "status", null, 1, 1, GetStatusMessage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getStatusTraceEClass, GetStatusTrace.class, "GetStatusTrace", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGetStatusTrace_Status(), theCorePackage.getProcessStatus(), null, "status", null, 1, 1, GetStatusTrace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/ecl/docs
		createDocsAnnotations();
		// http://www.eclipse.org/ecl/input
		createInputAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/docs</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createDocsAnnotations() {
		String source = "http://www.eclipse.org/ecl/docs";
		addAnnotation
		  (listPluginsEClass,
		   source,
		   new String[] {
			   "description", "Returns list of all plugins.",
			   "returns", "Lists all available plugins. ",
			   "example", "list-plugins | foreach [val item]  {\n\n\tif [$item | get name | matches \"Q7 ECL.*\"]{\n    \t\t$item | get name | log\n\t}\n}"
		   });
		addAnnotation
		  (getListPlugins_IncludeDependencies(),
		   source,
		   new String[] {
			   "description", "When true, returned plugins includes information about imported packages and bundle dependencies."
		   });
		addAnnotation
		  (listFeaturesEClass,
		   source,
		   new String[] {
			   "description", "Returns list of all features.",
			   "returns", "Lists all available features. ",
			   "example", "list-features | foreach [val item]  {\n\tif [$item | get id | matches \"com.xored.q7.*\"]{\n    \t\t$item | get name | log\n\t}\n}"
		   });
		addAnnotation
		  (listRepositoriesEClass,
		   source,
		   new String[] {
			   "description", "Returns list of p2 repositories.",
			   "returns", "List of p2 repositories",
			   "example", "list-repositories | foreach [val item] {\n\tif [$item | get name | equals \"download cache\"]{\n\t\t$item | get isArtifact | equals true | verify-true\n\t}\n}\n"
		   });
		addAnnotation
		  (addRepositoryEClass,
		   source,
		   new String[] {
			   "description", "Adds p2 repository.",
			   "returns", "nothing"
		   });
		addAnnotation
		  (removeRepositoryEClass,
		   source,
		   new String[] {
			   "description", "Removes p2 repository.",
			   "returns", "nothing"
		   });
		addAnnotation
		  (updateFeatureEClass,
		   source,
		   new String[] {
			   "description", "Updates feature with defined name.",
			   "returns", "nothing"
		   });
		addAnnotation
		  (sortByEClass,
		   source,
		   new String[] {
			   "description", "Sorts list of objects from input stream by defined field. ",
			   "returns", "sorted list of objects",
			   "example", "list-working-sets | sort-by -field name | foreach {get name | log}"
		   });
		addAnnotation
		  (listInstallUnitsEClass,
		   source,
		   new String[] {
			   "description", "Returns list of all install units.",
			   "returns", "Lists all install units.",
			   "example", "list-install-units | write-lines -uri \"workspace:/Project/Folder/file.txt\"\n"
		   });
		addAnnotation
		  (getLogEClass,
		   source,
		   new String[] {
			   "description", "Returns log entries sorted by timestamp descending.",
			   "returns", "Log entries",
			   "example", "get-log -levels error | as-table-data | write-csv-file \"workspace:/Project/file2.csv\""
		   });
		addAnnotation
		  (getGetLog_Levels(),
		   source,
		   new String[] {
			   "description", "Log entry severities to show. Any of <code>error</code>, <code>warning</code>, <code>info</code>, <code>OK</code>, <code>Cancel</code> in any letter case."
		   });
		addAnnotation
		  (getGetLog_Limit(),
		   source,
		   new String[] {
			   "description", "Number of log entries to get. Default value is 100."
		   });
		addAnnotation
		  (logEClass,
		   source,
		   new String[] {
			   "description", "Writes an entry into Eclipse log",
			   "returns", "Nothing",
			   "example", "log -message \"Error\" -severity error -plugin \"com.xored.q7\"\necho \"Warning\" | log -severity warning"
		   });
		addAnnotation
		  (getLog_Message(),
		   source,
		   new String[] {
			   "description", "Writes an entry into Eclipse log"
		   });
		addAnnotation
		  (getLog_Severity(),
		   source,
		   new String[] {
			   "description", "Log entry severity. Can be <code>info</code>, <code>warning</code>, <code>error</code>, <code>ok</code>, <code>cancel</code> in any letter case. Default value is <code>info</code>."
		   });
		addAnnotation
		  (getLog_Plugin(),
		   source,
		   new String[] {
			   "description", "ID of plugin adding log entry. Default value is <code>org.eclipse.rcptt.ecl.platform</code>"
		   });
		addAnnotation
		  (echoEClass,
		   source,
		   new String[] {
			   "description", "Takes a string argument and writes it into an output pipe. ",
			   "returns", "value of <code>str</code> argument",
			   "example", "echo \"MyStr\" | log"
		   });
		addAnnotation
		  (clearLogEClass,
		   source,
		   new String[] {
			   "description", "Removes log file. Note that if log view is open, it may enter into inconsistent state. If it is important, use <code>clear-log-view</code> instead",
			   "returns", "Nothing"
		   });
		addAnnotation
		  (listLaunchConfigurationsEClass,
		   source,
		   new String[] {
			   "description", "Returns list of launch configurations.",
			   "returns", "List of launch configurations.",
			   "example", "list-launch-configurations | write-lines -uri \"workspace:/Project/Folder/file.txt\""
		   });
		addAnnotation
		  (launchEClass,
		   source,
		   new String[] {
			   "descriprion", "Launches a configuration in specified mode. ",
			   "returns", "Nothing."
		   });
		addAnnotation
		  (substituteVariablesEClass,
		   source,
		   new String[] {
			   "description", "<a href=\'http://https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.jdt.doc.user%2Freference%2Fpreferences%2Frun-debug%2Fref-string_substitution.htm\'>Eclipse string substitution.</a> Recursively resolves and replaces all variable references in the given expression with their corresponding values. Allows the client to control whether references to undefined variables are reported as an error (i.e. an exception is thrown). Unavailable if AUT does not have org.eclipse.core.variables plug-in.",
			   "returns", "expression with variable references replaced with variable values",
			   "example", "//writes prop val to AUT workspace log\nlog [substitute-variables \"${system_property:propertyName}\"] "
		   });
		addAnnotation
		  (getSubstituteVariables_Expression(),
		   source,
		   new String[] {
			   "description", "expression referencing variables"
		   });
		addAnnotation
		  (getSubstituteVariables_IgnoreUndefined(),
		   source,
		   new String[] {
			   "description", "whether a reference to an undefined variable is to be considered an error (i.e. throw an exception)"
		   });
		addAnnotation
		  (getWorkspaceLocationEClass,
		   source,
		   new String[] {
			   "descriprion", "Returns the path to workspace root.",
			   "returns", "path to workspace root",
			   "example", "get-workspace-location | equals \"/Users/My_MacAir/aut-Q7-1.3.12-B2\" | verify-true"
		   });
		addAnnotation
		  (findInWorkspaceEClass,
		   source,
		   new String[] {
			   "description", "Perform regex search per segment. Returns list of matched paths.",
			   "returns", "list of matched paths",
			   "example", "find-in-workspace \"project/dir1.*/dir2/file.*.txt\"",
			   "recorded", "false"
		   });
		addAnnotation
		  (getStatusMessageEClass,
		   source,
		   new String[] {
			   "description", "Takes ProcessStatus from input and returns first root cause.",
			   "returns", "String with first root cause.",
			   "example", "try {\n\tthrow-error \"Error ocurred\"\n} -error [val e] -catch {\n\t$e | get-status-trace | log\n}"
		   });
		addAnnotation
		  (getStatusTraceEClass,
		   source,
		   new String[] {
			   "description", "Takes ProcessStatus from input and returns trace.",
			   "returns", "String with status trace.",
			   "example", "try {\n\tthrow-error \"Error ocurred\"\n} -error [val e] -catch {\n\t$e | get-status-trace | log\n}"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/input</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createInputAnnotations() {
		String source = "http://www.eclipse.org/ecl/input";
		addAnnotation
		  (getLog_Message(),
		   source,
		   new String[] {
		   },
		   new URI[] {
			 URI.createURI(CorePackage.eNS_URI).appendFragment("//Foreach/input/%http:%2F%2Fwww.eclipse.org%2Fecl%2Finput%")
		   });
		addAnnotation
		  (getEcho_Str(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getGetStatusMessage_Status(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getGetStatusTrace_Status(),
		   source,
		   new String[] {
		   });
	}

} //CommandsPackageImpl
