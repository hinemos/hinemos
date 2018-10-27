/*
 * SWTCalendarListener.java - An interface for notifying for date changed Mark
 * Bryan Yu swtcalendar.sourceforge.net
 * Modified by: NTT DATA INTELLILINK Corporation <http://www.hinemos.info/>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.vafada.swtcalendar;

import org.eclipse.swt.internal.SWTEventListener;

@SuppressWarnings("restriction")
public interface SWTCalendarListener extends SWTEventListener {
	public void dateChanged(SWTCalendarEvent event);
}
