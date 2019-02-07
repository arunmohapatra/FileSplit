package com.progress.common.util;
import com.progress.common.networkevents.*;
public interface IEAddEvent extends IEventObject, IEClientSpecificEvent
{
	public Object getChild()
		throws java.rmi.RemoteException;
 
}

// END OF FILE
 
