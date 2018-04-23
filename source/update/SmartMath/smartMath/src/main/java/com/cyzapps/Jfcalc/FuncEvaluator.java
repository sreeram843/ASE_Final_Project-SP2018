package com.cyzapps.Jfcalc;

import java.io.IOException;
import java.util.*;

import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEFunction;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.ExprAnalyzer;

import com.cyzapps.Jsma.PatternManager;
import com.cyzapps.Jsma.SMErrProcessor;
import com.cyzapps.Jsma.UnknownVarOperator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.PatternSyntaxException;

public class FuncEvaluator    {

    public static abstract class ConsoleInputStream    {
        public abstract void doBeforeInput();
        public abstract String inputString() throws InterruptedException;
        public abstract void doAfterInput();
    }
    public static ConsoleInputStream msstreamConsoleInput = null;
    
    public static abstract class LogOutputStream {
        public abstract void outputString(String str) throws InterruptedException;
    }
    public static LogOutputStream msstreamLogOutput = null;

    public static abstract class FunctionInterrupter    {
        public abstract boolean shouldInterrupt();
        public abstract void interrupt() throws InterruptedException;        
    }
    public static FunctionInterrupter msfunctionInterrupter = null;

    public static abstract class GraphPlotter    {
        public abstract boolean plotGraph(String strGraphInfo);
    }
    public static GraphPlotter msgraphPlotter = null;
    public static GraphPlotter msgraphPlotter3D = null;
    
    public static PatternManager mspm = null;
    
    public static abstract class FileOperator    {
        public abstract boolean outputGraphFile(String strFileName, String strFileContent) throws IOException;
    }
    public static FileOperator msfileOperator = null;
    
    public static boolean isAExprDatumFunction(String strName, int nParamNum) {
        return (nParamNum == 1) && (strName.compareToIgnoreCase("is_aexpr_datum") == 0
                                || strName.compareToIgnoreCase("get_boolean_aexpr_true") == 0
                                || strName.compareToIgnoreCase("get_boolean_aexpr_false") == 0);
    }
    
