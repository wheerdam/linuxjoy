/*
	Copyright 2016 Wira Mulia

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package joymonitor;

import javax.swing.*;
import java.awt.*;

import org.bbi.linuxjoy.LinuxJoystick;

/**
 *
 * @author wira
 */
public class MonitorCanvas extends JPanel {

    private MonitorWindow mw;
    private int[] axisValues;
    private boolean[] buttonValues;

    public MonitorCanvas(MonitorWindow mw) {
        this.mw = mw;
    }

    @Override
    public void paint(Graphics gg) {
        String str;
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
        this.setSize(this.getParent().getSize());
        int W = this.getWidth();
        int H = this.getHeight();
        int str_w;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, W, H);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        if(mw.isJoystickOpen()) {
            for(int i = 0; i < axisValues.length; i++) {
                
                g.setColor(Color.YELLOW);
                str = "Axis " + i;
                str_w = g.getFontMetrics().stringWidth(str);
                g.drawString(str, 95-str_w, 5*(i+1)+25*i+12+g.getFontMetrics().getHeight()/2);
                g.setColor(Color.DARK_GRAY);
                g.fillRect(100, 5*(i+1)+25*i, W-105, 25);
                g.setColor(Color.BLUE);
                g.fillRect(100, 5*(i+1)+25*i, (int)(getPercentage(axisValues[i])*(W-105)), 25);
                g.setColor(Color.WHITE);
                //g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
                str = String.format("%.2f", getPercentage(axisValues[i]));
                str_w = g.getFontMetrics().stringWidth(str);
                g.drawString(str, W-20-str_w, 5*(i+1)+25*i+12+g.getFontMetrics().getHeight()/2);
            }

            int row = 0;
            for(int i = 0; i < buttonValues.length; i++) {
                if(i != 0 && i % 6 == 0) {
                    row++;
                }
                g.setColor(buttonValues[i] ? Color.CYAN : Color.DARK_GRAY);
                g.fillRect(5*(i-6*row+1)+(W-35)/6*(i-6*row), 10+5*axisValues.length+25*axisValues.length+row*30, (W-35)/6, 25);
                str = "" + i;
                str_w = g.getFontMetrics().stringWidth(str);
                g.setColor(Color.YELLOW);
                g.drawString(str, 5*(i-6*row+1)+(W-35)/6*(i-6*row)+(W-35)/12-str_w/2,
                        10+5*axisValues.length+25*axisValues.length+row*30+25/2+g.getFontMetrics().getHeight()/2);
            }
        } else {
            g.setColor(Color.RED);
            str = "No joystick open";
            str_w = g.getFontMetrics().stringWidth(str);
            g.drawString(str, W/2-str_w/2, H/2-g.getFontMetrics().getHeight()/2);

        }
    }

    public void setTotalAxes(int v) {
        axisValues = new int[v];
    }

    public void setTotalButtons(int v) {
        buttonValues = new boolean[v];
    }

    public void setAxisValue(int num, int val) {
        axisValues[num] = val;
    }

    public void setButtonValue(int num, boolean val) {
        buttonValues[num] = val;
    }

    public double getPercentage(int val) {
        return ( (val + 32767) / (2.0*32767) );
    }
}
