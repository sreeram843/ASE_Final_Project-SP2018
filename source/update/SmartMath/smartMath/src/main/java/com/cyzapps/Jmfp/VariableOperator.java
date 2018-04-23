package com.cyzapps.Jmfp;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfcalc.BaseData;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jsma.UnknownVarOperator;

public class VariableOperator {
	// variable name is not case sensitive.
	public static class Variable	{
		protected String mstrName = "";
		protected BaseData.DataClass mdatumValue = new BaseData.DataClass();
		
		public Variable()   {
        }
		public Variable(String strName)	{
			setName(strName);
		}
		public Variable(String strName, BaseData.DataClass datumValue)	{
			setVariable(strName, datumValue);
		}
		
		public void setName(String strName)	{
			mstrName = strName.toLowerCase(Locale.US);
		}
		
		public String getName()	{
			return mstrName;
		}
		
		public BaseData.DataClass setValue(BaseData.DataClass datumValue)	{
			mdatumValue = datumValue;
			return mdatumValue;
		}
		
		public BaseData.DataClass getValue()	{
			return mdatumValue;	// have to use reference, cannot use copy or deep copy for variable values
		}
        
		public void setVariable(String strName, BaseData.DataClass datumValue)	{
			mstrName = strName.toLowerCase(Locale.US);
            mdatumValue = datumValue;
		}
		
		public void clear()	{
			// something like free memory although no memory is actually freed.
			mdatumValue = new BaseData.DataClass();
		}
		
		public static boolean isValidVarName(String strName)	{
			if (strName == null)	{
				return false;
			} else if (strName.trim().length() != strName.length())	{
				// blanks before and after
				return false;
			} else if (strName.length() == 0)	{
				return false;
			} else {
				if (ElemAnalyzer.isNameChar(strName, 0) != 1)	{
					return false;	// first char is wrong.
				}
				for (int idx = 0; idx < strName.length(); idx ++)	{
					if (ElemAnalyzer.isNameChar(strName, idx) == 0)	{
						return false;
					}
				}
				return true;
			}
		}
	}

	public static Variable lookUpPreDefined(String strName) throws ErrProcessor.JFCALCExpErrException {
		String strLowerCaseName = strName.toLowerCase(Locale.US);
		Variable var = null;
		if (strLowerCaseName.equals("null"))	{
			var = new Variable(strLowerCaseName);
		} else if (strLowerCaseName.equals("true"))	{
			var = new Variable(strLowerCaseName);
			BaseData.DataClass datumValue = new BaseData.DataClass();
			datumValue.setDataValue(MFPNumeric.TRUE, BaseData.DATATYPES.DATUM_BOOLEAN);
			var.setValue(datumValue);
		} else if (strLowerCaseName.equals("false"))	{
			var = new Variable(strLowerCaseName);
			BaseData.DataClass datumValue = new BaseData.DataClass();
			datumValue.setDataValue(MFPNumeric.FALSE, BaseData.DATATYPES.DATUM_BOOLEAN);
			var.setValue(datumValue);
		} else if (strLowerCaseName.equals("pi"))	{
			var = new Variable(strLowerCaseName);
			BaseData.DataClass datumValue = new BaseData.DataClass();
			// The accuracy of Math.PI is not high enough because it is a double
			datumValue.setDataValue(MFPNumeric.PI, BaseData.DATATYPES.DATUM_DOUBLE);
			var.setValue(datumValue);
		} else if (strLowerCaseName.equals("e"))	{
			var = new Variable(strLowerCaseName);
			BaseData.DataClass datumValue = new BaseData.DataClass();
			// The accuracy of Math.E is not high enough because it is a double
			datumValue.setDataValue(MFPNumeric.E, BaseData.DATATYPES.DATUM_DOUBLE);
			var.setValue(datumValue);
		} else if (strLowerCaseName.equals("inf"))	{
			var = new Variable(strLowerCaseName);
			BaseData.DataClass datumValue = new BaseData.DataClass();
			datumValue.setDataValue(MFPNumeric.INF, BaseData.DATATYPES.DATUM_DOUBLE);
			var.setValue(datumValue);
		} else if (strLowerCaseName.equals("infi"))	{
			var = new Variable(strLowerCaseName);
			BaseData.DataClass datumValue = new BaseData.DataClass();
			datumValue.setComplex(MFPNumeric.ZERO, MFPNumeric.INF);
			var.setValue(datumValue);
		} else if (strLowerCaseName.equals("nan"))	{
			var = new Variable(strLowerCaseName);
			BaseData.DataClass datumValue = new BaseData.DataClass();
			datumValue.setDataValue(MFPNumeric.NAN, BaseData.DATATYPES.DATUM_DOUBLE);
			var.setValue(datumValue);
		} else if (strLowerCaseName.equals("nani"))	{
			var = new Variable(strLowerCaseName);
			BaseData.DataClass datumValue = new BaseData.DataClass();
			datumValue.setComplex(MFPNumeric.ZERO, MFPNumeric.NAN);
			var.setValue(datumValue);
		}
		return var;
	}
	
