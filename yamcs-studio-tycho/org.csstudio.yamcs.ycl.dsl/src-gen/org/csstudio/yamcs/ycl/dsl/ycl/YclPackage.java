/**
 */
package org.csstudio.yamcs.ycl.dsl.ycl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.csstudio.yamcs.ycl.dsl.ycl.YclFactory
 * @model kind="package"
 * @generated
 */
public interface YclPackage extends EPackage
{
  /**
   * The package name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNAME = "ycl";

  /**
   * The package namespace URI.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_URI = "http://www.yamcs.org/ycl";

  /**
   * The package namespace name.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  String eNS_PREFIX = "ycl";

  /**
   * The singleton instance of the package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  YclPackage eINSTANCE = org.csstudio.yamcs.ycl.dsl.ycl.impl.YclPackageImpl.init();

  /**
   * The meta object id for the '{@link org.csstudio.yamcs.ycl.dsl.ycl.impl.ModelImpl <em>Model</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.ModelImpl
   * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.YclPackageImpl#getModel()
   * @generated
   */
  int MODEL = 0;

  /**
   * The feature id for the '<em><b>Commands</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODEL__COMMANDS = 0;

  /**
   * The number of structural features of the '<em>Model</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int MODEL_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.csstudio.yamcs.ycl.dsl.ycl.impl.CommandImpl <em>Command</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.CommandImpl
   * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.YclPackageImpl#getCommand()
   * @generated
   */
  int COMMAND = 1;

  /**
   * The feature id for the '<em><b>Name</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMMAND__NAME = 0;

  /**
   * The feature id for the '<em><b>Assignments</b></em>' containment reference list.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMMAND__ASSIGNMENTS = 1;

  /**
   * The number of structural features of the '<em>Command</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMMAND_FEATURE_COUNT = 2;

  /**
   * The meta object id for the '{@link org.csstudio.yamcs.ycl.dsl.ycl.impl.CommandIdImpl <em>Command Id</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.CommandIdImpl
   * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.YclPackageImpl#getCommandId()
   * @generated
   */
  int COMMAND_ID = 2;

  /**
   * The feature id for the '<em><b>Id</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMMAND_ID__ID = 0;

  /**
   * The number of structural features of the '<em>Command Id</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int COMMAND_ID_FEATURE_COUNT = 1;

  /**
   * The meta object id for the '{@link org.csstudio.yamcs.ycl.dsl.ycl.impl.ArgumentAssignmentImpl <em>Argument Assignment</em>}' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.ArgumentAssignmentImpl
   * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.YclPackageImpl#getArgumentAssignment()
   * @generated
   */
  int ARGUMENT_ASSIGNMENT = 3;

  /**
   * The feature id for the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ARGUMENT_ASSIGNMENT__NAME = 0;

  /**
   * The feature id for the '<em><b>Value</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ARGUMENT_ASSIGNMENT__VALUE = 1;

  /**
   * The number of structural features of the '<em>Argument Assignment</em>' class.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   * @ordered
   */
  int ARGUMENT_ASSIGNMENT_FEATURE_COUNT = 2;


  /**
   * Returns the meta object for class '{@link org.csstudio.yamcs.ycl.dsl.ycl.Model <em>Model</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Model</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.Model
   * @generated
   */
  EClass getModel();

  /**
   * Returns the meta object for the containment reference list '{@link org.csstudio.yamcs.ycl.dsl.ycl.Model#getCommands <em>Commands</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Commands</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.Model#getCommands()
   * @see #getModel()
   * @generated
   */
  EReference getModel_Commands();

  /**
   * Returns the meta object for class '{@link org.csstudio.yamcs.ycl.dsl.ycl.Command <em>Command</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Command</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.Command
   * @generated
   */
  EClass getCommand();

  /**
   * Returns the meta object for the containment reference '{@link org.csstudio.yamcs.ycl.dsl.ycl.Command#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference '<em>Name</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.Command#getName()
   * @see #getCommand()
   * @generated
   */
  EReference getCommand_Name();

  /**
   * Returns the meta object for the containment reference list '{@link org.csstudio.yamcs.ycl.dsl.ycl.Command#getAssignments <em>Assignments</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the containment reference list '<em>Assignments</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.Command#getAssignments()
   * @see #getCommand()
   * @generated
   */
  EReference getCommand_Assignments();

