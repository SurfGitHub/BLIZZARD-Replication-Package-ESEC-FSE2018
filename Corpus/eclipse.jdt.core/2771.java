/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.FloatUtil;

public class DoubleLiteral extends NumberLiteral {

    double value;

    public  DoubleLiteral(char[] token, int s, int e) {
        super(token, s, e);
    }

    public void computeConstant() {
        Double computedValue;
        try {
            computedValue = Double.valueOf(String.valueOf(source));
        } catch (NumberFormatException e) {
            try {
                double v = FloatUtil.valueOfHexDoubleLiteral(source);
                if (v == Double.POSITIVE_INFINITY) {
                    return;
                }
                if (Double.isNaN(v)) {
                    return;
                }
                value = v;
                constant = Constant.fromValue(v);
            } catch (NumberFormatException e1) {
            }
            return;
        }
        final double doubleValue = computedValue.doubleValue();
        if (doubleValue > Double.MAX_VALUE) {
            // error: the number is too large to represent
            return;
        }
        if (doubleValue < Double.MIN_VALUE) {
            // see 1F6IGUU
            // a true 0 only has '0' and '.' in mantissa
            // 1.0e-5000d is non-zero, but underflows to 0
            boolean isHexaDecimal = false;
            label: for (//it is welled formated so just test against '0' and potential . D d  
            int i = 0; //it is welled formated so just test against '0' and potential . D d  
            i < source.length; //it is welled formated so just test against '0' and potential . D d  
            i++) {
                switch(source[i]) {
                    case '0':
                    case '.':
                        break;
                    case 'x':
                    case 'X':
                        isHexaDecimal = true;
                        break;
                    case 'e':
                    case 'E':
                    case 'f':
                    case 'F':
                    case 'd':
                    case 'D':
                        if (isHexaDecimal) {
                            return;
                        }
                        // no exponent - mantissa is all zero
                        break label;
                    case 'p':
                    case 'P':
                        break label;
                    default:
                        // error: the number is too small to represent
                        return;
                }
            }
        }
        value = doubleValue;
        constant = Constant.fromValue(value);
    }

    /**
	 * Code generation for the double literak
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
    public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
        int pc = codeStream.position;
        if (valueRequired)
            if ((implicitConversion >> 4) == T_double)
                codeStream.generateInlinedValue(value);
            else
                codeStream.generateConstant(constant, implicitConversion);
        codeStream.recordPositionsFrom(pc, this.sourceStart);
    }

    public TypeBinding literalType(BlockScope scope) {
        return DoubleBinding;
    }

    public void traverse(ASTVisitor visitor, BlockScope blockScope) {
        visitor.visit(this, blockScope);
        visitor.endVisit(this, blockScope);
    }
}
