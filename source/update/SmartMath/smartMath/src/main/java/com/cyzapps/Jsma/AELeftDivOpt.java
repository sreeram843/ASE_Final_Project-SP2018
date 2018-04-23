package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jfcalc.BaseData;

public class AELeftDivOpt extends AbstractExpr {

	// here mlistChildren and mlistOpts are not used because it leads to difficulty in merging.
	public AbstractExpr maeLeft = AEInvalid.AEINVALID, maeRight = AEInvalid.AEINVALID;
	
	public AELeftDivOpt() {
		initAbstractExpr();
	}
	
	public AELeftDivOpt(AbstractExpr aeLeft, AbstractExpr aeRight) throws SMErrProcessor.JSmartMathErrException {
		setAELeftDivOpt(aeLeft, aeRight);
	}

	public AELeftDivOpt(AbstractExpr aexprOrigin) throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException {
		copy(aexprOrigin);
	}

	@Override
	protected void initAbstractExpr() {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV;
		maeLeft = AEInvalid.AEINVALID;
		maeRight = AEInvalid.AEINVALID;
	}

	@Override
	public void validateAbstractExpr() throws SMErrProcessor.JSmartMathErrException {
		if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV)	{
			throw new SMErrProcessor.JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
		}
	}

	private void setAELeftDivOpt(AbstractExpr aeLeft, AbstractExpr aeRight) throws SMErrProcessor.JSmartMathErrException {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV;
		maeLeft = aeLeft;
		maeRight = aeRight;
		validateAbstractExpr();
	}

	@Override
	protected void copy(AbstractExpr aexprOrigin) throws ErrProcessor.JFCALCExpErrException,
            SMErrProcessor.JSmartMathErrException {
		((AELeftDivOpt)aexprOrigin).validateAbstractExpr();
		super.copy(aexprOrigin);
		maeLeft = ((AELeftDivOpt)aexprOrigin).maeLeft;
		maeRight = ((AELeftDivOpt)aexprOrigin).maeRight;
	}

	@Override
	protected void copyDeep(AbstractExpr aexprOrigin)
			throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException {
		((AELeftDivOpt)aexprOrigin).validateAbstractExpr();
		super.copyDeep(aexprOrigin);
		maeLeft = ((AELeftDivOpt)aexprOrigin).maeLeft.cloneSelf();
		maeRight = ((AELeftDivOpt)aexprOrigin).maeRight.cloneSelf();
	}

	@Override
	public AbstractExpr cloneSelf() throws ErrProcessor.JFCALCExpErrException,
            SMErrProcessor.JSmartMathErrException {
		AbstractExpr aeReturn = new AELeftDivOpt();
		aeReturn.copyDeep(this);
		return aeReturn;
	}

	@Override
	public int[] recalcAExprDim(boolean bUnknownAsSingle) throws SMErrProcessor.JSmartMathErrException,
            ErrProcessor.JFCALCExpErrException {
		validateAbstractExpr();

		int[] narrayDimReturn = maeLeft.recalcAExprDim(bUnknownAsSingle);
		int[] narrayOprd = maeRight.recalcAExprDim(bUnknownAsSingle);
		if (narrayDimReturn.length == 0)	{	// left operand is a number, not a matrix
			narrayDimReturn = narrayOprd;
		} else	{
			if (narrayDimReturn.length - 1 > narrayOprd.length)	{
				throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
			}
			for (int idx1 = 0; idx1 < narrayDimReturn.length - 1; idx1 ++)	{
				if (narrayDimReturn[idx1] != narrayOprd[idx1])	{
					throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
				}
			}
			int[] narrayLeftOprd = narrayDimReturn;
			narrayDimReturn = new int[narrayOprd.length - narrayLeftOprd.length + 2];
			for (int idx1 = 1; idx1 <= narrayOprd.length - narrayLeftOprd.length + 1; idx1 ++)	{
				narrayDimReturn[idx1] = narrayOprd[idx1 + narrayLeftOprd.length - 2];
			}
			narrayDimReturn[0] = narrayLeftOprd[narrayLeftOprd.length - 1];
		}
		return narrayDimReturn;
	}

	@Override
	public boolean isEqual(AbstractExpr aexpr) throws ErrProcessor.JFCALCExpErrException {
		if (menumAEType != aexpr.menumAEType)	{
			return false;
		} else if (maeLeft.isEqual(((AELeftDivOpt)aexpr).maeLeft) == false)	{
			return false;
		} else if (maeRight.isEqual(((AELeftDivOpt)aexpr).maeRight) == false)	{
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
                                boolean bAllowConversion) throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException, InterruptedException  {
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
        if (!(aePattern instanceof AELeftDivOpt))   {
            return false;
        }
        if (maeLeft.isPatternMatch(((AELeftDivOpt)aePattern).maeLeft, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
            return false;
        }
        if (maeRight.isPatternMatch(((AELeftDivOpt)aePattern).maeRight, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
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
        AELeftDivOpt aeReturn = new AELeftDivOpt();
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
		AELeftDivOpt aeReturn = new AELeftDivOpt();
        aeReturn.copy(this);
        for (int idx = 0; idx < listFromToMap.size(); idx ++)	{
			AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
			AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
			if (aeReturn.maeLeft.isEqual(aeFrom))	{
				aeReturn.maeLeft = aeTo;    // no need to clone coz AbstractExpr is immutable
				listReplacedChildren.add(aeReturn.maeLeft);
				break;
			}
		}
		
		for (int idx = 0; idx < listFromToMap.size(); idx ++)	{
			AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
			AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
			if (aeReturn.maeRight.isEqual(aeFrom))	{
				aeReturn.maeRight = aeTo;    // no need to clone coz AbstractExpr is immutable
				listReplacedChildren.add(aeReturn.maeRight);
				break;
			}
		}
		return aeReturn;
	}

	@Override
	public AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws ErrProcessor.JFCALCExpErrException, SMErrProcessor.JSmartMathErrException {
		validateAbstractExpr();
		if (maeRight instanceof AEPosNegOpt)	{
            AEPosNegOpt aeRight = new AEPosNegOpt();
            aeRight.copy(maeRight);
			for (int idx = 0; idx < aeRight.mlistChildren.size(); idx ++)	{
				AELeftDivOpt aeNewChild = new AELeftDivOpt(maeLeft, aeRight.mlistChildren.get(idx));
				aeRight.mlistChildren.set(idx, aeNewChild);
			}
			return aeRight;
		}
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
        BaseData.DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumLeft, new BaseData.CalculateOperator(BaseData.OPERATORTYPES.OPERATOR_LEFTDIVIDE, 2), datumRight);
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
            BaseData.DataClass datum = ExprEvaluator.evaluateTwoOperandCell(((AEConst)aeLeft).getDataClassRef(),
                                                                new BaseData.CalculateOperator(BaseData.OPERATORTYPES.OPERATOR_LEFTDIVIDE, 2),
                                                                ((AEConst)aeRight).getDataClassRef());
            return new AEConst(datum);        
        } else {
            return new AELeftDivOpt(aeLeft, aeRight);
        }
    }
    
	@Override
	public AbstractExpr simplifyAExpr(LinkedList<UnknownVarOperator.UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces,
            SimplifyParams simplifyParams)
			throws InterruptedException, SMErrProcessor.JSmartMathErrException,
            ErrProcessor.JFCALCExpErrException {
		validateAbstractExpr();
		
		AbstractExpr aeLeft = maeLeft.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
		AbstractExpr aeRight = maeRight.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
		
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        LinkedList<BaseData.CalculateOperator> listOpts = new LinkedList<BaseData.CalculateOperator>();
        listChildren.add(aeLeft);
        listChildren.add(aeRight);
        listOpts.add(new BaseData.CalculateOperator(BaseData.OPERATORTYPES.OPERATOR_DIVIDE, 2));
        listOpts.add(new BaseData.CalculateOperator(BaseData.OPERATORTYPES.OPERATOR_MULTIPLY, 2));
		AbstractExpr aeReturn = new AEMulDivOpt(listChildren, listOpts);
		aeReturn = aeReturn.distributeAExpr(simplifyParams);
		return aeReturn;
	}

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        if ((enumAET.getValue() >= ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER.getValue()
                    && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())
                || ((enumAET.getValue() ==  ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV.getValue()
                    || enumAET.getValue() ==  ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV.getValue())
                && nLeftOrRight <= 0))    {
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
			int nReturn = maeLeft.compareAExpr(((AELeftDivOpt)aexpr).maeLeft);
			if (nReturn != 0)	{
				nReturn = maeRight.compareAExpr(((AELeftDivOpt)aexpr).maeRight);
			}
			return nReturn;
		}
	}
	
	// identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
	@Override
	public boolean isNegligible() throws SMErrProcessor.JSmartMathErrException, ErrProcessor.JFCALCExpErrException {
		validateAbstractExpr();
		return maeRight.isNegligible();
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
        strOutput += "\\";
        if (bRightNeedBracketsWhenToStr)    {
            strOutput += "(" + maeRight.output() + ")";
        } else  {
            strOutput += maeRight.output();
        }
		return strOutput;
	}

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws SMErrProcessor.JSmartMathErrException, ErrProcessor.JFCALCExpErrException {
        AbstractExpr aeLeft = (maeLeft instanceof AEConst)?maeLeft:maeLeft.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        AbstractExpr aeRight = (maeRight instanceof AEConst)?maeRight:maeRight.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AELeftDivOpt(aeLeft, aeRight);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws SMErrProcessor.JSmartMathErrException {
        AbstractExpr aeLeft = maeLeft, aeRight = maeLeft;
        if (maeLeft instanceof AEConst
                && ((AEConst)maeLeft).getDataClassRef().getDataType() == BaseData.DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeLeft = ((AEConst)maeLeft).getDataClassRef().getAExpr();
        }
        if (maeRight instanceof AEConst
                && ((AEConst)maeRight).getDataClassRef().getDataType() == BaseData.DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeRight = ((AEConst)maeRight).getDataClassRef().getAExpr();
        }
        return new AELeftDivOpt(aeLeft, aeRight);
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maeLeft.getVarAppearanceCnt(strVarName);
        nCnt += maeRight.getVarAppearanceCnt(strVarName);
        return nCnt;
    }
}
