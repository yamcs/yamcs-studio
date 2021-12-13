/********************************************************************************
 * Copyright (c) 2010, 2021 ITER Organization and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.symbol.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Permuation matrix used to flip/rotate images
 */
public class PermutationMatrix {

    private final double[][] matrix;

    public PermutationMatrix(double x1, double y1, double x2, double y2) {
        matrix = new double[2][2];
        matrix[0][0] = round(x1);
        matrix[0][1] = round(y1);
        matrix[1][0] = round(x2);
        matrix[1][1] = round(y2);
    }

    public PermutationMatrix(double[][] matrix) {
        this.matrix = matrix;
        for (var i = 0; i < matrix.length; i++) {
            for (var j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = round(matrix[i][j]);
            }
        }
    }

    /**
     * Generate [2,2] identity matrix
     */
    public static PermutationMatrix generateIdentityMatrix() {
        var matrix = new double[][] { { 1, 0 }, { 0, 1 } };
        return new PermutationMatrix(matrix);
    }

    /**
     * Generate horizontal flip [2,2] matrix
     */
    public static PermutationMatrix generateFlipVMatrix() {
        var matrix = new double[][] { { 1, 0 }, { 0, -1 } };
        return new PermutationMatrix(matrix);
    }

    /**
     * Generate vertical flip [2,2] matrix
     */
    public static PermutationMatrix generateFlipHMatrix() {
        var matrix = new double[][] { { -1, 0 }, { 0, 1 } };
        return new PermutationMatrix(matrix);
    }

    /**
     * Generate rotation [2,2] matrix
     */
    public static PermutationMatrix generateRotationMatrix(double angleInDegree) {
        var angleInRadian = angleInDegree * Math.PI / 180.0;
        var sin = Math.sin(angleInRadian);
        var cos = Math.cos(angleInRadian);
        var matrix = new double[2][2];

        matrix[0][0] = cos;
        matrix[0][1] = -sin;
        matrix[1][0] = sin;
        matrix[1][1] = cos;

        return new PermutationMatrix(matrix);
    }

    public PermutationMatrix multiply(PermutationMatrix pm) {
        var m1 = getMatrix();
        var m2 = pm.getMatrix();

        int p1 = m1.length, p2 = m2.length, q2 = m2[0].length;
        var result = new double[p1][q2];
        for (var i = 0; i < p1; i++) {
            for (var j = 0; j < q2; j++) {
                for (var k = 0; k < p2; k++) {
                    result[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return new PermutationMatrix(result);
    }

    public double[][] getMatrix() {
        return matrix;
    }

    public void roundToIntegers() {
        matrix[0][0] = Math.round(matrix[0][0]);
        matrix[0][1] = Math.round(matrix[0][1]);
        matrix[1][0] = Math.round(matrix[1][0]);
        matrix[1][1] = Math.round(matrix[1][1]);
    }

    private double round(double x) {
        var df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.valueOf(df.format(x)) + 0.0;
    }

    @Override
    public int hashCode() {
        var prime = 31;
        var result = 1;
        result = prime * result + Arrays.hashCode(matrix);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        var other = (PermutationMatrix) obj;
        if (!Arrays.deepEquals(matrix, other.matrix)) {
            return false;
        }
        return true;
    }
}
