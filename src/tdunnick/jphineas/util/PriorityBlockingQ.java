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

/**
 * I don't find a bounded priority blocking queue in java.util.concurrent, so
 * here it is (I hope :-).  Note this is not a full BlockingQueue implementation
 * and the priority is an explicit parameter, rather than implemented as a 
 * comparator.
 * 
 * @author tld
 *
 */
public class PriorityBlockingQ
{
	/** the queue */
	Object[] queue = null;
	/** priorities of queue entries */
	int[] priority = null;
	/** back of the queue, where the line forms */
	int back = 0;
	/** number of things in the line */
	int sz = 0;

	/**
	 * Creates the blocking queue
	 * @param sz of this queue
	 */
	public PriorityBlockingQ (int sz)
	{
		queue = new Object[sz];
		priority = new int[sz];
	}

	/**
	 * Get current size the queue (number of objects in it)
	 * @return it's size
	 */
	public synchronized int size()
	{
		return sz;
	}
	
	/**
	 * Get the room left in this queue
	 * @return reserve capacity
	 */
	public synchronized int room ()
	{
		return queue.length - sz;
	}

	/**
	 * Conditionally add an object to the queue.
	 * @param o to add
	 * @param prior (ity) for ordering the queue
	 * @param timeout to wait for room to become available
	 * @return true if object added
	 * @throws InterruptedException
	 */
	public synchronized boolean offer (Object o, int prior, long timeout)
	  throws InterruptedException
	{
		if (sz == queue.length)
			wait (timeout);
		return add (o, prior);
	}
	
	/**
	 * Put an object in the queue.  Block until room becomes available.
	 * @param o object to add
	 * @param prior (ity) for ordering the queue
	 * @return true if object added
	 * @throws InterruptedException
	 */
	public synchronized boolean put(Object o, int prior)
			throws InterruptedException
	{
		while (sz == queue.length)
			wait();
		return add(o, prior);
	}
	
	/**
	 * check if this object is already in the queue
	 * @param o object to check
	 * @return true if already queued
	 */
	public synchronized boolean contains (Object o)
	{
		for (int i = 0; i < sz; i++)
		{
			if (queue[i].equals(o))
				return true;
		}
		return false;
	}
	
	/**
	 * Add an object to the queue. 
	 * @param o object to add
	 * @param prior (ity) for ordering the queue
	 * @return true if object added
	 * @throws InterruptedException
	 */
	public synchronized boolean add (Object o, int prior)
	{
		if (contains (o))
			return true;
		if (sz >= queue.length)
			return false;
		// find priority spot in the queue
		if (back >= queue.length)
			back = 0;
		int n = back;
		for (int i = 0; i < sz; i++)
		{
			int p = n - 1;
			if (p < 0)
				p = queue.length - 1;
			if (prior <= priority[p])
				break;
			priority[n] = priority[p];
			queue[n] = queue[p];
			n = p;			
		}
		queue[n] = o;
		priority[n] = prior;
		back++;
		sz++;
		notify();
		return true;
	}

	/**
	 * Get the front object in the queue. Block until one becomes available.
	 * @return the object or null if fails
	 * @throws InterruptedException
	 */
	public synchronized Object take() throws InterruptedException
	{
		while (sz < 1)
			wait();
		return poll();
	}
	
	/**
	 * Get the front object in the queue. Wait a while if nothing is available.
	 * @param timeout maximum time to wait for an object
	 * @return the object or null if fails
	 * @throws InterruptedException
	 */
	public synchronized Object poll (long timeout) throws InterruptedException
	{
		if (sz < 1)
			wait (timeout);
		return poll ();
	}
	
	/**
	 * Get the front object in the queue.
	 * @return the object or null if fails
	 * @throws InterruptedException
	 */
	public synchronized Object poll()
	{
		Object o = peek ();
		if (o != null)
		  sz--;
		notify();
		return o;
	}
	
	/**
	 * Peek at the front object in the queue, but don't remove it.
	 * @return the object or null if fails
	 * @throws InterruptedException
	 */
	public synchronized Object peek ()
	{
		if (sz < 1)
			return null;
		int front = (back - sz);
		if (front < 0)
			front += queue.length;
		return queue[front];		
	}
	
	/**
	 * Clear all objects from this queue.
	 */
	public synchronized void clear ()
	{
		sz = 0;
		notify ();
	}
}
