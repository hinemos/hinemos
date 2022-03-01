/*_############################################################################
  _## 
  _##  SNMP4J - ArgumentParser.java  
  _## 
  _##  Copyright (C) 2003-2020  Frank Fock (SNMP4J.org)
  _##  
  _##  Licensed under the Apache License, Version 2.0 (the "License");
  _##  you may not use this file except in compliance with the License.
  _##  You may obtain a copy of the License at
  _##  
  _##      http://www.apache.org/licenses/LICENSE-2.0
  _##  
  _##  Unless required by applicable law or agreed to in writing, software
  _##  distributed under the License is distributed on an "AS IS" BASIS,
  _##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  _##  See the License for the specific language governing permissions and
  _##  limitations under the License.
  _##  
  _##########################################################################*/
package org.snmp4j.util;

import java.text.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.snmp4j.smi.OctetString;

/**
 * The {@code ArgumentParser} parsers a command line array into Java
 * objects and associates each object with the corresponding command line option
 * according to predefined schemes for options and parameters.
 * <p>
 * The format specification for options is:</p>
 * <pre>
 * [-&lt;option&gt;\[&lt;type&gt;[\&lt;&lt;regex&gt;\&gt;]{&lt;parameter&gt;[=&lt;default&gt;]}\]] ...
 * </pre>
 * where
 * <ul>
 * <li>'-' indicates a mandatory option ('+' would indicate an optional
 * option)</li>
 * <li>&lt;option&gt; is the name of the option, for example 'h' for 'help'</li>
 * <li>&lt;type&gt; is one of 'i' (integer), 'l' (long), 'o' (octet string),
 * and 's' (string)</li>
 * <li>&lt;regex&gt; is a regular expression pattern that describes valid
 * values</li>
 * <li>&lt;default&gt; is a default value. If a default value is given, then
 * a mandatory option is in fact optional</li>
 * </ul>
 * <p>
 * The format specification for parameters is:</p>
 * <pre>
 * [-&lt;parameter&gt;[&lt;type&gt;[&lt;&lt;regex&gt;&gt;]{[=&lt;default&gt;]}]]... [+&lt;optionalParameter&gt;[&lt;type&gt;[&lt;&lt;regex&gt;&gt;]{[=&lt;default&gt;]}]]... [&lt;..&gt;]
 * </pre>
 * where
 * <ul>
 * <li>'-' indicates a mandatory parameter whereas '+' would indicate an
 * optional parameter which must not be followed by a mandatory parameter</li>
 * <li>&lt;parameter&gt; is the name of the parameter, for example 'port'</li>
 * <li>&lt;type&gt; is one of 'i' (integer), 'l' (long), and 's' (string)</li>
 * <li>&lt;regex&gt; is a regular expression pattern that describes valid values</li>
 * <li>&lt;default&gt; is a default value</li>
 * <li>&lt;..&gt; (two consecutive dots after a space at the end of the pattern)
 * indicate that the last parameter may occur more than once</li>
 * </ul>
 *
 * @author Frank Fock
 * @version 1.10
 * @since 1.9
 */
public class ArgumentParser {

  public static final String[] TYPES = { "i", "l", "s", "o" };
  public static final int TYPE_INTEGER = 0;
  public static final int TYPE_LONG = 1;
  public static final int TYPE_STRING = 2;
  public static final int TYPE_OCTET_STRING = 3;

  private Map<String, ArgumentFormat> optionFormat;
  private Map<? extends String, ? extends ArgumentFormat> parameterFormat;

  /**
   * Creates an argument parser with the specified option and parameter formats.
   * @param optionFormat
   *    the option format pattern to parse (see {@link ArgumentParser}).
   * @param parameterFormat
   *    the parameter format pattern to parse (see {@link ArgumentParser}).
   */
  public ArgumentParser(String optionFormat, String parameterFormat) {
    this.optionFormat = parseFormat(optionFormat, false);
    this.parameterFormat = parseFormat(parameterFormat, true);
  }

  public Map<String, ArgumentFormat> getOptionFormat() {
    return optionFormat;
  }

  public Map<? extends String, ? extends ArgumentFormat> getParameterFormat() {
    return parameterFormat;
  }

