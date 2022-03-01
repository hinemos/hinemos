/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.draw2d;

import org.eclipse.draw2d.rap.swt.graphics.ColorUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * A collection of color-related constants.
 */
//[RAP AM] remove statics
public class ColorConstants {

    static class SystemColorFactory {
        private static Color getColor(final int which) {
            Display display = Display.getCurrent();
            if (display != null)
                return display.getSystemColor(which);
            display = Display.getDefault();
            final Color result[] = new Color[1];
            display.syncExec(new Runnable() {
                public void run() {
                    synchronized (result) {
                        result[0] = Display.getCurrent().getSystemColor(which);
                    }
                }
            });
            synchronized (result) {
                return result[0];
            }
        }
    }

    /**
     * @see SWT#COLOR_WIDGET_HIGHLIGHT_SHADOW
     */
    public static Color buttonLightest() { return SystemColorFactory
            .getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
    }
    /**
     * @see SWT#COLOR_WIDGET_BACKGROUND
     */
    public static Color button() { return SystemColorFactory.getColor(SWT.COLOR_WIDGET_BACKGROUND);}
    /**
     * @see SWT#COLOR_WIDGET_NORMAL_SHADOW
     */
    public static Color buttonDarker() { return SystemColorFactory
            .getColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
    }
    /**
     * @see SWT#COLOR_WIDGET_DARK_SHADOW
     */
    public static Color buttonDarkest() { return SystemColorFactory
            .getColor(SWT.COLOR_WIDGET_DARK_SHADOW);
    }

    /**
     * @see SWT#COLOR_LIST_BACKGROUND
     */
    public static Color listBackground() { return SystemColorFactory
            .getColor(SWT.COLOR_LIST_BACKGROUND);
    }
    /**
     * @see SWT#COLOR_LIST_FOREGROUND
     */
    public static Color listForeground() { return SystemColorFactory
            .getColor(SWT.COLOR_LIST_FOREGROUND);
    }

    /**
     * @see SWT#COLOR_WIDGET_BACKGROUND
     */
    public static Color menuBackground() { return SystemColorFactory
            .getColor(SWT.COLOR_WIDGET_BACKGROUND);
    }
    /**
     * @see SWT#COLOR_WIDGET_FOREGROUND
     */
    public static Color menuForeground() { return SystemColorFactory
            .getColor(SWT.COLOR_WIDGET_FOREGROUND);
    }
    /**
     * @see SWT#COLOR_LIST_SELECTION
     */
    public static Color menuBackgroundSelected() { return SystemColorFactory
            .getColor(SWT.COLOR_LIST_SELECTION);
    }
    /**
     * @see SWT#COLOR_LIST_SELECTION_TEXT
     */
    public static Color menuForegroundSelected() { return SystemColorFactory
            .getColor(SWT.COLOR_LIST_SELECTION_TEXT);
    }

    /**
     * @see SWT#COLOR_TITLE_BACKGROUND
     */
    public static Color titleBackground() { return SystemColorFactory
            .getColor(SWT.COLOR_TITLE_BACKGROUND);
    }
    /**
     * @see SWT#COLOR_TITLE_BACKGROUND_GRADIENT
     */
    public static Color titleGradient() { return SystemColorFactory
            .getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
    }
    /**
     * @see SWT#COLOR_TITLE_FOREGROUND
     */
    public static Color titleForeground() { return SystemColorFactory
            .getColor(SWT.COLOR_TITLE_FOREGROUND);
    }
    /**
     * @see SWT#COLOR_TITLE_INACTIVE_FOREGROUND
     */
    public static Color titleInactiveForeground() { return SystemColorFactory
            .getColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
    }
    /**
     * @see SWT#COLOR_TITLE_INACTIVE_BACKGROUND
     */
    public static Color titleInactiveBackground() { return SystemColorFactory
            .getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
    }
    /**
     * @see SWT#COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT
     */
    public static Color titleInactiveGradient() { return SystemColorFactory
            .getColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
    }

    /**
     * @see SWT#COLOR_INFO_FOREGROUND
     */
    public static Color tooltipForeground() { return SystemColorFactory
            .getColor(SWT.COLOR_INFO_FOREGROUND);
    }
    /**
     * @see SWT#COLOR_INFO_BACKGROUND
     */
    public static Color tooltipBackground() { return SystemColorFactory
            .getColor(SWT.COLOR_INFO_BACKGROUND);
    }

    /*
     * Misc. colors
     */
    /** One of the pre-defined colors */
    public static Color white() { return ColorUtil.getColor(255);//new Color(null, 255, 255, 255);
    }
    /** One of the pre-defined colors */
    public static Color lightGray() { return ColorUtil.getColor(192);//new Color(null, 192, 192, 192);
    }
    /** One of the pre-defined colors */
    public static Color gray() { return ColorUtil.getColor(128);//new Color(null, 128, 128, 128);
    }
    /** One of the pre-defined colors */
    public static Color darkGray() { return ColorUtil.getColor(64);//new Color(null, 64, 64, 64);
    }
    /** One of the pre-defined colors */
    public static Color black() { return ColorUtil.getColor(0);//new Color(null, 0, 0, 0);
    }
    /** One of the pre-defined colors */
    public static Color red() { return ColorUtil.getColor(255,0,0);//new Color(null, 255, 0, 0);
    }
    /** One of the pre-defined colors */
    public static Color orange() { return ColorUtil.getColor(255,196,0);//new Color(null, 255, 196, 0);
    }
    /** One of the pre-defined colors */
    public static Color yellow() { return ColorUtil.getColor(255,255,0);//new Color(null, 255, 255, 0);
    }
    /** One of the pre-defined colors */
    public static Color green() { return ColorUtil.getColor(0,255,0);//new Color(null, 0, 255, 0);
    }
    /** One of the pre-defined colors */
    public static Color lightGreen() { return ColorUtil.getColor(96,255,96);//new Color(null, 96, 255, 96);
    }
    /** One of the pre-defined colors */
    public static Color darkGreen() { return ColorUtil.getColor(0,127,0);//new Color(null, 0, 127, 0);
    }
    /** One of the pre-defined colors */
    public static Color cyan() { return ColorUtil.getColor(0,255,255);//new Color(null, 0, 255, 255);
    }
    /** One of the pre-defined colors */
    public static Color lightBlue() { return ColorUtil.getColor(127,127,255);//new Color(null, 127, 127, 255);
    }
    /** One of the pre-defined colors */
    public static Color blue() { return ColorUtil.getColor(0,0,255);//new Color(null, 0, 0, 255);
    }
    /** One of the pre-defined colors */
    public static Color darkBlue() { return ColorUtil.getColor(0,0,127);//new Color(null, 0, 0, 127);
    }

}
