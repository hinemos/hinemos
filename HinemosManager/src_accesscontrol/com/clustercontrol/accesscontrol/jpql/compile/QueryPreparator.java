/*
 * Copyright 2008 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.clustercontrol.accesscontrol.jpql.compile;

import net.sf.jpasecurity.jpql.parser.JpqlAbstractSchemaName;
import net.sf.jpasecurity.jpql.parser.JpqlAnd;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlEquals;
import net.sf.jpasecurity.jpql.parser.JpqlExists;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlFromItem;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariableDeclaration;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlOr;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlParserTreeConstants;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlSelect;
import net.sf.jpasecurity.jpql.parser.JpqlSelectClause;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpression;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpressions;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlStringLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.ToStringVisitor;

import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;


/**
 */
public class QueryPreparator {

	/**
	 * Creates a <tt>JpqlWhere</tt> node.
	 */
	public static JpqlWhere createWhere(Node child) {
		JpqlWhere where = new JpqlWhere(JpqlParserTreeConstants.JJTWHERE);
		child.jjtSetParent(where);
		where.jjtAddChild(child, 0);
		return where;
	}

	/**
	 * Creates a <tt>JpqlNamedInputParameter</tt> node with the specified name.
	 */
	public static JpqlNamedInputParameter createNamedParameter(String name) {
		JpqlNamedInputParameter parameter = new JpqlNamedInputParameter(JpqlParserTreeConstants.JJTNAMEDINPUTPARAMETER);
		parameter.setValue(name);
		return parameter;
	}

	/**
	 * Creates a <tt>JpqlNamedInputParameter</tt> node with the specified name.
	 */
	public static JpqlStringLiteral createStringLiteral(String value) {
		JpqlStringLiteral stringLiteral = new JpqlStringLiteral(JpqlParserTreeConstants.JJTSTRINGLITERAL);
		stringLiteral.setValue("'" + value + "'");
		return stringLiteral;
	}

	/**
	 * Connects the specified node with <tt>JpqlAnd</tt>.
	 */
	public static JpqlAnd createAnd(Node node1, Node node2) {
		return appendChildren(new JpqlAnd(JpqlParserTreeConstants.JJTAND), node1, node2);
	}

	/**
	 * Connects the specified node with <tt>JpqlOr</tt>.
	 */
	public static JpqlOr createOr(Node node1, Node node2) {
		return appendChildren(new JpqlOr(JpqlParserTreeConstants.JJTOR), node1, node2);
	}

	/**
	 * Connects the specified node with <tt>JpqlEquals</tt>.
	 */
	public static JpqlEquals createEquals(Node node1, Node node2) {
		return appendChildren(new JpqlEquals(JpqlParserTreeConstants.JJTEQUALS), node1, node2);
	}

	/**
	 * Appends the specified children to the list of children of the specified parent.
	 * @return the parent
	 */
	public static <N extends Node> N appendChildren(N parent, Node... children) {
		for (int i = 0; i < children.length; i++) {
			parent.jjtAddChild(children[i], i);
			children[i].jjtSetParent(parent);
		}
		return parent;
	}

	/**
	 * Creates brackets for the specified node with <tt>JpqlBrackets</tt>.
	 */
	public static JpqlBrackets createBrackets(Node node) {
		JpqlBrackets brackets = new JpqlBrackets(JpqlParserTreeConstants.JJTBRACKETS);
		brackets.jjtAddChild(node, 0);
		node.jjtSetParent(brackets);
		return brackets;
	}

	/**
	 * Creates a <tt>JpqlPath</tt> node for the specified string.
	 */
	public static JpqlPath createPath(String pathString) {
		String[] pathComponents = pathString.split("\\.");
		JpqlIdentifier identifier = createIdentifier(pathComponents[0]);
		JpqlPath path = appendChildren(new JpqlPath(JpqlParserTreeConstants.JJTPATH), identifier);
		for (int i = 1; i < pathComponents.length; i++) {
			JpqlIdentificationVariable identificationVariable
			= new JpqlIdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
			identificationVariable.setValue(pathComponents[i]);
			identificationVariable.jjtSetParent(path);
			path.jjtAddChild(identificationVariable, i);
		}
		return path;
	}

	/**
	 * Creates a <tt>JpqlSubselect</tt> node for the specified access rule.
	 */
	public static JpqlSubselect createSubselect(JpqlSelectClause selectClause,
			JpqlFrom from,
			JpqlWhere where) {
		return appendChildren(new JpqlSubselect(JpqlParserTreeConstants.JJTSUBSELECT), selectClause, from, where);
	}