  protected static Map<String, ArgumentFormat> parseFormat(String format, boolean parameterFormat) {
    Map<String, ArgumentFormat> options = new LinkedHashMap<String, ArgumentFormat>();
    ArgumentFormat last = null;
    StringTokenizer st = new StringTokenizer(format, " ");
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if ("..".equals(token)) {
        if (last != null) {
          last.vararg = true;
          break;
        }
        else {
          throw new IllegalArgumentException("'..' without parameter definition");
        }
      }
      ArgumentFormat af = new ArgumentFormat();
      last = af;
      af.parameter = parameterFormat;
      af.mandatory = (token.charAt(0) != '+');
      token = token.substring(1);
      if (token.endsWith("]")) {
        af.option = token.substring(0, token.indexOf('['));
        token = token.substring(af.option.length()+1, token.length()-1);
        StringTokenizer pt = new StringTokenizer(token, ",", true);
        List<ArgumentParameter> params = new ArrayList<ArgumentParameter>();
        String inRegex = null;
        for (int i=1; (pt.hasMoreTokens()); i++) {
          String param = pt.nextToken();
          if (inRegex != null) {
            inRegex += param;
            param = inRegex;
          }
          else if (",".equals(param)) {
            continue;
          }
          if (param.indexOf('<')>0 && param.indexOf('>') <0) {
            inRegex = param;
          }
          ArgumentParameter ap = new ArgumentParameter();
          ap.name = ""+i;
          if (param.endsWith("}")) {
            param = parseParameterFormat(param, ap);
          }
          if (param.endsWith(">")) {
            inRegex = null;
            int regexPos = param.indexOf('<');
            ap.pattern =
                Pattern.compile(param.substring(regexPos+1, param.length()-1));
            param = param.substring(0, regexPos);
            if (param.endsWith("}")) {
              parseParameterFormat(param, ap);
            }
            else {
              ap.type = getType(param);
            }
          }
          else if (inRegex != null) {
            continue;
          }
          else {
            ap.type = getType(param);
          }
          params.add(ap);
        }
        af.params = params.toArray(new ArgumentParameter[params.size()]);
      }
      else {
        af.option = token;
        if (af.parameter) {
          throw new IllegalArgumentException("Parameter "+token+" has no type");
        }
      }
      options.put(af.option, af);
    }
    return options;
  }

  private static String parseParameterFormat(String param, ArgumentParameter ap) {
    int posEnd = param.indexOf('<');
    posEnd = posEnd>0 ? posEnd :  param.indexOf("{");
    ap.type = getType(param.substring(0, posEnd));
    String defaultValue = param.substring(param.indexOf('{')+1, param.length()-1);
    int posEqual = defaultValue.indexOf('=');
    if (posEqual >= 0) {
      ap.defaultValue = defaultValue.substring(posEqual+1);
      ap.name = defaultValue.substring(0, posEqual);
    }
    else {
      ap.name = defaultValue;
    }
    int posParamEnd = param.indexOf('>');
    if (posParamEnd > 0) {
      posEnd = posParamEnd+1;
    }
    param = param.substring(0, posEnd);
    return param;
  }

  private static int getType(String type) {
    return Arrays.binarySearch(TYPES, type);
  }

  /**
   * Parses the given command line and returns a map of parameter/option names
   * to a <code>List</code> of values. Each value may be of type
   * <code>Integer</code>, <code>Long</code>, and <code>String</code>.
   * @param args
   *    the command line argument list.
   * @return Map
   *    a map that returns options and parameters in the order they have been
   *    parsed, where each map entry has the option/parameter name as key and
   *    the value as value.
   * @throws ParseException
   *    if the command line does not match the patterns for options and
   *    parameters.
   */
  @SuppressWarnings("unchecked")
  public Map<String,List<?>> parse(String[] args) throws ParseException {
    Map<String,List<?>> options = new LinkedHashMap<String,List<?>>();
    Iterator<? extends ArgumentFormat> params = parameterFormat.values().iterator();
    ArgumentFormat lastFormat = null;
    for (int i=0; i<args.length; i++) {
      if (args[i].length() == 0) {
        continue;
      }
      ArgumentFormat format;
      if (args[i].charAt(0) == '-') {
        String option = args[i].substring(1);
        format = optionFormat.get(option);
        if (format == null) {
          throw new ParseException("Unknown option '"+option+"' at position "+i, i);
        }
      }
      else {
        format = params.hasNext() ? params.next() :
            ((lastFormat != null) && (lastFormat.isVariableLength())) ? lastFormat : null;
        if (format == null) {
          throw new ParseException("Unrecognized parameter at position "+i, i);
        }
      }
      if ((format.getParameters() != null) &&
          (format.getParameters().length > 0)) {
        int diff = (format.isParameter()) ? 1 : 0;
        List values = parseValues(args, i+(1-diff), format);
        i += Math.max(values.size() - diff, 0);
        if (format.isVariableLength() &&
            options.containsKey(format.getOption())) {
          List extValues = options.get(format.getOption());
          extValues.addAll(values);
        }
        else {
          addValues2Option(format.getOption(), values, options);
        }
      }
      else {
        addValues2Option(format.getOption(), null, options);
      }
      lastFormat = format;
    }
    while (params.hasNext()) {
      ArgumentFormat af = params.next();
      if (af.isMandatory()) {
        if (af.getParameters() != null && af.getParameters().length > 0) {
          List defaults = new ArrayList();
          for (ArgumentParameter parameter : af.getParameters()) {
            if (parameter.getDefaultValue() == null) {
              throw new ArgumentParseException(-1, null, af, parameter);
            }
            else {
              defaults.add(parseParameterValue(parameter,
                  parameter.getDefaultValue(),
                  af, defaults.size()));

            }
          }
          addValues2Option(af.getOption(), defaults, options);
        }
      }
      else {
        throw new ArgumentParseException(-1, null, af, null);
      }
    }
    for (ArgumentFormat of : optionFormat.values()) {
      if (of.isMandatory() && !options.containsKey(of.getOption())) {
        List defaults = new ArrayList();
        if (of.getParameters() != null) {
          for (int i = 0; i < of.getParameters().length; i++) {
            if (of.getParameters()[i] == null) {
              continue;
            }
            if (of.getParameters()[i].getDefaultValue() != null) {
              defaults.add(parseParameterValue(of.getParameters()[i],
                      of.getParameters()[i].getDefaultValue(),
                      of, i));
            }
          }
        }
        if (defaults.size() == 0) {
          throw new ArgumentParseException(-1, null, of, ((of.getParameters() != null && of.getParameters().length > 0) ? of.getParameters()[0] : null));
        } else {
          addValues2Option(of.getOption(), defaults, options);
        }
      }
    }
    return options;
  }

  @SuppressWarnings("unchecked")
  protected void addValues2Option(String option, List values, Map<String,List<?>> options) {
    List existingValues = options.get(option);
    if ((existingValues != null) && (values != null)) {
      existingValues.addAll(values);
    }
    else {
      options.put(option, values);
    }
  }

  protected List<?> parseValues(String[] args, int offset, ArgumentFormat format) throws ParseException {
    int numParams = format.getParameters().length;
    List<Object> values = new ArrayList<Object>(numParams);
    for (int i=0; (i+offset < args.length) && (i < numParams); i++) {
      try {
        values.add(parseParameterValue(format.getParameters()[i],
                                       args[i + offset], format,
                                       i+offset));
      }
      catch (ArgumentParseException apex) {
        throw apex;
      }
      catch (Exception ex) {
        ex.printStackTrace();
        int pos = i + offset;
        throw new ArgumentParseException(pos,
                                         args[pos],
                                         format,
                                         format.getParameters()[i]);
      }
    }
    return values;
  }

  protected Object parseParameterValue(ArgumentParameter type,
                                       String value,
                                       ArgumentFormat format, int pos) throws
      org.snmp4j.util.ArgumentParser.ArgumentParseException
  {
    if (value.startsWith("'") && value.endsWith("'")) {
      value = value.substring(1, value.length()-2);
    }
    if (type.pattern != null) {
      Matcher m = type.pattern.matcher(value);
      if (!m.matches()) {
        throw new ArgumentParseException("Value '"+value+"' for "+
                                         (format.isParameter() ?
                                          "parameter " : "option ")+
                                         format.getOption()+
                                         ((format.getParameters().length > 1) ?
                                          " part "+type.getName() : "")+
                                         " does not match pattern '"+
                                         type.pattern.pattern()+"'",
                                         pos,
                                         value,
                                         format,
                                         type);
      }
    }
    switch (type.getType()) {
      case TYPE_INTEGER:
        return new Integer(value);
      case TYPE_LONG:
        return new Long(value);
      case TYPE_OCTET_STRING:
        return OctetString.fromHexString(value, ':');
      default:
        return value;
    }
  }

  public static class ArgumentFormat {
    private String option;
    private boolean mandatory;
    private boolean parameter;
    private ArgumentParameter[] params;
    private boolean vararg;

    public boolean isMandatory() {
      return mandatory;
    }

    public boolean isParameter() {
      return parameter;
    }

    public String getOption() {
      return option;
    }

    public ArgumentParameter[] getParameters() {
      return params;
    }

    public boolean isVariableLength() {
      return vararg;
    }

    public String toString() {
      return "ArgumentFormat[option="+option+",parameter="+parameter+
          ",vararg="+vararg+
          ",mandatatory="+mandatory+",parameters="+
          ((params == null) ? "<null>" : Arrays.asList(params).toString())+"]";
    }
  }

  public static class ArgumentParameter {
    private String name;
    private int type;
    private Pattern pattern;
    private String defaultValue;

    public String getName() {
      return name;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    public int getType() {
      return type;
    }

    public String toString() {
      return "ArgumentParameter[name="+name+",type="+type+
          ",patttern="+((pattern == null) ? null : pattern.pattern())+
          ",defaultValue="+defaultValue+"]";
    }
  }

  public static class ArgumentParseException extends ParseException {

    private ArgumentParameter parameterFormatDetail;
    private ArgumentFormat parameterFormat;
    private String value;

    public ArgumentParseException(int position,
                                  String value,
                                  ArgumentFormat parameterFormat,
                                  ArgumentParameter parameterFormatDetail) {
      super((value != null)
            ? "Invalid value '"+value+"' at position "+position
            : "Mandatory parameter "+((parameterFormat ==  null) ? "<unknown>" : parameterFormat.getOption())+"("+
              ((parameterFormatDetail == null) ? "<unknown>" : parameterFormatDetail.getName())+") not specified", position);
      this.parameterFormat = parameterFormat;
      this.parameterFormatDetail = parameterFormatDetail;
      this.value = value;
    }

    public ArgumentParseException(String message,
                                  int position,
                                  String value,
                                  ArgumentFormat parameterFormat,
                                  ArgumentParameter parameterFormatDetail) {
      super(message, position);
      this.parameterFormat = parameterFormat;
      this.parameterFormatDetail = parameterFormatDetail;
      this.value = value;
    }

    public ArgumentParameter getParameterFormatDetail() {
      return parameterFormatDetail;
    }

    public ArgumentFormat getParameterFormat() {
      return parameterFormat;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * Gets the first option value of a list of values - if available.
   * @param optionValues
   *    a probably empty list of values - could be {@code null}.
   * @return
   *    the first option value in {@code optionValues} if it exists,
   *    {@code null} otherwise.
   * @since 1.9.2
   */
  public static Object getFirstValue(List<? extends Object> optionValues) {
    if ((optionValues != null) && (optionValues.size()>0)) {
      return optionValues.get(0);
    }
    return null;
  }

  /**
   * Gets the {@code n}-th option value of a list of values - if available.
   * @param args
   *    a parameter and options list.
   * @param name
   *    the option or parameter name to return
   * @param index
   *    the index (zero based) of the option/parameter value to return.
   * @return
   *    the {@code n}-th (zero based) option value in
   *    {@code args.get(name)} if it exists, {@code null} otherwise.
   * @since 1.10
   */
  public static Object getValue(Map args, String name, int index) {
    List values = (List)args.get(name);
    if ((values != null) && (values.size() > index)) {
      return values.get(index);
    }
    return null;
  }

  /**
   * Test application to try out patterns and command line parameters.
   * The default option and parameter patterns can be overridden by setting
   * the system properties {@code org.snmp4j.OptionFormat} and
   * {@code org.snmp4j.ParameterFormat} respectively.
   * <p>
   * The given command line is parsed using the specified patterns and the
   * parsed values are returned on the console output.
   * </p>
   * <p>
   * The default option pattern is {@code -o1[i{parameter1}] -o2[s,l]}
   * and the default parameter pattern is
   * {@code -param1[i] -param2[s<(udp|tcp):.*[/[0-9]+]?>] +optParam1[l{=-100}] ..
   * }
   * </p>
   * @param args
   *    the command line arguments to match with the specified format patterns.
   */
  public static void main(String[] args) {
    ArgumentParser argumentparser =
        new ArgumentParser(System.getProperty("org.snmp4j.OptionFormat",
                                              "-o1[i{parameter1}] -o2[s,l]"),
                           System.getProperty("org.snmp4j.ParameterFormat",
                                              "-param1[i] -param2[s<(udp|tcp):.*[/[0-9]+]?>{=udp:127.0.0.1/161}] "+
                                              "+optParam1[l{=-100}] .."));
    System.out.println("Option format is: "+argumentparser.getOptionFormat());
    System.out.println("Parameter format is: "+argumentparser.getParameterFormat());
    Map options = null;
    try {
      options = argumentparser.parse(args);
      System.out.println(options);
    }
    catch (ParseException ex) {
      System.err.println("Failed to parse args: "+ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Select a argument/parameter set from a given selection of sets by the
   * command contained in the supplied argument list. The command is the first
   * argument not starting with "-".
   * @param args
   *    the argument list to scan.
   * @param optionFormat
   *    the option format which is common to all commands (or their super set)
   * @param commandSets
   *    the possible command sets, where each set is identified by its command
   *    string in the first element of the command set.
   * @return
   *    the command set matching the command in the argument list.
   * @throws ParseException
   *    if the command found in {@code args} cannot be found in the
   *    {@code commandSets}, or {@code null} if {@code args}
   *    does not contain any command.
   * @since 1.10
   */
  public static String[] selectCommand(String[] args, String optionFormat,
                                       String[][] commandSets)
      throws ParseException
  {
    ArgumentParser ap =
        new ArgumentParser(optionFormat, "#command[s] +following[s] ..");
    Map params = ap.parse(args);
    String command = (String) ArgumentParser.getValue(params, "command", 0);
    for (String[] commandSet : commandSets) {
      if (commandSet[0].equals(command)) {
        return commandSet;
      }
    }
    throw new ParseException("Command '"+command+"' not found", 0);
  }
}
