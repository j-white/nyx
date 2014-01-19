/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.linear;

import java.io.Serializable;

import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.linear.MatrixVisitorException;
import org.apache.commons.math.exception.util.LocalizedFormats;

/**
 * Differs from the Array2DRowRealMatrix in that it stores entries in:
 *    data[column][row]
 *
 * as opposed to:
 *    data[row][column]
 *
 * making it more efficient to store large column vectors.
 */
public class Array2DColumnRealMatrix extends AbstractRealMatrix implements Serializable {

    /** Serializable version identifier */
    private static final long serialVersionUID = -1067294169172445528L;

    /** Entries of the matrix */
    protected double data[][];

    /**
     * Creates a matrix with no data
     */
    public Array2DColumnRealMatrix() {
    }

    /**
     * Create a new RealMatrix with the supplied row and column dimensions.
     *
     * @param rowDimension  the number of rows in the new matrix
     * @param columnDimension  the number of columns in the new matrix
     * @throws IllegalArgumentException if row or column dimension is not
     *  positive
     */
    public Array2DColumnRealMatrix(final int rowDimension, final int columnDimension)
        throws IllegalArgumentException {
        super(rowDimension, columnDimension);
        data = new double[columnDimension][rowDimension];
    }

    /**
     * Create a new RealMatrix using the input array as the underlying
     * data array.
     * <p>The input array is copied, not referenced. This constructor has
     * the same effect as calling {@link #Array2DColumnRealMatrix(double[][], boolean)}
     * with the second argument set to <code>true</code>.</p>
     *
     * @param d data for new matrix
     * @throws IllegalArgumentException if <code>d</code> is not rectangular
     *  (not all rows have the same length) or empty
     * @throws NullPointerException if <code>d</code> is null
     * @see #Array2DColumnRealMatrix(double[][], boolean)
     */
    public Array2DColumnRealMatrix(final double[][] d)
        throws IllegalArgumentException, NullPointerException {
        copyIn(d);
    }

    /**
     * Create a new RealMatrix using the input array as the underlying
     * data array.
     * <p>If an array is built specially in order to be embedded in a
     * RealMatrix and not used directly, the <code>copyArray</code> may be
     * set to <code>false</code. This will prevent the copying and improve
     * performance as no new array will be built and no data will be copied.</p>
     * @param d data for new matrix
     * @param copyArray if true, the input array will be copied, otherwise
     * it will be referenced
     * @throws IllegalArgumentException if <code>d</code> is not rectangular
     *  (not all rows have the same length) or empty
     * @throws NullPointerException if <code>d</code> is null
     * @see #Array2DColumnRealMatrix(double[][])
     */
    public Array2DColumnRealMatrix(final double[][] d, final boolean copyArray)
        throws IllegalArgumentException, NullPointerException {
        if (copyArray) {
            copyIn(d);
        } else {
            if (d == null) {
                throw new NullPointerException();
            }
            final int nRows = d.length;
            if (nRows == 0) {
                throw MathRuntimeException.createIllegalArgumentException(
                      LocalizedFormats.AT_LEAST_ONE_ROW);
            }
            final int nCols = d[0].length;
            if (nCols == 0) {
                throw MathRuntimeException.createIllegalArgumentException(
                      LocalizedFormats.AT_LEAST_ONE_COLUMN);
            }
            for (int r = 1; r < nRows; r++) {
                if (d[r].length != nCols) {
                    throw MathRuntimeException.createIllegalArgumentException(
                          LocalizedFormats.DIFFERENT_ROWS_LENGTHS, nCols, d[r].length);
                }
            }
            data = d;
        }
    }

