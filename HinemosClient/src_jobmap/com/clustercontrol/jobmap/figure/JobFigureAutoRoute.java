/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.figure;


import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;

public class JobFigureAutoRoute extends BendpointConnectionRouter {
	private JobFigure target;
	private boolean xyChange;
	private double scale;
	
	private static int INTERVAL = 15;
	
	private static final PrecisionPoint A_POINT = new PrecisionPoint();
	
	public JobFigureAutoRoute(JobFigure source, JobFigure target, boolean xyChange, double scale) {
		this.target = target;
		this.xyChange = xyChange;
		this.scale = scale;
	}
	
	@Override
	public void route(Connection conn) {
		PointList points = conn.getPoints();
		points.removeAllPoints();
		
		Point ref1, ref2;
		ref1 = conn.getSourceAnchor().getReferencePoint();
		ref2 = conn.getTargetAnchor().getReferencePoint();
		
		if (xyChange) {
			int lineY = target.getSize().height/2 + INTERVAL -10;
			A_POINT.setLocation(conn.getSourceAnchor().getLocation(ref2));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(ref1.getTranslated((INTERVAL/2-2)*scale, 0));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(new Point((int)Math.ceil(ref1.x + (INTERVAL/2-2)*scale), (int)Math.ceil(ref2.y - (int)lineY*scale)));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(ref2.getTranslated(-INTERVAL*scale, -lineY*scale));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(ref2.getTranslated(-INTERVAL*scale, 0));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(conn.getTargetAnchor().getLocation(ref1));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
		} else {
			int lineX = target.getSize().width/2 + INTERVAL -10;
			
			A_POINT.setLocation(conn.getSourceAnchor().getLocation(ref2));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(ref1.getTranslated(0, (INTERVAL/2-2)*scale));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(new Point((int)Math.ceil(ref2.x - (int)lineX*scale), (int)Math.ceil(ref1.y + (INTERVAL/2-2)*scale)));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(ref2.getTranslated(-lineX*scale, -INTERVAL*scale));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(ref2.getTranslated(0, -INTERVAL*scale));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
			
			A_POINT.setLocation(conn.getTargetAnchor().getLocation(ref1));
			conn.translateToRelative(A_POINT);
			points.addPoint(A_POINT);
		}

	}
}
