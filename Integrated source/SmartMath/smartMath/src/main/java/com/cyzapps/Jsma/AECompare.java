package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jfcalc.BaseData;
import com.cyzapps.Jfcalc.ErrProcessor;

public class AECompare extends AbstractExpr {

	public AbstractExpr maeLeft = AEInvalid.AEINVALID;
	public AbstractExpr maeRight = AEInvalid.AEINVALID;
	public BaseData.OPERATORTYPES moptType = BaseData.OPERATORTYPES.OPERATOR_EQ;	// by default it is ==.
	
	public AECompare() {
		initAbstractExpr();
	}
	
	public AECompare(AbstractExpr aeLeft, BaseData.OPERATORTYPES optType, AbstractExpr aeRight) throws SMErrProcessor.JSmartMathErrException {
		setAECompare(aeLeft, optType, aeRight);
	}

	public AECompare(AbstractExpr aeLeft, BaseData.CalculateOperator opt, AbstractExpr aeRight) throws SMErrProcessor.JSmartMathErrException {
		setAECompare(aeLeft, opt.getOperatorType(), aeRight);
	}

	public AECompare(AbstractExpr aexprOrigin) throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException {
		copy(aexprOrigin);
	}

	@Override
	protected void initAbstractExpr() {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_COMPARE;
		maeLeft = AEInvalid.AEINVALID;
		maeRight = AEInvalid.AEINVALID;
		moptType = BaseData.OPERATORTYPES.OPERATOR_EQ;	// by default it is ==.
	}

	@Override
	public void validateAbstractExpr() throws SMErrProcessor.JSmartMathErrException {
		if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_COMPARE)	{
			throw new SMErrProcessor.JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
		}
		
