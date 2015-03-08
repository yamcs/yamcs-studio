/**
 */
package org.csstudio.yamcs.ycl.dsl.ycl;

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
 *   <li>{@link org.csstudio.yamcs.ycl.dsl.ycl.Command#getName <em>Name</em>}</li>
 *   <li>{@link org.csstudio.yamcs.ycl.dsl.ycl.Command#getAssignments <em>Assignments</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.csstudio.yamcs.ycl.dsl.ycl.YclPackage#getCommand()
 * @model
 * @generated
 */
public interface Command extends EObject
{
  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @see org.csstudio.yamcs.ycl.dsl.ycl.YclPackage#getCommand_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.csstudio.yamcs.ycl.dsl.ycl.Command#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Assignments</b></em>' containment reference list.
   * The list contents are of type {@link org.csstudio.yamcs.ycl.dsl.ycl.ArgumentAssignment}.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Assignments</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Assignments</em>' containment reference list.
   * @see org.csstudio.yamcs.ycl.dsl.ycl.YclPackage#getCommand_Assignments()
   * @model containment="true"
   * @generated
   */
  EList<ArgumentAssignment> getAssignments();

} // Command
