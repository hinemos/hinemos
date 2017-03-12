/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent.util;

/**
 * Collector Task Interface
 * 
 * @since 4.0
 */
public interface CollectorTask {

	public CollectorId getCollectorId();

	public void start();

	public void shutdown();

	public void update(CollectorTask task);

	@Override
	public String toString();

}
