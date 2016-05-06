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

package tdunnick.jphineas.util;

import java.util.*;

public class ByteArray
{
	public final static byte[] insert (byte[] b1, byte[] b2, int start)
	{
		byte[] b = new byte [b1.length + b2.length];
		int i;
		for (i = 0; i < start; i++)
			b[i] = b1[i];
		for (int j = 0; j < b2.length; j++)
			b[i++] = b2[j];
		for (int j = start; j < b1.length; j++)
			b[i++] = b1[j];
		return b;
	}
	
	public final static byte[] append (byte[] b1, byte[] b2)
	{
		return insert (b1, b2, b1.length);
	}
	
	public final static byte[] copy (byte[] b1, int start, int length)
	{
		byte[] b = new byte[length];
		for (int i = 0; i < length; i++)
			b[i] = b1[start + i];
		return b;
	}
	
	public final static byte[] copy (byte[] b1, int start)
	{
		return copy (b1, start, b1.length - start);
	}
}
