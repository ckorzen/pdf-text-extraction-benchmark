/* (C) 2001-2002, DIUF, http://www.unifr.ch/diuf
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

package iiuf.xmillum;

import iiuf.util.Util;

import java.util.HashMap;

import org.w3c.dom.Element;

/**
 * ToolFactory
 *
 * This factory class produces tools.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class ToolFactory {

  protected static HashMap classes;
  protected static Class defaultClass;

  static {
    classes = new HashMap();
    defaultClass = Tool.class;
  }

  public static Tool getTool(Element element, ClassLoader loader) {
    if (!element.hasAttribute("class")) {
      System.err.println("No class defined for tool.");
      return null;
    }

    String className = element.getAttribute("class");
    Class clazz = (Class) classes.get(className);
    if (clazz == null) {
      try {
	clazz = Class.forName(className, true, loader);
      } catch (ClassNotFoundException e) {
	clazz = defaultClass;
      }
      classes.put(className, clazz);
    }

    Object obj = null;

    try {
      obj = clazz.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    try {
      return (Tool) obj;
    } catch (ClassCastException e) {
      e.printStackTrace();
      return null;
    }
  }
}
