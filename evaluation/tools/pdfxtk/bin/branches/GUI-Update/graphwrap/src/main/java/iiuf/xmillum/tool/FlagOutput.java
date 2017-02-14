/* (C) 2000-2002, DIUF, http://www.unifr.ch/diuf
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package iiuf.xmillum.tool;

import iiuf.dom.DOMUtils;
import iiuf.xmillum.BrowserContext;
import iiuf.xmillum.Parameter;
import iiuf.xmillum.ParameterException;
import iiuf.xmillum.Tool;
import iiuf.xmillum.Window;
import iiuf.xmillum.FlagAccess;
import iiuf.xmillum.FlagListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.w3c.dom.Element;

/**
 * FlagOutput
 *
 * Debugging tool for flag sets.
 *
 * <p>Optional initialization parameters:
 * <ul>
 *   <li>visible: 0/1 (default: 1)
 * </ul>
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class FlagOutput implements Tool {

  static Map parameters = new HashMap();

  static {
    parameters.put("flag", new Parameter() {
	public void setParam(BrowserContext c, Object o, String v, String opt) throws ParameterException {
	  FlagOutput f = (FlagOutput) o;
	  f.flagName = v;
	}
      });
  }

  String     flagName = null;
  FlagAccess flagAccess = null;

  public void activateTool(BrowserContext c, Element e) {
    Parameter.setParameters(c, e, this, parameters);

    if (flagName != null) {
      flagAccess = c.flagger.addFlagListener(flagName, new FlagListener() {
	  public void setFlag(Element e, String value) {
	    System.out.println(value+":");
	    System.out.print(DOMUtils.toString(e));
	  }
	});
    }
  }

  public void deactivateTool() {
  }
}
