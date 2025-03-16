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
package org.eclipse.rcptt.ecl.core.impl;

import java.util.Map;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.rcptt.ecl.core.Binding;
import org.eclipse.rcptt.ecl.core.Block;
import org.eclipse.rcptt.ecl.core.BoxedValue;
import org.eclipse.rcptt.ecl.core.Case;
import org.eclipse.rcptt.ecl.core.Command;
import org.eclipse.rcptt.ecl.core.ConvertedToEMFPipe;
import org.eclipse.rcptt.ecl.core.CoreFactory;
import org.eclipse.rcptt.ecl.core.CorePackage;
import org.eclipse.rcptt.ecl.core.Declaration;
import org.eclipse.rcptt.ecl.core.EclBoolean;
import org.eclipse.rcptt.ecl.core.EclByte;
import org.eclipse.rcptt.ecl.core.EclChar;
import org.eclipse.rcptt.ecl.core.EclDouble;
import org.eclipse.rcptt.ecl.core.EclException;
import org.eclipse.rcptt.ecl.core.EclFloat;
import org.eclipse.rcptt.ecl.core.EclInteger;
import org.eclipse.rcptt.ecl.core.EclList;
import org.eclipse.rcptt.ecl.core.EclLong;
import org.eclipse.rcptt.ecl.core.EclMap;
import org.eclipse.rcptt.ecl.core.EclMapEntry;
import org.eclipse.rcptt.ecl.core.EclShort;
import org.eclipse.rcptt.ecl.core.EclStackTraceEntry;
import org.eclipse.rcptt.ecl.core.EclString;
import org.eclipse.rcptt.ecl.core.Exec;
import org.eclipse.rcptt.ecl.core.ExecutableParameter;
import org.eclipse.rcptt.ecl.core.Foreach;
import org.eclipse.rcptt.ecl.core.Get;
import org.eclipse.rcptt.ecl.core.GetVal;
import org.eclipse.rcptt.ecl.core.Global;
import org.eclipse.rcptt.ecl.core.If;
import org.eclipse.rcptt.ecl.core.Let;
import org.eclipse.rcptt.ecl.core.Listen;
import org.eclipse.rcptt.ecl.core.LiteralParameter;
import org.eclipse.rcptt.ecl.core.Nullable;
import org.eclipse.rcptt.ecl.core.Parallel;
import org.eclipse.rcptt.ecl.core.Parameter;
import org.eclipse.rcptt.ecl.core.Pipeline;
import org.eclipse.rcptt.ecl.core.Proc;
import org.eclipse.rcptt.ecl.core.ProcInstance;
import org.eclipse.rcptt.ecl.core.ProcessStatus;
import org.eclipse.rcptt.ecl.core.RestoreState;
import org.eclipse.rcptt.ecl.core.SaveState;
import org.eclipse.rcptt.ecl.core.Script;
import org.eclipse.rcptt.ecl.core.Sequence;
import org.eclipse.rcptt.ecl.core.Serialized;
import org.eclipse.rcptt.ecl.core.SessionState;
import org.eclipse.rcptt.ecl.core.Switch;
import org.eclipse.rcptt.ecl.core.Val;
import org.eclipse.rcptt.ecl.core.With;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!--
 * end-user-doc -->
 * @generated
 */
