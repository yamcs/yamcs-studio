/**
 */
package org.yamcs.studio.ycl.dsl.ycl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Command</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.yamcs.studio.ycl.dsl.ycl.Command#getName <em>Name</em>}</li>
 *   <li>{@link org.yamcs.studio.ycl.dsl.ycl.Command#getAssignments <em>Assignments</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.yamcs.studio.ycl.dsl.ycl.YclPackage#getCommand()
 * @model
 * @generated
 */
public interface Command extends EObject
{
  /**
   * Returns the value of the '<em><b>Name</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' containment reference.
   * @see #setName(CommandId)
   * @see org.yamcs.studio.ycl.dsl.ycl.YclPackage#getCommand_Name()
   * @model containment="true"
   * @generated
   */
  CommandId getName();

  /**
   * Sets the value of the '{@link org.yamcs.studio.ycl.dsl.ycl.Command#getName <em>Name</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' containment reference.
   * @see #getName()
   * @generated
   */
  void setName(CommandId value);

  /**
   * Returns the value of the '<em><b>Assignments</b></em>' containment reference list.
   * The list contents are of type {@link org.yamcs.studio.ycl.dsl.ycl.ArgumentAssignment}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Assignments</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Assignments</em>' containment reference list.
   * @see org.yamcs.studio.ycl.dsl.ycl.YclPackage#getCommand_Assignments()
   * @model containment="true"
   * @generated
   */
  EList<ArgumentAssignment> getAssignments();

} // Command