		if (moptType != BaseData.OPERATORTYPES.OPERATOR_EQ && moptType != BaseData.OPERATORTYPES.OPERATOR_NEQ
					&& moptType != BaseData.OPERATORTYPES.OPERATOR_LARGER
					&& moptType != BaseData.OPERATORTYPES.OPERATOR_SMALLER
					&& moptType != BaseData.OPERATORTYPES.OPERATOR_NOLARGER
					&& moptType != BaseData.OPERATORTYPES.OPERATOR_NOSMALLER)	{
				throw new SMErrProcessor.JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
		}
	}
	
	private void setAECompare(AbstractExpr aeLeft, BaseData.OPERATORTYPES optType, AbstractExpr aeRight) throws SMErrProcessor.JSmartMathErrException {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_COMPARE;
		maeLeft = aeLeft;
		moptType = optType;
		maeRight = aeRight;
		validateAbstractExpr();
	}
	
	@Override
	protected void copy(AbstractExpr aexprOrigin) throws ErrProcessor.JFCALCExpErrException,
            SMErrProcessor.JSmartMathErrException {
		((AECompare)aexprOrigin).validateAbstractExpr();
		super.copy(aexprOrigin);
		maeLeft = ((AECompare)aexprOrigin).maeLeft;
		maeRight = ((AECompare)aexprOrigin).maeRight;
		moptType = ((AECompare)aexprOrigin).moptType;
	}

	@Override
	protected void copyDeep(AbstractExpr aexprOrigin)
			throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException {
		((AECompare)aexprOrigin).validateAbstractExpr();
		super.copyDeep(aexprOrigin);
		maeLeft = ((AECompare)aexprOrigin).maeLeft.cloneSelf();
		maeRight = ((AECompare)aexprOrigin).maeRight.cloneSelf();
		moptType = ((AECompare)aexprOrigin).moptType;
	}

	@Override
	public AbstractExpr cloneSelf() throws ErrProcessor.JFCALCExpErrException,
            SMErrProcessor.JSmartMathErrException {
		AbstractExpr aeReturn = new AECompare();
		aeReturn.copyDeep(this);
		return aeReturn;
	}

	@Override
	public int[] recalcAExprDim(boolean bUnknownAsSingle) throws SMErrProcessor.JSmartMathErrException,
            ErrProcessor.JFCALCExpErrException {
		validateAbstractExpr();
		// should always return true or false;
		return new int[0];
	}

	@Override
	public boolean isEqual(AbstractExpr aexpr) throws ErrProcessor.JFCALCExpErrException {
		if (menumAEType != aexpr.menumAEType)	{
			return false;
		} else if (moptType != ((AECompare)aexpr).moptType)	{
			return false;
		} else if (!maeLeft.isEqual(((AECompare)aexpr).maeLeft))	{
			return false;
		} else if (!maeRight.isEqual(((AECompare)aexpr).maeRight))	{
			return false;
		} else	{
			return true;
		}
	}

    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternManager.PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternManager.PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternManager.PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion)  throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException, InterruptedException    {
        /* do not call isPatternDegrade function because generally compare expression cannot degrade-match a pattern.*/
        if (aePattern.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE)   {
            // unknown variable
            for (int idx = 0; idx < listpeuMapUnknowns.size(); idx ++)  {
                if (listpeuMapUnknowns.get(idx).maePatternUnit.isEqual(aePattern))    {
                    if (isEqual(listpeuMapUnknowns.get(idx).maeExprUnit))   {
                        // this unknown variable has been mapped to an expression and the expression is the same as this
                        return true;
                    } else  {
                        // this unknown variable has been mapped to an expression but the expression is not the same as this
                        return false;
                    }
                }
            }
            // the aePattern is an unknown variable and it hasn't been mapped to some expressions before.
            PatternManager.PatternExprUnitMap peuMap = new PatternManager.PatternExprUnitMap(this, aePattern);
            listpeuMapUnknowns.add(peuMap);
            return true;
        }
        if (!(aePattern instanceof AECompare))   {
            return false;
        }
        if (moptType != ((AECompare)aePattern).moptType) {
            return false;
        }
        if (maeLeft.isPatternMatch(((AECompare)aePattern).maeLeft, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
            return false;
        }
        if (maeRight.isPatternMatch(((AECompare)aePattern).maeRight, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
            return false;
        }
        
        return true;
    }
    
	@Override
	public boolean isKnownValOrPseudo() {
		if (!maeLeft.isKnownValOrPseudo())	{
			return false;
		}
		if (!maeRight.isKnownValOrPseudo())	{
			return false;
		}
		return true;
	}
	
	// note that the return list should not be changed.
	@Override
	public LinkedList<AbstractExpr> getListOfChildren()	{
		LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
		listChildren.add(maeLeft);
		listChildren.add(maeRight);
		return listChildren;
	}
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException {
        if (listChildren == null || listChildren.size() != 2) {
            throw new SMErrProcessor.JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
        AECompare aeReturn = new AECompare();
        aeReturn.copy(this);
        aeReturn.maeLeft = listChildren.getFirst();
        aeReturn.maeRight = listChildren.getLast();
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
	
	// this function replaces children who equal aeFrom to aeTo and
	// returns the number of children that are replaced.
	@Override
	public AbstractExpr replaceChildren(LinkedList<PatternManager.PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException {
		AECompare aeReturn = new AECompare();
        aeReturn.copy(this);
        for (int idx = 0; idx < listFromToMap.size(); idx ++)	{
			AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
			AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
			if (aeReturn.maeLeft.isEqual(aeFrom))	{
				aeReturn.maeLeft = aeTo;
				listReplacedChildren.add(aeReturn.maeLeft);
				break;
			}
		}
		
		for (int idx = 0; idx < listFromToMap.size(); idx ++)	{
			AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
			AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
			if (aeReturn.maeRight.isEqual(aeFrom))	{
				aeReturn.maeRight = aeTo;
				listReplacedChildren.add(aeReturn.maeRight);
				break;
			}
		}
		return aeReturn;
	}

	@Override
	public AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException {
		validateAbstractExpr();
		return this;
	}
    
    // avoid to do any overhead work.
    @Override
	public BaseData.DataClass evaluateAExprQuick(
			LinkedList<UnknownVarOperator.UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, SMErrProcessor.JSmartMathErrException, ErrProcessor.JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        BaseData.DataClass datumLeft = maeLeft.evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
		BaseData.DataClass datumRight = maeRight.evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
        BaseData.DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumLeft, new BaseData.CalculateOperator(moptType, 2), datumRight);
        return datum;        
    }

    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVarOperator.UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, SMErrProcessor.JSmartMathErrException, ErrProcessor.JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        AbstractExpr aeLeft = maeLeft.evaluateAExpr(lUnknownVars, lVarNameSpaces);
		AbstractExpr aeRight = maeRight.evaluateAExpr(lUnknownVars, lVarNameSpaces);
        if (aeLeft instanceof AEConst && aeRight instanceof AEConst) {
            BaseData.DataClass datum = ExprEvaluator.evaluateTwoOperandCell(((AEConst)aeLeft).getDataClassRef(), new BaseData.CalculateOperator(moptType, 2), ((AEConst)aeRight).getDataClassRef());
            return new AEConst(datum);
        } else {
            return new AECompare(aeLeft, moptType, aeRight);
        }
    }

	// exceptions like cannot merge or unknown variable should be handled internally in this function.
	// if an exception is thrown out, something is wrong.
	@Override
	public AbstractExpr simplifyAExpr(
			LinkedList<UnknownVarOperator.UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces,
            SimplifyParams simplifyParams)
			throws InterruptedException, SMErrProcessor.JSmartMathErrException, ErrProcessor.JFCALCExpErrException {
		validateAbstractExpr();
		AbstractExpr aeLeft = maeLeft.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
		AbstractExpr aeRight = maeRight.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
		
		AbstractExpr aeReturn = new AECompare(aeLeft, moptType, aeRight);
		try	{
			// have to clone the parameters coz they may be changed inside.
			aeReturn = mergeCompare(aeLeft, aeRight, moptType, lUnknownVars, lVarNameSpaces, simplifyParams);
		} catch (SMErrProcessor.JSmartMathErrException e)	{
			if (e.m_se.m_enumErrorType != SMErrProcessor.ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
				throw e;
			}
		}
		
		aeReturn = aeReturn.distributeAExpr(simplifyParams);
		return aeReturn;
	}

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        if ((enumAET.getValue() > menumAEType.getValue()
                    && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())
                || (enumAET.getValue() == menumAEType.getValue() && nLeftOrRight <= 0))    {
            return true;
        }
        return false;
    }
    
	@Override
	public int compareAExpr(AbstractExpr aexpr) throws SMErrProcessor.JSmartMathErrException, ErrProcessor.JFCALCExpErrException {
		if (menumAEType.getValue() < aexpr.menumAEType.getValue())	{
			return 1;
		} else if (menumAEType.getValue() > aexpr.menumAEType.getValue())	{
			return -1;
		} else	{
			int nReturn = maeLeft.compareAExpr(((AECompare)aexpr).maeLeft);
			if (nReturn != 0)	{
				nReturn = maeRight.compareAExpr(((AECompare)aexpr).maeRight);
			}
			return nReturn;
		}
	}

	// identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
	@Override
	public boolean isNegligible() throws SMErrProcessor.JSmartMathErrException {
		validateAbstractExpr();
		return false;	// return false because this aexpr has been mostly simplified
						// but it is still not an AEConst. This means the data value
						// of this aexpr is still not know. Thus return false.
	}
	
	// output the string based expression of any abstract expression type.
	@Override
	public String output()	throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException {
		validateAbstractExpr();
        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
        bLeftNeedBracketsWhenToStr = maeLeft.needBracketsWhenToStr(menumAEType, 1);
        bRightNeedBracketsWhenToStr = maeRight.needBracketsWhenToStr(menumAEType, -1);
        
		String strOutput = "";
        if (bLeftNeedBracketsWhenToStr) {
            strOutput += "(" + maeLeft.output() + ")";
        } else  {
            strOutput += maeLeft.output();
        }
        strOutput += moptType.output();
        if (bRightNeedBracketsWhenToStr)    {
            strOutput += "(" + maeRight.output() + ")";
        } else  {
            strOutput += maeRight.output();
        }
		return strOutput;
	}
	
	// note that parameters of this function will be changed inside.
	public static AbstractExpr mergeCompare(AbstractExpr aeLeft, AbstractExpr aeRight, BaseData.OPERATORTYPES optType,
                                            LinkedList<UnknownVarOperator.UnknownVariable> lUnknownVars, LinkedList<LinkedList<Variable>> lVarNameSpaces,
                                            SimplifyParams simplifyParams) throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException, InterruptedException	{
		if (aeLeft instanceof AEConst && aeRight instanceof AEConst)	{
			// both aeInput1 and aeInput2 are constants.
			BaseData.DataClass datumLeft = ((AEConst)aeLeft).getDataClassRef(),
			datumRight = ((AEConst)aeRight).getDataClassRef();   // evaluateTwoOperandCell will not change datumLeft or datumRight.

			BaseData.CalculateOperator calcOpt = new BaseData.CalculateOperator(optType, 2);
			BaseData.DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumLeft, calcOpt, datumRight);
			AEConst aexprReturn = new AEConst(datum);
			return aexprReturn;
		} else if (aeLeft.isEqual(aeRight)) {
			// left and right are the same thing.
            if (optType == BaseData.OPERATORTYPES.OPERATOR_EQ  || optType == BaseData.OPERATORTYPES.OPERATOR_NOSMALLER
                    || optType == BaseData.OPERATORTYPES.OPERATOR_NOLARGER)	{
                // ==, >=, <=.
                return new AEConst(new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.TRUE));
            } else	{
                // !=, >, <
                return new AEConst(new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.FALSE));
            }
		} else  {   // if (!(aeLeft.isEqual(aeRight)))
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeLeft);
            listChildren.add(aeRight);
            LinkedList<BaseData.CalculateOperator> listOpts = new LinkedList<BaseData.CalculateOperator>();
            listOpts.add(new BaseData.CalculateOperator(BaseData.OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
            listOpts.add(new BaseData.CalculateOperator(BaseData.OPERATORTYPES.OPERATOR_SUBTRACT, 2));
            AbstractExpr aeLeftMinusRight = new AEPosNegOpt(listChildren, listOpts);
            // we need not to worry about variable name space because aeLeft and aeRight have been
            // simplified most using name space.
            aeLeftMinusRight = aeLeftMinusRight.simplifyAExprMost(lUnknownVars, lVarNameSpaces, simplifyParams);
            if (aeLeftMinusRight instanceof AEConst)    {
                // if not a constant, we cannot compare coz variable's values are unknown
                BaseData.DataClass datumLeftMinusRight = ((AEConst)aeLeftMinusRight).getDataClassRef();   // datumLeftMinusRight will not be modified.
                if (datumLeftMinusRight.getDataType() == BaseData.DATATYPES.DATUM_REF_DATA)  {
                    if (datumLeftMinusRight.isZeros(false)) { // left minus right is a zero matrix.
                        if (optType == BaseData.OPERATORTYPES.OPERATOR_EQ  || optType == BaseData.OPERATORTYPES.OPERATOR_NOSMALLER
                                || optType == BaseData.OPERATORTYPES.OPERATOR_NOLARGER)	{
                            // ==, >=, <=.
                            return new AEConst(new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.TRUE));
                        } else	{
                            // >, <, !=
                            return new AEConst(new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.FALSE));
                        }
                    } else  { // left minus right is not a zero matrix.
                        if (optType == BaseData.OPERATORTYPES.OPERATOR_EQ)   {
                            return new AEConst(new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.FALSE));
                        } else if (optType == BaseData.OPERATORTYPES.OPERATOR_NEQ) {
                            return new AEConst(new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.TRUE));
                        }
                    }
                } else  {
                    BaseData.DataClass datumZero = new BaseData.DataClass(BaseData.DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO);
                    BaseData.CalculateOperator calcOpt = new BaseData.CalculateOperator(optType, 2);
                    BaseData.DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumLeftMinusRight, calcOpt, datumZero);
                    AEConst aexprReturn = new AEConst(datum);
                    return aexprReturn;
                }
            }
        }
		
		throw new SMErrProcessor.JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
	}

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws SMErrProcessor.JSmartMathErrException, ErrProcessor.JFCALCExpErrException {
        AbstractExpr aeLeft = (maeLeft instanceof AEConst)?maeLeft:maeLeft.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        AbstractExpr aeRight = (maeRight instanceof AEConst)?maeRight:maeRight.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AECompare(aeLeft, moptType, aeRight);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws SMErrProcessor.JSmartMathErrException {
        AbstractExpr aeLeft = maeLeft, aeRight = maeRight;
        if (maeLeft instanceof AEConst
                && ((AEConst)maeLeft).getDataClassRef().getDataType() == BaseData.DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeLeft = ((AEConst)maeLeft).getDataClassRef().getAExpr();
        }
        if (maeRight instanceof AEConst
                && ((AEConst)maeRight).getDataClassRef().getDataType() == BaseData.DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeRight = ((AEConst)maeRight).getDataClassRef().getAExpr();
        }
        return new AECompare(aeLeft, moptType, aeRight);
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maeLeft.getVarAppearanceCnt(strVarName);
        nCnt += maeRight.getVarAppearanceCnt(strVarName);
        return nCnt;
    }
}
