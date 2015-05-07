/*
 *  Copyright (c) 2015 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
 *  
 *  This file is part of jPhineas.
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


// find the ancestor matching this tag

function findAncestor(o, tag)
{
  while (o && (o.nodeName != tag))
  {
    if (o.parentNode) o = o.parentNode;
    else if (o.offsetParent) o = o.offsetParent;
  }
  return (o);
}

// find the previous sibling matching this tag

function findPrevSibling(o, tag)
{
  while (o && (o.nodeName != tag))
  o = o.previousSibling;
  return (o);
}

// find the next sibling matching this tag

function findNextSibling(o, tag)
{
  while (o && (o.nodeName != tag))
  o = o.nextSibling;
  return (o);
}

// find the child matching this tag

function findChild(o, tag)
{
  if (o) o = findNextSibling(o.firstChild, tag);
  return (o);
}

// add a repeated field - assumes at least one parent TR with a 
// prompt and input in each of two TD elements.

function addfield(o)
{
  var fname = o.name;
  o = findAncestor(o, "TR");
  // get the index of the previous row
  var p = findPrevSibling(o.previousSibling, "TR");
  p = findChild(p, "TD");
  var i = p.innerHTML.replace(new RegExp("^\\s*" + fname), "") - 0;
  // create a new table row
  var c = document.createElement("TR");
  var d = document.createElement("TD");
  // add the prompt
  d.appendChild(document.createTextNode(fname + " " + (i + 1)));
  c.appendChild(d);
  // get input entry
  p = findChild(findNextSibling(p.nextSibling, "TD"), "INPUT");
  var f = document.createElement("INPUT");
  f.type = p.type;
  f.size = p.size;
  f.name = p.name.replace(new RegExp(fname + ".*\\]"), fname + "[" + i + "]");
  d = document.createElement("TD");
  d.appendChild(f);
  c.appendChild(d);
  // finally place it in the form
  if (o.parentNode) o.parentNode.insertBefore(c, o);
  else o.offsetParent.insertBefore(c, o);
}

/*
 * additions for Yetti to track and delete cookies on submit
 */
var tabber = new Array(); // Yetti tabbers used in config.c

function addTabber (t)
{
  var n = tabber.length;
  tabber[n] = t;
}

/*
function deleteCookie(cname) 
{
  var cookies = document.cookie.split(";");

  for (var i = 0; i < cookies.length; i++) 
  {
    var cookie = cookies[i].replace (/^\s+/, "");
    var eqPos = cookie.indexOf("=");
    var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
    if ((cname == null) || (cname == name))
    {
      document.cookie = name + "=deleted;expires=" + 
        new Date(0).toUTCString() + ";path=/";
    }
  }
}
*/

/*
 * our submit function...  
 * set the hidden request value to a name value pair to get around 
 * differences in how browser pass button values and to suppress
 * accidental submission by the Enter key and make user confirm
 * the submission.  
 */

function setRequest(o, v)
{
  o.form.ConfigurationRequest.value = o.name + ":" + v;
  if (confirm (o.value)) 
  {
    /* remove cookies???
    for (var i = 0; i < tabber.length; i++)
    {
      if (tabber[i])
        deleteCookie (tabber[i].defaults.id);
    }
    */
    o.form.submit();
  }
}

/*
 * confirms a restart request
function askRestart ()
{
  if (confirm ("Restart PHINEAS?"))
  {
    deleteCookie (null);
    window.location = "?restart";
  }
}
 */

/*
 * Support for bubble type help tips.
 * The following is loosely based on Bubble Tooltips by Alessandro Fulciniti
 * - http://pro.html.it - http://web-graphics.com 
 *
 * provide balloon tips help when mouse enters element area
 */
var bubbles = true;
function showHelp (o, h)
{
  if ((!bubbles) || (!o) || (!h) || (h.length == 0))
    hideHelp();
  else
  {
    var b = createHelp (h);
    if (b != null)
      moveHelp (o, b);
  }
}

/*
 * hide the help bubble after mouse leaves element area
 */
function hideHelp ()
{
  if (!document.getElementById)
    return;
  var b = document.getElementById ("balloon_help");
  if (b != null)
    b.style.display = "none";
}

/*
 * create the help bubble if need be and return it with the
 * help text embedded
 */
function createHelp (h)
{
  if (!document.getElementById)
    return null;
  var c = null;
  var b = document.getElementById ("balloon_help");
  if (b == null)
  {
    b = document.createElement ("span");
    b.id = "balloon_help";
    b.className = "tip";

    c = document.createElement ("span");
    c.className = "top";
    b.appendChild (c);

    var d = document.createElement ("span");
    d.className = "bottom";
    b.appendChild (d);

    document.body.appendChild (b);
  }
  else
  {
    // remove old text
    c = b.firstChild;
    c.removeChild (c.firstChild);
  }
  c.appendChild (document.createTextNode (h));
  b.style.display = "block";
  return (b);
}

/*
 * move our help bubble next to the input field, which is normally
 * a child of the object moused over.  That made assigning the events
 * easier and also gave a bigger trigger area for the event.
 */
function moveHelp (o, bubble)
{
  var p = getXY (o);
  if (!p)
  {
    alert ("can't move help - no xy");
    bubbles = false;
    return hideHelp ();
  }
  bubble.style.top = (p.x) + "px";
  bubble.style.left = (p.y - 40) + "px";
}

/*
 * get the XY coordinate of the last child element of this object
 * for positioning our help bubble
 */
function getXY (el)
{
  var r, b;
  // position the bubble at the last child element
  el = el.lastChild;
  while (el.nodeType != 1)
    el = el.previousSibling;
  // old school using offset parent
  if (el.offsetParent)
  {
    r = el.offsetWidth;
    b = el.offsetHeight;
    do
    {
      r += el.offsetLeft;
      b += el.offsetTop;
    } while (el = el.offsetParent);
  }
  // new way using bounding client rectangle
  else if (el.getBoundingClientRect)
  {
    var loc = el.getBoundingClientRect();
    r = loc.right;
    b = loc.bottom;
    var d = document.documentElement;
    if (d.scrollTop)
    {
      r += d.scrollLeft - d.clientLeft;
      b += d.scrollTop - d.clientTop;
    }
    else if (window.pageYOffset)
    {
      r += d.window.pageYOffset;
      b += d.window.pageXOffset;
    }
    else
    {
      d = document.body;
      r += d.scrollLeft;
      b += d.scrollTop;
    }
  }
  else
  {
    return null;
  }
  return { x:b, y:r };
}
