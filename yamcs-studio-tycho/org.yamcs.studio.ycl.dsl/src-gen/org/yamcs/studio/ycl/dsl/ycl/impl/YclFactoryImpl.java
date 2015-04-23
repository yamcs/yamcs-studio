/**
 */
package org.yamcs.studio.ycl.dsl.ycl.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.yamcs.studio.ycl.dsl.ycl.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class YclFactoryImpl extends EFactoryImpl implements YclFactory
{
  /**
   * Creates the default factory implementation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static YclFactory init()
  {
    try
    {
      YclFactory theYclFactory = (YclFactory)EPackage.Registry.INSTANCE.getEFactory(YclPackage.eNS_URI);
      if (theYclFactory != null)
      {
        return theYclFactory;
      }
    }
    catch (Exception exception)
    {
      EcorePlugin.INSTANCE.log(exception);
    }
    return new YclFactoryImpl();
  }

  /**
   * Creates an instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public YclFactoryImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EObject create(EClass eClass)
  {
    switch (eClass.getClassifierID())
    {
      case YclPackage.MODEL: return createModel();
      case YclPackage.COMMAND: return createCommand();
      case YclPackage.COMMAND_ID: return createCommandId();
      case YclPackage.ARGUMENT_ASSIGNMENT: return createArgumentAssignment();
      default:
        throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Model createModel()
  {
    ModelImpl model = new ModelImpl();
    return model;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Command createCommand()
  {
    CommandImpl command = new CommandImpl();
    return command;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public CommandId createCommandId()
  {
    CommandIdImpl commandId = new CommandIdImpl();
    return commandId;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ArgumentAssignment createArgumentAssignment()
  {
    ArgumentAssignmentImpl argumentAssignment = new ArgumentAssignmentImpl();
    return argumentAssignment;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public YclPackage getYclPackage()
  {
    return (YclPackage)getEPackage();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @deprecated
   * @generated
   */
  @Deprecated
  public static YclPackage getPackage()
  {
    return YclPackage.eINSTANCE;
  }

} //YclFactoryImpl