	/**
	 * Creates a <tt>JpqlSelectClause</tt> node to select the specified path.
	 */
	public static JpqlSelectClause createSelectClause(String selectedPath) {
		JpqlSelectExpression expression = new JpqlSelectExpression(JpqlParserTreeConstants.JJTSELECTEXPRESSION);
		expression = appendChildren(expression, createPath(selectedPath));
		JpqlSelectExpressions expressions = new JpqlSelectExpressions(JpqlParserTreeConstants.JJTSELECTEXPRESSIONS);
		expressions = appendChildren(expressions, expression);
		return appendChildren(new JpqlSelectClause(JpqlParserTreeConstants.JJTSELECTCLAUSE), expressions);
	}

	/**
	 * Creates a <tt>JpqlFrom</tt> node to select the specified path.
	 */
	public static JpqlFrom createFrom(String className, String alias) {
		JpqlIdentificationVariableDeclaration declaration
		= new JpqlIdentificationVariableDeclaration(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLEDECLARATION);
		declaration = appendChildren(declaration, createFromItem(className, alias));
		return appendChildren(new JpqlFrom(JpqlParserTreeConstants.JJTFROM), declaration);
	}

	/**
	 * Creates a <tt>JpqlFromItem</tt> node to select the specified path.
	 */
	public static JpqlFromItem createFromItem(String type, String alias) {
		JpqlAbstractSchemaName schemaName = new JpqlAbstractSchemaName(JpqlParserTreeConstants.JJTABSTRACTSCHEMANAME);
		return appendChildren(new JpqlFromItem(JpqlParserTreeConstants.JJTFROMITEM),
				appendChildren(schemaName, createIdentifier(type)),
				createIdentifier(alias));
	}

	/**
	 * Creates a <tt>JpqlIdentifier</tt> node to select the specified path.
	 */
	public static JpqlIdentifier createIdentifier(String value) {
		JpqlIdentifier identifier = new JpqlIdentifier(JpqlParserTreeConstants.JJTIDENTIFIER);
		identifier.setValue(value);
		return identifier;
	}

	/**
	 * Creates a <tt>JpqlExists</tt> node to select the specified path.
	 */
	public static JpqlExists createExists(JpqlSubselect node) {
		return appendChildren(new JpqlExists(JpqlParserTreeConstants.JJTEXISTS), node);
	}

	/**
	 * Creates an <tt>JpqlIn</tt> subtree for the specified access rule.
	 */
	public static JpqlIn createIn(Node node1, Node node2) {
		return appendChildren(new JpqlIn(JpqlParserTreeConstants.JJTIN), node1, node2);
	}

	/**
	 * オブジェクト権限チェックを行うJPQLのノードを返す
	 * ログインユーザIDでチェックを行う。
	 * 
	 * @param objectType オブジェクトタイプ
	 * @param mode オブジェクト権限(READ, WRITE, EXEC)
	 * @return オブジェクト権限チェックを行うJPQLのノード
	 */
	public static Node createObjectPrivilegeExists(String objectType, ObjectPrivilegeMode mode) {
		String privilegeAlias = "x";		// サブクエリで使用するalias(ObjectPrivilegeEntity)
		String objectAlias = "a";			// 検索対象のテーブルのalias

		JpqlEquals equals1 = createEquals(createPath(privilegeAlias + ".id.objectId"), createPath(objectAlias + ".objectId"));
		JpqlEquals equals2 = createEquals(createPath(privilegeAlias + ".id.objectType"), createStringLiteral(objectType));
		JpqlEquals equals3 = createEquals(createPath(privilegeAlias + ".id.objectPrivilege"), createStringLiteral(mode.name()));
		JpqlIn in = createIn(createPath(privilegeAlias + ".id.roleId"), createNamedParameter("roleIds"));
		JpqlExists node1 = createExists(
								createSubselect(
								createSelectClause(privilegeAlias),
								createFrom(ObjectPrivilegeInfo.class.getSimpleName(), privilegeAlias),
								createWhere(createAnd(createAnd(createAnd(equals1, equals2), equals3), in))));

		JpqlIn node2 = createIn(createPath(objectAlias + ".ownerRoleId"), createNamedParameter("roleIds"));
		Node node = null;
		if (objectType.equals(HinemosModuleConstant.JOB_MST)) {
			JpqlEquals equals7 = createEquals(createPath(objectAlias + ".id.jobunitId"), createStringLiteral(CreateJobSession.TOP_JOBUNIT_ID));
			node = createBrackets(createOr(createOr(node1, node2), equals7));
		} else {
			node = createBrackets(createOr(node1, node2));
		}
		return node;
	}

