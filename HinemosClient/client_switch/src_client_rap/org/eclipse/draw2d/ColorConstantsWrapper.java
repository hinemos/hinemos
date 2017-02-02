/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package org.eclipse.draw2d;

import org.eclipse.draw2d.rap.swt.graphics.ColorUtil;
import org.eclipse.swt.graphics.Color;

/**
 * ColorConstantsがRCPとRAPのdraw2dで呼び方が異なるためラップするためのクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ColorConstantsWrapper {
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
