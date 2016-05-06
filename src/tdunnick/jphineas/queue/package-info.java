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

/**
 * jPhineas queues are the mechanism that provides control, status, audit,
 * and persistence of state.  A single configuration file describes queue
 * types including a list of fields, and connections for persistence.  For
 * queue types, the naming used by the persistence is abstracted from the
 * application by an ID.  That allows dB's for example, to use field naming that
 * differs from jPhineas conventions if desired.
 * <p>
 * Likewise, connections are abstracted from the actual persistence and managed
 * here.  Initial support is provided for JDBC and a simple file based "Memory"
 * connection, but other types can be added as desired (for example a remote JMX
 * service).
 * <p>
 * A manager is created for each configuration.  It creates the needed connections
 * and types and uses them to dole out queues as requested.  The connections hold
 * the implementation details for a given persistence, which queue users access
 * via the queue or a queue row.
 * 
 */
package tdunnick.jphineas.queue;