	public static BaseData.DataClass lookUpPreDefined4Value(String strName) throws ErrProcessor.JFCALCExpErrException {
		Variable v = lookUpPreDefined(strName);
		if (v == null)
			return null;
		else
			return v.getValue();
	}
	
	public static Variable lookUpList(String strName, LinkedList<Variable> lVars)	{
		ListIterator<Variable> itr = lVars.listIterator();
		String strLowerCaseName = strName.toLowerCase(Locale.US);
		while (itr.hasNext())	{
			Variable var = itr.next();
			if(var.getName().equals(strLowerCaseName))	{
				return var;
			}
		}
		return null;
	}
	public static BaseData.DataClass lookUpList4Value(String strName, LinkedList<Variable> lVars)	{
		Variable v = lookUpList(strName, lVars);
		if (v == null)
			return null;
		else
			return v.getValue();
	}
	public static Variable lookUpSpaces(String strName, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException {
		ListIterator<LinkedList<Variable>> itr = lVarNameSpaces.listIterator();
		while (itr.hasNext())	{
			Variable var = lookUpList(strName, itr.next());
			if(var != null)	{
				return var;
			}
		}
		return lookUpPreDefined(strName);	// if we cannot find the variable in our defined variable space,
											// look up the predefined variable space.
	}
	public static BaseData.DataClass lookUpSpaces4Value(String strName, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException {
		Variable v = lookUpSpaces(strName, lVarNameSpaces);
		if (v == null)
			return null;
		else
			return v.getValue();
	}
	public static BaseData.DataClass setValueInList(LinkedList<Variable> lVars, String strName, BaseData.DataClass datumValue)	{
		Variable v = lookUpList(strName, lVars);
		if (v != null)	{
			v.setValue(datumValue);
			return datumValue;
		} else	{
			return null;
		}
	}
	public static BaseData.DataClass setValueInSpaces(LinkedList<LinkedList<Variable>> lVarNameSpaces, String strName, BaseData.DataClass datumValue) throws ErrProcessor.JFCALCExpErrException {
		Variable v = lookUpSpaces(strName, lVarNameSpaces);
		if (v != null)	{
			v.setValue(datumValue);
			return datumValue;
		} else	{
			return null;
		}
	}
    
    public static LinkedList<Variable> cloneVarList(LinkedList<Variable> listVars) throws ErrProcessor.JFCALCExpErrException {
        LinkedList<Variable> listVarsNew = new LinkedList<Variable>();
        for (int idx = 0; idx < listVars.size(); idx ++)    {
            if (listVars.get(idx) instanceof UnknownVarOperator.UnknownVariable) {
                UnknownVarOperator.UnknownVariable var = new UnknownVarOperator.UnknownVariable(listVars.get(idx).getName());
                if (((UnknownVarOperator.UnknownVariable)listVars.get(idx)).isValueAssigned()) {
                    BaseData.DataClass datumValue = new BaseData.DataClass();
                    datumValue.copyTypeValueDeep(listVars.get(idx).getValue());
                    var.setValue(datumValue);
                }
                listVarsNew.add(var);
            } else {    //Variable
                Variable var = new Variable(listVars.get(idx).getName());
                BaseData.DataClass datumValue = new BaseData.DataClass();
                datumValue.copyTypeValueDeep(listVars.get(idx).getValue());
                var.setValue(datumValue);
                listVarsNew.add(var);
            }
        }
        return listVarsNew;
    }
    
    public static LinkedList<LinkedList<Variable>> cloneVarSpaces(LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException {
        LinkedList<LinkedList<Variable>> lVarNameSpacesNew = new LinkedList<LinkedList<Variable>>();
        for (int idx = 0; idx < lVarNameSpaces.size(); idx ++)  {
            lVarNameSpacesNew.add(cloneVarList(lVarNameSpaces.get(idx)));
        }
        return lVarNameSpacesNew;
    }
}
