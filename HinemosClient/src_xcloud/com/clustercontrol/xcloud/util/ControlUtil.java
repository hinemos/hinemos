/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.xcloud.util.validation.ControlValidator;
import com.clustercontrol.xcloud.util.validation.ControlValidatorImpl;
import com.clustercontrol.xcloud.util.validation.ValidateException;

public class ControlUtil {
	
	private static final Log logger = LogFactory.getLog(ControlUtil.class);
	
	private ControlUtil(){};

	@SuppressWarnings("unchecked")
	public static <T> T deepCopy(T value) {
		T model = null;
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(value);
			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in = new ObjectInputStream(byteIn);
			model = (T)in.readObject();
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return model;
	}

	public static void setRequired(Control...requiredControls){
		for(Control requiredControl: requiredControls){
			if (requiredControl instanceof Text){
				final Text text = (Text)requiredControl;
				if(text.getEnabled() && text.getText().isEmpty()){
					requiredControl.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}
				text.addModifyListener(new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent e) {
						if(text != null && text.getEnabled() && text.getEditable() && text.getText().isEmpty()){
							text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
						} else if(text != null) {
							text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
						}
					}
				});
			} else if (requiredControl instanceof Combo) {
				final Combo text = (Combo)requiredControl;
				if(text.getEnabled() && text.getText().isEmpty()){
					requiredControl.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
				}
				text.addModifyListener(new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent e) {
						if(text != null && text.getEnabled() && text.getText().isEmpty()){
							text.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
						} else if(text != null) {
							text.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
						}
					}
				});
			}
		}
	}

	public static void setInputRestriction(Text[] texts,final Pattern pattern){
		for(Text text: texts){
			text.addVerifyListener(new VerifyListener(){
				Matcher matcher;
				@Override
				public void verifyText(VerifyEvent e) {
					if(!e.text.isEmpty()){
						String srcText = ((Text)e.getSource()).getText();
						matcher = pattern.matcher(srcText.substring(0, e.start) + e.text + srcText.substring(e.end));
						if(!matcher.matches()){
							e.doit = false;
						}
					}
				}
			});
		}
	}

	public static void setInputRestriction(Control[] controls,final Pattern pattern){
		for(Control control: controls){
			if(control instanceof Text){
				((Text)control).addVerifyListener(new VerifyListener(){
					Matcher matcher;
					@Override
					public void verifyText(VerifyEvent e) {
						if(!e.text.isEmpty()){
							String srcText = ((Text)e.getSource()).getText();
							matcher = pattern.matcher(srcText.substring(0, e.start) + e.text + srcText.substring(e.end));
							if(!matcher.matches()){
								e.doit = false;
							}
						}
					}
				});
			} else if(control instanceof Combo){
				((Combo)control).addVerifyListener(new VerifyListener(){
					Matcher matcher;
					@Override
					public void verifyText(VerifyEvent e) {
						if(!e.text.isEmpty()){
							String srcText = ((Combo)e.getSource()).getText();
							matcher = pattern.matcher(srcText.substring(0, e.start) + e.text + srcText.substring(e.end));
							if(!matcher.matches()){
								e.doit = false;
							}
						}
					}
				});
			}
		}
	}

	public static void setDigitOnlyInput(Text[] texts){
		for(Text text: texts){
			text.addModifyListener(new ModifyListener(){
				Pattern pattern = Pattern.compile("^-?[0-9]+$");
				Matcher matcher;
				@Override
				public void modifyText(ModifyEvent e) {
					matcher = pattern.matcher(((Text)e.getSource()).getText()); 
					if(matcher.find()){
						((Text)e.getSource()).setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
					} else {
						((Text)e.getSource()).setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
					}
				}
			});
			text.addKeyListener(new KeyAdapter(){
				@Override
				public void keyPressed(KeyEvent e) {
					if(!Character.isDigit(e.character) && !Character.isISOControl(e.character) && e.character != '-'){
						e.doit = false;
					}
				}
			});
		}
	}

	private final static ControlValidator validator = new ControlValidatorImpl();

	public static void validate(Dialog dialog) throws ValidateException, Exception{
		validator.validate(dialog);
	}

	public static void openError(Throwable e, String errorMessage) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos, true);
		e.printStackTrace(ps);

		ArrayList<Status> statusList = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new StringReader(bos.toString()))) {
			
			String str;
			while((str = r.readLine()) != null){
				if(str.startsWith("\t"))
					str = "      " + str;
				Status status = new Status(
						IStatus.ERROR,
						ClusterControlPlugin.getPluginId(),
						str);
				statusList.add(status);
			}
			
			MultiStatus multiStatus = new MultiStatus(
					ClusterControlPlugin.getPluginId(),
					IStatus.ERROR,
					statusList.toArray( new Status[] {} ),
					HinemosMessage.replace(e.getMessage())+ "\n\n" + Messages.getString( "message.accesscontrol.56" ),
					null);
			
			ErrorDialog.openError(
					null,
					Messages.getString( "error" ),
					errorMessage,
					multiStatus );
			
		} catch (IOException e1) {
			logger.error(e.getMessage(), e);
		}
	}
}
