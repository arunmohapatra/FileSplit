package com.progress.common.util;
import com.progress.common.networkevents.*;
import java.rmi.*;
import java.rmi.server.*;

public interface IEClientSpecificEvent extends IEventObject
{
    public Object guiID() throws RemoteException;
}

 
// END OF FILE
 
