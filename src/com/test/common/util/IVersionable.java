package com.progress.common.util ;

import java.rmi.*;


public interface IVersionable extends Remote
{
    public String getVersion() throws RemoteException ;
}

