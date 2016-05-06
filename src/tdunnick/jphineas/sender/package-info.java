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
 * The jPhineas {@link tdunnick.jphineas.sender.Sender sender} is responsible 
 * for outgoing requests.  Generally
 * those requests contain data (payloads) intended for the recipient, where the
 * recipient response indicates the status of the request.  However, the request
 * can also be for data (payloads) where the response returns data to the 
 * {@link tdunnick.jphineas.sender.Sender sender}.
 * <p>
 * Although jPhineas was created as a CDC PHIN-MS compatible messaging application,
 * the underlying ebXML is only one possible messaging protocol.  The expectation is
 * that other protocols (MLLP, sFTP, JMX, etc.) could be supported in future
 * version by the addition of appropriate "processor" classes.
 * <p>
 * The Architecture consists of a single J2EE servlet running within an appropriate
 * container (Tomcat, Jetty, etc).  The servlet is configured from an XML file specified
 * in web.xml.  This configuration is expected to have all of the needed information.
 * Some of that information is common to all protocols, while other information
 * may be protocol specific. It consists of three sections:
 * <ol>
 * <li>General data common to all protocols (demographics, shared resources, etc)</li>
 * <li>Routing data used to define end points for requests</li>
 * <li>Mapping data used to define sources and Routes for requests</li>
 * </ol>
 * <p>
 * The conventional layout for jPhineas related files is:
 * <ul>
 * <li>jphineas - default folder used for relative paths
 * <ul>
 * <li>config - configurations</li>
 * <li>queues - persistance</li>
 * <li>security - certificates, keystores, etc.</li>
 * <li>templates - various packaging templates</li>
 * <li>tmp - temporary files</li>
 * <li>log - log files</li>
 * <li>data - user (transport) data including...
 * <ul>
 * <li>incoming - received payloads</li>
 * <li>outgoing - payload waiting on transport</li>
 * <li>processed - payloads in transit</li>
 * <li>ack - payload acknoledgements</li>
 * </ul>
 * These may be further broken down by program, user, or data type as desired.
 * </li>
 * </ul>
 * </li>
 * </ul>
 * Note that conventions are just that... modify configuration paths to suit
 * your organizational needs.
 * <p>
 * A persistent set of transport {@link tdunnick.jphineas.queue.PhineasQ queues}
 * are used to control, monitor, and audit requests and subsequent responses. 
 * The {@link tdunnick.jphineas.sender.Sender sender} starts a 
 * {@link tdunnick.jphineas.sender.QueuePoller queue poller} which
 * places queued requests onto a {@link tdunnick.jphineas.util.PriorityBlockingQ list}.
 * A set of {@link tdunnick.jphineas.sender.QueueThread queue threads} 
 * started by the poller then picks items
 * off this list and invokes a {@link tdunnick.jphineas.sender.RouteProcessor processor} 
 * to construct, then send the message, and analyze the response.  
 * The queue is subsequently updated with the results.
 * <p>
 * The {@link tdunnick.jphineas.sender.Sender sender} also starts a 
 * {@link tdunnick.jphineas.sender.FolderPoller folder poller} which
 * tracks a set of folders for files and makes transport queue entries as they are
 * found. The {@link tdunnick.jphineas.sender.FolderPoller folder poller} 
 * also makes use of a 
 * {@link tdunnick.jphineas.sender.FolderProcessor processor} 
 * to manage the protocol specific actions,
 * including invoking any {@link tdunnick.jphineas.filter.PhineasInputFilter filter}
 * that may be plugged in as part of the processing (for example, an HL7 transform).
 * <p>
 * Both pollers and the queue threads are instances of
 * {@link tdunnick.jphineas.util.Pthread jPhineas thread} to provide a common interface for
 * controlled shutdown of the {@link tdunnick.jphineas.sender.Sender sender}.
 */

package tdunnick.jphineas.sender;