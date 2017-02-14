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

/**
 * ParameterException
 *
 * Exception when something is wrong with a parameter to set.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class ParameterException extends Exception {
  /**
   * Creates a ParameterException instance.
   *
   * @param message Error message.
   */
  public ParameterException(String message) {
    super(message);
  }
}
