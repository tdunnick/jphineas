/*
 *  Copyright (c) 2015 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
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

import groovy.util.GroovyTestCase;
import tdunnick.jphineas.util.*;

class PriorityBlockingQTest extends GroovyTestCase
{
	int sz = 10;
	PriorityBlockingQ q = null;
	
	protected void setUp() throws Exception
	{
		q = new PriorityBlockingQ (sz);
	}
	
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public final void testPriorityBlockingQ()
	{
		assert q != null : "queue not instantiated"
	}
	
	public final void testBasic()
	{
		int i;
		assert q.size() == 0 : "queue should be empty"
		for (i = 0; i < sz; i++)
		{
			q.put (Integer.valueOf(i), 0)
			assert q.size() == i + 1 : "queue size should be " + (i + 1);
		}
		for (i = 0; i < sz; i++)
		{
			Integer v = (Integer) q.take();
			assert i == v.intValue() : "expected value " + i + " but got " + v.intValue()
			assert q.size() == sz - (i+1) : "wrong queue size"
		}
	}
	
	public final void testPriority()
	{
		int i;
		for (i = 0; i < sz; i++)
		{
			q.put (Integer.valueOf(i), i)
			assert q.size() == i + 1 : "queue size should be " + (i + 1);
		}
		while (i-- > 0)
		{
			Integer v = (Integer) q.take();
			assert i == v.intValue() : "expected value " + i + " but got " + v.intValue()
			assert q.size() == i : "wrong queue size"
		}
	}
	
	public final void testPriority2 ()
	{
		int i;
		for (i = 0; i < sz; i++)
		{
			if ((i % 2) == 0)
			  q.put (Integer.valueOf(i), i)
			else
				q.put (Integer.valueOf(i), sz - i);
		}
		for (i = 0; i < sz; i++)
		{
			Integer v = (Integer) q.take();
			if ((i % 2) == 0)
			  assert v.intValue() == (i + 1) : "wrong even value"
			else
				assert v.intValue() == (sz - (i + 1)) : "wrong odd value"
		}
	}	
	public final void testPriority3()
	{
		int i;
		for (i = 0; i < sz; i++)
		{
			q.put (Integer.valueOf(i), i)
			assert q.size() == i + 1 : "queue size should be " + (i + 1);
		}
		while (i-- > 0)
		{
			Integer v = (Integer) q.take();
			assert i == v.intValue() : "expected value " + i + " but got " + v.intValue()
			assert q.size() == i : "wrong queue size " + q.size()
		}
		for (i = 0; i < sz; i++)
		{
			if ((i % 2) == 0)
			  q.put (Integer.valueOf(i), i)
			else
				q.put (Integer.valueOf(i), sz - i);
			assert q.size() == i + 1 : "queue size should be " + (i + 1) + " got " + q.size()
		}
		for (i = 0; i < sz; i++)
		{
			Integer v = (Integer) q.take();
			if ((i % 2) == 0)
			  assert v.intValue() == (i + 1) : "wrong even value"
			else
				assert v.intValue() == (sz - (i + 1)) : "wrong odd value"
			assert q.size() == sz - (i+1) : "wrong queue size " + q.size()
		}
	}
}