	/**
	 * オブジェクト権限チェックを行うJPQLのノードを返す
	 * オーナーロールIDでチェックを行う。
	 * 
	 * @param objectType オブジェクトタイプ
	 * @param mode オブジェクト権限(READ, WRITE, EXEC)
	 * @param ownerRoleId オーナーロールID
	 * @return オブジェクト権限チェックを行うJPQLのノード
	 */
	public static Node createObjectPrivilegeExists(String objectType, ObjectPrivilegeMode mode, String ownerRoleId) {
		String privilegeAlias = "x";		// サブクエリで使用するalias(ObjectPrivilegeEntity)
		String objectAlias = "a";			// 検索対象のテーブルのalias

		JpqlEquals equals1 = createEquals(createPath(privilegeAlias + ".id.objectId"), createPath(objectAlias + ".objectId"));
		JpqlEquals equals2 = createEquals(createPath(privilegeAlias + ".id.objectType"), createStringLiteral(objectType));
		JpqlEquals equals3 = createEquals(createPath(privilegeAlias + ".id.objectPrivilege"), createStringLiteral(mode.name()));
		JpqlEquals equals4 = createEquals(createPath(privilegeAlias + ".id.roleId"), createStringLiteral(ownerRoleId));
		JpqlExists node1 = createExists(
				createSubselect(
						createSelectClause(privilegeAlias),
						createFrom(ObjectPrivilegeInfo.class.getSimpleName(), privilegeAlias),
						createWhere(createAnd(createAnd(createAnd(equals1, equals2), equals3), equals4))));

		JpqlEquals node2 = createEquals(createPath(objectAlias + ".ownerRoleId"), createStringLiteral(ownerRoleId));
		Node node = null;
		if (objectType.equals(HinemosModuleConstant.JOB_MST)) {
			JpqlEquals equals7 = createEquals(createPath(objectAlias + ".id.jobunitId"), createStringLiteral(CreateJobSession.TOP_JOBUNIT_ID));
			node = createBrackets(createOr(createOr(node1, node2), equals7));
		} else {
			node = createBrackets(createOr(node1, node2));
		}
		return node;
	}


	public static void main(String[] args) {

		try {
			// JPQLの構文解析
			JpqlParser jpqlParser = new JpqlParser();
			JpqlFrom jpqlFrom = null;
			JpqlWhere jpqlWhere = null;
			JpqlStatement statement = jpqlParser.parseQuery("SELECT a FROM UserEntity a WHERE a.userType = 'test' AND a.userId IN (:userIds)");
			if (statement.jjtGetChild(0) instanceof JpqlSelect) {
				JpqlSelect jpqlSelect = (JpqlSelect)statement.jjtGetChild(0);
				for(int i=0 ; i<jpqlSelect.jjtGetNumChildren() ; i++ ) {
					if (jpqlSelect.jjtGetChild(i) instanceof JpqlFrom) {
						jpqlFrom = (JpqlFrom)jpqlSelect.jjtGetChild(i);
					} else if (jpqlSelect.jjtGetChild(i) instanceof JpqlWhere) {
						jpqlWhere = (JpqlWhere)jpqlSelect.jjtGetChild(i);
						break;
					}
				}
				// オブジェクト権限チェックのJPQLを挿入
				Node jpqlExists = QueryPreparator.createObjectPrivilegeExists("TEST", ObjectPrivilegeMode.MODIFY);
				if (jpqlWhere == null) {
					jpqlWhere = QueryPreparator.createWhere(jpqlExists);
					Node parent = jpqlFrom.jjtGetParent();
					for (int i = parent.jjtGetNumChildren(); i > 2; i--) {
						parent.jjtAddChild(parent.jjtGetChild(i - 1), i);
					}
					parent.jjtAddChild(jpqlWhere, 2);
				} else {
					Node condition = jpqlWhere.jjtGetChild(0);
					if (!(condition instanceof JpqlBrackets)) {
						condition = QueryPreparator.createBrackets(condition);
					}
					Node and = QueryPreparator.createAnd(condition, jpqlExists);
					and.jjtSetParent(jpqlWhere);
					jpqlWhere.jjtSetChild(and, 0);
				}
			}

			ToStringVisitor v = new ToStringVisitor();
			statement.jjtAccept(v, null);
			System.out.println("end");
		} catch (Exception e) {
			System.out.println("end");
		}
	}
}
