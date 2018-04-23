/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.SolveAnalyzer;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.Jfcalc.ErrProcessor;

import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class ParallelPASolver implements Runnable {
    private static int msnTaskCount = 0;
    private final int mID = msnTaskCount++;
    public PatternManager mpm = null;   //member variables of PatternManager are read-only after loading
                                        // thus mspm is thread safe.
    
    public AbstractExpr[] marrayaeToSolve = new AbstractExpr[0];
    public LinkedList<UnknownVariable> mlistVarUnknown = new LinkedList<UnknownVariable>();
    public LinkedList<LinkedList<Variable>> mlVarSpaces = new LinkedList<LinkedList<Variable>>();
    
    public LinkedList<LinkedList<UnknownVariable>> mlResultSpaces = new LinkedList<LinkedList<UnknownVariable>>();
    public Exception mexception = null;
    
    ParallelPASolver(PatternManager pm)    {
        mpm = pm;
    }
    
    ParallelPASolver(PatternManager pm, LinkedList<UnknownVariable> listVarUnknown,
                     LinkedList<LinkedList<Variable>> lVarSpaces,
                     AbstractExpr[] arrayaeToSolve) throws ErrProcessor.JFCALCExpErrException, JSmartMathErrException    {
        mpm = pm;
        setUnknownVarList(listVarUnknown);
        setVarSpaces(lVarSpaces);
        setAEToSolve(arrayaeToSolve);
    }
    
    
    public final void setUnknownVarList(LinkedList<UnknownVariable> listVarUnknown) throws ErrProcessor.JFCALCExpErrException {
        mlistVarUnknown = UnknownVarOperator.cloneUnknownVarList(listVarUnknown);
    }
    
    public final void setVarSpaces(LinkedList<LinkedList<Variable>> lVarSpaces) throws ErrProcessor.JFCALCExpErrException {
        mlVarSpaces = VariableOperator.cloneVarSpaces(lVarSpaces);
    }
    
    public final void setAEToSolve(AbstractExpr[] arrayaeToSolve) throws ErrProcessor.JFCALCExpErrException, JSmartMathErrException {
        marrayaeToSolve = new AbstractExpr[arrayaeToSolve.length];
        for (int idx = 0; idx < arrayaeToSolve.length; idx ++)  {
            marrayaeToSolve[idx] = arrayaeToSolve[idx];
        }
    }
    
    @Override
    public void run() {
        try {
            mlResultSpaces = SolveAnalyzer.solveExprVars(mpm, marrayaeToSolve, mlistVarUnknown, mlVarSpaces);
        } catch (InterruptedException ex) {
            mexception = ex;
        } catch (JMFPCompErrException ex) {
            mexception = ex;
        } catch (ErrProcessor.JFCALCExpErrException ex) {
            mexception = ex;
        } catch (JSmartMathErrException ex)  {
            mexception = ex;
        }
    }
}
