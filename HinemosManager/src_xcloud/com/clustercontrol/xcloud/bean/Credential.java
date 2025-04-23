/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.clustercontrol.xcloud.CloudManagerException;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com")
@XmlSeeAlso({AccessKeyCredential.class, UserCredential.class})
public abstract class Credential {
	public interface IVisitor {
		void visit(AccessKeyCredential credential) throws CloudManagerException;
		default void visit(GenericCredential credential) throws CloudManagerException{};
		void visit(UserCredential credential) throws CloudManagerException;
	}
	public static abstract class Visitor implements IVisitor {
		@Override
		public void visit(AccessKeyCredential credential) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void visit(GenericCredential credential) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
		@Override
		public void visit(UserCredential credential) throws CloudManagerException {
			throw new UnsupportedOperationException();
		}
	}
	public interface ITransformer<T> {
		T transform(AccessKeyCredential credential) throws CloudManagerException;
		T transform(GenericCredential credential) throws CloudManagerException;
		T transform(UserCredential credential) throws CloudManagerException;
		
	}

	public Credential() {
	}

	public abstract void visit(IVisitor visitor) throws CloudManagerException;

	public abstract <T> T transform(ITransformer<T> transformer) throws CloudManagerException;
	
	public abstract boolean match(Credential other);
}
