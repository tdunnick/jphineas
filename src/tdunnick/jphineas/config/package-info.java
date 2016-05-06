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
 * jPhineas includes utility classes for managing configurations.  The underlying object 
 * is a {@link tdunnick.jphineas.xml.XmlContent}.
 * <p>
 * The {@link tdunnick.jphineas.config.XmlConfig XmlConfig} class provides base 
 * methods for accessing configuration values without the need for a full tag "path".
 * Each class within jPhineas can be passed a configuration with "short cuts" to 
 * data of interest.
 * <p>
 * The jPhineas configurations are sub-classed to hide all actual tags from the
 * rest of the code base and provide getters instead, insulating configuration.
 * <p>
 * All folder and file references within the configuration may be either absolute
 * (from the root) or relative.  For the latter a <b>DefaultDir</b> may be specified
 * as the prefix.
 * <p>
 * Most tags/values may be "inherited" from parent nodes in the configuration.
 * The getters for those tags exist in the configuration class in which they may
 * first appear (e.g. the "default").
 * 
 */
package tdunnick.jphineas.config;