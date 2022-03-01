/*_############################################################################
  _## 
  _##  SNMP4J - AbstractVariable.java  
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
package org.snmp4j.smi;

import java.io.*;
import java.util.*;
import org.snmp4j.log.*;
import org.snmp4j.asn1.*;
import org.snmp4j.SNMP4JSettings;
// For JavaDoc:
import org.snmp4j.PDU;

/**
 * The {@code Variable} abstract class is the base class for all SNMP
 * variables.
 * <p>
 * All derived classes need to be registered with their SMI BER type in the
 * {@code smisyntaxes.properties}so that the
 * {@link #createFromBER(BERInputStream inputStream)} method
 * is able to decode a variable from a BER encoded stream.
 * <p>
 * To register additional syntaxes, set the system property
 * {@link #SMISYNTAXES_PROPERTIES} before decoding a Variable for the first
 * time. The path of the property file must be accessible from the classpath
 * and it has to be specified relative to the {@code Variable} class.
 *
 * @author Frank Fock
 * @author Jochen Katz
 * @version 1.8
 * @since 1.8
 */
public abstract class AbstractVariable implements Variable, Serializable {

  private static final long serialVersionUID = 1395840752909725320L;

  public static final String SMISYNTAXES_PROPERTIES =
      "org.snmp4j.smisyntaxes";
  private static final String SMISYNTAXES_PROPERTIES_DEFAULT =
      "smisyntaxes.properties";

  private static final Object[][] SYNTAX_NAME_MAPPING = {
      { "Integer32", (int) BER.INTEGER32},
      { "BIT STRING", (int) BER.BITSTRING},
      { "OCTET STRING", (int) BER.OCTETSTRING},
      { "OBJECT IDENTIFIER", (int) BER.OID},
      { "TimeTicks", (int) BER.TIMETICKS},
      { "Counter", (int) BER.COUNTER},
      { "Counter64", (int) BER.COUNTER64},
      { "EndOfMibView", BER.ENDOFMIBVIEW},
      { "Gauge", (int) BER.GAUGE32},
      { "Unsigned32", (int) BER.GAUGE32},
      { "IpAddress", (int) BER.IPADDRESS},
      { "NoSuchInstance", BER.NOSUCHINSTANCE},
      { "NoSuchObject", BER.NOSUCHOBJECT},
      { "Null", (int) BER.NULL},
      { "Opaque", (int) BER.OPAQUE}
  };

  private static Hashtable<Integer, Class<? extends Variable>> registeredSyntaxes = null;

  private static final LogAdapter logger =
      LogFactory.getLogger(AbstractVariable.class);

  /**
   * The abstract {@code Variable} class serves as the base class for all
   * specific SNMP syntax types.
   */
  public AbstractVariable() {
  }

  public abstract boolean equals(Object o);

  public abstract int compareTo(Variable o);

  public abstract int hashCode();

  /**
   * Returns the length of this {@code Variable} in bytes when encoded
   * according to the Basic Encoding Rules (BER).
   * @return
   *    the BER encoded length of this variable.
   */
  public abstract int getBERLength();

  public int getBERPayloadLength() {
    return getBERLength();
  }

  /**
   * Decodes a {@link Variable} from an {@link BERInputStream}.
   * @param inputStream
   *    an {@code BERInputStream} containing a BER encoded byte stream.
   * @throws IOException
   *    if the stream could not be decoded by using BER rules.
   */
  public abstract void decodeBER(BERInputStream inputStream) throws IOException;

  /**
   * Encodes a {@link Variable} to an {@link OutputStream}.
   * @param outputStream
   *    an {@code OutputStream}.
   * @throws IOException
   *    if an error occurs while writing to the stream.
   */
  public abstract void encodeBER(OutputStream outputStream) throws IOException;

