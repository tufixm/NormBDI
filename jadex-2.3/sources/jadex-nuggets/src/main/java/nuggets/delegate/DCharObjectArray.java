/*
 * DcharArray.java
 * Copyright (c) 2005 by University of Hamburg. All Rights Reserved.
 * Departament of Informatics. 
 * Distributed Systems and Information Systems.
 *
 * Created by walczak on Jan 18, 2006.  
 * Last revision $Revision: 4064 $ by:
 * $Author: walczak $ on $Date: 2006-02-23 18:48:47 +0100 (Do, 23 Feb 2006) $.
 */
package nuggets.delegate;

import nuggets.IAssembler;
import nuggets.ICruncher;

/** DcharArray 
 * @author walczak
 * @since  Jan 18, 2006
 */
public class DCharObjectArray extends ADelegate
{
	/** 
	 * @param o
	 * @param mill
	 * @see nuggets.delegate.ADelegate#persist(java.lang.Object, nuggets.ICruncher)
	 */
	public void persist(Object o, ICruncher mill, ClassLoader classloader)
	{

		  mill.startConcept(o);
        mill.put("type", Character[].class.getName());
		Character[] a=(Character[]) o;
		int len=a.length;
		mill.put("length", Integer.toString(len));

		for(int i=0; i<len; i++) {
			mill.addToken(a[i]!=null?Integer.toString(a[i].charValue(),16):"null");
		}
	}

	/** 
	 * @param clazz
	 * @param asm
	 * @return a new instance of this array
	 * @throws Exception
	 */
	public Object getInstance(Class clazz,IAssembler asm) throws Exception
	{
		int l=Integer.parseInt((String)asm.getAttributeValue("length"));
		Character[] a=new Character[l];
		for(int i=0; i<l; i++) {
			try {
				a[i] = new Character((char)Integer.parseInt(asm.nextToken(), 16));
			} catch(NumberFormatException e) { /* NOP */ }
		}
		return a;
	}	
}


/* 
 * $Log$
 * Revision 1.2  2006/02/23 17:46:25  walczak
 * LF
 *
 * Revision 1.1  2006/02/21 15:02:16  walczak
 * *** empty log message ***
 *
 * Revision 1.3  2006/02/17 12:48:54  walczak
 * yet even faster
 *
 * Revision 1.2  2006/02/16 17:41:08  walczak
 * no reference to strings in Maps but a direct inclusion.
 *
 * Revision 1.1  2006/01/20 18:11:01  walczak
 * ------------------------
 *
 */