/*
 * SWTCalendarEvent.java - The event created when the user changes the date.
 * Mark Bryan Yu swtcalendar.sourceforge.net
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

import java.util.Calendar;

import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Event;

public class SWTCalendarEvent extends TypedEvent {
	private static final long serialVersionUID = -4525931268845275613L;

	public SWTCalendarEvent(Event event) {
		super(event);
	}

	public Calendar getCalendar() {
		return (Calendar) this.data;
	}
}