    /**
     * Create a new (column) RealMatrix using <code>v</code> as the
     * data for the unique column of the <code>v.length x 1</code> matrix
     * created.
     * <p>The input array is copied, not referenced.</p>
     *
     * @param v column vector holding data for new matrix
     */
    public Array2DColumnRealMatrix(final double[] v) {
        final int nRows = v.length;
        data = new double[nRows][1];
        for (int row = 0; row < nRows; row++) {
            data[row][0] = v[row];
        }
    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix createMatrix(final int rowDimension, final int columnDimension)
        throws IllegalArgumentException {
        return new Array2DColumnRealMatrix(rowDimension, columnDimension);
    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix copy() {
        return new Array2DColumnRealMatrix(copyOut(), false);
    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix add(final RealMatrix m)
        throws IllegalArgumentException {
        try {
            return add((Array2DColumnRealMatrix) m);
        } catch (ClassCastException cce) {
            return super.add(m);
        }
    }

    /**
     * Compute the sum of this and <code>m</code>.
     *
     * @param m    matrix to be added
     * @return     this + m
     * @throws  IllegalArgumentException if m is not the same size as this
     */
    public Array2DColumnRealMatrix add(final Array2DColumnRealMatrix m)
        throws IllegalArgumentException {

        // safety check
        MatrixUtils.checkAdditionCompatible(this, m);

        final int rowCount    = getRowDimension();
        final int columnCount = getColumnDimension();
        final double[][] outData = new double[rowCount][columnCount];
        for (int row = 0; row < rowCount; row++) {
            final double[] dataRow    = data[row];
            final double[] mRow       = m.data[row];
            final double[] outDataRow = outData[row];
            for (int col = 0; col < columnCount; col++) {
                outDataRow[col] = dataRow[col] + mRow[col];
            }
        }

        return new Array2DColumnRealMatrix(outData, false);

    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix subtract(final RealMatrix m)
        throws IllegalArgumentException {
        try {
            return subtract((Array2DColumnRealMatrix) m);
        } catch (ClassCastException cce) {
            return super.subtract(m);
        }
    }

    /**
     * Compute  this minus <code>m</code>.
     *
     * @param m    matrix to be subtracted
     * @return     this + m
     * @throws  IllegalArgumentException if m is not the same size as this
     */
    public Array2DColumnRealMatrix subtract(final Array2DColumnRealMatrix m)
        throws IllegalArgumentException {

        // safety check
        MatrixUtils.checkSubtractionCompatible(this, m);

        final int rowCount    = getRowDimension();
        final int columnCount = getColumnDimension();
        final double[][] outData = new double[rowCount][columnCount];
        for (int row = 0; row < rowCount; row++) {
            final double[] dataRow    = data[row];
            final double[] mRow       = m.data[row];
            final double[] outDataRow = outData[row];
            for (int col = 0; col < columnCount; col++) {
                outDataRow[col] = dataRow[col] - mRow[col];
            }
        }

        return new Array2DColumnRealMatrix(outData, false);

    }

    /** {@inheritDoc} */
    @Override
    public RealMatrix multiply(final RealMatrix m)
        throws IllegalArgumentException {
        try {
            return multiply((Array2DColumnRealMatrix) m);
        } catch (ClassCastException cce) {
            return super.multiply(m);
        }
    }

    /**
     * Returns the result of postmultiplying this by <code>m</code>.
     * @param m    matrix to postmultiply by
     * @return     this*m
     * @throws     IllegalArgumentException
     *             if columnDimension(this) != rowDimension(m)
     */
    public Array2DColumnRealMatrix multiply(final Array2DColumnRealMatrix m)
        throws IllegalArgumentException {

        // safety check
        MatrixUtils.checkMultiplicationCompatible(this, m);

        final int nRows = this.getRowDimension();
        final int nCols = m.getColumnDimension();
        final int nSum = this.getColumnDimension();
        final double[][] outData = new double[nRows][nCols];
        for (int row = 0; row < nRows; row++) {
            final double[] dataRow    = data[row];
            final double[] outDataRow = outData[row];
            for (int col = 0; col < nCols; col++) {
                double sum = 0;
                for (int i = 0; i < nSum; i++) {
                    sum += dataRow[i] * m.data[i][col];
                }
                outDataRow[col] = sum;
            }
        }

        return new Array2DColumnRealMatrix(outData, false);

    }

    /** {@inheritDoc} */
    @Override
    public double[][] getData() {
        return copyOut();
    }

    /**
     * Returns a reference to the underlying data array.
     * <p>
     * Does <strong>not</strong> make a fresh copy of the underlying data.</p>
     *
     * @return 2-dimensional array of entries
     */
    public double[][] getDataRef() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public void setSubMatrix(final double[][] subMatrix, final int row, final int column)
    throws MatrixIndexException {
        if (data == null) {
            if (row > 0) {
                throw MathRuntimeException.createIllegalStateException(
                      LocalizedFormats.FIRST_ROWS_NOT_INITIALIZED_YET, row);
            }
            if (column > 0) {
                throw MathRuntimeException.createIllegalStateException(
                      LocalizedFormats.FIRST_COLUMNS_NOT_INITIALIZED_YET, column);
            }
            final int nRows = subMatrix.length;
            if (nRows == 0) {
                throw MathRuntimeException.createIllegalArgumentException(
                      LocalizedFormats.AT_LEAST_ONE_ROW);
            }

            final int nCols = subMatrix[0].length;
            if (nCols == 0) {
                throw MathRuntimeException.createIllegalArgumentException(
                      LocalizedFormats.AT_LEAST_ONE_COLUMN);
            }
            data = new double[subMatrix.length][nCols];
            for (int i = 0; i < data.length; ++i) {
                if (subMatrix[i].length != nCols) {
                    throw MathRuntimeException.createIllegalArgumentException(
                          LocalizedFormats.DIFFERENT_ROWS_LENGTHS, nCols, subMatrix[i].length);
                }
                System.arraycopy(subMatrix[i], 0, data[i + row], column, nCols);
            }
        } else {
            super.setSubMatrix(subMatrix, row, column);
        }

    }

    /** {@inheritDoc} */
    @Override
    public double getEntry(final int row, final int column)
        throws MatrixIndexException {
        try {
            return data[column][row];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new MatrixIndexException(
                      LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, getRowDimension(), getColumnDimension());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setEntry(final int row, final int column, final double value)
        throws MatrixIndexException {
        try {
            data[column][row] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new MatrixIndexException(
                      LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, getRowDimension(), getColumnDimension());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addToEntry(final int row, final int column, final double increment)
        throws MatrixIndexException {
        try {
            data[column][row] += increment;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new MatrixIndexException(
                      LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, getRowDimension(), getColumnDimension());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void multiplyEntry(final int row, final int column, final double factor)
        throws MatrixIndexException {
        try {
            data[column][row] *= factor;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new MatrixIndexException(
                      LocalizedFormats.NO_SUCH_MATRIX_ENTRY, row, column, getRowDimension(), getColumnDimension());
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getRowDimension() {
    	return ((data == null) || (data[0] == null)) ? 0 : data[0].length;
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnDimension() {
    	return (data == null) ? 0 : data.length;
    }

    /** {@inheritDoc} */
    @Override
    public double[] operate(final double[] v)
        throws IllegalArgumentException {
        final int nRows = this.getRowDimension();
        final int nCols = this.getColumnDimension();
        if (v.length != nCols) {
            throw MathRuntimeException.createIllegalArgumentException(
                  LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, nCols);
        }
        final double[] out = new double[nRows];
        for (int row = 0; row < nRows; row++) {
            final double[] dataRow = data[row];
            double sum = 0;
            for (int i = 0; i < nCols; i++) {
                sum += dataRow[i] * v[i];
            }
            out[row] = sum;
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public double[] preMultiply(final double[] v)
        throws IllegalArgumentException {

        final int nRows = getRowDimension();
        final int nCols = getColumnDimension();
        if (v.length != nRows) {
            throw MathRuntimeException.createIllegalArgumentException(
                  LocalizedFormats.VECTOR_LENGTH_MISMATCH, v.length, nRows);
        }

        final double[] out = new double[nCols];
        for (int col = 0; col < nCols; ++col) {
            double sum = 0;
            for (int i = 0; i < nRows; ++i) {
                sum += data[i][col] * v[i];
            }
            out[col] = sum;
        }

        return out;

    }

    /** {@inheritDoc} */
    @Override
    public double walkInRowOrder(final RealMatrixChangingVisitor visitor)
        throws MatrixVisitorException {
        final int rows    = getRowDimension();
        final int columns = getColumnDimension();
        visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
        for (int i = 0; i < rows; ++i) {
            final double[] rowI = data[i];
            for (int j = 0; j < columns; ++j) {
                rowI[j] = visitor.visit(i, j, rowI[j]);
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInRowOrder(final RealMatrixPreservingVisitor visitor)
        throws MatrixVisitorException {
        final int rows    = getRowDimension();
        final int columns = getColumnDimension();
        visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
        for (int i = 0; i < rows; ++i) {
            final double[] rowI = data[i];
            for (int j = 0; j < columns; ++j) {
                visitor.visit(i, j, rowI[j]);
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInRowOrder(final RealMatrixChangingVisitor visitor,
                                 final int startRow, final int endRow,
                                 final int startColumn, final int endColumn)
        throws MatrixIndexException, MatrixVisitorException {
        MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
        visitor.start(getRowDimension(), getColumnDimension(),
                      startRow, endRow, startColumn, endColumn);
        for (int i = startRow; i <= endRow; ++i) {
            final double[] rowI = data[i];
            for (int j = startColumn; j <= endColumn; ++j) {
                rowI[j] = visitor.visit(i, j, rowI[j]);
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInRowOrder(final RealMatrixPreservingVisitor visitor,
                                 final int startRow, final int endRow,
                                 final int startColumn, final int endColumn)
        throws MatrixIndexException, MatrixVisitorException {
        MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
        visitor.start(getRowDimension(), getColumnDimension(),
                      startRow, endRow, startColumn, endColumn);
        for (int i = startRow; i <= endRow; ++i) {
            final double[] rowI = data[i];
            for (int j = startColumn; j <= endColumn; ++j) {
                visitor.visit(i, j, rowI[j]);
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInColumnOrder(final RealMatrixChangingVisitor visitor)
        throws MatrixVisitorException {
        final int rows    = getRowDimension();
        final int columns = getColumnDimension();
        visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
        for (int j = 0; j < columns; ++j) {
            for (int i = 0; i < rows; ++i) {
                final double[] rowI = data[i];
                rowI[j] = visitor.visit(i, j, rowI[j]);
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInColumnOrder(final RealMatrixPreservingVisitor visitor)
        throws MatrixVisitorException {
        final int rows    = getRowDimension();
        final int columns = getColumnDimension();
        visitor.start(rows, columns, 0, rows - 1, 0, columns - 1);
        for (int j = 0; j < columns; ++j) {
            for (int i = 0; i < rows; ++i) {
                visitor.visit(i, j, data[i][j]);
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInColumnOrder(final RealMatrixChangingVisitor visitor,
                                    final int startRow, final int endRow,
                                    final int startColumn, final int endColumn)
        throws MatrixIndexException, MatrixVisitorException {
        MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
        visitor.start(getRowDimension(), getColumnDimension(),
                      startRow, endRow, startColumn, endColumn);
        for (int j = startColumn; j <= endColumn; ++j) {
            for (int i = startRow; i <= endRow; ++i) {
                final double[] rowI = data[i];
                rowI[j] = visitor.visit(i, j, rowI[j]);
            }
        }
        return visitor.end();
    }

    /** {@inheritDoc} */
    @Override
    public double walkInColumnOrder(final RealMatrixPreservingVisitor visitor,
                                    final int startRow, final int endRow,
                                    final int startColumn, final int endColumn)
        throws MatrixIndexException, MatrixVisitorException {
        MatrixUtils.checkSubMatrixIndex(this, startRow, endRow, startColumn, endColumn);
        visitor.start(getRowDimension(), getColumnDimension(),
                      startRow, endRow, startColumn, endColumn);
        for (int j = startColumn; j <= endColumn; ++j) {
            for (int i = startRow; i <= endRow; ++i) {
                visitor.visit(i, j, data[i][j]);
            }
        }
        return visitor.end();
    }

    /**
     * Returns a fresh copy of the underlying data array.
     *
     * @return a copy of the underlying data array.
     */
    private double[][] copyOut() {
        final int nRows = this.getRowDimension();
        final double[][] out = new double[nRows][this.getColumnDimension()];
        // can't copy 2-d array in one shot, otherwise get row references
        for (int i = 0; i < nRows; i++) {
            System.arraycopy(data[i], 0, out[i], 0, data[i].length);
        }
        return out;
    }

    /**
     * Replaces data with a fresh copy of the input array.
     * <p>
     * Verifies that the input array is rectangular and non-empty.</p>
     *
     * @param in data to copy in
     * @throws IllegalArgumentException if input array is empty or not
     *    rectangular
     * @throws NullPointerException if input array is null
     */
    private void copyIn(final double[][] in) {
        setSubMatrix(in, 0, 0);
    }

}
