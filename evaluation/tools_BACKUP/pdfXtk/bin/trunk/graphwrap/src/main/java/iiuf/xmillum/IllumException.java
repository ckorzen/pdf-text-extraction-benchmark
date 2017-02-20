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
 * IllumException
 *
 * Base class for exceptions thrown by xmillum.
 *
 * @author $Author: ohitz $
 * @version $Revision: 1.1 $
 */
public class IllumException extends Exception {
  Exception e;

  public IllumException(String message) {
    super(message);
  }

  public IllumException(Exception e) {
    super(e.toString());
    this.e = e;
  }

  public IllumException(Error e) {
    super(e.toString());
  }

  public void printStackTrace() {
    super.printStackTrace();
    e.printStackTrace();
  }
}
