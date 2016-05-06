/*
 *  Copyright (c) 2015-2016 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
 *  
 *  This file is part of jPhineas
 *
 *  jPhineas is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  jPhineas is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jPhineas.  If not, see <http://www.gnu.org/licenses/>.
 */

;

import tdunnick.jphineas.util.ByteArray;
import groovy.util.GroovyTestCase;

class ByteArrayTest extends GroovyTestCase
{
	String s = "The quick brown fox jumped over the lazy dogs"
	byte[] b1 = s.substring(0, 20).getBytes();
	byte[] b2 = s.substring (20).getBytes();
	
	public final void testInsert()
	{
		byte[] b = ByteArray.insert (b1, b2, 20)
		assert s.equals(new String(b)) : "insert failed"
	}

	public final void testAppend()
	{
		byte[] b = ByteArray.append (b1, b2)
		assert s.equals(new String(b)) : "append failed"
		
	}

	public final void testCopyByteArray()
	{
		byte[] b = ByteArray.copy (s.getBytes(), 0, 20);
		assert new String(b).equals(new String(b1)) : "copy from beginning failed"
		b = ByteArray.copy (s.getBytes(), 20)
		assert new String(b).equals (new String (b2)) : "copy from middle failed"
	}
}