public class CorePackageImpl extends EPackageImpl implements CorePackage {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass commandEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bindingEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass blockEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass pipelineEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sequenceEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass parallelEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass withEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass execEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass parameterEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass executableParameterEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass literalParameterEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass foreachEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass processStatusEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass convertedToEMFPipeEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass serializedEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass nullableEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ifEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclStringEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclExceptionEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclStackTraceEntryEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass listenEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass declarationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass valEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getValEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass letEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass procEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass procInstanceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass globalEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass sessionStateEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass saveStateEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass restoreStateEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass getEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclListEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclMapEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclMapEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass caseEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass switchEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclBooleanEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclCharEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclIntegerEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclFloatEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclLongEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclDoubleEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass boxedValueEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclByteEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eclShortEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType throwableEDataType = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass scriptEClass = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the
	 * package package URI value.
	 * <p>
	 * Note: the correct way to create the package is via the static factory
	 * method {@link #init init()}, which also performs initialization of the
	 * package, or returns the registered package, if one already exists. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.rcptt.ecl.core.CorePackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private CorePackageImpl() {
		super(eNS_URI, CoreFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model,
	 * and for any others upon which it depends.
	 * 
	 * <p>
	 * This method is used to initialize {@link CorePackage#eINSTANCE} when that
	 * field is accessed. Clients should not invoke it directly. Instead, they
	 * should simply access that field to obtain the package. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static CorePackage init() {
		if (isInited) return (CorePackage)EPackage.Registry.INSTANCE.getEPackage(CorePackage.eNS_URI);

		// Obtain or create and register package
		Object registeredCorePackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		CorePackageImpl theCorePackage = registeredCorePackage instanceof CorePackageImpl ? (CorePackageImpl)registeredCorePackage : new CorePackageImpl();

		isInited = true;

		// Initialize simple dependencies
		EcorePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theCorePackage.createPackageContents();

		// Initialize created meta-data
		theCorePackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theCorePackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(CorePackage.eNS_URI, theCorePackage);
		return theCorePackage;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCommand() {
		return commandEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getCommand_Host() {
		return (EAttribute)commandEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCommand_Bindings() {
		return (EReference)commandEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBinding() {
		return bindingEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBinding_Feature() {
		return (EReference)bindingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBinding_Command() {
		return (EReference)bindingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBlock() {
		return blockEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBlock_Commands() {
		return (EReference)blockEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPipeline() {
		return pipelineEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSequence() {
		return sequenceEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getParallel() {
		return parallelEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getWith() {
		return withEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getWith_Object() {
		return (EReference)withEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getWith_Do() {
		return (EReference)withEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExec() {
		return execEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExec_Namespace() {
		return (EAttribute)execEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getExec_Name() {
		return (EAttribute)execEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getExec_Parameters() {
		return (EReference)execEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getParameter() {
		return parameterEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getParameter_Name() {
		return (EAttribute)parameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getExecutableParameter() {
		return executableParameterEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getExecutableParameter_Command() {
		return (EReference)executableParameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getLiteralParameter() {
		return literalParameterEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLiteralParameter_Literal() {
		return (EAttribute)literalParameterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getLiteralParameter_Format() {
		return (EAttribute)literalParameterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getForeach() {
		return foreachEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getForeach_Item() {
		return (EReference)foreachEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getForeach_Index() {
		return (EReference)foreachEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getForeach_Do() {
		return (EReference)foreachEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getForeach_Input() {
		return (EReference)foreachEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getProcessStatus() {
		return processStatusEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getProcessStatus_PluginId() {
		return (EAttribute)processStatusEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getProcessStatus_Code() {
		return (EAttribute)processStatusEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getProcessStatus_Message() {
		return (EAttribute)processStatusEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getProcessStatus_Severity() {
		return (EAttribute)processStatusEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getProcessStatus_Exception() {
		return (EReference)processStatusEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getProcessStatus_Children() {
		return (EReference)processStatusEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getConvertedToEMFPipe() {
		return convertedToEMFPipeEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getConvertedToEMFPipe_Object() {
		return (EReference)convertedToEMFPipeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSerialized() {
		return serializedEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSerialized_Bytes() {
		return (EAttribute)serializedEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getNullable() {
		return nullableEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getNullable_Value() {
		return (EReference)nullableEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getNullable_Type() {
		return (EAttribute)nullableEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIf() {
		return ifEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIf_Condition() {
		return (EReference)ifEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIf_Then() {
		return (EReference)ifEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getIf_Else() {
		return (EReference)ifEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclString() {
		return eclStringEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclString_Value() {
		return (EAttribute)eclStringEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclException() {
		return eclExceptionEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclException_ClassName() {
		return (EAttribute)eclExceptionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclException_Message() {
		return (EAttribute)eclExceptionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEclException_StackTrace() {
		return (EReference)eclExceptionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclException_Throwable() {
		return (EAttribute)eclExceptionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEclException_Cause() {
		return (EReference)eclExceptionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEclException_Status() {
		return (EReference)eclExceptionEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclStackTraceEntry() {
		return eclStackTraceEntryEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclStackTraceEntry_Index() {
		return (EAttribute)eclStackTraceEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclStackTraceEntry_DeclaringClass() {
		return (EAttribute)eclStackTraceEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclStackTraceEntry_MethodName() {
		return (EAttribute)eclStackTraceEntryEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclStackTraceEntry_FileName() {
		return (EAttribute)eclStackTraceEntryEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclStackTraceEntry_LineNumber() {
		return (EAttribute)eclStackTraceEntryEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclStackTraceEntry_NativeMethod() {
		return (EAttribute)eclStackTraceEntryEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getListen() {
		return listenEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getListen_Source() {
		return (EAttribute)listenEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getListen_While() {
		return (EReference)listenEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDeclaration() {
		return declarationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getVal() {
		return valEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVal_Name() {
		return (EAttribute)valEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getVal_Input() {
		return (EAttribute)valEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getVal_Value() {
		return (EReference)valEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGetVal() {
		return getValEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGetVal_Name() {
		return (EAttribute)getValEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getLet() {
		return letEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getLet_Vals() {
		return (EReference)letEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getLet_Body() {
		return (EReference)letEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getProc() {
		return procEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getProc_Name() {
		return (EAttribute)procEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getProc_Vals() {
		return (EReference)procEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getProc_Body() {
		return (EReference)procEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getProcInstance() {
		return procInstanceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getProcInstance_Definition() {
		return (EReference)procInstanceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGlobal() {
		return globalEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGlobal_Vals() {
		return (EReference)globalEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGlobal_Override() {
		return (EAttribute)globalEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSessionState() {
		return sessionStateEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSessionState_Procs() {
		return (EReference)sessionStateEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSessionState_Decls() {
		return (EReference)sessionStateEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSaveState() {
		return saveStateEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRestoreState() {
		return restoreStateEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRestoreState_State() {
		return (EReference)restoreStateEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGet() {
		return getEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGet_Input() {
		return (EReference)getEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGet_Key() {
		return (EReference)getEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGet_Default() {
		return (EReference)getEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclList() {
		return eclListEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEclList_Elements() {
		return (EReference)eclListEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclMap() {
		return eclMapEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEclMap_Entries() {
		return (EReference)eclMapEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclMapEntry() {
		return eclMapEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEclMapEntry_Key() {
		return (EReference)eclMapEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEclMapEntry_Value() {
		return (EReference)eclMapEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCase() {
		return caseEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCase_Condition() {
		return (EReference)caseEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCase_Then() {
		return (EReference)caseEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSwitch() {
		return switchEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSwitch_Items() {
		return (EReference)switchEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSwitch_Default() {
		return (EReference)switchEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSwitch_Input() {
		return (EReference)switchEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclBoolean() {
		return eclBooleanEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclBoolean_Value() {
		return (EAttribute)eclBooleanEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclChar() {
		return eclCharEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclChar_Value() {
		return (EAttribute)eclCharEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclInteger() {
		return eclIntegerEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclInteger_Value() {
		return (EAttribute)eclIntegerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclFloat() {
		return eclFloatEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclFloat_Value() {
		return (EAttribute)eclFloatEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclLong() {
		return eclLongEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclLong_Value() {
		return (EAttribute)eclLongEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclDouble() {
		return eclDoubleEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclDouble_Value() {
		return (EAttribute)eclDoubleEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBoxedValue() {
		return boxedValueEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclByte() {
		return eclByteEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclByte_Value() {
		return (EAttribute)eclByteEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEclShort() {
		return eclShortEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEclShort_Value() {
		return (EAttribute)eclShortEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getThrowable() {
		return throwableEDataType;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getScript() {
		return scriptEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getScript_Content() {
		return (EAttribute)scriptEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CoreFactory getCoreFactory() {
		return (CoreFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
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
		commandEClass = createEClass(COMMAND);
		createEAttribute(commandEClass, COMMAND__HOST);
		createEReference(commandEClass, COMMAND__BINDINGS);

		bindingEClass = createEClass(BINDING);
		createEReference(bindingEClass, BINDING__FEATURE);
		createEReference(bindingEClass, BINDING__COMMAND);

		blockEClass = createEClass(BLOCK);
		createEReference(blockEClass, BLOCK__COMMANDS);

		pipelineEClass = createEClass(PIPELINE);

		sequenceEClass = createEClass(SEQUENCE);

		parallelEClass = createEClass(PARALLEL);

		withEClass = createEClass(WITH);
		createEReference(withEClass, WITH__OBJECT);
		createEReference(withEClass, WITH__DO);

		execEClass = createEClass(EXEC);
		createEAttribute(execEClass, EXEC__NAMESPACE);
		createEAttribute(execEClass, EXEC__NAME);
		createEReference(execEClass, EXEC__PARAMETERS);

		parameterEClass = createEClass(PARAMETER);
		createEAttribute(parameterEClass, PARAMETER__NAME);

		executableParameterEClass = createEClass(EXECUTABLE_PARAMETER);
		createEReference(executableParameterEClass, EXECUTABLE_PARAMETER__COMMAND);

		literalParameterEClass = createEClass(LITERAL_PARAMETER);
		createEAttribute(literalParameterEClass, LITERAL_PARAMETER__LITERAL);
		createEAttribute(literalParameterEClass, LITERAL_PARAMETER__FORMAT);

		foreachEClass = createEClass(FOREACH);
		createEReference(foreachEClass, FOREACH__ITEM);
		createEReference(foreachEClass, FOREACH__INDEX);
		createEReference(foreachEClass, FOREACH__DO);
		createEReference(foreachEClass, FOREACH__INPUT);

		scriptEClass = createEClass(SCRIPT);
		createEAttribute(scriptEClass, SCRIPT__CONTENT);

		processStatusEClass = createEClass(PROCESS_STATUS);
		createEAttribute(processStatusEClass, PROCESS_STATUS__PLUGIN_ID);
		createEAttribute(processStatusEClass, PROCESS_STATUS__CODE);
		createEAttribute(processStatusEClass, PROCESS_STATUS__MESSAGE);
		createEAttribute(processStatusEClass, PROCESS_STATUS__SEVERITY);
		createEReference(processStatusEClass, PROCESS_STATUS__EXCEPTION);
		createEReference(processStatusEClass, PROCESS_STATUS__CHILDREN);

		convertedToEMFPipeEClass = createEClass(CONVERTED_TO_EMF_PIPE);
		createEReference(convertedToEMFPipeEClass, CONVERTED_TO_EMF_PIPE__OBJECT);

		serializedEClass = createEClass(SERIALIZED);
		createEAttribute(serializedEClass, SERIALIZED__BYTES);

		nullableEClass = createEClass(NULLABLE);
		createEReference(nullableEClass, NULLABLE__VALUE);
		createEAttribute(nullableEClass, NULLABLE__TYPE);

		ifEClass = createEClass(IF);
		createEReference(ifEClass, IF__CONDITION);
		createEReference(ifEClass, IF__THEN);
		createEReference(ifEClass, IF__ELSE);

		boxedValueEClass = createEClass(BOXED_VALUE);

		eclByteEClass = createEClass(ECL_BYTE);
		createEAttribute(eclByteEClass, ECL_BYTE__VALUE);

		eclShortEClass = createEClass(ECL_SHORT);
		createEAttribute(eclShortEClass, ECL_SHORT__VALUE);

		eclIntegerEClass = createEClass(ECL_INTEGER);
		createEAttribute(eclIntegerEClass, ECL_INTEGER__VALUE);

		eclLongEClass = createEClass(ECL_LONG);
		createEAttribute(eclLongEClass, ECL_LONG__VALUE);

		eclDoubleEClass = createEClass(ECL_DOUBLE);
		createEAttribute(eclDoubleEClass, ECL_DOUBLE__VALUE);

		eclFloatEClass = createEClass(ECL_FLOAT);
		createEAttribute(eclFloatEClass, ECL_FLOAT__VALUE);

		eclBooleanEClass = createEClass(ECL_BOOLEAN);
		createEAttribute(eclBooleanEClass, ECL_BOOLEAN__VALUE);

		eclCharEClass = createEClass(ECL_CHAR);
		createEAttribute(eclCharEClass, ECL_CHAR__VALUE);

		eclStringEClass = createEClass(ECL_STRING);
		createEAttribute(eclStringEClass, ECL_STRING__VALUE);

		eclExceptionEClass = createEClass(ECL_EXCEPTION);
		createEAttribute(eclExceptionEClass, ECL_EXCEPTION__CLASS_NAME);
		createEAttribute(eclExceptionEClass, ECL_EXCEPTION__MESSAGE);
		createEReference(eclExceptionEClass, ECL_EXCEPTION__STACK_TRACE);
		createEAttribute(eclExceptionEClass, ECL_EXCEPTION__THROWABLE);
		createEReference(eclExceptionEClass, ECL_EXCEPTION__CAUSE);
		createEReference(eclExceptionEClass, ECL_EXCEPTION__STATUS);

		eclStackTraceEntryEClass = createEClass(ECL_STACK_TRACE_ENTRY);
		createEAttribute(eclStackTraceEntryEClass, ECL_STACK_TRACE_ENTRY__INDEX);
		createEAttribute(eclStackTraceEntryEClass, ECL_STACK_TRACE_ENTRY__DECLARING_CLASS);
		createEAttribute(eclStackTraceEntryEClass, ECL_STACK_TRACE_ENTRY__METHOD_NAME);
		createEAttribute(eclStackTraceEntryEClass, ECL_STACK_TRACE_ENTRY__FILE_NAME);
		createEAttribute(eclStackTraceEntryEClass, ECL_STACK_TRACE_ENTRY__LINE_NUMBER);
		createEAttribute(eclStackTraceEntryEClass, ECL_STACK_TRACE_ENTRY__NATIVE_METHOD);

		listenEClass = createEClass(LISTEN);
		createEAttribute(listenEClass, LISTEN__SOURCE);
		createEReference(listenEClass, LISTEN__WHILE);

		declarationEClass = createEClass(DECLARATION);

		valEClass = createEClass(VAL);
		createEAttribute(valEClass, VAL__NAME);
		createEReference(valEClass, VAL__VALUE);
		createEAttribute(valEClass, VAL__INPUT);

		getValEClass = createEClass(GET_VAL);
		createEAttribute(getValEClass, GET_VAL__NAME);

		letEClass = createEClass(LET);
		createEReference(letEClass, LET__VALS);
		createEReference(letEClass, LET__BODY);

		procEClass = createEClass(PROC);
		createEAttribute(procEClass, PROC__NAME);
		createEReference(procEClass, PROC__VALS);
		createEReference(procEClass, PROC__BODY);

		procInstanceEClass = createEClass(PROC_INSTANCE);
		createEReference(procInstanceEClass, PROC_INSTANCE__DEFINITION);

		globalEClass = createEClass(GLOBAL);
		createEReference(globalEClass, GLOBAL__VALS);
		createEAttribute(globalEClass, GLOBAL__OVERRIDE);

		sessionStateEClass = createEClass(SESSION_STATE);
		createEReference(sessionStateEClass, SESSION_STATE__PROCS);
		createEReference(sessionStateEClass, SESSION_STATE__DECLS);

		saveStateEClass = createEClass(SAVE_STATE);

		restoreStateEClass = createEClass(RESTORE_STATE);
		createEReference(restoreStateEClass, RESTORE_STATE__STATE);

		getEClass = createEClass(GET);
		createEReference(getEClass, GET__INPUT);
		createEReference(getEClass, GET__KEY);
		createEReference(getEClass, GET__DEFAULT);

		eclListEClass = createEClass(ECL_LIST);
		createEReference(eclListEClass, ECL_LIST__ELEMENTS);

		eclMapEClass = createEClass(ECL_MAP);
		createEReference(eclMapEClass, ECL_MAP__ENTRIES);

		eclMapEntryEClass = createEClass(ECL_MAP_ENTRY);
		createEReference(eclMapEntryEClass, ECL_MAP_ENTRY__KEY);
		createEReference(eclMapEntryEClass, ECL_MAP_ENTRY__VALUE);

		caseEClass = createEClass(CASE);
		createEReference(caseEClass, CASE__CONDITION);
		createEReference(caseEClass, CASE__THEN);

		switchEClass = createEClass(SWITCH);
		createEReference(switchEClass, SWITCH__INPUT);
		createEReference(switchEClass, SWITCH__ITEMS);
		createEReference(switchEClass, SWITCH__DEFAULT);

		// Create data types
		throwableEDataType = createEDataType(THROWABLE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model. This
	 * method is guarded to have no affect on any invocation but its first. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
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
		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		blockEClass.getESuperTypes().add(this.getCommand());
		pipelineEClass.getESuperTypes().add(this.getBlock());
		sequenceEClass.getESuperTypes().add(this.getBlock());
		parallelEClass.getESuperTypes().add(this.getBlock());
		withEClass.getESuperTypes().add(this.getCommand());
		execEClass.getESuperTypes().add(this.getCommand());
		executableParameterEClass.getESuperTypes().add(this.getParameter());
		literalParameterEClass.getESuperTypes().add(this.getParameter());
		foreachEClass.getESuperTypes().add(this.getCommand());
		scriptEClass.getESuperTypes().add(this.getCommand());
		ifEClass.getESuperTypes().add(this.getCommand());
		eclByteEClass.getESuperTypes().add(this.getBoxedValue());
		eclShortEClass.getESuperTypes().add(this.getBoxedValue());
		eclIntegerEClass.getESuperTypes().add(this.getBoxedValue());
		eclLongEClass.getESuperTypes().add(this.getBoxedValue());
		eclDoubleEClass.getESuperTypes().add(this.getBoxedValue());
		eclFloatEClass.getESuperTypes().add(this.getBoxedValue());
		eclBooleanEClass.getESuperTypes().add(this.getBoxedValue());
		eclCharEClass.getESuperTypes().add(this.getBoxedValue());
		eclStringEClass.getESuperTypes().add(this.getBoxedValue());
		listenEClass.getESuperTypes().add(this.getCommand());
		declarationEClass.getESuperTypes().add(this.getCommand());
		valEClass.getESuperTypes().add(this.getDeclaration());
		getValEClass.getESuperTypes().add(this.getCommand());
		letEClass.getESuperTypes().add(this.getCommand());
		procEClass.getESuperTypes().add(this.getCommand());
		procInstanceEClass.getESuperTypes().add(this.getCommand());
		globalEClass.getESuperTypes().add(this.getCommand());
		saveStateEClass.getESuperTypes().add(this.getCommand());
		restoreStateEClass.getESuperTypes().add(this.getCommand());
		getEClass.getESuperTypes().add(this.getCommand());
		caseEClass.getESuperTypes().add(this.getCommand());
		switchEClass.getESuperTypes().add(this.getCommand());

		// Initialize classes and features; add operations and parameters
		initEClass(commandEClass, Command.class, "Command", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getCommand_Host(), ecorePackage.getEString(), "host", null, 0, 1, Command.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCommand_Bindings(), this.getBinding(), null, "bindings", null, 0, -1, Command.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(bindingEClass, Binding.class, "Binding", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBinding_Feature(), theEcorePackage.getEStructuralFeature(), null, "feature", null, 0, 1, Binding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBinding_Command(), this.getCommand(), null, "command", null, 0, 1, Binding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(blockEClass, Block.class, "Block", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getBlock_Commands(), this.getCommand(), null, "commands", null, 1, -1, Block.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(pipelineEClass, Pipeline.class, "Pipeline", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(sequenceEClass, Sequence.class, "Sequence", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(parallelEClass, Parallel.class, "Parallel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(withEClass, With.class, "With", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getWith_Object(), theEcorePackage.getEObject(), null, "object", null, 1, 1, With.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getWith_Do(), this.getCommand(), null, "do", null, 1, 1, With.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(execEClass, Exec.class, "Exec", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getExec_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, Exec.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getExec_Name(), ecorePackage.getEString(), "name", null, 0, 1, Exec.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getExec_Parameters(), this.getParameter(), null, "parameters", null, 0, -1, Exec.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(parameterEClass, Parameter.class, "Parameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getParameter_Name(), ecorePackage.getEString(), "name", null, 0, 1, Parameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(executableParameterEClass, ExecutableParameter.class, "ExecutableParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getExecutableParameter_Command(), this.getCommand(), null, "command", null, 1, 1, ExecutableParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(literalParameterEClass, LiteralParameter.class, "LiteralParameter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getLiteralParameter_Literal(), ecorePackage.getEString(), "literal", null, 0, 1, LiteralParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getLiteralParameter_Format(), ecorePackage.getEString(), "format", null, 0, 1, LiteralParameter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(foreachEClass, Foreach.class, "Foreach", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getForeach_Item(), this.getVal(), null, "item", null, 0, 1, Foreach.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getForeach_Index(), this.getVal(), null, "index", null, 0, 1, Foreach.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getForeach_Do(), this.getCommand(), null, "do", null, 0, 1, Foreach.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getForeach_Input(), ecorePackage.getEObject(), null, "input", null, 0, -1, Foreach.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(scriptEClass, Script.class, "Script", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getScript_Content(), theEcorePackage.getEString(), "content", null, 0, 1, Script.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(processStatusEClass, ProcessStatus.class, "ProcessStatus", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getProcessStatus_PluginId(), ecorePackage.getEString(), "pluginId", null, 0, 1, ProcessStatus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getProcessStatus_Code(), ecorePackage.getEInt(), "code", null, 0, 1, ProcessStatus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getProcessStatus_Message(), ecorePackage.getEString(), "message", null, 0, 1, ProcessStatus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getProcessStatus_Severity(), ecorePackage.getEInt(), "severity", null, 0, 1, ProcessStatus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getProcessStatus_Exception(), this.getEclException(), null, "exception", null, 0, 1, ProcessStatus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getProcessStatus_Children(), this.getProcessStatus(), null, "children", null, 0, -1, ProcessStatus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(convertedToEMFPipeEClass, ConvertedToEMFPipe.class, "ConvertedToEMFPipe", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getConvertedToEMFPipe_Object(), theEcorePackage.getEObject(), null, "object", null, 0, 1, ConvertedToEMFPipe.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(serializedEClass, Serialized.class, "Serialized", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSerialized_Bytes(), ecorePackage.getEByteArray(), "bytes", null, 0, 1, Serialized.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(nullableEClass, Nullable.class, "Nullable", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getNullable_Value(), theEcorePackage.getEObject(), null, "value", null, 0, 1, Nullable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getNullable_Type(), theEcorePackage.getEString(), "type", null, 0, 1, Nullable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(ifEClass, If.class, "If", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getIf_Condition(), theEcorePackage.getEObject(), null, "condition", null, 0, 1, If.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIf_Then(), this.getCommand(), null, "then", null, 0, 1, If.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getIf_Else(), this.getCommand(), null, "else", null, 0, 1, If.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(boxedValueEClass, BoxedValue.class, "BoxedValue", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(eclByteEClass, EclByte.class, "EclByte", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclByte_Value(), theEcorePackage.getEByte(), "value", null, 0, 1, EclByte.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclShortEClass, EclShort.class, "EclShort", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclShort_Value(), theEcorePackage.getEShort(), "value", null, 0, 1, EclShort.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclIntegerEClass, EclInteger.class, "EclInteger", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclInteger_Value(), theEcorePackage.getEInt(), "value", null, 0, 1, EclInteger.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclLongEClass, EclLong.class, "EclLong", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclLong_Value(), theEcorePackage.getELong(), "value", null, 0, 1, EclLong.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclDoubleEClass, EclDouble.class, "EclDouble", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclDouble_Value(), theEcorePackage.getEDouble(), "value", null, 0, 1, EclDouble.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclFloatEClass, EclFloat.class, "EclFloat", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclFloat_Value(), theEcorePackage.getEFloat(), "value", null, 0, 1, EclFloat.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclBooleanEClass, EclBoolean.class, "EclBoolean", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclBoolean_Value(), theEcorePackage.getEBoolean(), "value", null, 0, 1, EclBoolean.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclCharEClass, EclChar.class, "EclChar", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclChar_Value(), theEcorePackage.getEChar(), "value", null, 0, 1, EclChar.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclStringEClass, EclString.class, "EclString", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclString_Value(), theEcorePackage.getEString(), "value", null, 0, 1, EclString.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclExceptionEClass, EclException.class, "EclException", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclException_ClassName(), theEcorePackage.getEString(), "className", null, 0, 1, EclException.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEclException_Message(), theEcorePackage.getEString(), "message", null, 0, 1, EclException.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEclException_StackTrace(), this.getEclStackTraceEntry(), null, "stackTrace", null, 0, -1, EclException.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEclException_Throwable(), this.getThrowable(), "throwable", null, 0, 1, EclException.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEclException_Cause(), this.getEclException(), null, "cause", null, 0, 1, EclException.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEclException_Status(), this.getProcessStatus(), null, "status", null, 0, 1, EclException.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclStackTraceEntryEClass, EclStackTraceEntry.class, "EclStackTraceEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEclStackTraceEntry_Index(), theEcorePackage.getEInt(), "index", null, 0, 1, EclStackTraceEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEclStackTraceEntry_DeclaringClass(), theEcorePackage.getEString(), "declaringClass", null, 0, 1, EclStackTraceEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEclStackTraceEntry_MethodName(), theEcorePackage.getEString(), "methodName", null, 0, 1, EclStackTraceEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEclStackTraceEntry_FileName(), theEcorePackage.getEString(), "fileName", null, 0, 1, EclStackTraceEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEclStackTraceEntry_LineNumber(), ecorePackage.getEInt(), "lineNumber", null, 0, 1, EclStackTraceEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEclStackTraceEntry_NativeMethod(), theEcorePackage.getEBoolean(), "nativeMethod", null, 0, 1, EclStackTraceEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(listenEClass, Listen.class, "Listen", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getListen_Source(), theEcorePackage.getEString(), "source", null, 1, 1, Listen.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getListen_While(), this.getCommand(), null, "while", null, 1, 1, Listen.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(declarationEClass, Declaration.class, "Declaration", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(valEClass, Val.class, "Val", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getVal_Name(), theEcorePackage.getEString(), "name", null, 0, 1, Val.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getVal_Value(), theEcorePackage.getEObject(), null, "value", null, 0, 1, Val.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getVal_Input(), theEcorePackage.getEBoolean(), "input", "false", 0, 1, Val.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getValEClass, GetVal.class, "GetVal", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGetVal_Name(), theEcorePackage.getEString(), "name", null, 0, 1, GetVal.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(letEClass, Let.class, "Let", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getLet_Vals(), this.getDeclaration(), null, "vals", null, 0, -1, Let.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getLet_Body(), this.getCommand(), null, "body", null, 0, 1, Let.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(procEClass, Proc.class, "Proc", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getProc_Name(), theEcorePackage.getEString(), "name", null, 0, 1, Proc.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getProc_Vals(), this.getDeclaration(), null, "vals", null, 0, -1, Proc.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getProc_Body(), this.getCommand(), null, "body", null, 0, 1, Proc.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(procInstanceEClass, ProcInstance.class, "ProcInstance", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getProcInstance_Definition(), this.getProc(), null, "definition", null, 0, 1, ProcInstance.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(globalEClass, Global.class, "Global", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGlobal_Vals(), this.getDeclaration(), null, "vals", null, 0, -1, Global.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGlobal_Override(), theEcorePackage.getEBoolean(), "override", "false", 0, 1, Global.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sessionStateEClass, SessionState.class, "SessionState", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSessionState_Procs(), this.getProc(), null, "procs", null, 0, -1, SessionState.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSessionState_Decls(), this.getDeclaration(), null, "decls", null, 0, -1, SessionState.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(saveStateEClass, SaveState.class, "SaveState", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(restoreStateEClass, RestoreState.class, "RestoreState", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRestoreState_State(), this.getSessionState(), null, "state", null, 0, 1, RestoreState.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(getEClass, Get.class, "Get", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGet_Input(), theEcorePackage.getEObject(), null, "input", null, 0, 1, Get.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGet_Key(), theEcorePackage.getEObject(), null, "key", null, 0, 1, Get.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGet_Default(), theEcorePackage.getEObject(), null, "default", null, 0, 1, Get.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclListEClass, EclList.class, "EclList", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getEclList_Elements(), theEcorePackage.getEObject(), null, "elements", null, 0, -1, EclList.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclMapEClass, EclMap.class, "EclMap", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getEclMap_Entries(), this.getEclMapEntry(), null, "entries", null, 0, -1, EclMap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(eclMapEntryEClass, EclMapEntry.class, "EclMapEntry", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getEclMapEntry_Key(), theEcorePackage.getEObject(), null, "key", null, 0, 1, EclMapEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEclMapEntry_Value(), theEcorePackage.getEObject(), null, "value", null, 0, 1, EclMapEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(caseEClass, Case.class, "Case", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCase_Condition(), theEcorePackage.getEObject(), null, "condition", null, 1, 1, Case.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getCase_Then(), this.getCommand(), null, "then", null, 0, 1, Case.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(switchEClass, Switch.class, "Switch", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSwitch_Input(), ecorePackage.getEObject(), null, "input", null, 1, 1, Switch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSwitch_Items(), this.getCase(), null, "items", null, 1, -1, Switch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSwitch_Default(), this.getCommand(), null, "default", null, 0, 1, Switch.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize data types
		initEDataType(throwableEDataType, Throwable.class, "Throwable", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// http://www.eclipse.org/ecl/internal
		createInternalAnnotations();
		// http://www.eclipse.org/ecl/docs
		createDocsAnnotations();
		// http://www.eclipse.org/ecl/input
		createInputAnnotations();
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/internal</b>.
	 * <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * @generated
	 */
	protected void createInternalAnnotations() {
		String source = "http://www.eclipse.org/ecl/internal";
		addAnnotation
		  (getCommand_Host(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getCommand_Bindings(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (execEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (procInstanceEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getProcInstance_Definition(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (saveStateEClass,
		   source,
		   new String[] {
		   });
		addAnnotation
		  (restoreStateEClass,
		   source,
		   new String[] {
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/input</b>.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected void createInputAnnotations() {
		String source = "http://www.eclipse.org/ecl/input";
		addAnnotation
		  (getForeach_Input(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getGet_Input(),
		   source,
		   new String[] {
		   });
		addAnnotation
		  (getSwitch_Input(),
		   source,
		   new String[] {
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/ecl/docs</b>.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected void createDocsAnnotations() {
		String source = "http://www.eclipse.org/ecl/docs";
		addAnnotation
		  (withEClass,
		   source,
		   new String[] {
			   "description", "Executes a <code>do</code> script by passing given <code>object</code> to each command inside <code>do</code>. The primary use case for this command is to extract a common prefix for a sequence of commands.",
			   "returns", "Output of a <code>do</code> command",
			   "example", "get-view \"Package Explorer\" | get-button \"Collapse All (M1+M2+Numpad_Divide)\" | click\nget-view \"Package Explorer\" | get-button \"Link with Editor\" | click\n// can be shorten to:\nwith [get-view \"Package Explorer\"] {\n    get-button \"Collapse All (M1+M2+Numpad_Divide)\" | click\n    get-button \"Link with Editor\" | click\n}\n"
		   });
		addAnnotation
		  (foreachEClass,
		   source,
		   new String[] {
			   "description", "Reads objects from input pipe and for each object executes a given <code>do</code> command for each item. An optional variable declaration to hold a current value can be passed, if it is not passed, then current value is sent into an input pipe of a <code>do</code> command.  ",
			   "returns", "An aggregated output of <code>do</code> command",
			   "example", "// Implicit item: sent to an input pipe of inner script\nlist-launch-configurations | foreach { get name | log }\n\n// Explicit item:\nlist-launch-configuration | foreach [val item] {\n   $item | get-name | log\n}"
		   });
		addAnnotation
		  (ifEClass,
		   source,
		   new String[] {
			   "description", "Based on a condition corresponds either command either from <code>then</code> or <code>else</code> branch. ",
			   "returns", "Output of executed branch",
			   "example", "if [get-property caption -raw | not-eq \"Test\" ] {\r\n\tselect-item\r\n\tget-menu Delete | click\r\n\tget-window \"Delete Resources\" | get-button OK | click\r\n}"
		   });
		addAnnotation
		  (getIf_Condition(),
		   source,
		   new String[] {
			   "description", "Accepted values are boolean, string (<code>true</code> when case-insensitive equals to <code>true</code>, <code>false</code> otherwise) and number (<code>0</code> for <code>false</code>, <code>true</code> otherwise). "
		   });
		addAnnotation
		  (getIf_Then(),
		   source,
		   new String[] {
			   "description", "Branch being executed on <code>true</code> input. Can be omitted."
		   });
		addAnnotation
		  (getIf_Else(),
		   source,
		   new String[] {
			   "description", "Branch being executed on <code>false</code> input. Can be omitted."
		   });
		addAnnotation
		  (listenEClass,
		   source,
		   new String[] {
			   "description", "Listens for state changes while executing commands.",
			   "returns", "State difference before and after execution.",
			   "example", "// assert no new errors while clicking on the button\r\nlisten errorLog {\r\n\tget-button OK | click\r\n} | assert-empty"
		   });
		addAnnotation
		  (getListen_Source(),
		   source,
		   new String[] {
			   "description", "Source to listen for, for example, <code>errorLog</code>."
		   });
		addAnnotation
		  (getListen_While(),
		   source,
		   new String[] {
			   "description", "Commands to execute while listening for the source."
		   });
		addAnnotation
		  (valEClass,
		   source,
		   new String[] {
			   "description", "Creates a new variable declaration. This command can be used only with <a href=\"#let\">let</a> and <a href=\"#proc\">proc</a>.",
			   "returns", "An internal object, describing a variable",
			   "example", "let [val button [get-button OK]] {\n    $button | click\n}"
		   });
		addAnnotation
		  (getVal_Name(),
		   source,
		   new String[] {
			   "description", "Variable name. Later this name can be used to refer to a variable value by prepending a dollar sign, i.e. $varName. If variable with the same name already defined in outer scope, then it will be hidden by inner variable with the same name."
		   });
		addAnnotation
		  (getVal_Value(),
		   source,
		   new String[] {
			   "description", "Variable value. When variable is defined in <a href=\"#proc\">proc</a> block, this value denotes the default parameter value."
		   });
		addAnnotation
		  (getVal_Input(),
		   source,
		   new String[] {
			   "description", "An option to indicate that variable value should be taken from input pipe. "
		   });
		addAnnotation
		  (getValEClass,
		   source,
		   new String[] {
			   "description", "Returns variable value by its name. Syntax sugar form <code>$name</code> can be used instead."
		   });
		addAnnotation
		  (letEClass,
		   source,
		   new String[] {
			   "description", "Defines variables and evaluates <code>body</code> script. Variable declarations are only visible from withing a body script and cannot be accessed after \'let\' command. If some variable has <code>-input</code> flag, then <code>let</code> command must be used in a pipeline (see example 2). ",
			   "returns", "An output of a <code>body</code> script",
			   "example", "// example #1, simple usage of two variables\nlet [val foo \"hello\"]\n     [val bar \"world!\"] {\n\tformat \"%s, %s\" $foo $bar | show-alert\n}\n\n// example #2, using input argument\nget-window \"New Project\" | let [val window -input] [val button OK] {\n    $window | get-button $button | click\n}\n"
		   });
		addAnnotation
		  (getLet_Vals(),
		   source,
		   new String[] {
			   "description", "List of variables"
		   });
		addAnnotation
		  (getLet_Body(),
		   source,
		   new String[] {
			   "description", "Script to be executed"
		   });
		addAnnotation
		  (procEClass,
		   source,
		   new String[] {
			   "description", "Declares a new procedure in a context of current ECL session, which can later be accessed as a usual command.",
			   "returns", "An output of a <code>body</code> script",
			   "example", "// example 1\n// declaration:\nproc \"click-button\" [val window] [val button] {\n   get-window $window | get-button $button | click\n}\n\n// usage\nclick-button \"New Project\" \"Cancel\"\n\n// example #2, using input argument and default value\n// declaration:\nproc \"set-text-after-label\" [val parent -input] [val label] [val text \"\"] {\n\t$parent | get-editbox -after [get-label $label] | set-text $text\n}\n\n// usage:\nwith [get-window \"New Project\"] {\n   set-text-after-label \"Name:\" \"Sample Project\"\n}\nget-editor | set-text-after-label \"Text\""
		   });
		addAnnotation
		  (getProc_Name(),
		   source,
		   new String[] {
			   "description", "Procedure name. Must be unique in a context of current ECL session"
		   });
		addAnnotation
		  (procInstanceEClass,
		   source,
		   new String[] {
			   "description", "Marker superclass for user-defined procs"
		   });
		addAnnotation
		  (globalEClass,
		   source,
		   new String[] {
			   "description", "Creates a new global variable declaration.",
			   "returns", "An internal object, describing a variable",
			   "example", "// declaring an empty list:\n\nglobal [val myList [list]]\n\n\n\n// filling the list with all New/... menu items:\n\nrepeat [val index] -times [get-elements-count] -command {\n\nlet [val oldlist [$myList]] [val itemName [get-item-property [$index] \"getText()\"]] {\n\n// check if the item is not separator or Other...\n\nif [and [$itemName | not-eq \"\"][$itemName | not-eq \"&Other.*\"]] {\n\nglobal [val myList [appendToList [$oldlist] [$itemName]]] -override\n\n}\n\n}\n\n}"
		   });
		addAnnotation
		  (getEClass,
		   source,
		   new String[] {
			   "description", "Depending on input object, either returns a value by key from <a href=\"#map\">map</a>, or element by index from <a href=\"#list\">list</a>, or member of EMF object by name.",
			   "example", "//types 1 into  log\nemit \"1\" \"2\" \"3\" | to-list | get 0 | str | log\n//types Febuary into log\nmap [entry 1 January][entry 2 Febuary] | get 2 | log"
		   });
		addAnnotation
		  (getGet_Input(),
		   source,
		   new String[] {
			   "description", "Object to get a value from"
		   });
		addAnnotation
		  (getGet_Key(),
		   source,
		   new String[] {
			   "description", "Depending on an input object either a key in a map, or index in a list, or name of a feature/attribute of EMF object"
		   });
		addAnnotation
		  (getGet_Default(),
		   source,
		   new String[] {
			   "description", "Value to return if input object does not contain value by requested key"
		   });
		addAnnotation
		  (caseEClass,
		   source,
		   new String[] {
			   "description", "Corresponds command from <code>then</code> branch if an object from the <code>input</code> matches the <code>condition</code> object.",
			   "returns", "An internal object describing the case.",
			   "example", "list 1 2 3 | get 0 | switch [case 1 {\r\n\tlog -message \"First item is 1\"\r\n}] [case 2 {\n\tlog -message \"First item is 2\"\n}]"
		   });
		addAnnotation
		  (getCase_Condition(),
		   source,
		   new String[] {
			   "description", "Object used to match to the <code>input</code>. Accepted values are boolean, string (<code>true</code> when case-insensitive equals to <code>true</code>, <code>false</code> otherwise) and number (<code>0</code> for <code>false</code>, <code>true</code> otherwise). "
		   });
		addAnnotation
		  (getCase_Then(),
		   source,
		   new String[] {
			   "description", "Branch being executed when <code>input</code> matches the object specified in <code>condition</code>"
		   });
		addAnnotation
		  (switchEClass,
		   source,
		   new String[] {
			   "description", "Reads object from input pipe or <code>input</code> parameter and executes a <code>case</code> part with matching <code>condition</code> parameter (if any) sending <code>input</code> parameter as an input. If no <code>case</code> matched, the <code>default<code> block (if any) will be executed.",
			   "returns", "An output of executed block (<code>case</code> command or <code>-default</code> block). If nothing is executed, no output will be produced.",
			   "example", "list 1 2 3 | get 0 | switch [case 1 {\n\techo \"Found 1\"\n}] [case 2 {\n\techo \"Found 2\"\n}] - default {\n\techo \"Unknown item\"\n} | equals \"Found 1\" | verify-true\n\n///////////\n\nproc \"set-schedules-view-value\" [val view] [val date] [val direction] {\n// First, determine View to set desired Date value in\nget-view Schedules | get-section $view | get-property \"getText()\" -raw | switch\n[case \"Year View\" {\necho \"The YEAR View is being Evaluated!\"\n// Second, determine if the desired Date is already set\nset-view-value $view \"\\d\\d\\d\\d\" $date $direction }]\n[case \"Month View\" {\necho \"The MONTH View is being Evaluated!\"\nset-view-value $view \"(January|February|March|April|May|June|July|August|September|October|November|December), \\d+\" $date $direction}]\n[case \"Week View\" {\necho \"The WEEK View is being Evaluated!\"\nset-view-value $view \"Week \\d+, \\d+\" $date $direction }]\n[case \"Day View\" {\necho \"The DAY View is being Evaluated!\"\nset-view-value $view \"(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday), .*? \\d+, \\d+\" $date $direction}]\n-default {\necho \"The View Value specified in NOT Supported!\"\n}\n}\n"
		   });
		addAnnotation
		  (getSwitch_Input(),
		   source,
		   new String[] {
			   "description", "The input object to match."
		   });
		addAnnotation
		  (getSwitch_Items(),
		   source,
		   new String[] {
			   "description", "One or more <code>case</code> blocks. Block with a condition matching the input will be executed."
		   });
		addAnnotation
		  (getSwitch_Default(),
		   source,
		   new String[] {
			   "description", "Optional parameter. The block which will be executed in case no <code>case</code> block is executed."
		   });
	}

} // CorePackageImpl