  /**
   * Creates a {@link Variable} from a BER encoded {@link BERInputStream}.
   * Subclasses of {@code Variable} are registered using the properties file
   * {@code smisyntaxes.properties} in this package. The properties are
   * read when this method is called first.
   *
   * @param inputStream
   *    an {@code BERInputStream} containing a BER encoded byte stream.
   * @return
   *    an instance of a subclass of {@code Variable}.
   * @throws IOException
   *    if the {@code inputStream} is not properly BER encoded.
   */
  public static Variable createFromBER(BERInputStream inputStream) throws
      IOException {
    if (!inputStream.markSupported()) {
      throw new IOException(
          "InputStream for decoding a Variable must support marks");
    }
    if (SNMP4JSettings.isExtensibilityEnabled() &&
        (registeredSyntaxes == null)) {
      registerSyntaxes();
    }
    inputStream.mark(2);
    int type = inputStream.read();
    Variable variable;
    if (SNMP4JSettings.isExtensibilityEnabled()) {
      Class<? extends Variable> c = registeredSyntaxes.get(type);
      if (c == null) {
        throw new IOException("Encountered unsupported variable syntax: " +
                              type);
      }
      try {
        variable = c.newInstance();
      }
      catch (IllegalAccessException aex) {
        throw new IOException("Could not access variable syntax class for: " +
                              c.getName());
      }
      catch (InstantiationException iex) {
        throw new IOException(
            "Could not instantiate variable syntax class for: " +
            c.getName());
      }
    }
    else {
      variable = createVariable(type);
    }
    inputStream.reset();
    variable.decodeBER(inputStream);
    return variable;
  }

  private static Variable createVariable(int smiSyntax) {
    switch (smiSyntax) {
      case SMIConstants.SYNTAX_OBJECT_IDENTIFIER: {
        return new OID();
      }
      case SMIConstants.SYNTAX_INTEGER: {
        return new Integer32();
      }
      case SMIConstants.SYNTAX_OCTET_STRING: {
        return new OctetString();
      }
      case SMIConstants.SYNTAX_GAUGE32: {
        return new Gauge32();
      }
      case SMIConstants.SYNTAX_COUNTER32: {
        return new Counter32();
      }
      case SMIConstants.SYNTAX_COUNTER64: {
        return new Counter64();
      }
      case SMIConstants.SYNTAX_NULL: {
        return new Null();
      }
      case SMIConstants.SYNTAX_TIMETICKS: {
        return new TimeTicks();
      }
      case SMIConstants.EXCEPTION_END_OF_MIB_VIEW: {
        return new Null(SMIConstants.EXCEPTION_END_OF_MIB_VIEW);
      }
      case SMIConstants.EXCEPTION_NO_SUCH_INSTANCE: {
        return new Null(SMIConstants.EXCEPTION_NO_SUCH_INSTANCE);
      }
      case SMIConstants.EXCEPTION_NO_SUCH_OBJECT: {
        return new Null(SMIConstants.EXCEPTION_NO_SUCH_OBJECT);
      }
      case SMIConstants.SYNTAX_OPAQUE: {
        return new Opaque();
      }
      case SMIConstants.SYNTAX_IPADDRESS: {
        return new IpAddress();
      }
      default: {
        throw new IllegalArgumentException("Unsupported variable syntax: " +
                                           smiSyntax);
      }
    }
  }

  /**
   * Creates a {@code Variable} from the supplied SMI syntax identifier.
   * Subclasses of {@code Variable} are registered using the properties
   * file {@code smisyntaxes.properties} in this package. The properties
   * are read when this method is called for the first time.
   *
   * @param smiSyntax
   *    an SMI syntax identifier of the registered types, which is typically
   *    defined by {@link SMIConstants}.
   * @return
   *    a {@code Variable} variable instance of the supplied SMI syntax.
   */
  public static Variable createFromSyntax(int smiSyntax) {
    if (!SNMP4JSettings.isExtensibilityEnabled()) {
      return createVariable(smiSyntax);
    }
    if (registeredSyntaxes == null) {
      registerSyntaxes();
    }
    Class<? extends Variable> c = registeredSyntaxes.get(smiSyntax);
    if (c == null) {
      throw new IllegalArgumentException("Unsupported variable syntax: " +
                                         smiSyntax);
    }
    try {
      Variable variable = c.newInstance();
      return variable;
    }
    catch (IllegalAccessException aex) {
      throw new RuntimeException("Could not access variable syntax class for: " +
                                 c.getName());
    }
    catch (InstantiationException iex) {
      throw new RuntimeException(
          "Could not instantiate variable syntax class for: " +
          c.getName());
    }
  }