    public static BaseData.DataClass evaluateFunction(String strName, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        boolean bHasAExprData = false;
        for (int idx = 0; idx < tsParameter.size(); idx ++) {
            if (tsParameter.get(idx).getDataType() == BaseData.DATATYPES.DATUM_ABSTRACT_EXPR)  {
                bHasAExprData = true;
            }
        }
        
        if (bHasAExprData && !isAExprDatumFunction(strName, tsParameter.size())) {    // this is evaluate aexpr.
            LinkedList<AbstractExpr> listFuncChildren = new LinkedList<AbstractExpr>();
            try {
                for (int idx = 0; idx < tsParameter.size(); idx ++) {
                    if (tsParameter.get(idx).getDataType() == BaseData.DATATYPES.DATUM_ABSTRACT_EXPR)  {
                        listFuncChildren.addFirst(tsParameter.get(idx).getAExpr()); // the order of the parameters is from last to first, so need addFirst instead of add.
                    } else {
                        BaseData.DataClass datumChild = new BaseData.DataClass();
                        datumChild.copyTypeValue(tsParameter.get(idx));
                        listFuncChildren.addFirst(new AEConst(datumChild)); // the order of the parameters is from last to first, so need addFirst instead of add.
                    }
                }
                return new BaseData.DataClass(new AEFunction(strName, listFuncChildren));
            } catch (SMErrProcessor.JSmartMathErrException e) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_GET_RESULT);
            }
        }
        
        if (msfunctionInterrupter != null)    {
            // for debug or killing a background thread.
            if (msfunctionInterrupter.shouldInterrupt())    {
                msfunctionInterrupter.interrupt();
            }
        }
        
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        String strNameLowCase = strName.toLowerCase(Locale.US);
        /* the follows are internal functions */
        if ( strNameLowCase.compareTo("rand") == 0)    /* rand */
        {
            datumReturnNum = rand_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("ceil") == 0)    /* ceil */
        {
            datumReturnNum = ceil_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("floor") == 0)    /* floor */
        {
            datumReturnNum = floor_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("round") == 0)    /* round */
        {
            datumReturnNum = round_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("and") == 0)    /* logic and */
        {
            datumReturnNum = and_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("or") == 0)    /* logic or */
        {
            datumReturnNum = or_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("sin") == 0)    /* sin */
        {
            datumReturnNum = sin_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("cos") == 0)    /* cos */
        {
            datumReturnNum = cos_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("tan") == 0)    /* tan */
        {
            datumReturnNum = tan_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("asin") == 0)    /* arcsin */
        {
            datumReturnNum = asin_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("acos") == 0)    /* arccos */
        {
            datumReturnNum = acos_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("atan") == 0)    /* arctan */
        {
            datumReturnNum = atan_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("log") == 0)    /* logE */
        {
            datumReturnNum = log_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("exp") == 0)    /* exp */
        {
            datumReturnNum = exp_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("real") == 0)    /* real */
        {
            datumReturnNum = real_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("image") == 0)    /* image */
        {
            datumReturnNum = image_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("abs") == 0)    /* abs */
        {
            datumReturnNum = abs_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("angle") == 0)    /* angle */
        {
            datumReturnNum = angle_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("mod") == 0)    /* mod */
        {
            datumReturnNum = mod_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("conv_bin_to_dec") == 0
                || strNameLowCase.compareTo("conv_oct_to_dec") == 0
                || strNameLowCase.compareTo("conv_hex_to_dec") == 0
                || strNameLowCase.compareTo("conv_dec_to_bin") == 0
                || strNameLowCase.compareTo("conv_dec_to_oct") == 0
                || strNameLowCase.compareTo("conv_dec_to_hex") == 0 
                || strNameLowCase.compareTo("conv_bin_to_hex") == 0
                || strNameLowCase.compareTo("conv_hex_to_bin") == 0
                || strNameLowCase.compareTo("conv_bin_to_oct") == 0
                || strNameLowCase.compareTo("conv_oct_to_bin") == 0
                || strNameLowCase.compareTo("conv_oct_to_hex") == 0
                || strNameLowCase.compareTo("conv_hex_to_oct") == 0)
        {
            datumReturnNum = conv_bin_dec_hex_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("pow") == 0)    /* pow */
        {
            datumReturnNum = pow_Function(tsParameter, lVarNameSpaces);
            
        }
        else if ( strNameLowCase.compareTo("copy_file") == 0)    /* copy file or folder */
        {
            datumReturnNum = IOLib.copyFile(tsParameter);
        }
        else if ( strNameLowCase.compareTo("move_file") == 0)    /* move file or folder */
        {
            datumReturnNum = IOLib.moveFile(tsParameter);
        }
        else if ( strNameLowCase.compareTo("create_file") == 0)    /* create file or folder */
        {
            datumReturnNum = IOLib.createFile(tsParameter);
        }
        else if ( strNameLowCase.compareTo("delete_file") == 0)    /* delete file or folder */
        {
            datumReturnNum = IOLib.deleteFile(tsParameter);
        }
        else if ( strNameLowCase.compareTo("list_files") == 0)    /* list files in a folder */
        {
            datumReturnNum = IOLib.listFiles(tsParameter);
        }
        else if ( strNameLowCase.compareTo("print_file_list") == 0 || strNameLowCase.compareTo("ls") == 0 || strNameLowCase.compareTo("dir") == 0)    /* list files in a folder */
        {
            datumReturnNum = print_file_list_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("is_file_existing") == 0)    /* does the file exist? */
        {
            datumReturnNum = IOLib.isExistingFile(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_directory") == 0)    /* is the file actually directory */
        {
            datumReturnNum = IOLib.isDirectory(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_file_executable") == 0)    /* is the file executable */
        {
            datumReturnNum = IOLib.isExecutable(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_file_hidden") == 0)    /* is the file hidden */
        {
            datumReturnNum = IOLib.isHidden(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_file_readable") == 0)    /* is the file readable */
        {
            datumReturnNum = IOLib.isReadable(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_file_writable") == 0)    /* is the file writable */
        {
            datumReturnNum = IOLib.isWritable(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_file_normal") == 0)    /* is the file a regular file */
        {
            datumReturnNum = IOLib.isNormalFile(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_symbol_link") == 0)    /* is the file a symbol link */
        {
            datumReturnNum = IOLib.isSymbolLink(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_path_absolute") == 0)    /* is the path is absolute, file can be non-existing */
        {
            datumReturnNum = IOLib.isAbsolutePath(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_path_parent") == 0)    /* is path1 parent of path2? */
        {
            datumReturnNum = IOLib.isParentPath(tsParameter);
        }
        else if ( strNameLowCase.compareTo("is_path_same") == 0)    /* is the file same as another file */
        {
            datumReturnNum = IOLib.comparePath(tsParameter);
            if (datumReturnNum.getDataValue().intValue() == 0) {
                datumReturnNum = new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.TRUE);
            } else {
                datumReturnNum = new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.FALSE);
            }
        }
        else if ( strNameLowCase.compareTo("get_file_separator") == 0)    /* get file path separator */
        {
            if (tsParameter.size() != 0) throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
            datumReturnNum = new BaseData.DataClass(BaseData.DATATYPES.DATUM_STRING, MFPAdapter.STRING_PATH_DIVISOR);
        }
        else if ( strNameLowCase.compareTo("get_file_path") == 0)    /* get file path */
        {
            datumReturnNum = IOLib.getPath(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_absolute_path") == 0)    /* get absolute path */
        {
            datumReturnNum = IOLib.getAbsolutePath(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_canonical_path") == 0)    /* get canonical path */
        {
            datumReturnNum = IOLib.getCanonicalPath(tsParameter);
        }
        else if ( strNameLowCase.compareTo("change_dir") == 0 || strNameLowCase.compareTo("cd") == 0)    /* change working dir */
        {
            datumReturnNum = IOLib.changeDir(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_working_dir") == 0 || strNameLowCase.compareTo("pwd") == 0)    /* change working dir */
        {
            datumReturnNum = IOLib.getWorkingDir(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_file_size") == 0)    /* get file size */
        {
            datumReturnNum = IOLib.getFileSize(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_file_last_modified_time") == 0)    /* get file last modified time */
        {
            datumReturnNum = IOLib.getFileLastModifiedTime(tsParameter);
        }
        else if ( strNameLowCase.compareTo("set_file_last_modified_time") == 0)    /* set file last modified time */
        {
            datumReturnNum = IOLib.setFileLastModifiedTime(tsParameter);
        }
        else if ( strNameLowCase.compareTo("fopen") == 0)   /* fopen */
        {
            datumReturnNum = IOLib.fOpen(tsParameter);
        }
        else if ( strNameLowCase.compareTo("fclose") == 0)   /* fclose */
        {
            datumReturnNum = IOLib.fClose(tsParameter);
        }
        else if ( strNameLowCase.compareTo("feof") == 0)   /* feof */
        {
            datumReturnNum = IOLib.fEof(tsParameter);
        }
        else if ( strNameLowCase.compareTo("fread") == 0)   /* fread */
        {
            datumReturnNum = IOLib.fRead(tsParameter);
        }
        else if ( strNameLowCase.compareTo("fwrite") == 0)   /* fwrite */
        {
            IOLib.fWrite(tsParameter);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("input") == 0)    /* input */
        {
            datumReturnNum = input_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("pause") == 0)    /* pause */
        {
            datumReturnNum = pause_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("scanf") == 0)    /* scanf */
        {
            datumReturnNum = scanf_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("sscanf") == 0 || strNameLowCase.compareTo("fscanf") == 0)   /* sscanf and fscanf*/
        {
            datumReturnNum = IOLib.sfScanf(tsParameter);
        }
        else if ( strNameLowCase.compareTo("freadline") == 0)   /* read line*/
        {
            datumReturnNum = IOLib.fReadLine(tsParameter);
        }
        else if ( strNameLowCase.compareTo("print") == 0)    /* print */
        {
            datumReturnNum = print_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("sprintf") == 0)   /* sprintf */
        {
            String strOutput = IOLib.sPrintf(tsParameter);
            datumReturnNum = new BaseData.DataClass(BaseData.DATATYPES.DATUM_STRING, strOutput);
        }
        else if ( strNameLowCase.compareTo("printf") == 0)   /* printf */
        {
            datumReturnNum = printf_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("fprintf") == 0)   /* fprintf */
        {
            IOLib.fPrintf(tsParameter);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("now") == 0)   /* now */
        {
            datumReturnNum = MFPDateTime.getNowTS(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_time_stamp") == 0)   /* get time stamp from a string or yyyy mm dd hh mm ss */
        {
            datumReturnNum = MFPDateTime.getTS(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_year") == 0)   /* get year */
        {
            datumReturnNum = MFPDateTime.getYear(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_month") == 0)   /* get month */
        {
            datumReturnNum = MFPDateTime.getMonth(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_day_of_year") == 0)   /* get day of year */
        {
            datumReturnNum = MFPDateTime.getDayOfYear(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_day_of_month") == 0)   /* get day of month */
        {
            datumReturnNum = MFPDateTime.getDayOfMonth(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_day_of_week") == 0)   /* get day of week */
        {
            datumReturnNum = MFPDateTime.getDayOfWeek(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_hour") == 0)   /* get hour */
        {
            datumReturnNum = MFPDateTime.getHour(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_minute") == 0)   /* get minute */
        {
            datumReturnNum = MFPDateTime.getMinute(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_second") == 0)   /* get second */
        {
            datumReturnNum = MFPDateTime.getSecond(tsParameter);
        }
        else if ( strNameLowCase.compareTo("get_millisecond") == 0)   /* get millisecond */
        {
            datumReturnNum = MFPDateTime.getMilliSecond(tsParameter);
        }
        else if ( strNameLowCase.compareTo("clone") == 0)    /* clone array or anything else */
        {
            datumReturnNum = clone_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("alloc_array") == 0)    /* alloc array */
        {
            datumReturnNum = alloc_array_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("eye") == 0)    /* alloc an I array */
        {
            datumReturnNum = eye_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("ones") == 0 || strNameLowCase.compareTo("zeros") == 0 )    /* alloc an zeros or ones array */
        {
            datumReturnNum = ones_zeros_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("includes_nan_or_inf_or_null") == 0 || strNameLowCase.compareTo("includes_nan_or_inf") == 0
                || strNameLowCase.compareTo("includes_nan") == 0 || strNameLowCase.compareTo("includes_inf") == 0|| strNameLowCase.compareTo("includes_null") == 0)
        {
            datumReturnNum = includes_nan_inf_null_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("is_nan_or_inf") == 0 || strNameLowCase.compareTo("is_nan_or_inf_or_null") == 0
                || strNameLowCase.compareTo("is_inf") == 0)
        {
            datumReturnNum = is_nan_inf_null_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("is_aexpr_datum") == 0)
        {
            datumReturnNum = is_aexpr_datum_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("get_boolean_aexpr_true") == 0)
        {
            datumReturnNum = get_boolean_aexpr_true_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("get_boolean_aexpr_false") == 0)
        {
            datumReturnNum = get_boolean_aexpr_false_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("is_eye") == 0 || strNameLowCase.compareTo("is_zeros") == 0 )    /* is eye (zeros) or not */
        {
            datumReturnNum = is_eye_zeros_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("size") == 0)    /* size */
        {
            datumReturnNum = size_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("set_array_elem") == 0)    /* set array element */
        {
            datumReturnNum = set_array_elem_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("recip") == 0)    // reciprocal of 2D matrix or a number
        {
            datumReturnNum = recip_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("left_recip") == 0)    // left reciprocal of 2D matrix or a number
        {
            datumReturnNum = left_recip_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("eig") == 0 || strNameLowCase.compareTo("get_eigen_values") == 0)
        {
            datumReturnNum = eigen_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("deter") == 0 || strNameLowCase.compareTo("det") == 0)    /* calculate determinant of a 2D square array */
        {
            datumReturnNum = deter_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("rank") == 0)    /* calculate determinant of a 2D square array */
        {
            datumReturnNum = rank_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("upper_triangular_matrix") == 0)    /* calculate determinant of a 2D square array */
        {
            datumReturnNum = upper_triangular_matrix_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("invert") == 0)    /* invert a 2D array */
        {
            datumReturnNum = invert_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("roots_internal") == 0)   /* calculate roots by Java code, this function interface is not released to user */
        {
            datumReturnNum = roots_internal_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("get_continous_root") == 0) /* this function get the continous root from root list */
        {
            datumReturnNum = get_continous_root_Function(tsParameter, lVarNameSpaces);
        }
        else if ( strNameLowCase.compareTo("plot_2d_curves") == 0)    /* plot 2d curves, this function interface is not released to user */
        {
            PlotLib.plot2DCurves(strName, "", tsParameter, lVarNameSpaces, msgraphPlotter, msfileOperator);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("plot_multi_xy") == 0)    /* plot multiple xy */
        {
            /*
             * plot_multi_xy has the following functions:
             * 1. plotted chart name;
             * 2. plotted chart settings (a string);
             * 3. plotted curve 1, curve 1 settings (a string)
             * 4. plotted curve 1, x;
             * 5. plotted curve 1, y;
             * ...
             * This function supports at most 8 curves
             */
            PlotLib.plotMultiXY(strName, tsParameter, msgraphPlotter, msfileOperator);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("plot_2d_expressions") == 0)    /* plot 2d expressions */
        {
            PlotLib.plot2DExprCurves(strName, "", tsParameter, msgraphPlotter, msfileOperator);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("plot_polar_curves") == 0)    /* plot polar curves, this function interface is not released to user */
        {
            PlotLib.plot2DCurves(strName, "multiRangle", tsParameter, lVarNameSpaces, msgraphPlotter, msfileOperator);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("plot_multi_rangle") == 0)    /* plot multiple r-angle */
        {
            PlotLib.plotMultiXY(strName, tsParameter, msgraphPlotter, msfileOperator);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("plot_polar_expressions") == 0)    /* plot polar expressions */
        {
            PlotLib.plot2DExprCurves(strName, "polarExpr", tsParameter, msgraphPlotter, msfileOperator);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("plot_3d_surfaces") == 0)    /* plot 3d surfaces, this function interface is not released to user */
        {
            PlotLib.plot3DSurfaces(strName, tsParameter, lVarNameSpaces, msgraphPlotter3D, msfileOperator);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("plot_multi_xyz") == 0)    /* plot multiple xyz */
        {
            PlotLib.plotMultiXYZ(strName, tsParameter, msgraphPlotter3D, msfileOperator);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("plot_3d_expressions") == 0)    /* plot 3d expressions */
        {
            PlotLib.plot3DExprSurfaces(strName, tsParameter, msgraphPlotter3D, msfileOperator);
            datumReturnNum = null;
        }
        else if ( strNameLowCase.compareTo("plot_expressions") == 0)    /* plot 2d or 3d expressions */
        {
            PlotLib.plotExprs(strName, tsParameter, msgraphPlotter, msgraphPlotter3D, msfileOperator);
            datumReturnNum = null;
        }
        else if (strNameLowCase.compareTo("sum_over") == 0 /* sum_over or SIGMA */
                || strNameLowCase.compareTo("product_over") == 0) /* product_over or PI */
        {
            datumReturnNum = sum_product_over_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("integrate") == 0)    /* integrate */
        {
            // adaptively select integration method
            datumReturnNum = integrate_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("integ_gk") == 0)    /* integrate */
        {
            datumReturnNum = integ_GK_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("integ_basic") == 0)    /* integrate */
        {
            datumReturnNum = integ_Basic_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("lim") == 0)    /* evaluate a lim expression */
        {
            datumReturnNum = lim_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("evaluate") == 0)    /* evaluate a string expression */
        {
            datumReturnNum = evaluate_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("system") == 0)
        {
            datumReturnNum = system_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("sleep") == 0)
        {
            datumReturnNum = sleep_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("conv_str_to_ints") == 0)    /* convert a string to an int array */
        {
            datumReturnNum = conv_str_to_ints_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("conv_ints_to_str") == 0)    /* convert an int array or a single int to a string */
        {
            datumReturnNum = conv_ints_to_str_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("strlen") == 0)    /* string length */
        {
            datumReturnNum = strlen_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("strcpy") == 0)    /* string copy, string index must be integer so convert MFPNumeric to long then to int */
        {
            datumReturnNum = strcpy_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("strcat") == 0)    /* string catenate */
        {
            datumReturnNum = strcat_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("strcmp") == 0 /* string compare, string index must be integer so convert MFPNumeric to long then to int */
                || strNameLowCase.compareTo("stricmp") == 0)     /* string case insensitive compare */
        {
            datumReturnNum = strcmp_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("strsub") == 0)    /* sub-string, string index must be integer so convert MFPNumeric to long then to int */
        {
            datumReturnNum = strsub_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("tostring") == 0
                || strNameLowCase.compareTo("to_string") == 0    /* convert to string */
                || strNameLowCase.compareTo("to_lowercase_string") == 0  /* to lower case string */
                || strNameLowCase.compareTo("to_uppercase_string") == 0) /* to upper case string */
        {
            datumReturnNum = to_string_Function(strNameLowCase, tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("expr_to_string") == 0)    /* convert an expr to string for testing purpose */
        {
            datumReturnNum = expr_to_string_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("trim") == 0)
        {
            datumReturnNum = trim_string_Function(0, tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("trim_left") == 0)
        {
            datumReturnNum = trim_string_Function(-1, tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("trim_right") == 0)
        {
            datumReturnNum = trim_string_Function(1, tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("split") == 0)
        {
            datumReturnNum = split_string_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("get_num_of_results_sets") == 0)    /* get number of solved result sets*/
        {
            datumReturnNum = get_num_of_results_sets_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("get_solved_results_set") == 0)    /* get one solved results set */
        {
            datumReturnNum = get_solved_results_set_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("get_variable_results") == 0)    /* get all results of a variable */
        {
            datumReturnNum = get_variable_results_Function(tsParameter, lVarNameSpaces);
        }
        else if (strNameLowCase.compareTo("iff") == 0)    /* if (condition1, trueresult1, condition2, trueresult2, ... falseresult) */
        {
            datumReturnNum = iff_Function(tsParameter, lVarNameSpaces);
        }
        else  /* call functions defined by users*/
        {
            LinkedList<MFPAdapter.FunctionEntry> lFunctionSpace = MFPAdapter.m_slFunctionSpace;
            ListIterator<MFPAdapter.FunctionEntry> itrFE = lFunctionSpace.listIterator();
    
            boolean bFunctionFound = false;
            while (itrFE.hasNext())    {
                MFPAdapter.FunctionEntry fe = itrFE.next();
                if (fe.matchFunction(strNameLowCase, tsParameter.size()))    {
                    // get it.
                    bFunctionFound = true;
                    ScriptAnalyzer sa = new ScriptAnalyzer();
                    Statement sCurrent = fe.m_sLines[fe.m_nStartStatementPos];
                    LinkedList<Variable> lParams = new LinkedList<Variable>();
                    ListIterator<BaseData.DataClass> itrDatum = tsParameter.listIterator(tsParameter.size());
                    int nVariableIndex = 0;
                    /*
                     * note that the parameter values in tsParameter are pushed in (stack) while the parameter
                     * variable names in fe.m_sf.m_strParams are appended in (queue).
                     */
                    BaseData.DataClass datumValue;
                    while(itrDatum.hasPrevious())    {
                        if (!(fe.m_sf.m_bIncludeOptParam) || nVariableIndex <= (fe.m_sf.m_strParams.length - 2))    {
                            datumValue = new BaseData.DataClass();    // avoid change of parameter inside a function.
                            // we do not use deep copy here because reference type parameter should be able to change inside a function
                            datumValue.copyTypeValue(itrDatum.previous());
                        } else    {
                            // the optional parameters.
                            BaseData.DataClass[] dataList = new BaseData.DataClass[itrDatum.previousIndex() + 1];
                            int nOptVarIndex = 0;
                            while(itrDatum.hasPrevious())    {
                                dataList[nOptVarIndex] = new BaseData.DataClass();
                                // avoid change of parameter (except reference type parameter) inside a function.
                                dataList[nOptVarIndex].copyTypeValue(itrDatum.previous());
                                nOptVarIndex ++;
                            }
                            datumValue = new BaseData.DataClass(dataList);
                        }
                        Variable var = new Variable(fe.m_sf.m_strParams[nVariableIndex], datumValue);
                        lParams.addLast(var);
                        nVariableIndex ++;
                    }
                    if (fe.m_sf.m_bIncludeOptParam && nVariableIndex == fe.m_sf.m_strParams.length - 1)    {
                        // opt arg list is empty
                        BaseData.DataClass[] dataList = new BaseData.DataClass[0];
                        datumValue = new BaseData.DataClass(dataList);
                        Variable var = new Variable(fe.m_sf.m_strParams[nVariableIndex], datumValue);
                        lParams.addLast(var);
                    }
                    try {
                        sCurrent.analyze();
                        // a function should not be able to read namespaces outside.
                        sa.analyzeBlock(fe.m_sLines, fe.m_nStartStatementPos, lParams, new LinkedList<LinkedList<Variable>>());
                    } catch(ScriptAnalyzer.FuncRetException e)    {
                        datumReturnNum = e.m_datumReturn;
                        break;
                    } catch(ScriptAnalyzer.ScriptStatementException e)    {
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                                strName, e);
                    } catch(JMFPCompErrException e)    {
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                                strName, e);
                    } catch(Exception e)    {
                        // unexcepted exception
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION,
                                strName);   // unexcepted exception does not append to lower level
                    }
                    break;
                }
            }
            if (bFunctionFound == false)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_UNDEFINED_FUNCTION);
            }
        }
        return datumReturnNum;
    }

    public static BaseData.DataClass rand_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* rand */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 0)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        Random rn = new Random();
        datumReturnNum.setDataValue(new MFPNumeric(rn.nextDouble()), BaseData.DATATYPES.DATUM_DOUBLE);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass ceil_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* ceil */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() > 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        int nScale = 0;
        if (tsParameter.size() == 2)    {
            BaseData.DataClass dsScale = new BaseData.DataClass();
            dsScale.copyTypeValueDeep(tsParameter.poll());
            dsScale.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            nScale = (int)dsScale.getDataValue().longValue();
            if (nScale < 0 || nScale == Long.MAX_VALUE)    {   //nScale == Long.MAX_VALUE means overflow.
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
        }
        BaseData.DataClass dsTmp = new BaseData.DataClass();
        dsTmp.copyTypeValueDeep(tsParameter.poll());    // make sure parameter variables not changed inside function
        dsTmp.changeDataType(BaseData.DATATYPES.DATUM_DOUBLE);
        datumReturnNum.setDataValue(dsTmp.getDataValue().setScale(nScale, MFPNumeric.ROUND_CEILING), BaseData.DATATYPES.DATUM_DOUBLE);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass floor_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* floor */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() > 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        int nScale = 0;
        if (tsParameter.size() == 2)    {
            BaseData.DataClass dsScale = new BaseData.DataClass();
            dsScale.copyTypeValueDeep(tsParameter.poll());
            dsScale.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            nScale = (int)dsScale.getDataValue().longValue();
            if (nScale < 0 || nScale == Long.MAX_VALUE)    {   //nScale == Long.MAX_VALUE means overflow.
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
        }
        BaseData.DataClass dsTmp = new BaseData.DataClass();
        dsTmp.copyTypeValueDeep(tsParameter.poll());    // make sure parameter variables not changed inside function
        dsTmp.changeDataType(BaseData.DATATYPES.DATUM_DOUBLE);
        datumReturnNum.setDataValue(dsTmp.getDataValue().setScale(nScale, MFPNumeric.ROUND_FLOOR), BaseData.DATATYPES.DATUM_DOUBLE);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass round_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* round */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() > 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        int nScale = 0;
        if (tsParameter.size() == 2)    {
            BaseData.DataClass dsScale = new BaseData.DataClass();
            dsScale.copyTypeValueDeep(tsParameter.poll());
            dsScale.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            nScale = (int)dsScale.getDataValue().longValue();
            if (nScale < 0 || nScale == Long.MAX_VALUE)    {   //nScale == Long.MAX_VALUE means overflow.
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
        }
        BaseData.DataClass dsTmp = new BaseData.DataClass();
        dsTmp.copyTypeValueDeep(tsParameter.poll());    // make sure parameter variables not changed inside function
        dsTmp.changeDataType(BaseData.DATATYPES.DATUM_DOUBLE);
        datumReturnNum.setDataValue(dsTmp.getDataValue().setScale(nScale, MFPNumeric.ROUND_HALF_UP), BaseData.DATATYPES.DATUM_DOUBLE);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass and_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* logic and */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() == 0)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        datumReturnNum.setDataValue(MFPNumeric.ONE, BaseData.DATATYPES.DATUM_BOOLEAN);
        do
        {
            BaseData.DataClass dsTmp = new BaseData.DataClass();
            dsTmp.copyTypeValueDeep(tsParameter.removeLast());    //process the parameters from first to last
            dsTmp.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);
            if (dsTmp.getDataValue().isActuallyZero())    {
                datumReturnNum.setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_BOOLEAN);
                break;
            }
        } while (tsParameter.size() != 0);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass or_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* logic or */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() == 0)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        datumReturnNum.setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_BOOLEAN);
        do
        {
            BaseData.DataClass dsTmp = new BaseData.DataClass();
            dsTmp.copyTypeValueDeep(tsParameter.removeLast());    //process the parameters from first to last
            dsTmp.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);  // after change data type to boolean, long value should be 1 or 0.
            if (dsTmp.getDataValue().isTrue())    {   // need not to use is actually true here.
                datumReturnNum.setDataValue(MFPNumeric.TRUE, BaseData.DATATYPES.DATUM_BOOLEAN);
                break;
            }
        } while (tsParameter.size() != 0);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass sin_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* sin */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        // doesn't support complex yet.
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        // need not to copy type and value here because inside evaluateSin parameter will not be changed.
        datumReturnNum = BuiltinProcedures.evaluateSin(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass cos_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* cos */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        // need not to copy type and value here because inside evaluateCos parameter will not be changed.
        datumReturnNum = BuiltinProcedures.evaluateCos(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass tan_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* tan */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        // need not to copy type and value here because inside evaluateTan parameter will not be changed.
        datumReturnNum = BuiltinProcedures.evaluateTan(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass asin_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* arcsin */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        // need not to copy type and value here because inside evaluateASin parameter will not be changed.
        datumReturnNum = BuiltinProcedures.evaluateASin(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass acos_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* arccos */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        // need not to copy type and value here because inside evaluateACos parameter will not be changed.
        datumReturnNum = BuiltinProcedures.evaluateACos(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass atan_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* arctan */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        // need not to copy type and value here because inside evaluateATan parameter will not be changed.
        datumReturnNum = BuiltinProcedures.evaluateATan(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass log_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* logE */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        // need not to copy type and value here because inside evaluateLog parameter will not be changed.
        datumReturnNum = BuiltinProcedures.evaluateLog(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass exp_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* exp */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        // need not to copy type and value here because inside evaluateExp parameter will not be changed.
        datumReturnNum = BuiltinProcedures.evaluateExp(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass real_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* real */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass dsTmp = new BaseData.DataClass();
        dsTmp.copyTypeValueDeep(tsParameter.poll());
        datumReturnNum = dsTmp.getRealDataClass();            
        return datumReturnNum;
    }
    
    public static BaseData.DataClass image_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* image */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1 && tsParameter.size() != 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        boolean bReturnI = false;
        if (tsParameter.size() == 2) {
            // now determine return an image or a real value
            BaseData.DataClass dsReturnType = new BaseData.DataClass();
            dsReturnType.copyTypeValueDeep(tsParameter.poll());
            dsReturnType.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);
            bReturnI = dsReturnType.getDataValue().booleanValue();
        }
        BaseData.DataClass dsTmp = new BaseData.DataClass();
        dsTmp.copyTypeValueDeep(tsParameter.poll());
        if (bReturnI) {
            // we need to return something like 3*i
            BaseData.DataClass dsZero = new BaseData.DataClass(BaseData.DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
            dsTmp.setReal(dsZero);
            return dsTmp;
        } else {
            datumReturnNum = dsTmp.getImageDataClass();
            return datumReturnNum;
        }
    }
    
    public static BaseData.DataClass abs_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* abs */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        datumReturnNum = BuiltinProcedures.evaluateAbs(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass angle_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* angle */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass dsTmp = new BaseData.DataClass();
        dsTmp.copyTypeValueDeep(tsParameter.poll());
        MFPNumeric[] mfpNumRadAng = dsTmp.getComplexRadAngle();
        datumReturnNum.setDataValue(mfpNumRadAng[1]);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass mod_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* mod */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass dsTmp1 = new BaseData.DataClass();
        dsTmp1.copyTypeValueDeep(tsParameter.poll());
        dsTmp1.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);    /* cast to integer */
        BaseData.DataClass dsTmp2 = new BaseData.DataClass();
        dsTmp2.copyTypeValueDeep(tsParameter.poll());
        dsTmp2.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);    /* cast to integer */
        if (dsTmp1.getDataValue().compareTo(MFPNumeric.ZERO) <= 0)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        datumReturnNum.setDataValue(    // here dsTmp1 and dsTmp2 have been changed to MFP_INTEGER_TYPE, so can use toBigInteger() directly
            new MFPNumeric(dsTmp2.getDataValue().toBigInteger().mod(dsTmp1.getDataValue().toBigInteger())),
            BaseData.DATATYPES.DATUM_INTEGER);
        return datumReturnNum;
    }
    
    /* convert bin dec hex to another positional notation */
    public static BaseData.DataClass conv_bin_dec_hex_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        /* num2 <-> num8 <-> num10 <-> num16, support int and double */
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        int nRadixFrom = 10, nRadixTo = 16;
        if (strNameLowCase.compareTo("conv_bin_to_dec") == 0)   {
            nRadixFrom = 2;
            nRadixTo = 10;
        } else if (strNameLowCase.compareTo("conv_oct_to_dec") == 0)  {
            nRadixFrom = 8;
            nRadixTo = 10;
        } else if (strNameLowCase.compareTo("conv_hex_to_dec") == 0)  {
            nRadixFrom = 16;
            nRadixTo = 10;
        } else if (strNameLowCase.compareTo("conv_dec_to_bin") == 0)  {
            nRadixFrom = 10;
            nRadixTo = 2;
        } else if (strNameLowCase.compareTo("conv_dec_to_oct") == 0)  {
            nRadixFrom = 10;
            nRadixTo = 8;
        } else if (strNameLowCase.compareTo("conv_dec_to_hex") == 0)  {
            nRadixFrom = 10;
            nRadixTo = 16;
        } else if (strNameLowCase.compareTo("conv_bin_to_hex") == 0)  {
            nRadixFrom = 2;
            nRadixTo = 16;
        } else if (strNameLowCase.compareTo("conv_hex_to_bin") == 0)   {
            nRadixFrom = 16;
            nRadixTo = 2;
        } else if (strNameLowCase.compareTo("conv_bin_to_oct") == 0)    {
            nRadixFrom = 2;
            nRadixTo = 8;
        } else if (strNameLowCase.compareTo("conv_oct_to_bin") == 0)    {
            nRadixFrom = 8;
            nRadixTo = 2;
        } else if (strNameLowCase.compareTo("conv_oct_to_hex") == 0)   {
            nRadixFrom = 8;
            nRadixTo = 16;
        } else if (strNameLowCase.compareTo("conv_hex_to_oct") == 0)   {
            nRadixFrom = 16;
            nRadixTo = 8;
        }
        BaseData.DataClass dsTmp = new BaseData.DataClass();
        dsTmp.copyTypeValueDeep(tsParameter.poll());
        // only support 2 input data types: String or binary double
        if (dsTmp.getDataType() != BaseData.DATATYPES.DATUM_STRING)    {
            dsTmp.changeDataType(BaseData.DATATYPES.DATUM_DOUBLE);
            datumReturnNum = dsTmp;
        } else  {
            // input is a string. Note that if input is a string, we don't allow blank in the head or tail.
            String strInput = dsTmp.getStringValue();
            if (nRadixFrom == 2)    {
                strInput = "0b" + strInput;
            } else if (nRadixFrom == 8) {
                strInput = "00" + strInput; // have to use two 0s to cope with the case like .317
            } else if (nRadixFrom == 16)    {
                strInput = "0x" + strInput;
            }
            BaseData.CurPos curPos = new BaseData.CurPos();
            datumReturnNum = ElemAnalyzer.getNumber(strInput, curPos);
            if (curPos.m_nPos < strInput.length())  {   // only part of the input is a number, which is not accepted.
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_FORMAT);
            }
            datumReturnNum.changeDataType(BaseData.DATATYPES.DATUM_DOUBLE);
        }
        // now datumReturnNum is a double.
        String strOutput = "";
        if (nRadixTo == 10) {   // if return decimal, return a number value, otherwise return a string.
            if (datumReturnNum.isSingleInteger())   {
                datumReturnNum.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            }
        } else {
            if (datumReturnNum.isSingleInteger())   {
                BigInteger bigIntValue = datumReturnNum.getDataValue().toBigInteger();
                strOutput = bigIntValue.toString(nRadixTo);
            } else  {
                MFPNumeric mfpNumValue = datumReturnNum.getDataValue();
                BigInteger bigIntValue = mfpNumValue.toBigInteger();
                strOutput = bigIntValue.toString(nRadixTo) + ".";
                mfpNumValue = mfpNumValue.subtract(new MFPNumeric(bigIntValue));
                double dDenominator = 1.0;
                while (mfpNumValue.abs().isActuallyPositive())  {
                    dDenominator /= nRadixTo;
                    // cannot use MFPNumeric.divide(MFPNumeric, MFPNumeric) here because when dDenominator is very close to zero
                    // it will treated as zero.
                    //int nQuotient = (int)mfpNumValue.divide(new MFPNumeric(dDenominator)).longValue();
                    // also, avoid to use BigDecimal.divide(BigDecimal) to avoid exception.
                    int nQuotient = (int)MFPNumeric.divide(mfpNumValue.toBigDecimal(), new BigDecimal(dDenominator)).longValue();
                    if (nQuotient < 10) {
                        strOutput += nQuotient;
                    } else  {
                        strOutput += (char)('a' + nQuotient - 10);
                    }
                    mfpNumValue = mfpNumValue.subtract(new MFPNumeric(dDenominator * nQuotient));
                }
            }
            datumReturnNum.setStringValue(strOutput);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass pow_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* pow */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        int nNumofParams = tsParameter.size();
        if (nNumofParams < 2 || nNumofParams > 3)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass dsNumOfOperands = null;
        if (nNumofParams == 3)    {
            dsNumOfOperands = tsParameter.poll();
        }
        BaseData.DataClass dsPower = tsParameter.poll();
        BaseData.DataClass dsBase = tsParameter.poll();
        // need not to copy type and value here because inside evaluatePower parameters will not be changed.
        datumReturnNum = BuiltinProcedures.evaluatePower(dsBase, dsPower, dsNumOfOperands);
        
        return datumReturnNum;
    }
    
    public static BaseData.DataClass print_file_list_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() > 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        String strPath = ".";
        if (tsParameter.size() > 0) {
            BaseData.DataClass dsParam = new BaseData.DataClass();
            dsParam.copyTypeValueDeep(tsParameter.poll());
            dsParam.changeDataType(BaseData.DATATYPES.DATUM_STRING);
            strPath = dsParam.getStringValue();
        }
        LinkedList<String> listOutputs = new LinkedList<String>();
        int nReturn = IOLib.outputFileList(strPath, listOutputs);
        
        if (msstreamLogOutput != null)    {
            for (int idx = 0; idx < listOutputs.size(); idx ++) {
                msstreamLogOutput.outputString(listOutputs.get(idx) + "\n");
            }
        }
        datumReturnNum = new BaseData.DataClass(BaseData.DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));    // do not return anything.
        return datumReturnNum;
    }
    
    public static BaseData.DataClass input_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* input */, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1 && tsParameter.size() != 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        boolean bInputString = false;
        if (tsParameter.size() == 2)    {
            BaseData.DataClass datumInputType = tsParameter.poll();
            if (datumInputType != null && datumInputType.getDataType() == BaseData.DATATYPES.DATUM_STRING
                    && datumInputType.getStringValue().equalsIgnoreCase("s"))    {
                bInputString = true;
            }
        }
        
        BaseData.DataClass datumPrompt = tsParameter.poll();    // need not to deep copy coz only output value.
        if (datumPrompt == null)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        datumReturnNum = null;    // do not return anything if no valid input.
        if (msstreamConsoleInput != null)    {
            msstreamConsoleInput.doBeforeInput();
            while (true)    {
                if (msstreamLogOutput != null)    {
                    if (datumPrompt.getDataType() == BaseData.DATATYPES.DATUM_STRING)    {
                        // should not include the double quote if print's parameter is a string.
                        // but if part of the parameter is a string, e.g. [56, "abc"], double quote
                        // should be used.
                        msstreamLogOutput.outputString(datumPrompt.getStringValue());
                    } else    {
                        msstreamLogOutput.outputString(datumPrompt.output());
                    }
                }
                
                String strInput = msstreamConsoleInput.inputString();
                if (strInput != null)    {
                    if (bInputString)    {
                        datumReturnNum = new BaseData.DataClass(BaseData.DATATYPES.DATUM_STRING, strInput);
                    } else {
                        BaseData.DataClass datumStrExpr = new BaseData.DataClass(BaseData.DATATYPES.DATUM_STRING, strInput);
                        LinkedList<Variable> l = new LinkedList<Variable>();
                        lVarNameSpaces.addFirst(l);
                        ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
                        try    {
                            datumReturnNum = exprEvaluator.evaluateExpression(
                                                        datumStrExpr.getStringValue(), new BaseData.CurPos());
                        } catch (ErrProcessor.JFCALCExpErrException e)    {
                            continue;    // invalid input.
                        } finally {
                            lVarNameSpaces.poll();    // this will run before try...catch, i.e. before continue
                        }
                    }
                    break;    // valid input
                } else    {
                    continue;    // invalid input
                }
            }
            msstreamConsoleInput.doAfterInput();
        }
        if (datumReturnNum == null)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
        }            
        return datumReturnNum;
    }
    
    public static BaseData.DataClass pause_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* input */, InterruptedException
    {
        BaseData.DataClass datumReturnNum = null;
        if (tsParameter.size() > 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        
        BaseData.DataClass datumPrompt = new BaseData.DataClass(BaseData.DATATYPES.DATUM_STRING, "");
        if (tsParameter.size() == 1) {
            datumPrompt = tsParameter.poll();    // need not to deep copy coz only output value.
        }
        if (datumPrompt == null)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        if (msstreamConsoleInput != null)    {
            msstreamConsoleInput.doBeforeInput();
            if (msstreamLogOutput != null)    {
                if (datumPrompt.getDataType() == BaseData.DATATYPES.DATUM_STRING)    {
                    // should not include the double quote if print's parameter is a string.
                    // but if part of the parameter is a string, e.g. [56, "abc"], double quote
                    // should be used.
                    msstreamLogOutput.outputString(datumPrompt.getStringValue());
                } else    {
                    msstreamLogOutput.outputString(datumPrompt.output());
                }
            }

            String strInput = msstreamConsoleInput.inputString();   // discard input.
            msstreamConsoleInput.doAfterInput();
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass scanf_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* input */, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        datumReturnNum.setDataList(new BaseData.DataClass[0], BaseData.DATATYPES.DATUM_REF_DATA);
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumFormat = tsParameter.poll();    // need not to deep copy coz only output value.
        datumFormat.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        if (msstreamConsoleInput != null) {
	        msstreamConsoleInput.doBeforeInput();
	        String strInput = msstreamConsoleInput.inputString();
	        msstreamConsoleInput.doAfterInput();
	        BaseData.DataClass datumInput = new BaseData.DataClass(BaseData.DATATYPES.DATUM_STRING, strInput);
	        LinkedList<BaseData.DataClass> listNewParams = new LinkedList<BaseData.DataClass>();
	        listNewParams.add(datumFormat);
	        listNewParams.add(datumInput);
	        datumReturnNum = IOLib.sfScanf(listNewParams);
        }
        if (datumReturnNum == null)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
        }            
        return datumReturnNum;
    }
    
    /* print */
    public static BaseData.DataClass print_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass dsParam = new BaseData.DataClass();
        dsParam.copyTypeValueDeep(tsParameter.poll());
        if (msstreamLogOutput != null)    {
            if (dsParam.getDataType() == BaseData.DATATYPES.DATUM_STRING)    {
                // should not include the double quote if print's parameter is a string.
                // but if part of the parameter is a string, e.g. [56, "abc"], double quote
                // should be used.
                msstreamLogOutput.outputString(dsParam.getStringValue());
            } else    {
                msstreamLogOutput.outputString(dsParam.output());
            }
        }
        datumReturnNum = null;    // do not return anything.
        return datumReturnNum;
    }
    
    /* printf */
    public static BaseData.DataClass printf_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        String strOutput = IOLib.sPrintf(tsParameter);
        BaseData.DataClass datumOutput = new BaseData.DataClass(BaseData.DATATYPES.DATUM_STRING, strOutput);
        LinkedList<BaseData.DataClass> listPrintParams = new LinkedList<BaseData.DataClass>();
        listPrintParams.add(datumOutput);
        BaseData.DataClass datumReturnNum = print_Function(listPrintParams, lVarNameSpaces);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass alloc_array_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* alloc array */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() < 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        } else  {
            int[] nListArraySize = new int[0];
            BaseData.DataClass datumDefault = new BaseData.DataClass();
            datumDefault.setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_INTEGER); // default data is ZERO
            if (tsParameter.size() <= 2 && tsParameter.getLast().getDataType() == BaseData.DATATYPES.DATUM_REF_DATA)    {
                // one or two paremeters first of which is a vector
                if (tsParameter.size() == 2)    {
                    datumDefault.copyTypeValueDeep(tsParameter.poll());
                }
                BaseData.DataClass datumParam = new BaseData.DataClass();
                datumParam.copyTypeValueDeep(tsParameter.poll());
                if (datumParam.getDataListSize() == 0 || datumParam.getDataListSize() > 16)  {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                } else {
                    nListArraySize = new int[datumParam.getDataListSize()];
                    for (int idx = 0; idx < datumParam.getDataListSize(); idx ++)   {
                        BaseData.DataClass datumSize = datumParam.getDataList()[idx];
                        datumSize.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
                        if (datumSize.getDataValue().isActuallyNegative() || datumSize.getDataValue().compareTo(new MFPNumeric(65536)) > 0)  {
                            // size should not be greater than 65536
                            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                        }
                        nListArraySize[idx] = (int)datumSize.getDataValue().longValue();
                    }
                }
            } else  {
                nListArraySize = new int[tsParameter.size()];
                for (int index = 0; index < nListArraySize.length; index ++)    {
                    BaseData.DataClass datumParam = new BaseData.DataClass();
                    datumParam.copyTypeValueDeep(tsParameter.removeLast());
                    datumParam.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
                    if (datumParam.getDataValue().isActuallyNegative() || datumParam.getDataValue().compareTo(new MFPNumeric(65536)) > 0)    {
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                    }
                    nListArraySize[index] = (int)datumParam.getDataValue().longValue();
                }
            }
            datumReturnNum.allocDataArray(nListArraySize, datumDefault);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass clone_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* alloc array */
    {
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumReturnNum = tsParameter.getFirst().cloneSelf();
        return datumReturnNum;
    }
    
    public static BaseData.DataClass eye_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* alloc an I array */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() < 1 || tsParameter.size() > 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumDim = new BaseData.DataClass();
        datumDim.setDataValue(MFPNumeric.TWO, BaseData.DATATYPES.DATUM_INTEGER);
        if (tsParameter.size() == 2)    {
            datumDim.copyTypeValueDeep(tsParameter.poll());
            datumDim.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            if (datumDim.getDataValue().compareTo(MFPNumeric.ONE) < 0 || datumDim.getDataValue().compareTo(new MFPNumeric(16)) > 0)  {
                // dim should not be greater than 16
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
        }
        BaseData.DataClass datumSize = new BaseData.DataClass();
        datumSize.copyTypeValueDeep(tsParameter.poll());
        datumSize.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        if (datumSize.getDataValue().isActuallyNegative() || datumSize.getDataValue().compareTo(new MFPNumeric(65536)) > 0)  {
            // size should not be greater than 65536
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        
        datumReturnNum = BuiltinProcedures.createEyeMatrix((int)datumSize.getDataValue().longValue(), (int)datumDim.getDataValue().longValue());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass ones_zeros_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* alloc an zeros or ones array */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        int [] nlistSizes;
        if (tsParameter.size() < 1)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        } else if (tsParameter.size() == 1 && tsParameter.get(0).getDataType() == BaseData.DATATYPES.DATUM_REF_DATA)    {
            // a single paremeter which is a vector
            BaseData.DataClass datumParam = new BaseData.DataClass();
            datumParam.copyTypeValueDeep(tsParameter.poll());
            if (datumParam.getDataListSize() > 16)  {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            } else {
                nlistSizes = new int[datumParam.getDataListSize()];
                for (int idx = 0; idx < datumParam.getDataListSize(); idx ++)   {
                    BaseData.DataClass datumSize = datumParam.getDataList()[idx];
                    datumSize.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
                    // here we consider a special case where size(a_simple_value) = [0]
                    // to make zeros(size(a_value)) always work, zeros([0]) should be allowed.
                    if ((datumParam.getDataListSize() == 1 && datumSize.getDataValue().isActuallyNegative())
                            || (datumParam.getDataListSize() > 1 && datumSize.getDataValue().compareTo(MFPNumeric.ONE) < 0)
                            || datumSize.getDataValue().compareTo(new MFPNumeric(65536)) > 0)  {
                        // size should not be greater than 65536
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                    }
                    nlistSizes[idx] = (int)datumSize.getDataValue().longValue();
                }
            }
        } else  {
            // multiple parameters and each of them is a positive integer.
            nlistSizes = new int[tsParameter.size()];
            for (int idx = 0; idx < nlistSizes.length; idx ++)   {
                BaseData.DataClass datumSize = tsParameter.poll();
                datumSize.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
                // here we consider a special case where size(a_simple_value) = [0]
                // to make zeros(size(a_value)) always work, zeros([0]) should be allowed.
                if ((nlistSizes.length == 1 && datumSize.getDataValue().isActuallyNegative())
                        || (nlistSizes.length > 1 && datumSize.getDataValue().compareTo(MFPNumeric.ONE) < 0)
                        || datumSize.getDataValue().compareTo(new MFPNumeric(65536)) > 0)  {
                    // size should not be greater than 65536
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
                }
                nlistSizes[nlistSizes.length - 1 - idx] = (int)datumSize.getDataValue().longValue();
            }
        }
        
        BaseData.DataClass datumUniValue = new BaseData.DataClass();
        if (strNameLowCase.compareTo("zeros") == 0)   {
            datumUniValue.setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_INTEGER);
        } else  {   // if (strNameLowCase.compareTo("ones") == 0)
            datumUniValue.setDataValue(MFPNumeric.ONE, BaseData.DATATYPES.DATUM_INTEGER);
        }
        datumReturnNum = BuiltinProcedures.createUniValueMatrix(nlistSizes, datumUniValue);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass includes_nan_inf_null_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
        int nSearchMode = 0;
        if (strNameLowCase.compareTo("includes_nan_or_inf_or_null") == 0) {
            nSearchMode = 15;
        } else if (strNameLowCase.compareTo("includes_nan_or_inf") == 0) {
            nSearchMode = 14;
        } else if (strNameLowCase.compareTo("includes_nan") == 0)   {
            nSearchMode = 2;
        } else if (strNameLowCase.compareTo("includes_inf") == 0)    {
            nSearchMode = 12;
        } else if (strNameLowCase.compareTo("includes_null") == 0)  {
            nSearchMode = 1;
        }
        boolean bReturn = BuiltinProcedures.includesAbnormalValues(datum, nSearchMode);
        datumReturnNum.setDataValue(MFPNumeric.valueOf(bReturn), BaseData.DATATYPES.DATUM_BOOLEAN);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass is_aexpr_datum_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
        if (datum.getDataType() == BaseData.DATATYPES.DATUM_ABSTRACT_EXPR) {
            datumReturnNum.setDataValue(true);
        } else {
            datumReturnNum.setDataValue(false);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass get_boolean_aexpr_true_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
        
        if (datum.getDataType() == BaseData.DATATYPES.DATUM_ABSTRACT_EXPR) {
            datumReturnNum.setDataValue(true);
        } else {
            datumReturnNum.copyTypeValueDeep(datum);
            datumReturnNum.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass get_boolean_aexpr_false_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
        
        if (datum.getDataType() == BaseData.DATATYPES.DATUM_ABSTRACT_EXPR) {
            datumReturnNum.setDataValue(false);
        } else {
            datumReturnNum.copyTypeValueDeep(datum);
            datumReturnNum.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass is_nan_inf_null_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datum = tsParameter.poll();   // need not to deep copy in this function.
        if (strNameLowCase.compareTo("is_nan_or_inf_or_null") == 0 && datum.getDataType() == BaseData.DATATYPES.DATUM_NULL) {
            datumReturnNum.setDataValue(MFPNumeric.TRUE, BaseData.DATATYPES.DATUM_BOOLEAN);
        } else if (datum.getDataType() == BaseData.DATATYPES.DATUM_COMPLEX || datum.getDataType() == BaseData.DATATYPES.DATUM_DOUBLE || datum.getDataType() == BaseData.DATATYPES.DATUM_INTEGER) {
            MFPNumeric mfpNumReal = datum.getReal();
            MFPNumeric mfpNumImage = datum.getImage();
            if (!mfpNumImage.isActuallyZero()) {
                // has image part, should not be nan or inf or -inf
                datumReturnNum.setDataValue(MFPNumeric.FALSE, BaseData.DATATYPES.DATUM_BOOLEAN);
            } else if (mfpNumReal.isInf())  {
                datumReturnNum.setDataValue(MFPNumeric.TRUE, BaseData.DATATYPES.DATUM_BOOLEAN);
            } else if (mfpNumReal.isNan() && (strNameLowCase.compareTo("is_nan_or_inf") == 0 || strNameLowCase.compareTo("is_nan_or_inf_or_null") == 0)) {
                datumReturnNum.setDataValue(MFPNumeric.TRUE, BaseData.DATATYPES.DATUM_BOOLEAN);
            } else {
                datumReturnNum.setDataValue(MFPNumeric.FALSE, BaseData.DATATYPES.DATUM_BOOLEAN);
            }
        } else {
            datumReturnNum.setDataValue(MFPNumeric.FALSE, BaseData.DATATYPES.DATUM_BOOLEAN);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass is_eye_zeros_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* is eye (zeros) or not */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() < 1 || tsParameter.size() > 2)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumExplicitNullIsZero = new BaseData.DataClass();
        datumExplicitNullIsZero.setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_BOOLEAN);
        if (tsParameter.size() == 2)    {
            datumExplicitNullIsZero.copyTypeValueDeep(tsParameter.poll());
            datumExplicitNullIsZero.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);
        }
        boolean bExplicitNullIsZero = true;
        if (datumExplicitNullIsZero.getDataValue().isActuallyZero())    {
            // false;
            bExplicitNullIsZero = false;
        }
        BaseData.DataClass datum = new BaseData.DataClass();
        datum.copyTypeValueDeep(tsParameter.poll());
        boolean bReturn = false;
        if (strNameLowCase.compareTo("is_eye") == 0)    {
            bReturn = datum.isEye(bExplicitNullIsZero);
        } else    {    // is zeros?
            bReturn = datum.isZeros(bExplicitNullIsZero);
        }
        if (bReturn)    {
            datumReturnNum.setDataValue(MFPNumeric.ONE, BaseData.DATATYPES.DATUM_BOOLEAN);
        } else    {
            datumReturnNum.setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_BOOLEAN);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass size_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* size */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() < 1 || tsParameter.size() > 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass dsParamSizeDim = null;
        if (tsParameter.size() == 2)    {
            dsParamSizeDim = new BaseData.DataClass();
            dsParamSizeDim.copyTypeValueDeep(tsParameter.poll());
            dsParamSizeDim.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            if (dsParamSizeDim.getDataValue().isActuallyNonPositive())    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
        }
        BaseData.DataClass dsParamArray = new BaseData.DataClass();
        dsParamArray.copyTypeValueDeep(tsParameter.poll());
        int[] nArraySize = dsParamArray.recalcDataArraySize();
        int nSizeArrayLen = nArraySize.length;
        if (dsParamSizeDim != null)    {
            nSizeArrayLen = Math.min(nSizeArrayLen, (int)dsParamSizeDim.getDataValue().longValue());
        }
        BaseData.DataClass[] dataList = new BaseData.DataClass[nSizeArrayLen];
        for (int index = 0; index < nSizeArrayLen; index ++)    {
            dataList[index] = new BaseData.DataClass(BaseData.DATATYPES.DATUM_INTEGER, new MFPNumeric(nArraySize[index]));
        }
        datumReturnNum = new BaseData.DataClass(dataList);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass set_array_elem_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* set array element */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 3 && tsParameter.size() != 4)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumDefault = new BaseData.DataClass();   // default is NULL
        // datumDefault.SetDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
        if (tsParameter.size() == 4)    {
            // the 4th parameter is the default value for the new elements (i.e. elems will be added 
            // into the array besides the set_array_elem function explicitly set.
            datumDefault.copyTypeValueDeep(tsParameter.poll());
        } else  {   // tsParameter.size() == 3
            switch(tsParameter.getFirst().getDataType())    {
                case DATUM_BOOLEAN:
                case DATUM_INTEGER:
                case DATUM_DOUBLE:
                    datumDefault.setDataValue(MFPNumeric.ZERO, tsParameter.getFirst().getDataType());
                    break;
                case DATUM_COMPLEX:
                    datumDefault.setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_DOUBLE);
                    break;
                case DATUM_STRING:
                    datumDefault.setStringValue("");
                    break;
                default:
                    // datumDefault has been initialized as NULL.
            }
        }
        BaseData.DataClass datumValue = new BaseData.DataClass();
        datumValue.copyTypeValueDeep(tsParameter.poll());
        BaseData.DataClass datumIndex = new BaseData.DataClass();
        datumIndex.copyTypeValueDeep(tsParameter.poll());
        datumIndex.changeDataType(BaseData.DATATYPES.DATUM_REF_DATA);
        if (datumIndex.getDataListSize() == 0)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_WRONG_INDEX);
        }
        int[] nListArrayIndex = new int[datumIndex.getDataListSize()];
        for (int index = 0; index < datumIndex.getDataListSize(); index ++)    {
            int[] nListArrayIndex1 = new int[1];
            nListArrayIndex1[0] = index;
            BaseData.DataClass datumTmp = datumIndex.getDataAtIndex(nListArrayIndex1);
            datumTmp.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            if (datumTmp.getDataValue().longValue() < 0)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            nListArrayIndex[index] = (int)datumTmp.getDataValue().longValue();
        }
        BaseData.DataClass datumArray = tsParameter.poll();    // here datumArray is parameter by reference.
        datumArray.createDataAtIndexByRef(nListArrayIndex, datumValue, datumDefault);
        datumReturnNum = datumArray;
        return datumReturnNum;
    }
    
    public static BaseData.DataClass recip_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    // reciprocal of 2D matrix or a number
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        // now only support 2D matrix or a number.
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumOperand = tsParameter.poll();
        BaseData.DataClass datum = new BaseData.DataClass();
        datum.copyTypeValueDeep(datumOperand);
        int[] narraySize = datum.recalcDataArraySize();
        datum.populateDataArray(narraySize, false);
        datumReturnNum = BuiltinProcedures.evaluateReciprocal(datum);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass left_recip_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    // left reciprocal of 2D matrix or a number
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        // now only support 2D matrix or a number.
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumOperand = tsParameter.poll();
        BaseData.DataClass datum = new BaseData.DataClass();
        datum.copyTypeValueDeep(datumOperand);
        int[] narraySize = datum.recalcDataArraySize();
        datum.populateDataArray(narraySize, false);
        datumReturnNum = BuiltinProcedures.evaluateLeftReciprocal(datum);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass eigen_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException    /* calculate eigen values/vectors */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1 && tsParameter.size() != 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datum2DSqrA, datum2DSqrB;
        BaseData.DataClass datumParam2 = null;
        if (tsParameter.size() == 2)    {
            datumParam2 = tsParameter.poll();
            if (datumParam2.isZeros(true))  {
                // B cannot be zero or zero matrix.
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
        }
        BaseData.DataClass datumParam1 = tsParameter.poll();
        int[] narraySize = datumParam1.recalcDataArraySize();
        if (datumParam1.getDataType() == BaseData.DATATYPES.DATUM_BOOLEAN)  {
            datum2DSqrA = new BaseData.DataClass();
            datum2DSqrA.copyTypeValueDeep(datumParam1);
            datum2DSqrA.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        } else if (datumParam1.getDataType() == BaseData.DATATYPES.DATUM_INTEGER
                || datumParam1.getDataType() == BaseData.DATATYPES.DATUM_DOUBLE
                || datumParam1.getDataType() == BaseData.DATATYPES.DATUM_COMPLEX)    {
            datum2DSqrA = new BaseData.DataClass();
            datum2DSqrA.copyTypeValueDeep(datumParam1);
        } else if (datumParam1.getDataType() == BaseData.DATATYPES.DATUM_REF_DATA)    {
            datum2DSqrA = new BaseData.DataClass();
            if (narraySize.length == 1 && narraySize[0] == 1)    {
                BaseData.DataClass datumElem = new BaseData.DataClass();
                datumElem.copyTypeValueDeep(datumParam1);
                BaseData.DataClass[] dataList = new BaseData.DataClass[1];
                dataList[0] = datumElem;
                datum2DSqrA.setDataList(dataList);
                narraySize = datum2DSqrA.recalcDataArraySize();
            } else  {
                datum2DSqrA.copyTypeValueDeep(datumParam1);
            }
            datum2DSqrA.populateDataArray(narraySize, false);
            if (narraySize.length != 2 || narraySize[0] == 0 || narraySize[0] != narraySize[1] || narraySize[0] > 12) {
                // array matrix size shouldn't be greater than 12.
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
            }
        } else    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        if (datumParam2 == null)    {   // don't have the second parameter, so it is I by default.
            if (datum2DSqrA.getDataType() != BaseData.DATATYPES.DATUM_REF_DATA)  {
                datum2DSqrB = new BaseData.DataClass();
                datum2DSqrB.setDataValue(MFPNumeric.ONE, BaseData.DATATYPES.DATUM_INTEGER);
            } else {
                datum2DSqrB = new BaseData.DataClass();
                BaseData.DataClass[] datumList = new BaseData.DataClass[narraySize[0]];
                for (int idx0 = 0; idx0 < datumList.length; idx0 ++)   {
                    datumList[idx0] = new BaseData.DataClass();
                    BaseData.DataClass[] datumListChildren = new BaseData.DataClass[narraySize[1]];
                    for (int idx1 = 0; idx1 < datumListChildren.length; idx1 ++)    {
                        datumListChildren[idx1] = new BaseData.DataClass();
                        if (idx0 == idx1)   {
                            datumListChildren[idx1].setDataValue(MFPNumeric.ONE, BaseData.DATATYPES.DATUM_INTEGER);
                        } else {
                            datumListChildren[idx1].setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_INTEGER);
                        }
                    }
                    datumList[idx0].setDataList(datumListChildren);
                }
                datum2DSqrB.setDataList(datumList);
            }
        } else  {
            int[] narraySizeB = datumParam2.recalcDataArraySize();
            if (datumParam2.getDataType() == BaseData.DATATYPES.DATUM_BOOLEAN)  {
                datum2DSqrB = new BaseData.DataClass();
                datum2DSqrB.copyTypeValueDeep(datumParam2);
                datum2DSqrB.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            } else if (datumParam2.getDataType() == BaseData.DATATYPES.DATUM_INTEGER
                    || datumParam2.getDataType() == BaseData.DATATYPES.DATUM_DOUBLE
                    || datumParam2.getDataType() == BaseData.DATATYPES.DATUM_COMPLEX)    {
                datum2DSqrB = new BaseData.DataClass();
                datum2DSqrB.copyTypeValueDeep(datumParam2);
            } else if (datumParam2.getDataType() == BaseData.DATATYPES.DATUM_REF_DATA)    {
                datum2DSqrB = new BaseData.DataClass();
                if (narraySizeB.length == 1 && narraySizeB[0] == 1)    {
                    BaseData.DataClass datumElem = new BaseData.DataClass();
                    datumElem.copyTypeValueDeep(datumParam2);
                    BaseData.DataClass[] dataList = new BaseData.DataClass[1];
                    dataList[0] = datumElem;
                    datum2DSqrB.setDataList(dataList);
                    narraySizeB = datum2DSqrB.recalcDataArraySize();
                } else  {
                    datum2DSqrB.copyTypeValueDeep(datumParam2);
                }
                datum2DSqrB.populateDataArray(narraySizeB, false);
            } else    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            if (narraySizeB.length != narraySize.length) {  // A is a matrix but B isn't or vise versa
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
            } else if (narraySize.length == 2 && (narraySize[0] != narraySizeB[0] || narraySize[1] != narraySizeB[1])) {
                // A and B sizes are not match.
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
            }
        }
        // now parameter A and B are ok.
        if (datum2DSqrA.getDataType() != BaseData.DATATYPES.DATUM_REF_DATA)  {
            // inputs are single values
            BaseData.DataClass datumEigenValue = BuiltinProcedures.evaluateDivision(datum2DSqrA, datum2DSqrB);
            if (strNameLowCase.compareTo("get_eigen_values") == 0)   {   // return eigen values
                datumReturnNum = datumEigenValue;
            } else {    // return eigen vector and eigen value
                BaseData.DataClass datumEigenVector = new BaseData.DataClass();
                datumEigenVector.setDataValue(MFPNumeric.ONE, BaseData.DATATYPES.DATUM_INTEGER);
                BaseData.DataClass[] datumList = new BaseData.DataClass[2];
                datumList[0] = datumEigenVector;
                datumList[1] = datumEigenValue;
                datumReturnNum = new BaseData.DataClass();
                datumReturnNum.setDataList(datumList);
            }
        } else {
            // inputs are 2d square matrices.
            LinkedList<BaseData.DataClass> listEigenValues = MatrixLib.calculateEigenValues(datum2DSqrA, datum2DSqrB, msfunctionInterrupter);
            if (strNameLowCase.compareTo("get_eigen_values") == 0)   {   // return eigen values
                datumReturnNum = new BaseData.DataClass();
                datumReturnNum.setDataList(listEigenValues.toArray(new BaseData.DataClass[narraySize[0]]));
            } else if (narraySize[0] > 1)  {   // return eigen vector matrix and eigen values
                BaseData.DataClass[] datumDataVectors = new BaseData.DataClass[narraySize[0]];
                for (int idx = 0; idx < listEigenValues.size(); idx ++) {
                    datumDataVectors[idx] = new BaseData.DataClass();
                    BaseData.DataClass datumAMinusEigB = BuiltinProcedures.evaluateMultiplication(listEigenValues.get(idx), datum2DSqrB);
                    datumAMinusEigB = BuiltinProcedures.evaluateSubstraction(datum2DSqrA, datumAMinusEigB);
                    LinkedList<BaseData.DataClass> listEigenVector = MatrixLib.calculateZeroVector(datumAMinusEigB);
                    datumDataVectors[idx].setDataList(listEigenVector.toArray(new BaseData.DataClass[narraySize[1]]));
                }
                BaseData.DataClass datumVectorMatrix = new BaseData.DataClass();
                datumVectorMatrix.setDataList(datumDataVectors);
                datumVectorMatrix = BuiltinProcedures.evaluateTransposition(datumVectorMatrix);
                // calculate eigen value matrix.
                BaseData.DataClass datumEigenValues = new BaseData.DataClass();
                BaseData.DataClass[] datumListEigVals = new BaseData.DataClass[narraySize[0]];
                for (int idx0 = 0; idx0 < narraySize[0]; idx0 ++)  {
                    BaseData.DataClass[] datumListEigValChildren = new BaseData.DataClass[narraySize[1]];
                    for (int idx1 = 0; idx1 < narraySize[1]; idx1 ++)   {
                        if (idx1 == idx0)   {
                            datumListEigValChildren[idx1] = listEigenValues.get(idx1);
                        } else {
                            datumListEigValChildren[idx1] = new BaseData.DataClass();
                            datumListEigValChildren[idx1].setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_INTEGER);
                        }
                    }
                    datumListEigVals[idx0] = new BaseData.DataClass();
                    datumListEigVals[idx0].setDataList(datumListEigValChildren);
                }
                datumEigenValues.setDataList(datumListEigVals);

                BaseData.DataClass[] datumReturnList = new BaseData.DataClass[2];
                // return value includes two elements, first is eigen vector matrix, each column is an eigen vector,
                // second is the eigen values list.
                datumReturnList[0] = datumVectorMatrix;
                datumReturnList[1] = datumEigenValues;
                datumReturnNum = new BaseData.DataClass();
                datumReturnNum.setDataList(datumReturnList);
            } else { // it is a 1 * 1 matrix, have to be handled specially.
                BaseData.DataClass datumEigenVector = new BaseData.DataClass();
                BaseData.DataClass datumEigenVectorChild = new BaseData.DataClass();
                datumEigenVectorChild.setDataValue(MFPNumeric.ONE, BaseData.DATATYPES.DATUM_INTEGER);
                BaseData.DataClass[] datumList = new BaseData.DataClass[1];
                datumList[0] = datumEigenVectorChild;
                BaseData.DataClass datumTmp = new BaseData.DataClass();
                datumTmp.setDataList(datumList);
                datumList = new BaseData.DataClass[1];
                datumList[0] = datumTmp;
                datumEigenVector.setDataList(datumList);
                
                BaseData.DataClass datumEigenValues = new BaseData.DataClass();
                // actually there is only one eigen value.
                BaseData.DataClass[] datumEigValList = new BaseData.DataClass[1];
                datumEigValList[0] = new BaseData.DataClass();
                datumEigValList[0].setDataList(listEigenValues.toArray(new BaseData.DataClass[narraySize[0]]));
                datumEigenValues.setDataList(datumEigValList);
                
                BaseData.DataClass[] datumReturnList = new BaseData.DataClass[2];
                datumReturnList[0] = datumEigenVector;
                datumReturnList[1] = datumEigenValues;
                datumReturnNum = new BaseData.DataClass();
                datumReturnNum.setDataList(datumReturnList);
            }
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass deter_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* calculate determinant of a 2D square array */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumOperand = tsParameter.poll();
        if (datumOperand.getDataType() == BaseData.DATATYPES.DATUM_BOOLEAN)  {
            datumReturnNum = new BaseData.DataClass();
            datumReturnNum.copyTypeValueDeep(datumOperand);
            datumReturnNum.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        } else if (datumOperand.getDataType() == BaseData.DATATYPES.DATUM_INTEGER
                || datumOperand.getDataType() == BaseData.DATATYPES.DATUM_DOUBLE
                || datumOperand.getDataType() == BaseData.DATATYPES.DATUM_COMPLEX)    {
            datumReturnNum = new BaseData.DataClass();
            datumReturnNum.copyTypeValueDeep(datumOperand);
        } else if (datumOperand.getDataType() == BaseData.DATATYPES.DATUM_REF_DATA)    {
            int[] narraySize = datumOperand.recalcDataArraySize();
            BaseData.DataClass datum2DMatrix = new BaseData.DataClass();
            if (narraySize.length == 1 && narraySize[0] == 1)    {
                BaseData.DataClass datumElem = new BaseData.DataClass();
                datumElem.copyTypeValueDeep(datumOperand);
                BaseData.DataClass[] dataList = new BaseData.DataClass[1];
                dataList[0] = datumElem;
                datum2DMatrix.setDataList(dataList);
                narraySize = datum2DMatrix.recalcDataArraySize();
            } else  {
                datum2DMatrix.copyTypeValueDeep(datumOperand);
            }
            datum2DMatrix.populateDataArray(narraySize, false);
            datumReturnNum = BuiltinProcedures.evaluateDeterminant(datum2DMatrix);
        } else    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass rank_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* calculate rank of a 2D square array */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        datumReturnNum = MatrixLib.calculateMatrixRank(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass upper_triangular_matrix_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* calculate upper triangular matrix of a 2D square array */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        datumReturnNum = MatrixLib.calculateUpperTriangularMatrix(tsParameter.poll());
        return datumReturnNum;
    }
    
    public static BaseData.DataClass invert_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* invert a 2D array */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumOperand = tsParameter.poll();
        if (datumOperand.getDataType() == BaseData.DATATYPES.DATUM_BOOLEAN
                || datumOperand.getDataType() == BaseData.DATATYPES.DATUM_INTEGER
                || datumOperand.getDataType() == BaseData.DATATYPES.DATUM_DOUBLE
                || datumOperand.getDataType() == BaseData.DATATYPES.DATUM_COMPLEX)    {
            BaseData.DataClass datumNonMatrix = new BaseData.DataClass();
            datumNonMatrix.copyTypeValueDeep(datumOperand);
            datumReturnNum = BuiltinProcedures.evaluateDivision(
                                new BaseData.DataClass(BaseData.DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE),
                                datumOperand);
        } else if (datumOperand.getDataType() == BaseData.DATATYPES.DATUM_REF_DATA)    {
            int[] narraySize = datumOperand.recalcDataArraySize();
            if (narraySize.length == 1 && narraySize[0] == 1)    {
                BaseData.DataClass datumElem = new BaseData.DataClass();
                datumElem.copyTypeValueDeep(datumOperand.getDataList()[0]);
                BaseData.DataClass[] dataList = new BaseData.DataClass[1];
                dataList[0] = BuiltinProcedures.evaluateDivision(
                                new BaseData.DataClass(BaseData.DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE),
                                datumElem);
                datumReturnNum.setDataList(dataList);
            } else    {
                BaseData.DataClass datum2DMatrix = new BaseData.DataClass();
                datum2DMatrix.copyTypeValueDeep(datumOperand);    // deep copy because PopulateDataArray will change array elements.
                datum2DMatrix.populateDataArray(narraySize, false);
                datumReturnNum = BuiltinProcedures.invert2DSquare(datum2DMatrix);
            }
        } else    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        return datumReturnNum;
    }
    
    /* calculate roots by Java code, this function interface is not released to user */
    public static BaseData.DataClass roots_internal_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        LinkedList<BaseData.DataClass> listParams = new LinkedList<BaseData.DataClass>();
        if (tsParameter.size() == 0)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        } else if (tsParameter.size() == 1) {
            if (tsParameter.get(0).getDataType() != BaseData.DATATYPES.DATUM_REF_DATA)   {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
            }
            BaseData.DataClass datumParam = tsParameter.get(0).cloneSelf();
            for (int idx = 0; idx < datumParam.getDataListSize(); idx ++)   {
                listParams.add(datumParam.getDataList()[idx]);
            }
        } else  {
            for (int idx = 0; idx < tsParameter.size(); idx ++)
            {
                BaseData.DataClass datumParam = new BaseData.DataClass();
                datumParam.copyTypeValueDeep(tsParameter.get(idx));
                listParams.addFirst(datumParam);
            }
        }
        LinkedList<BaseData.DataClass> listResults = MathLib.solvePolynomial(listParams, msfunctionInterrupter);
        BaseData.DataClass[] arraydatumChildren = new BaseData.DataClass[listResults.size()];
        for (int idx = 0; idx < listResults.size(); idx ++) {
            arraydatumChildren[idx] = listResults.get(idx);
        }
        datumReturnNum.setDataList(arraydatumChildren);            
        return datumReturnNum;
    }

    /* this function get the continous root from root list. This function is useful when plot 3-order polynomial implicit functions */
    public static BaseData.DataClass get_continous_root_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        LinkedList<BaseData.DataClass> listParams = new LinkedList<BaseData.DataClass>();
        if (tsParameter.size() != 4)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumNext = new BaseData.DataClass();
        datumNext.copyTypeValueDeep(tsParameter.poll());
        BaseData.DataClass datumPrev = new BaseData.DataClass();
        datumPrev.copyTypeValueDeep(tsParameter.poll());
        BaseData.DataClass datumSuggestedIdx = new BaseData.DataClass();
        datumSuggestedIdx.copyTypeValueDeep(tsParameter.poll());
        datumSuggestedIdx.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        int nSuggestedIdx = datumSuggestedIdx.getDataValue().intValue();
        BaseData.DataClass datumRoots = new BaseData.DataClass();
        datumRoots.copyTypeValueDeep(tsParameter.poll());
        datumRoots.changeDataType(BaseData.DATATYPES.DATUM_REF_DATA);
        for (int idx = 0; idx < datumRoots.getDataListSize(); idx ++) {
            datumRoots.getDataList()[idx].changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
        }
        if (datumRoots.getDataListSize() == 0) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        } else if (datumRoots.getDataListSize() == 1) {
            datumReturnNum = datumRoots.getDataList()[0];
            return datumReturnNum;
        }
        
        boolean bPrevNextAvgValid = false;
        BaseData.DataClass datumPrevNextAvg = new BaseData.DataClass(BaseData.DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO);
        if (datumNext.getDataType() != BaseData.DATATYPES.DATUM_NULL) {
            datumNext.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
            if (!datumNext.getReal().isNan() && !datumNext.getImage().isNan()) {
                datumPrevNextAvg = datumNext;
                bPrevNextAvgValid = true;
            }
        }
        if (datumPrev.getDataType() != BaseData.DATATYPES.DATUM_NULL) {
            datumPrev.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
            if (!datumPrev.getReal().isNan() && !datumPrev.getImage().isNan()) {
                datumPrevNextAvg = BuiltinProcedures.evaluateAdding(datumPrev, datumPrevNextAvg);
                bPrevNextAvgValid = true;
            }
        }
        
        if (bPrevNextAvgValid) {
            MFPNumeric mfpPrevNextAvgReal = datumPrevNextAvg.getReal(), mfpPrevNextAvgImage = datumPrevNextAvg.getImage();
            int nSelectedIdx = 0;
            MFPNumeric mfpGapReal = datumRoots.getDataList()[0].getReal().subtract(mfpPrevNextAvgReal);
            MFPNumeric mfpGapImage = datumRoots.getDataList()[0].getImage().subtract(mfpPrevNextAvgImage);
            MFPNumeric mfpMinGap = mfpGapReal.abs().add(mfpGapImage.abs()); // no need to calculate (x1-x2)**2+(y1-y2)**2
            for (int idx = 0; idx < datumRoots.getDataListSize(); idx ++) {
                mfpGapReal = datumRoots.getDataList()[idx].getReal().subtract(mfpPrevNextAvgReal);
                mfpGapImage = datumRoots.getDataList()[idx].getImage().subtract(mfpPrevNextAvgImage);
                MFPNumeric mfpGap = mfpGapReal.abs().add(mfpGapImage.abs());
                if (mfpGap.compareTo(mfpMinGap) < 0) {
                    nSelectedIdx = idx;
                    mfpMinGap = mfpGap;
                }
            }
            datumReturnNum = datumRoots.getDataList()[nSelectedIdx];
        } else if (nSuggestedIdx >= 0 && nSuggestedIdx < datumRoots.getDataListSize()) {
            datumReturnNum = datumRoots.getDataList()[nSuggestedIdx];
        } else {
            datumReturnNum = datumRoots.getDataList()[0];
        }
        return datumReturnNum;
    }
    
    /* sum_over or SIGMA and product_over or PI */
    public static BaseData.DataClass sum_product_over_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 3)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumEndRaw = new BaseData.DataClass();
        datumEndRaw.copyTypeValueDeep(tsParameter.poll());
        datumEndRaw.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        BaseData.DataClass datumStartRaw = new BaseData.DataClass();
        datumStartRaw.copyTypeValueDeep(tsParameter.poll());
        datumStartRaw.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        BaseData.DataClass datumStrExpr = new BaseData.DataClass();
        datumStrExpr.copyTypeValueDeep(tsParameter.poll());
        datumStrExpr.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        
        String strStart = datumStartRaw.getStringValue();
        if (strStart == null)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        String[] strarrayStartParts = strStart.trim().split("=");   // use trim to prevent situation like " x= 8" (variable name becomes " x")
        if (strarrayStartParts.length < 2 || strarrayStartParts[0].length() == 0)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        String strVarName = strarrayStartParts[0].trim();
        for (int idx = 0; idx < strVarName.length(); idx ++)    {
            int nNameCharType = ElemAnalyzer.isNameChar(strVarName, idx);
            if (idx == 0 && nNameCharType != 1)  {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            if (idx > 0 && nNameCharType == 0)  {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
        }
        // now variable name is valid
        UnknownVarOperator.UnknownVariable var = new UnknownVarOperator.UnknownVariable(strVarName.trim());
        LinkedList<UnknownVarOperator.UnknownVariable> lUnknown = new LinkedList<UnknownVarOperator.UnknownVariable>();
        lUnknown.addFirst(var);
        AbstractExpr aeStrExpr = null;
        try {
            aeStrExpr = ExprAnalyzer.analyseExpression(datumStrExpr.getStringValue(), new BaseData.CurPos());
        } catch (Exception e) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        try {
            // need to evaluate considering second order integral, x+y + 1, y is a constant, so simplifymost will calculate y + 1 directly.
            aeStrExpr = aeStrExpr.evaluateAExpr(lUnknown, lVarNameSpaces); // look on unknown dim as single value dim.
        }catch (Exception e)	{
            // for like x + y + 1, y may not in lUnknown and lVarNameSpaces. This exception may be thrown as cannot get result exception. so catch
            // any exceptions that throw.
        }
        LinkedList<Variable> l = new LinkedList<Variable>();
        l.addFirst(var);
        lVarNameSpaces.addFirst(l);
        ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
        BaseData.DataClass datumEnd = exprEvaluator.evaluateExpression(datumEndRaw.getStringValue(), new BaseData.CurPos());    // evaluate data end if it is a string.
        if (datumEnd == null)   {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
        }
        datumEnd.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        BaseData.DataClass datumStart = exprEvaluator.evaluateExpression(datumStartRaw.getStringValue(), new BaseData.CurPos());    // evaluate data end if it is a string.
        if (datumStart == null)   {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
        }
        datumStart.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        MFPNumeric mfpNumStart = datumStart.getDataValue();
        MFPNumeric mfpNumEnd = datumEnd.getDataValue();
        MFPNumeric mfpNumStep = (mfpNumStart.compareTo(mfpNumEnd) > 0)?MFPNumeric.MINUS_ONE:MFPNumeric.ONE;
        MFPNumeric mfpNumEndPlusStep = mfpNumEnd.add(mfpNumStep);
        MFPNumeric idx = mfpNumStart;
        MFPNumeric mfpNumValueReal = MFPNumeric.ZERO;
        MFPNumeric mfpNumValueImage = MFPNumeric.ZERO;
        MFPNumeric mfpNumReturnReal = (strNameLowCase.compareTo("sum_over") == 0)?MFPNumeric.ZERO:MFPNumeric.ONE;
        MFPNumeric mfpNumReturnImage = MFPNumeric.ZERO;
        while (!idx.isEqual(mfpNumEndPlusStep))   {
            BaseData.DataClass datumIndex = new BaseData.DataClass(BaseData.DATATYPES.DATUM_INTEGER, idx);
            var.setValue(datumIndex);
            BaseData.DataClass datumExprValue = null;
            if (aeStrExpr != null) {
                try {
                    datumExprValue = aeStrExpr.evaluateAExprQuick(lUnknown, lVarNameSpaces);
                } catch (Exception ex) {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                }
            }
            //DataClass datumExprValue = exprEvaluator.evaluateExpression(datumStrExpr.getStringValue(), new CurPos());
            if (datumExprValue == null)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
            }
            mfpNumValueReal = datumExprValue.getReal();
            mfpNumValueImage = datumExprValue.getImage();
            if (strNameLowCase.compareTo("sum_over") == 0)  {
                mfpNumReturnReal = mfpNumReturnReal.add(mfpNumValueReal);
                if (!mfpNumReturnImage.isActuallyZero() || !mfpNumValueImage.isActuallyZero())  {
                    mfpNumReturnImage = mfpNumReturnImage.add(mfpNumValueImage);
                }
            } else {
                mfpNumReturnReal = mfpNumReturnReal.multiply(mfpNumValueReal)
                        .subtract(mfpNumReturnImage.multiply(mfpNumValueImage));
                if (!mfpNumReturnImage.isActuallyZero() || !mfpNumValueImage.isActuallyZero())  {
                    mfpNumReturnImage = mfpNumReturnReal.multiply(mfpNumValueImage)
                            .add(mfpNumReturnImage.multiply(mfpNumValueReal));
                }
            }
            idx = idx.add(mfpNumStep);
        }
        datumReturnNum.setComplex(mfpNumReturnReal, mfpNumReturnImage);
        lVarNameSpaces.poll();
        return datumReturnNum;
    }
    
    /* integrate basic method */
    public static BaseData.DataClass integ_Basic_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() < 4 || tsParameter.size() > 5)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumNumofSteps = new BaseData.DataClass(BaseData.DATATYPES.DATUM_INTEGER, MFPNumeric.ONE);
        if (tsParameter.size() == 5)    {
            datumNumofSteps.copyTypeValueDeep(tsParameter.poll());
            datumNumofSteps.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        }
        if (datumNumofSteps.getDataValue().compareTo(MFPNumeric.ONE) < 0)    {
            datumNumofSteps.setDataValue(MFPNumeric.ONE, BaseData.DATATYPES.DATUM_INTEGER);
        }
        long nNumOfSteps = datumNumofSteps.getDataValue().longValue();
        if (nNumOfSteps > 65536)    {
            nNumOfSteps = 65536;    // number of steps should be no more than 65536.
        }
        BaseData.DataClass datumEndRaw = new BaseData.DataClass();
        datumEndRaw.copyTypeValueDeep(tsParameter.poll());
        if (datumEndRaw.getDataType() != BaseData.DATATYPES.DATUM_STRING)   {
            datumEndRaw.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
        }
        BaseData.DataClass datumStartRaw = new BaseData.DataClass();
        datumStartRaw.copyTypeValueDeep(tsParameter.poll());
        if (datumStartRaw.getDataType() != BaseData.DATATYPES.DATUM_STRING) {
            datumStartRaw.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
        }
        BaseData.DataClass datumStrDeltaVar = new BaseData.DataClass();
        datumStrDeltaVar.copyTypeValueDeep(tsParameter.poll());
        datumStrDeltaVar.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        BaseData.DataClass datumStrExpr = new BaseData.DataClass();
        datumStrExpr.copyTypeValueDeep(tsParameter.poll());
        datumStrExpr.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        UnknownVarOperator.UnknownVariable var = new UnknownVarOperator.UnknownVariable(datumStrDeltaVar.getStringValue().trim());
        LinkedList<UnknownVarOperator.UnknownVariable> lUnknown = new LinkedList<UnknownVarOperator.UnknownVariable>();
        lUnknown.addFirst(var);
        AbstractExpr aeStrExpr = null;
        try {
            aeStrExpr = ExprAnalyzer.analyseExpression(datumStrExpr.getStringValue(), new BaseData.CurPos());
        } catch (Exception e) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        try {
            // need to evaluate considering second order integral, x+y + 1, y is a constant, so simplifymost will calculate y + 1 directly.
            aeStrExpr = aeStrExpr.evaluateAExpr(lUnknown, lVarNameSpaces); // look on unknown dim as single value dim.
        }catch (Exception e)	{
            // for like x + y + 1, y may not in lUnknown and lVarNameSpaces. This exception may be thrown as cannot get result exception. so catch
            // any exceptions that throw.
        }
        // variable list has to be added after aeStrExpr is constructed considering x + y + 1, x is in lUnknown but y is not, if l is added
        // aeStrExpr is constructed then both x and y are in lVarNameSpaces. Then y is treated as variable NULL.
        LinkedList<Variable> l = new LinkedList<Variable>();
        l.addFirst(var);
        lVarNameSpaces.addFirst(l);
        ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
        BaseData.DataClass datumEnd = datumEndRaw;
        if (datumEndRaw.getDataType() == BaseData.DATATYPES.DATUM_STRING)   {
            datumEnd = exprEvaluator.evaluateExpression(datumEndRaw.getStringValue(), new BaseData.CurPos());    // evaluate data end if it is a string.
            if (datumEnd == null)   {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
            }
            datumEnd.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
        }
        BaseData.DataClass datumStart = datumStartRaw;
        if (datumStartRaw.getDataType() == BaseData.DATATYPES.DATUM_STRING)   {
            datumStart = exprEvaluator.evaluateExpression(datumStartRaw.getStringValue(), new BaseData.CurPos());    // evaluate data start if it is a string.
            if (datumStart == null)   {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
            }
            datumStart.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
        }
        
        MFPNumeric mfpNumStartReal = datumStart.getReal();
        MFPNumeric mfpNumStartImage = datumStart.getImage();
        MFPNumeric mfpNumEndReal = datumEnd.getReal();
        MFPNumeric mfpNumEndImage = datumEnd.getImage();
        MFPNumeric mfpNumStepReal = MFPNumeric.ZERO;
        MFPNumeric mfpNumStepImage = MFPNumeric.ZERO;
        boolean bStartEndInfOrNan = mfpNumStartReal.isNanOrInf() || mfpNumStartImage.isNanOrInf() || mfpNumEndReal.isNanOrInf() || mfpNumEndImage.isNanOrInf();
        boolean bConsiderImage = !mfpNumStartImage.isActuallyZero() || !mfpNumEndImage.isActuallyZero();
        if (!bStartEndInfOrNan) {
            // save time because no need to create MFPNumeric
            BigDecimal bigDecStepReal = mfpNumEndReal.toBigDecimal().subtract(mfpNumStartReal.toBigDecimal())
                    .divide(new BigDecimal(nNumOfSteps), MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
            mfpNumStepReal = new MFPNumeric(bigDecStepReal);
            BigDecimal bigDecStepImage = mfpNumEndImage.toBigDecimal().subtract(mfpNumStartImage.toBigDecimal())
                    .divide(new BigDecimal(nNumOfSteps), MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
            mfpNumStepImage = new MFPNumeric(bigDecStepImage);
        } else {
            MFPNumeric mfpNumOfSteps = new MFPNumeric(nNumOfSteps);
            MFPNumeric mfpNumRealDistance = mfpNumEndReal.subtract(mfpNumStartReal);
            if (!mfpNumRealDistance.isActuallyZero()) {
                mfpNumStepReal = MFPNumeric.divide(mfpNumRealDistance, mfpNumOfSteps);
            }
            MFPNumeric mfpNumImageDistance = mfpNumEndImage.subtract(mfpNumStartImage);
            if (!mfpNumImageDistance.isActuallyZero()) {
                mfpNumStepImage = MFPNumeric.divide(mfpNumImageDistance, mfpNumOfSteps);
            }
        }
        BaseData.DataClass datumIndex = new BaseData.DataClass();
        MFPNumeric mfpNumReturnReal = MFPNumeric.ZERO;
        BigDecimal bigDecReturnReal = BigDecimal.ZERO;
        MFPNumeric mfpNumReturnImage = MFPNumeric.ZERO;
        BigDecimal bigDecReturnImage = BigDecimal.ZERO;
        MFPNumeric mfpNumIdxReal = mfpNumStartReal;
        MFPNumeric mfpNumIdxImage = mfpNumStartImage;
        boolean bUseBigDec2Calc = !bStartEndInfOrNan;
        BigDecimal bigDecHalf = BigDecimal.valueOf(0.5);
        for (int index = 0; index <= nNumOfSteps; index++)
        {
            if (index == nNumOfSteps) {
                mfpNumIdxReal = mfpNumEndReal;
                mfpNumIdxImage = mfpNumEndImage;
            } else if (index != 0) {
                mfpNumIdxReal = mfpNumIdxReal.add(mfpNumStepReal);
                mfpNumIdxImage = mfpNumIdxImage.add(mfpNumStepImage);
            }
            if (mfpNumIdxImage.isActuallyZero()) {
                datumIndex.setDataValue(mfpNumIdxReal);
            } else {
                datumIndex.setComplex(mfpNumIdxReal, mfpNumIdxImage);
            }
            var.setValue(datumIndex);
            
            BaseData.DataClass datumExprValue = null;
            if (aeStrExpr != null) {
                try {
                    datumExprValue = aeStrExpr.evaluateAExprQuick(lUnknown, lVarNameSpaces);
                } catch (Exception ex) {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                }
            }
            /*
            DataClass datumExprValue = exprEvaluator.evaluateExpression(datumStrExpr.getStringValue(), new CurPos());
            
            if (datumExprValue == null)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
            }*/
            MFPNumeric mfpNumValueReal = datumExprValue.getReal();
            MFPNumeric mfpNumValueImage = datumExprValue.getImage();
            if (bUseBigDec2Calc && (mfpNumValueReal.isNanOrInf() || mfpNumValueImage.isNanOrInf())) {
                // we were using big decimal to do calculation to make it faster. Now we cannot because we see inf or nan.
                mfpNumReturnReal = new MFPNumeric(bigDecReturnReal);
                mfpNumReturnImage = new MFPNumeric(bigDecReturnImage);
                bUseBigDec2Calc = false;
            }
            if (!bConsiderImage && !mfpNumValueImage.isActuallyZero()) {
                bConsiderImage = true;  // now we need to consider image.
            }
            boolean bHeadOrTail = (index == 0 || index == nNumOfSteps);
            if (bUseBigDec2Calc) {
                if (!mfpNumValueReal.isActuallyZero() && !mfpNumStepReal.isActuallyZero()) {
                    BigDecimal bigDecThis = mfpNumValueReal.toBigDecimal().multiply(mfpNumStepReal.toBigDecimal());
                    bigDecThis = bigDecThis.setScale(MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                    if (bHeadOrTail)    {
                        bigDecThis = bigDecThis.multiply(bigDecHalf);
                    }
                    bigDecReturnReal = bigDecReturnReal.add(bigDecThis);
                }
                if (!mfpNumValueImage.isActuallyZero() && !mfpNumStepImage.isActuallyZero()) {
                    BigDecimal bigDecThis = mfpNumValueImage.toBigDecimal().multiply(mfpNumStepImage.toBigDecimal());
                    bigDecThis = bigDecThis.setScale(MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                    if (bHeadOrTail)    {
                        bigDecThis = bigDecThis.multiply(bigDecHalf);
                    }
                    bigDecReturnReal = bigDecReturnReal.subtract(bigDecThis);
                }
                if (!mfpNumValueReal.isActuallyZero() && !mfpNumStepImage.isActuallyZero()) {
                    BigDecimal bigDecThis = mfpNumValueReal.toBigDecimal().multiply(mfpNumStepImage.toBigDecimal());
                    bigDecThis = bigDecThis.setScale(MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                    if (bHeadOrTail)    {
                        bigDecThis = bigDecThis.multiply(bigDecHalf);
                    }
                    bigDecReturnImage = bigDecReturnImage.add(bigDecThis);
                }
                if (!mfpNumValueImage.isActuallyZero() && !mfpNumStepReal.isActuallyZero()) {
                    BigDecimal bigDecThis = mfpNumValueImage.toBigDecimal().multiply(mfpNumStepReal.toBigDecimal());
                    bigDecThis = bigDecThis.setScale(MFPNumeric.THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
                    if (bHeadOrTail)    {
                        bigDecThis = bigDecThis.multiply(bigDecHalf);
                    }
                    bigDecReturnImage = bigDecReturnImage.add(bigDecThis);
                }
            } else {
                MFPNumeric mfpNumThis = mfpNumValueReal.multiply(mfpNumStepReal);
                if (bHeadOrTail)    {
                    mfpNumThis = mfpNumThis.multiply(MFPNumeric.HALF);
                }
                mfpNumReturnReal = mfpNumReturnReal.add(mfpNumThis);
                mfpNumThis = mfpNumValueImage.multiply(mfpNumStepImage);
                if (bHeadOrTail)    {
                    mfpNumThis = mfpNumThis.multiply(MFPNumeric.HALF);
                }
                mfpNumReturnReal = mfpNumReturnReal.subtract(mfpNumThis);
                if (bConsiderImage) {
                    mfpNumThis = mfpNumValueReal.multiply(mfpNumStepImage);
                    if (bHeadOrTail)    {
                        mfpNumThis = mfpNumThis.multiply(MFPNumeric.HALF);
                    }
                    mfpNumReturnImage = mfpNumReturnImage.add(mfpNumThis);
                    mfpNumThis = mfpNumValueImage.multiply(mfpNumStepReal);
                    if (bHeadOrTail)    {
                        mfpNumThis = mfpNumThis.multiply(MFPNumeric.HALF);
                    }
                    mfpNumReturnImage = mfpNumReturnImage.add(mfpNumThis);
                }
            }
        }
        if (bUseBigDec2Calc)    {
            mfpNumReturnReal = new MFPNumeric(bigDecReturnReal);
            mfpNumReturnImage = new MFPNumeric(bigDecReturnImage);
        }
        if (mfpNumReturnImage.isActuallyZero()) {
            datumReturnNum.setDataValue(mfpNumReturnReal);
        } else {
            datumReturnNum.setComplex(mfpNumReturnReal, mfpNumReturnImage);
        }
        lVarNameSpaces.poll();
        return datumReturnNum;
    }
    
    /* integrate Gauss-Kronrod method */
    public static BaseData.DataClass integ_GK_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        if (tsParameter.size() < 4 || tsParameter.size() > 8)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumCheckFinalResult = new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.FALSE);
        if (tsParameter.size() == 8) {
            datumCheckFinalResult.copyTypeValueDeep(tsParameter.poll());
            datumCheckFinalResult.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);
        }
        boolean bCheckFinalResult = datumCheckFinalResult.getDataValue().booleanValue();
        
        BaseData.DataClass datumExceptNotEnoughSteps = new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.FALSE);
        if (tsParameter.size() == 7) {
            datumExceptNotEnoughSteps.copyTypeValueDeep(tsParameter.poll());
            datumExceptNotEnoughSteps.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);
        }
        boolean bExceptNotEnoughSteps = datumExceptNotEnoughSteps.getDataValue().booleanValue();
        
        BaseData.DataClass datumCheckConverge = new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.TRUE);
        if (tsParameter.size() == 6) {
            datumCheckConverge.copyTypeValueDeep(tsParameter.poll());
            datumCheckConverge.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);
        }
        boolean bCheckConverge = datumCheckConverge.getDataValue().booleanValue();
        
        BaseData.DataClass datumNumofSteps = new BaseData.DataClass(BaseData.DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
        if (tsParameter.size() == 5)    {
            datumNumofSteps.copyTypeValueDeep(tsParameter.poll());
            datumNumofSteps.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            if (datumNumofSteps.getDataValue().isActuallyNegative())    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);  // if number of steps is a parameter, it must be positive or 0.
            }
        }
        long nNumOfSteps = datumNumofSteps.getDataValue().longValue();
        if (nNumOfSteps > 1024)    {
            nNumOfSteps = 1024;    // number of steps should be no more than 1024.
        }
        BaseData.DataClass datumEndRaw = new BaseData.DataClass();
        datumEndRaw.copyTypeValueDeep(tsParameter.poll());
        if (datumEndRaw.getDataType() != BaseData.DATATYPES.DATUM_STRING)   {
            datumEndRaw.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
        }
        BaseData.DataClass datumStartRaw = new BaseData.DataClass();
        datumStartRaw.copyTypeValueDeep(tsParameter.poll());
        if (datumStartRaw.getDataType() != BaseData.DATATYPES.DATUM_STRING) {
            datumStartRaw.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
        }
        BaseData.DataClass datumStrDeltaVar = new BaseData.DataClass();
        datumStrDeltaVar.copyTypeValueDeep(tsParameter.poll());
        datumStrDeltaVar.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        String strDeltaVar = datumStrDeltaVar.getStringValue().trim();
        BaseData.DataClass datumStrExpr = new BaseData.DataClass();
        datumStrExpr.copyTypeValueDeep(tsParameter.poll());
        datumStrExpr.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        UnknownVarOperator.UnknownVariable var = new UnknownVarOperator.UnknownVariable(datumStrDeltaVar.getStringValue().trim());
        LinkedList<UnknownVarOperator.UnknownVariable> lUnknown = new LinkedList<UnknownVarOperator.UnknownVariable>();
        lUnknown.addFirst(var);
        AbstractExpr aeStrExpr = null;
        try {
            aeStrExpr = ExprAnalyzer.analyseExpression(datumStrExpr.getStringValue(), new BaseData.CurPos());
        } catch (Exception e) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        try {
            // need to evaluate considering second order integral, x+y + 1, y is a constant, so simplifymost will calculate y + 1 directly.
            aeStrExpr = aeStrExpr.evaluateAExpr(lUnknown, lVarNameSpaces); // look on unknown dim as single value dim.
        }catch (Exception e)	{
            // for like x + y + 1, y may not in lUnknown and lVarNameSpaces. This exception may be thrown as cannot get result exception. so catch
            // any exceptions that throw.
        }
        // variable list has to be added after aeStrExpr is constructed considering x + y + 1, x is in lUnknown but y is not, if l is added
        // aeStrExpr is constructed then both x and y are in lVarNameSpaces. Then y is treated as variable NULL.
        LinkedList<Variable> l = new LinkedList<Variable>();
        l.addFirst(var);
        lVarNameSpaces.addFirst(l);
        ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
        BaseData.DataClass datumEnd = datumEndRaw;
        if (datumEndRaw.getDataType() == BaseData.DATATYPES.DATUM_STRING)   {
            datumEnd = exprEvaluator.evaluateExpression(datumEndRaw.getStringValue(), new BaseData.CurPos());    // evaluate data end if it is a string.
            if (datumEnd == null)   {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
            }
            datumEnd.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
        }
        BaseData.DataClass datumStart = datumStartRaw;
        if (datumStartRaw.getDataType() == BaseData.DATATYPES.DATUM_STRING)   {
            datumStart = exprEvaluator.evaluateExpression(datumStartRaw.getStringValue(), new BaseData.CurPos());    // evaluate data start if it is a string.
            if (datumStart == null)   {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
            }
            datumStart.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
        }
        
        BaseData.DataClass[] darrayResults = MathLib.integByGaussKronrod(aeStrExpr/*datumStrExpr.getStringValue()*/, strDeltaVar,
                                                                datumStart, datumEnd, (int)nNumOfSteps,
                                                                bCheckConverge, bExceptNotEnoughSteps, bCheckFinalResult,
                                                                lVarNameSpaces, msfunctionInterrupter);
        lVarNameSpaces.poll();  // dont forget to pop-up stack.
        return darrayResults[0];
    }

    /* integrate adaptive method */
    public static BaseData.DataClass integrate_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        if (tsParameter.size() != 2 && tsParameter.size() != 4 && tsParameter.size() != 5)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        } else if (tsParameter.size() == 2)   {
            // indefinite integral
            BaseData.DataClass datumVarName = new BaseData.DataClass();
            datumVarName.copyTypeValueDeep(tsParameter.poll());
            datumVarName.changeDataType(BaseData.DATATYPES.DATUM_STRING);
            String strVarName = datumVarName.getStringValue().trim();
            BaseData.DataClass datumExpr = new BaseData.DataClass();
            datumExpr.copyTypeValueDeep(tsParameter.poll());
            datumExpr.changeDataType(BaseData.DATATYPES.DATUM_STRING);
            String strExpr = datumExpr.getStringValue().trim();
            UnknownVarOperator.UnknownVariable var = new UnknownVarOperator.UnknownVariable(strVarName);
            LinkedList<UnknownVarOperator.UnknownVariable> l = new LinkedList<UnknownVarOperator.UnknownVariable>();
            l.addFirst(var);
            try {
                AbstractExpr aeInteg = ExprAnalyzer.analyseExpression(strExpr, new BaseData.CurPos());
                aeInteg = aeInteg.simplifyAExprMost(l, lVarNameSpaces, new SimplifyParams(true, true, true)); // look on unknown dim as single value dim.
                AbstractExpr aeReturn = mspm.integInDefByPtn1VarIntegIdentifier(aeInteg, l, lVarNameSpaces);
                if (aeReturn == null) {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                }
                //String strOutputB4Optimize = aeReturn.output();
                //aeReturn = ExprAnalyzer.analyseExpression(strOutputB4Optimize, new CurPos());
                aeReturn = aeReturn.simplifyAExprMost(l, lVarNameSpaces, new SimplifyParams(false, true, true));
                BaseData.DataClass datumReturn = new BaseData.DataClass(BaseData.DATATYPES.DATUM_STRING, aeReturn.output());
                return datumReturn;
            } catch (SMErrProcessor.JSmartMathErrException ex) {
                if (ex.m_se.m_enumErrorType == SMErrProcessor.ERRORTYPES.ERROR_CANNOT_SOLVE_CALCULATION) {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                } else {
                    throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
            }
        } else {
            //definite integral
            boolean bUseBasicInteg = true;  // by default, using basic which is much quick.
            BaseData.DataClass datumNumofSteps = new BaseData.DataClass(BaseData.DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
            int nParamIdx = 0;
            if (tsParameter.size() == 5)    {
                datumNumofSteps.copyTypeValueDeep(tsParameter.peek());
                nParamIdx ++;
                datumNumofSteps.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            }
            long nNumOfSteps = datumNumofSteps.getDataValue().longValue();
            if (nNumOfSteps <= 0)    {
                bUseBasicInteg = false;    // use Gauss-Kronrod.
            }
            BaseData.DataClass datumStrDeltaVar = new BaseData.DataClass();
            datumStrDeltaVar.copyTypeValueDeep(tsParameter.get(nParamIdx + 2));
            datumStrDeltaVar.changeDataType(BaseData.DATATYPES.DATUM_STRING);
            Variable var = new Variable(datumStrDeltaVar.getStringValue().trim());
            LinkedList<Variable> l = new LinkedList<Variable>();
            l.addFirst(var);
            lVarNameSpaces.addFirst(l);
            ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
            if (bUseBasicInteg)    {
                BaseData.DataClass datumEnd = new BaseData.DataClass();
                datumEnd.copyTypeValueDeep(tsParameter.get(nParamIdx));
                if (datumEnd.getDataType() != BaseData.DATATYPES.DATUM_STRING)   {
                    datumEnd.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
                } else {
                    datumEnd = exprEvaluator.evaluateExpression(datumEnd.getStringValue(), new BaseData.CurPos());    // evaluate data end if it is a string.
                    if (datumEnd == null)   {
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
                    }
                    datumEnd.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
                }
                if (datumEnd.getReal().isInf() || datumEnd.getImage().isInf()) {
                    bUseBasicInteg = false;
                }
            }

            if (bUseBasicInteg)    {
                BaseData.DataClass datumStart = new BaseData.DataClass();
                datumStart.copyTypeValueDeep(tsParameter.get(nParamIdx + 1));
                if (datumStart.getDataType() != BaseData.DATATYPES.DATUM_STRING) {
                    datumStart.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
                } else {
                    datumStart = exprEvaluator.evaluateExpression(datumStart.getStringValue(), new BaseData.CurPos());    // evaluate data end if it is a string.
                    if (datumStart == null)   {
                        throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
                    }
                    datumStart.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);
                }
                if (datumStart.getReal().isInf() || datumStart.getImage().isInf()) {
                    bUseBasicInteg = false;
                }
            }
            lVarNameSpaces.poll();      // needs to poll first because it contains variable to integrated.

            if (bUseBasicInteg) {
                return integ_Basic_Function(tsParameter, lVarNameSpaces);
            } else {
                if (tsParameter.size() == 5) {
                    tsParameter.poll(); // the number of steps is set to be default.
                }
                return integ_GK_Function(tsParameter, lVarNameSpaces);
            }
        }
    }
    
    /* evaluate a lim expression */
    public static BaseData.DataClass lim_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size()!= 3)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumX0Expr = new BaseData.DataClass();
        BaseData.DataClass datumStrXVarName = new BaseData.DataClass();
        BaseData.DataClass datumStrExpr = new BaseData.DataClass();
        datumX0Expr.copyTypeValueDeep(tsParameter.poll());
        if (datumX0Expr.getDataType() != BaseData.DATATYPES.DATUM_STRING) {
            datumX0Expr.changeDataType(BaseData.DATATYPES.DATUM_COMPLEX);    // if not a string, then it is a value.
        }
        datumStrXVarName.copyTypeValueDeep(tsParameter.poll());
        datumStrXVarName.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        datumStrExpr.copyTypeValueDeep(tsParameter.poll());
        datumStrExpr.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        
        ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
        // now calculate x0
        boolean bLimFromLeft2Right = true;
        BaseData.DataClass datumX0 = datumX0Expr;
        if (datumX0Expr.getDataType() == BaseData.DATATYPES.DATUM_STRING) {
            String strX0Expr = datumX0Expr.getStringValue().trim();
            String[] strarrayX0ExprParts = strX0Expr.split(" ");
            if (strarrayX0ExprParts.length >= 1 && strarrayX0ExprParts[strarrayX0ExprParts.length - 1].length() >= 2)  {
                // one or more than one substrings
                String strLastPart = strarrayX0ExprParts[strarrayX0ExprParts.length - 1];
                if (strLastPart.substring(strLastPart.length() - 2).compareTo("+0") == 0) {
                    bLimFromLeft2Right = false;
                }
            } else if (strarrayX0ExprParts.length > 1 && strarrayX0ExprParts[strarrayX0ExprParts.length - 1].compareTo("0") == 0
                    && strarrayX0ExprParts[strarrayX0ExprParts.length - 2].length() >= 1
                    && strarrayX0ExprParts[strarrayX0ExprParts.length - 2].charAt(strarrayX0ExprParts[strarrayX0ExprParts.length - 2].length() - 1) == '+') {
                // at least two substrings
                bLimFromLeft2Right = false;
            }
            datumX0 = exprEvaluator.evaluateExpression(datumX0Expr.getStringValue(), new BaseData.CurPos());
            if (datumX0 == null)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
            }
        }
        
        // now calculate f_x0
        Variable var = new Variable(datumStrXVarName.getStringValue().trim());
        LinkedList<Variable> l = new LinkedList<Variable>();
        l.addFirst(var);
        lVarNameSpaces.addFirst(l);
        var.setValue(datumX0);
        BaseData.DataClass datumF_X0 = exprEvaluator.evaluateExpression(datumStrExpr.getStringValue(), new BaseData.CurPos());
        if (datumF_X0 == null)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
        }
        lVarNameSpaces.poll();
        
        boolean bConsiderImg = !datumX0.getImage().isActuallyZero();
        // now, calculate three points from left or right.
        MFPNumeric mfpNumStepReal = MFPNumeric.valueOf(MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN);
        if (bLimFromLeft2Right == false)    {
            mfpNumStepReal = mfpNumStepReal.negate();
        }
        if (datumX0.getReal().isPosInf())   {
            mfpNumStepReal = MFPNumeric.valueOf(MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX);
        } else if (datumX0.getReal().isNegInf())   {
            mfpNumStepReal = MFPNumeric.valueOf(-MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX);
        }
        MFPNumeric mfpNumStepImage = MFPNumeric.valueOf(MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN);
        if (datumX0.getImage().isPosInf())   {
            mfpNumStepImage = MFPNumeric.valueOf(MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX);
        } else if (datumX0.getImage().isNegInf())   {
            mfpNumStepImage = MFPNumeric.valueOf(-MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX);
        }
        
        BaseData.DataClass datumStepReal = new BaseData.DataClass(BaseData.DATATYPES.DATUM_DOUBLE, mfpNumStepReal);
        BaseData.DataClass datumStepImage = new BaseData.DataClass(BaseData.DATATYPES.DATUM_DOUBLE, mfpNumStepImage);
        BaseData.DataClass[] datumArrayXs = new BaseData.DataClass[3];
        BaseData.DataClass[] datumArrayF_Xs = new BaseData.DataClass[datumArrayXs.length];
        for (int idx = 0; idx < datumArrayXs.length; idx ++)    {
            datumArrayXs[idx] = new BaseData.DataClass();
            BaseData.DataClass datumMultiply = new BaseData.DataClass();
            datumMultiply.setDataValue(datumArrayXs.length - idx);
            BaseData.DataClass datumDistanceReal = BuiltinProcedures.evaluateMultiplication(datumStepReal, datumMultiply);
            if (datumX0.getReal().isInf()) {
                datumArrayXs[idx] = datumDistanceReal;
            } else {
                datumArrayXs[idx] = BuiltinProcedures.evaluateSubstraction(datumX0.getRealDataClass(), datumDistanceReal);
            }
            if (bConsiderImg)   {
                BaseData.DataClass datumDistanceImage = BuiltinProcedures.evaluateMultiplication(datumStepImage, datumMultiply);
                BaseData.DataClass datumXsReal = datumArrayXs[idx];
                datumArrayXs[idx] = new BaseData.DataClass();
                if (datumX0.getImage().isInf()) {
                    datumArrayXs[idx].setComplex(datumXsReal, datumDistanceImage);
                } else {
                    BaseData.DataClass datumXsImage = BuiltinProcedures.evaluateSubstraction(datumX0.getImageDataClass(), datumDistanceImage);
                    datumArrayXs[idx].setComplex(datumXsReal, datumXsImage);
                }
            }

            lVarNameSpaces.addFirst(l);
            var.setValue(datumArrayXs[idx]);
            datumArrayF_Xs[idx] = exprEvaluator.evaluateExpression(datumStrExpr.getStringValue(), new BaseData.CurPos());
            if (datumArrayF_Xs[idx] == null)    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
            }

            lVarNameSpaces.poll();
        }
        
        datumReturnNum = MathLib.getLimValue(datumArrayXs, datumArrayF_Xs, datumX0, datumF_X0);
        return datumReturnNum;
    }
    
    /* evaluate a string expression */
    public static BaseData.DataClass evaluate_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() % 2 != 1)    //number of parameters must be odd.
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        LinkedList<Variable> l = new LinkedList<Variable>();
        lVarNameSpaces.addFirst(l);
        int nNumOfDeltaVars = (tsParameter.size() - 1)/2;
        for (int idx = 0; idx < nNumOfDeltaVars; idx ++) {
            BaseData.DataClass datumValue = new BaseData.DataClass();
            BaseData.DataClass datumStrDeltaVar = new BaseData.DataClass();
            datumValue.copyTypeValueDeep(tsParameter.poll());
            datumStrDeltaVar.copyTypeValueDeep(tsParameter.poll());
            datumStrDeltaVar.changeDataType(BaseData.DATATYPES.DATUM_STRING);
            Variable var = new Variable(datumStrDeltaVar.getStringValue().trim());
            var.setValue(datumValue);
            l.addFirst(var);
            
        }
        BaseData.DataClass datumStrExpr = new BaseData.DataClass();
        datumStrExpr.copyTypeValueDeep(tsParameter.poll());
        
        // here out-variable name spaces (lVarNameSpaces) must be heritated from upper level because of the following case:
        // evaluate("evaluate(\"x+y+3\",\"x\",4)", "y", 1)
        ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
        datumReturnNum = exprEvaluator.evaluateExpression(
                                    datumStrExpr.getStringValue(), new BaseData.CurPos());
        lVarNameSpaces.poll();
        if (datumReturnNum == null)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_VOID_DATA);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass sleep_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumSleepMilliSeconds = new BaseData.DataClass();
        datumSleepMilliSeconds.copyTypeValueDeep(tsParameter.poll());
        datumSleepMilliSeconds.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        Thread.sleep((int)datumSleepMilliSeconds.getDataValue().longValue());
        datumReturnNum = null;
        return datumReturnNum;
    }
    
    public static BaseData.DataClass system_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumSystemCmd = new BaseData.DataClass();
        datumSystemCmd.copyTypeValueDeep(tsParameter.poll());
        datumSystemCmd.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        String strSysCmd = datumSystemCmd.getStringValue();
        Thread threadExecI = null, threadExecO = null, threadExecE = null;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(strSysCmd);
            // at this moment, only support output.
            SysExecProcIOE sysExecI = new SysExecProcIOE(p, 1, msstreamConsoleInput, msstreamLogOutput, msstreamLogOutput);
            //SysExecProcIOE sysExecO = new SysExecProcIOE(p, 0, msstreamConsoleInput, msstreamLogOutput, msstreamLogOutput);
            SysExecProcIOE sysExecE = new SysExecProcIOE(p, 2, msstreamConsoleInput, msstreamLogOutput, msstreamLogOutput);
            
            threadExecI = new Thread(sysExecI);
            //Thread threadExecO = new Thread(sysExecO);
            threadExecE = new Thread(sysExecE);
            threadExecI.start();
            //threadExecO.start();
            threadExecE.start();
            int exitVal = p.waitFor();
            threadExecI.interrupt();
            //threadExecO.interrupt();
            threadExecE.interrupt();
            datumReturnNum.setDataValue(new MFPNumeric(exitVal));
        } catch (Throwable t) {
            if (threadExecI != null && threadExecI.isAlive()) {
                threadExecI.interrupt();
            }
            if (threadExecO != null && threadExecO.isAlive()) {
                threadExecO.interrupt();
            }
            if (threadExecE != null && threadExecE.isAlive()) {
                threadExecE.interrupt();
            }
            
            if (p != null) {
                p.destroy();
            }
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_RUNTIME_ERROR);
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass conv_str_to_ints_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* string length */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumString = new BaseData.DataClass();
        datumString.copyTypeValueDeep(tsParameter.poll());
        datumString.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        String str = datumString.getStringValue();
        if (str == null) {
            str = "";
        }
        BaseData.DataClass[] datumarray = new BaseData.DataClass[str.length()];
        for (int idx = 0; idx < str.length(); idx ++) {
            BaseData.DataClass datum = new BaseData.DataClass();
            datum.setDataValue(new MFPNumeric(str.codePointAt(idx)), BaseData.DATATYPES.DATUM_INTEGER);
            datumarray[idx] = datum;
        }
        datumReturnNum.setDataList(datumarray);
        return datumReturnNum;
    }    
    
    public static BaseData.DataClass conv_ints_to_str_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* string length */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumInput = new BaseData.DataClass();
        datumInput.copyTypeValueDeep(tsParameter.poll());
        String str = "";
        if (datumInput.getDataType() != BaseData.DATATYPES.DATUM_REF_DATA) {
            datumInput.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            int nValue = datumInput.getDataValue().intValue();
            try {
                char[] chars = Character.toChars(nValue);
                for (char c : chars) {
                    str += c;
                }
            } catch (IllegalArgumentException e) {
                char c = 0; // invalid char.
                str += c;
            }
        } else {
            int[] narrayValues = new int[datumInput.getDataListSize()];
            for (int idx = 0; idx < datumInput.getDataListSize(); idx ++) {
                BaseData.DataClass datum = datumInput.getDataList()[idx];
                datum.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
                narrayValues[idx] = datum.getDataValue().intValue();
                try {
                    char[] chars = Character.toChars(narrayValues[idx]);
                    for (char c : chars) {
                        str += c;
                    }
                } catch (IllegalArgumentException e) {
                    char c = 0; // invalid char.
                    str += c;
                }
            }
        }
        datumReturnNum.setStringValue(str);
        return datumReturnNum;
    }    
    
    public static BaseData.DataClass strlen_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* string length */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumString = new BaseData.DataClass();
        datumString.copyTypeValueDeep(tsParameter.poll());
        datumString.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        MFPNumeric mfpNumStrLen = new MFPNumeric(datumString.getStringValue().length());
        datumReturnNum.setDataValue(mfpNumStrLen, BaseData.DATATYPES.DATUM_INTEGER);
        return datumReturnNum;
    }
    
    /* string copy, string index must be integer so convert MFPNumeric to long then to int */
    public static BaseData.DataClass strcpy_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() > 6 || tsParameter.size() < 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumStrSrc = new BaseData.DataClass();
        datumStrSrc.copyTypeValueDeep(tsParameter.getLast());
        tsParameter.removeLast();
        datumStrSrc.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        BaseData.DataClass datumStrDest = new BaseData.DataClass();
        datumStrDest.copyTypeValueDeep(tsParameter.getLast());
        tsParameter.removeLast();
        datumStrDest.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        BaseData.DataClass[] datumlistParams = new BaseData.DataClass[4];
        int nIndex = 0;
        for (; nIndex < 4; nIndex ++)    {
            datumlistParams[nIndex] = new BaseData.DataClass();
            if (nIndex == 0)    {
                // src start
                datumlistParams[nIndex].setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_INTEGER);
            } else if (nIndex == 1)    {
                // src end (one character passed the last character)
                datumlistParams[nIndex].setDataValue(
                        new MFPNumeric(datumStrSrc.getStringValue().length()),
                        BaseData.DATATYPES.DATUM_INTEGER);
            } else if (nIndex == 2)    {
                // dest start
                datumlistParams[nIndex].setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_INTEGER);
            } else if (nIndex == 3)    {
                // dest end (one character passed the last character)
                datumlistParams[nIndex].setDataValue(
                        new MFPNumeric(datumStrDest.getStringValue().length()),
                        BaseData.DATATYPES.DATUM_INTEGER);
            }
        }
        nIndex = 0;
        while(tsParameter.size() > 0)    {
            datumlistParams[nIndex].copyTypeValueDeep(tsParameter.getLast());
            tsParameter.removeLast();
            datumlistParams[nIndex].changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            nIndex ++;
        }
        String strSrc = datumStrSrc.getStringValue();
        String strDest = datumStrDest.getStringValue();
        int nSrcStart = (int)datumlistParams[0].getDataValue().longValue();
        if (nSrcStart > strSrc.length() || nSrcStart < 0)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        int nSrcEnd = (int)datumlistParams[1].getDataValue().longValue();
        if (nSrcEnd > strSrc.length() || nSrcEnd < nSrcStart)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        int nDestStart = (int)datumlistParams[2].getDataValue().longValue();
        if (nDestStart > strDest.length() || nDestStart < 0)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        int nDestEnd = (int)datumlistParams[3].getDataValue().longValue();
        if (nDestEnd > strDest.length() || nDestEnd < nDestStart)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        String strReturn = strDest.substring(0, nDestStart)
                + strSrc.substring(nSrcStart, nSrcEnd)
                + strDest.substring(nDestEnd);
        datumReturnNum.setStringValue(strReturn);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass strcat_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* string catenate */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() < 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        datumReturnNum.setStringValue("");
        while (tsParameter.size() > 0)    {
            BaseData.DataClass datumStr = new BaseData.DataClass();
            datumStr.copyTypeValueDeep(tsParameter.poll());
            datumStr.changeDataType(BaseData.DATATYPES.DATUM_STRING);
            datumReturnNum.setStringValue(datumStr.getStringValue() + datumReturnNum.getStringValue());
        }
        return datumReturnNum;
    }
    
    /* string compare (case sensative or ignore case, string index must be integer so convert MFPNumeric to long then to int */
    public static BaseData.DataClass strcmp_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() > 6 || tsParameter.size() < 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumStrSrc = new BaseData.DataClass();
        datumStrSrc.copyTypeValueDeep(tsParameter.getLast());
        tsParameter.removeLast();
        datumStrSrc.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        BaseData.DataClass datumStrDest = new BaseData.DataClass();
        datumStrDest.copyTypeValueDeep(tsParameter.getLast());
        tsParameter.removeLast();
        datumStrDest.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        BaseData.DataClass[] datumlistParams = new BaseData.DataClass[4];
        int nIndex = 0;
        for (; nIndex < 4; nIndex ++)    {
            datumlistParams[nIndex] = new BaseData.DataClass();
            if (nIndex == 0)    {
                // src start
                datumlistParams[nIndex].setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_INTEGER);
            } else if (nIndex == 1)    {
                // src end (one character passed the last character)
                datumlistParams[nIndex].setDataValue(
                        new MFPNumeric(datumStrSrc.getStringValue().length()),
                        BaseData.DATATYPES.DATUM_INTEGER);
            } else if (nIndex == 2)    {
                // dest start
                datumlistParams[nIndex].setDataValue(MFPNumeric.ZERO, BaseData.DATATYPES.DATUM_INTEGER);
            } else if (nIndex == 3)    {
                // dest end (one character passed the last character)
                datumlistParams[nIndex].setDataValue(
                        new MFPNumeric(datumStrDest.getStringValue().length()),
                        BaseData.DATATYPES.DATUM_INTEGER);
            }
        }
        nIndex = 0;
        while(tsParameter.size() > 0)    {
            datumlistParams[nIndex].copyTypeValueDeep(tsParameter.getLast());
            tsParameter.removeLast();
            datumlistParams[nIndex].changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            nIndex++;
        }
        String strSrc = datumStrSrc.getStringValue();
        String strDest = datumStrDest.getStringValue();
        int nSrcStart = (int)datumlistParams[0].getDataValue().longValue();
        if (nSrcStart > strSrc.length() || nSrcStart < 0)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        int nSrcEnd = (int)datumlistParams[1].getDataValue().longValue();
        if (nSrcEnd > strSrc.length() || nSrcEnd < nSrcStart)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        int nDestStart = (int)datumlistParams[2].getDataValue().longValue();
        if (nDestStart > strDest.length() || nDestStart < 0)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        int nDestEnd = (int)datumlistParams[3].getDataValue().longValue();
        if (nDestEnd > strDest.length() || nDestEnd < nDestStart)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        int nReturn = 0;
        if (strNameLowCase.compareTo("strcmp") == 0) {
            nReturn = strSrc.substring(nSrcStart, nSrcEnd)
                        .compareTo(strDest.substring(nDestStart, nDestEnd));
        } else {    // stricmp
            nReturn = strSrc.substring(nSrcStart, nSrcEnd)
                        .compareToIgnoreCase(strDest.substring(nDestStart, nDestEnd));
        }
        datumReturnNum.setDataValue(new MFPNumeric(nReturn), BaseData.DATATYPES.DATUM_INTEGER);
        return datumReturnNum;
    }
    
    /* sub-string, string index must be integer so convert MFPNumeric to long then to int */
    public static BaseData.DataClass strsub_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() > 3 || tsParameter.size() < 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumStr = new BaseData.DataClass();
        datumStr.copyTypeValueDeep(tsParameter.getLast());
        tsParameter.removeLast();
        datumStr.changeDataType(BaseData.DATATYPES.DATUM_STRING);
        BaseData.DataClass datumStart = new BaseData.DataClass();
        datumStart.copyTypeValueDeep(tsParameter.getLast());
        tsParameter.removeLast();
        datumStart.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        int nStart = (int)datumStart.getDataValue().longValue();
        if (nStart < 0 || nStart > datumStr.getStringValue().length())    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        BaseData.DataClass datumEnd = new BaseData.DataClass();
        int nEnd = datumStr.getStringValue().length();
        datumEnd.setDataValue(new MFPNumeric(nEnd), BaseData.DATATYPES.DATUM_INTEGER);
        if (tsParameter.size() > 0)    {
            datumEnd.copyTypeValueDeep(tsParameter.getLast());
            tsParameter.removeLast();
            datumEnd.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
            if (datumEnd.getDataValue().longValue() < nStart || datumEnd.getDataValue().longValue() > datumStr.getStringValue().length())    {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            nEnd = (int)datumEnd.getDataValue().longValue();
        }
        datumReturnNum.setStringValue(datumStr.getStringValue().substring(nStart, nEnd));
        return datumReturnNum;
    }
    
    /* convert to string, to lower case string or to upper case string */
    public static BaseData.DataClass to_string_Function(String strNameLowCase, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumValue = new BaseData.DataClass();
        datumValue.copyTypeValueDeep(tsParameter.poll());
        String strValue = "";
        if (datumValue.getDataType() == BaseData.DATATYPES.DATUM_STRING) {
            strValue = datumValue.getStringValue();
        } else  {
            strValue = datumValue.output();
        }
        if (strNameLowCase.compareTo("to_lowercase_string") == 0)    {
            datumReturnNum.setStringValue(strValue.toLowerCase(Locale.US));
        } else if (strNameLowCase.compareTo("to_uppercase_string") == 0)    {
            datumReturnNum.setStringValue(strValue.toUpperCase(Locale.US));
        } else  {   // to_string or tostring
            datumReturnNum.setStringValue(strValue);
        }
        return datumReturnNum;
    }
    
    /* convert an expr to string for testing purpose */
    public static BaseData.DataClass expr_to_string_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumValue = new BaseData.DataClass();
        datumValue.copyTypeValueDeep(tsParameter.poll());
        if (datumValue.getDataType() != BaseData.DATATYPES.DATUM_STRING) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        } else  {
            String strExpr = datumValue.getStringValue();
            String strOutput;
            AbstractExpr aexpr = AEInvalid.AEINVALID;
            try {
                aexpr = ExprAnalyzer.analyseExpression(strExpr, new BaseData.CurPos());
                strOutput = aexpr.output();
            } catch (SMErrProcessor.JSmartMathErrException ex) {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_FORMAT);
            }
            datumReturnNum.setStringValue(strOutput);
        }
        return datumReturnNum;
    }
    
    /* trim string */
    public static BaseData.DataClass trim_string_Function(int nTrimSide, LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumValue = new BaseData.DataClass();
        datumValue.copyTypeValueDeep(tsParameter.poll());
        if (datumValue.getDataType() != BaseData.DATATYPES.DATUM_STRING) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        String strInput = datumValue.getStringValue();
        String strOutput = strInput;
        if (nTrimSide < 0) {
            // trim left
            int i = 0;
            while (i < strInput.length() && strInput.charAt(i) <= ' ') {
                i++;
            }
            strOutput = strInput.substring(i);
        } else if (nTrimSide > 0) {
            // trim right
            int i = strInput.length()-1;
            while (i >= 0 && Character.isWhitespace(strInput.charAt(i))) {
                i--;
            }
            strOutput = strInput.substring(0,i+1);
        } else  {   // nTrimSide == 0
            strOutput = strInput.trim();
        }
        datumReturnNum.setStringValue(strOutput);
        return datumReturnNum;
    }
    
    /* split string */
    public static BaseData.DataClass split_string_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException, InterruptedException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumValue = new BaseData.DataClass();
        datumValue.copyTypeValueDeep(tsParameter.getLast());
        if (datumValue.getDataType() != BaseData.DATATYPES.DATUM_STRING) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        String strInput = datumValue.getStringValue();
        BaseData.DataClass datumReg = new BaseData.DataClass();
        datumReg.copyTypeValueDeep(tsParameter.getFirst());
        if (datumReg.getDataType() != BaseData.DATATYPES.DATUM_STRING) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        String strReg = datumReg.getStringValue();
        String[] strarrayOutputs = new String[0];
        try {
            strarrayOutputs = strInput.split(strReg);
        } catch (PatternSyntaxException e) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_FORMAT);
        }
        BaseData.DataClass[] dataarrayOutputs = new BaseData.DataClass[strarrayOutputs.length];
        for (int idx = 0; idx < strarrayOutputs.length; idx ++) {
            BaseData.DataClass datumReturn = new BaseData.DataClass(BaseData.DATATYPES.DATUM_STRING, strarrayOutputs[idx]);
            dataarrayOutputs[idx] = datumReturn;
        }
        datumReturnNum.setDataList(dataarrayOutputs, BaseData.DATATYPES.DATUM_REF_DATA);
        return datumReturnNum;
    }
    
    public static BaseData.DataClass get_num_of_results_sets_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* get number of solved result sets*/
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        // a read only function. Will not change parameter.
        if (tsParameter.size() != 1)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumValue = tsParameter.poll(); // need not to do deep copy coz it is read only function
        //datumValue.copyTypeValueDeep(tsParameter.poll());
        if (datumValue.getDataType() != BaseData.DATATYPES.DATUM_REF_DATA) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        } else  {
            int nNumOfResultSets = datumValue.getDataListSize();
            datumReturnNum.setDataValue(new MFPNumeric(nNumOfResultSets));
        }
        return datumReturnNum;
    }
    
    public static BaseData.DataClass get_solved_results_set_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* get one solved results set */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumIndex = tsParameter.poll().cloneSelf();  // index should not be too large. it must be an integer.
        datumIndex.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        BaseData.DataClass datumResultSets = tsParameter.poll().cloneSelf();
        if (datumResultSets.getDataType() != BaseData.DATATYPES.DATUM_REF_DATA) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        if (datumIndex.getDataValue().longValue() < 0
                || datumIndex.getDataValue().longValue() >= datumResultSets.getDataListSize())   {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        
        datumReturnNum = datumResultSets.getDataList()[(int)datumIndex.getDataValue().longValue()];
        return datumReturnNum;
    }
    
    public static BaseData.DataClass get_variable_results_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException    /* get all results of a variable */
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() != 2)
        {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        BaseData.DataClass datumVarIndex = tsParameter.poll().cloneSelf();   // variable index shouldn't be too large. It should be an integer.
        datumVarIndex.changeDataType(BaseData.DATATYPES.DATUM_INTEGER);
        if (datumVarIndex.getDataValue().longValue() < 0)   {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        BaseData.DataClass datumResultSets = tsParameter.poll().cloneSelf();
        if (datumResultSets.getDataType() != BaseData.DATATYPES.DATUM_REF_DATA) {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        BaseData.DataClass[] arrayDataValues = new BaseData.DataClass[datumResultSets.getDataListSize()];
        for (int idx = 0; idx < datumResultSets.getDataListSize(); idx ++)  {
            if (datumVarIndex.getDataValue().longValue() >= datumResultSets.getDataList()[idx].getDataListSize())   {
                throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
            arrayDataValues[idx]
                    = datumResultSets.getDataList()[idx].getDataList()[(int)datumVarIndex.getDataValue().longValue()];
        }
        datumReturnNum.setDataList(arrayDataValues);
        return datumReturnNum;
    }
    
    /* if (condition1, trueresult1, condition2, trueresult2, ... falseresult) */
    public static BaseData.DataClass iff_Function(LinkedList<BaseData.DataClass> tsParameter, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws ErrProcessor.JFCALCExpErrException
    {
        BaseData.DataClass datumReturnNum = new BaseData.DataClass();
        if (tsParameter.size() < 3 || (tsParameter.size() - 1) % 2 != 0)    {
            throw new ErrProcessor.JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        
        // keep in mind that in tsParameter the order of params is from last to first.
        int idx = 0;
        boolean bGetValue = false;
        while (idx <= tsParameter.size() - 2)    {
            BaseData.DataClass datumIfCondition = tsParameter.get(tsParameter.size() - 1 - idx).cloneSelf();
            datumIfCondition.changeDataType(BaseData.DATATYPES.DATUM_BOOLEAN);
            if (datumIfCondition.isEqual(new BaseData.DataClass(BaseData.DATATYPES.DATUM_BOOLEAN, MFPNumeric.ZERO)))    {
                // condition == false
                idx += 2;
            } else    {
                // condition == true
                datumReturnNum.copyTypeValueDeep(tsParameter.get(tsParameter.size() - 2 - idx));
                bGetValue = true;
                break;
            }
        }
        if (bGetValue == false) {
            datumReturnNum.copyTypeValueDeep(tsParameter.getFirst());
        }
        return datumReturnNum;
    }
}