  /**
   * Returns the meta object for class '{@link org.csstudio.yamcs.ycl.dsl.ycl.CommandId <em>Command Id</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Command Id</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.CommandId
   * @generated
   */
  EClass getCommandId();

  /**
   * Returns the meta object for the attribute '{@link org.csstudio.yamcs.ycl.dsl.ycl.CommandId#getId <em>Id</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Id</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.CommandId#getId()
   * @see #getCommandId()
   * @generated
   */
  EAttribute getCommandId_Id();

  /**
   * Returns the meta object for class '{@link org.csstudio.yamcs.ycl.dsl.ycl.ArgumentAssignment <em>Argument Assignment</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for class '<em>Argument Assignment</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.ArgumentAssignment
   * @generated
   */
  EClass getArgumentAssignment();

  /**
   * Returns the meta object for the attribute '{@link org.csstudio.yamcs.ycl.dsl.ycl.ArgumentAssignment#getName <em>Name</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Name</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.ArgumentAssignment#getName()
   * @see #getArgumentAssignment()
   * @generated
   */
  EAttribute getArgumentAssignment_Name();

  /**
   * Returns the meta object for the attribute '{@link org.csstudio.yamcs.ycl.dsl.ycl.ArgumentAssignment#getValue <em>Value</em>}'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the meta object for the attribute '<em>Value</em>'.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.ArgumentAssignment#getValue()
   * @see #getArgumentAssignment()
   * @generated
   */
  EAttribute getArgumentAssignment_Value();

  /**
   * Returns the factory that creates the instances of the model.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the factory that creates the instances of the model.
   * @generated
   */
  YclFactory getYclFactory();

  /**
   * <!-- begin-user-doc -->
   * Defines literals for the meta objects that represent
   * <ul>
   *   <li>each class,</li>
   *   <li>each feature of each class,</li>
   *   <li>each enum,</li>
   *   <li>and each data type</li>
   * </ul>
   * <!-- end-user-doc -->
   * @generated
   */
  interface Literals
  {
    /**
     * The meta object literal for the '{@link org.csstudio.yamcs.ycl.dsl.ycl.impl.ModelImpl <em>Model</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.ModelImpl
     * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.YclPackageImpl#getModel()
     * @generated
     */
    EClass MODEL = eINSTANCE.getModel();

    /**
     * The meta object literal for the '<em><b>Commands</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference MODEL__COMMANDS = eINSTANCE.getModel_Commands();

    /**
     * The meta object literal for the '{@link org.csstudio.yamcs.ycl.dsl.ycl.impl.CommandImpl <em>Command</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.CommandImpl
     * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.YclPackageImpl#getCommand()
     * @generated
     */
    EClass COMMAND = eINSTANCE.getCommand();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' containment reference feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMMAND__NAME = eINSTANCE.getCommand_Name();

    /**
     * The meta object literal for the '<em><b>Assignments</b></em>' containment reference list feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EReference COMMAND__ASSIGNMENTS = eINSTANCE.getCommand_Assignments();

    /**
     * The meta object literal for the '{@link org.csstudio.yamcs.ycl.dsl.ycl.impl.CommandIdImpl <em>Command Id</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.CommandIdImpl
     * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.YclPackageImpl#getCommandId()
     * @generated
     */
    EClass COMMAND_ID = eINSTANCE.getCommandId();

    /**
     * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute COMMAND_ID__ID = eINSTANCE.getCommandId_Id();

    /**
     * The meta object literal for the '{@link org.csstudio.yamcs.ycl.dsl.ycl.impl.ArgumentAssignmentImpl <em>Argument Assignment</em>}' class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.ArgumentAssignmentImpl
     * @see org.csstudio.yamcs.ycl.dsl.ycl.impl.YclPackageImpl#getArgumentAssignment()
     * @generated
     */
    EClass ARGUMENT_ASSIGNMENT = eINSTANCE.getArgumentAssignment();

    /**
     * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute ARGUMENT_ASSIGNMENT__NAME = eINSTANCE.getArgumentAssignment_Name();

    /**
     * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    EAttribute ARGUMENT_ASSIGNMENT__VALUE = eINSTANCE.getArgumentAssignment_Value();

  }

} //YclPackage