  /**
   * Register SNMP syntax classes from a properties file. The registered
   * syntaxes are used by the {@link #createFromBER} method to type-safe
   * instantiate sub-classes from {@code Variable} from an BER encoded
   * {@code InputStream}.
   */
  @SuppressWarnings("unchecked")
  private synchronized static void registerSyntaxes() {
    String syntaxes = System.getProperty(SMISYNTAXES_PROPERTIES,
                                         SMISYNTAXES_PROPERTIES_DEFAULT);
    InputStream is = Variable.class.getResourceAsStream(syntaxes);
    if (is == null) {
      throw new InternalError("Could not read '" + syntaxes +
                              "' from classpath!");
    }
    Properties props = new Properties();
    try {
      props.load(is);
      Hashtable<Integer, Class<? extends Variable>> regSyntaxes = new Hashtable<Integer, Class<? extends Variable>>(props.size());
      for (Enumeration en = props.propertyNames(); en.hasMoreElements(); ) {
        String id = en.nextElement().toString();
        String className = props.getProperty(id);
        try {
          Class<? extends Variable> c = (Class<? extends Variable>) Class.forName(className);
          regSyntaxes.put(new Integer(id), c);
        }
        catch (ClassNotFoundException cnfe) {
          logger.error(cnfe);
        }
        catch (ClassCastException ccex) {
          logger.error(ccex);
        }
      }
      // atomic syntax registration
      registeredSyntaxes = regSyntaxes;
    }
    catch (IOException iox) {
      String txt = "Could not read '" + syntaxes + "': " +
          iox.getMessage();
      logger.error(txt);
      throw new InternalError(txt);
    }
    finally {
      try {
        is.close();
      }
      catch (IOException ex) {
        logger.warn(ex);
      }
    }
  }

  /**
   * Gets the ASN.1 syntax identifier value of this SNMP variable.
   * @return
   *    an integer value less than 128 for regular SMI objects and a value greater or equal than 128
   *    for exception values like noSuchObject, noSuchInstance, and
   *    endOfMibView.
   */
  public abstract int getSyntax();

  /**
   * Checks whether this variable represents an exception like
   * noSuchObject, noSuchInstance, and endOfMibView.
   * @return
   *    {@code true} if the syntax of this variable is an instance of
   *    {@code Null} and its syntax equals one of the following:
   *    <UL>
   *    <LI>{@link SMIConstants#EXCEPTION_NO_SUCH_OBJECT}</LI>
   *    <LI>{@link SMIConstants#EXCEPTION_NO_SUCH_INSTANCE}</LI>
   *    <LI>{@link SMIConstants#EXCEPTION_END_OF_MIB_VIEW}</LI>
   *    </UL>
   */
  public boolean isException() {
    return Null.isExceptionSyntax(getSyntax());
  }

  /**
   * Gets a string representation of the variable.
   * @return
   *    a string representation of the variable's value.
   */
  public abstract String toString();

  /**
   * Returns an integer representation of this variable if
   * such a representation exists.
   * @return
   *    an integer value (if the native representation of this variable
   *    would be a long, then the long value will be casted to int).
   * @throws UnsupportedOperationException if an integer representation
   * does not exists for this Variable.
   * @since 1.7
   */
  public abstract int toInt();

  /**
   * Returns a long representation of this variable if
   * such a representation exists.
   * @return
   *    a long value.
   * @throws UnsupportedOperationException if a long representation
   * does not exists for this Variable.
   * @since 1.7
   */
  public abstract long toLong();

  public abstract Object clone();

  /**
   * Gets a textual description of the supplied syntax type.
   * @param syntax
   *    the BER code of the syntax.
   * @return
   *    a textual description like 'Integer32' for {@code syntax}
   *    as used in the Structure of Management Information (SMI) modules.
   *    '?' is returned if the supplied syntax is unknown.
   */
  public static String getSyntaxString(int syntax) {
    switch (syntax) {
      case BER.INTEGER:
        return "Integer32";
      case BER.BITSTRING:
        return "BIT STRING";
      case BER.OCTETSTRING:
        return "OCTET STRING";
      case BER.OID:
        return "OBJECT IDENTIFIER";
      case BER.TIMETICKS:
        return "TimeTicks";
      case BER.COUNTER:
        return "Counter";
      case BER.COUNTER64:
        return "Counter64";
      case BER.ENDOFMIBVIEW:
        return "EndOfMibView";
      case BER.GAUGE32:
        return "Gauge";
      case BER.IPADDRESS:
        return "IpAddress";
      case BER.NOSUCHINSTANCE:
        return "NoSuchInstance";
      case BER.NOSUCHOBJECT:
        return "NoSuchObject";
      case BER.NULL:
        return "Null";
      case BER.OPAQUE:
        return "Opaque";
    }
    return "?";
  }

  /**
   * Gets a textual description of this Variable.
   * @return
   *    a textual description like 'Integer32'
   *    as used in the Structure of Management Information (SMI) modules.
   *    '?' is returned if the syntax is unknown.
   * @since 1.7
   */
  public final String getSyntaxString() {
    return getSyntaxString(getSyntax());
  }

  /**
   * Returns the BER syntax ID for the supplied syntax string (as returned
   * by {@link #getSyntaxString(int)}).
   * @param syntaxString
   *    the textual representation of the syntax.
   * @return
   *    the corresponding BER ID.
   * @since 1.6
   */
  public static int getSyntaxFromString(String syntaxString) {
    for (Object[] aSYNTAX_NAME_MAPPING : SYNTAX_NAME_MAPPING) {
      if (aSYNTAX_NAME_MAPPING[0].equals(syntaxString)) {
        return (Integer) aSYNTAX_NAME_MAPPING[1];
      }
    }
    return BER.NULL;
  }

  /**
   * Converts the value of this {@code Variable} to a (sub-)index
   * value.
   * @param impliedLength
   *    specifies if the sub-index has an implied length. This parameter applies
   *    to variable length variables only (e.g. {@link OctetString} and
   *    {@link OID}). For other variables it has no effect.
   * @return
   *    an OID that represents this value as an (sub-)index.
   * @throws UnsupportedOperationException
   *    if this variable cannot be used in an index.
   * @since 1.7
   */
  public abstract OID toSubIndex(boolean impliedLength);

  /**
   * Sets the value of this {@code Variable} from the supplied (sub-)index.
   * @param subIndex
   *    the sub-index OID.
   * @param impliedLength
   *    specifies if the sub-index has an implied length. This parameter applies
   *    to variable length variables only (e.g. {@link OctetString} and
   *    {@link OID}). For other variables it has no effect.
   * @throws UnsupportedOperationException
   *    if this variable cannot be used in an index.
   * @since 1.7
   */
  public abstract void fromSubIndex(OID subIndex, boolean impliedLength);

  /**
   * Indicates whether this variable is dynamic, which means that it might
   * change its value while it is being (BER) serialized. If a variable is
   * dynamic, it will be cloned on-the-fly when it is added to a {@link PDU}
   * with {@link PDU#add(VariableBinding)}. By cloning the value, it is
   * ensured that there are no inconsistent changes between determining the
   * length with {@link #getBERLength()} for encoding enclosing SEQUENCES and
   * the actual encoding of the Variable itself with {@link #encodeBER}.
   *
   * @return
   *    {@code false} by default. Derived classes may override this
   *    if implementing dynamic {@link Variable} instances.
   * @since 1.8
   */
  public boolean isDynamic() {
    return false;
  }

  /**
   * Tests if two variables have the same value.
   * @param a
   *   a variable.
   * @param b
   *   another variable.
   * @return
   *   {@code true} if
   *   {@code a == null) ?  (b == null) : a.equals(b)}.
   *   @since 2.0
   */
  public static boolean equal(AbstractVariable a, AbstractVariable b) {
    return (a == null) ?  (b == null) : a.equals(b);
  }

}
