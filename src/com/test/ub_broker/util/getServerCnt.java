
/*************************************************************/
/* Copyright (c) 1984-2005 by Progress Software Corporation  */
/*                                                           */
/* All rights reserved.  No part of this program or document */
/* may be  reproduced in  any form  or by  any means without */
/* permission  in  writing  from  Data Language Corporation. */
/*************************************************************/
/*
 * $Id: getServerCnt.java,v 1.6 1999/10/06 14:18:12 tan Exp $
 */

/*********************************************************************/
/* Module : getServerCnt                                             */
/*                                                                   */
/*                                                                   */
/*                                                                   */
/*********************************************************************/

package com.progress.ubroker.util;


import java.lang.*;
import java.util.*;
import com.progress.common.licensemgr.*;
import com.progress.common.message.*;
import com.progress.common.exception.*;


/*********************************************************************/
/*                                                                   */
/* Class getServerCnt                                                */
/*                                                                   */
/*********************************************************************/
public class getServerCnt implements ubConstants
{
private static final int END_OF_LIST = 0;
private static final int ARBITRARY_LARGE_VALUE = 4096;

private boolean hasDevLicense = false;
private boolean hasNonDevLicense = false;

   /*
   ** Method: getMaxServers
   **
   **  The follow code will attempt to establish a ProductInfo
   **  object for all of the WebSpeed/OpenAppServer products
   **  and pick the largest user (server) count from the bunch.
   **  The following products are looked for:
   **
   **  WEBSPEED: (uBrokerType == SERVERTYPE_WEBSPEED)
   **      WEBSPEED_DEVSRVR
   **      WEBVISION_DEVSRVR
   **      WEBSPEED_TS
   **      WEBSPEED_ENT_TS
   **      WEBVISION           (complete install on NT)
   **      WEBSPEED_WORKSHOP   (complete install on NT)
   **      OE_APPLICATION_SVR_BASIC
   **      OE_APPLICATION_SVR_ENT
   **
   **  APPSERVER: (uBrokerType == SERVERTYPE_APPSERVER)
   **      PROVISION_DEVSRVR
   **      WEBVISION_DEVSRVR
   **      PROVISION          (complete install on NT)
   **      WEBVISION           (complete install on NT)
   **      APPSERVER
   **      APPSERVER_SECURE
   **      OE_APPLICATION_SVR_BASIC
   **      OE_APPLICATION_SVR_ENT
   **
   */

   public int getMaxServers(int initServers, int uBrokerType)
        throws LicenseMgr.CannotContactLicenseMgr,
          LicenseMgr.NotLicensed

   {
       LicenseMgr licMgr;
       ProductInfo prodInfo;
       int productID;
       int indx;
       int prodUsers;
       int currUsers;
       boolean fAllowed = true;
       int[] pidList = new int[12];


       // create an instance of a LicenseMgr object
       licMgr = new LicenseMgr();

       /*
       ** Fill the pidList array based on the Broker's
       ** operating mode.
       */
       if (uBrokerType == SERVERTYPE_APPSERVER)
       {
           /*
           ** Determine R2R - for AppServer
           */
           try
           {
               // Be sure we are allowed to run AppServer
               fAllowed = licMgr.checkR2Run(LicenseMgr.R2R_APP_SERV);
           }
           catch (LicenseMgr.NotLicensed e)
           {
               fAllowed = false;
           }
           pidList[0] = LicenseMgr.PROVISION_DEVSRVR;
           pidList[1] = LicenseMgr.WEBVISION_DEVSRVR;
           pidList[2] = LicenseMgr.WEBVISION;
           pidList[3] = LicenseMgr.PROVISION;
           pidList[4] = LicenseMgr.APPSERVER;
           pidList[5] = LicenseMgr.APPSERVER_SECURE;
           pidList[6] = LicenseMgr.OE_APPLICATION_SVR_BASIC;
           pidList[7] = LicenseMgr.OE_APPLICATION_SVR_ENT;
           pidList[8] = LicenseMgr.OE_STUDIO;
           pidList[9] = LicenseMgr.OE_DEVELOPMENT_SERVER;
           pidList[10] = END_OF_LIST;
       }
       else
           if (uBrokerType == SERVERTYPE_WEBSPEED)
           {
               /*
               ** Determine R2R - for WebSpeed
               */
               try
               {
                   // Be sure we are allowed to run Transaction Server
                   fAllowed = licMgr.checkR2Run(LicenseMgr.R2R_WEB_CLIENT);
               }
               catch (LicenseMgr.NotLicensed e)
               {
                   fAllowed = false;
               }
               pidList[0] = LicenseMgr.WEBSPEED_DEVSRVR;
               pidList[1] = LicenseMgr.WEBVISION_DEVSRVR;
               pidList[2] = LicenseMgr.WEBSPEED_TS;
               pidList[3] = LicenseMgr.WEBSPEED_ENT_TS;
               pidList[4] = LicenseMgr.WEBVISION;
               pidList[5] = LicenseMgr.WEBSPEED_WORKSHOP;
               pidList[6] = LicenseMgr.OE_APPLICATION_SVR_BASIC;
               pidList[7] = LicenseMgr.OE_APPLICATION_SVR_ENT;
               pidList[8] = LicenseMgr.OE_STUDIO;
               pidList[9] = LicenseMgr.OE_DEVELOPMENT_SERVER;
               pidList[10] = END_OF_LIST;
           }
           else
               return initServers;  /* simply return initial value -DataServers */

       /*
       ** If neither AppServer/WebSpeed enabled (i.e. fAllowed=false)
       ** throw a not licensed exception.
       */
       if (fAllowed == false)
          throw new LicenseMgr.NotLicensed();

       currUsers = 0;
       for(indx = 0; (pidList[indx] != END_OF_LIST); ++indx)
       {
           try
           {
               productID = pidList[indx];
               prodInfo = new ProductInfo(productID, licMgr);

               prodUsers = prodInfo.getMaxUserCount();

               /*
               ** If user count is unlimited (-1), set max
               ** to an arbitrary large value.
               */

/*
               if (prodUsers == -1)
                   prodUsers = 256;
*/
               if (prodUsers == -1)
                   prodUsers = ARBITRARY_LARGE_VALUE;

               /*
               ** If the product is a DevelopMent Server
               ** fix the number of instances (users) at 2
               ** NOTE: this value WILL be overwritten if
               ** an non-development server (AppServer/Trans Svr)
               ** installed.
               ** Identify both development and non-development products
               ** so that the user count can be validated in the
               ** ubProperties class.
               */

               if ( (productID == LicenseMgr.WEBSPEED_DEVSRVR)  ||
                    (productID == LicenseMgr.PROVISION_DEVSRVR) ||
                    (productID == LicenseMgr.WEBVISION_DEVSRVR) ||
                    (productID == LicenseMgr.WEBVISION) ||
                    (productID == LicenseMgr.PROVISION) ||
                    (productID == LicenseMgr.WEBSPEED_WORKSHOP) ||
                    (productID == LicenseMgr.OE_STUDIO) ||
                    (productID == LicenseMgr.OE_DEVELOPMENT_SERVER ))
               {
                   hasDevLicense = true;
                   prodUsers = 2;
               }
               else
                   hasNonDevLicense = true;

               if (prodUsers > currUsers)
                   currUsers = prodUsers;
           }
           catch (LicenseMgr.NoSuchProduct e)
           {
               /* System.out.println("NoSuchProduct Exception caught: " + e.getMessage());  */
               continue;
           }
           catch (java.rmi.RemoteException re)
           {
               continue;
           }
       }
       /*
       ** If we didn't find any of the Broker's product's
       ** throw a not licensed exception.
        */
       if (currUsers == 0)
           throw new LicenseMgr.NotLicensed();

       return currUsers;
   }

   /* This method must be called subsequent to calling
   ** the getMaxServers method in order to have valid data
   */
   public boolean hasDevLicenseProduct()
   {
       return hasDevLicense;
   }

   /* This method must be called subsequent to calling
   ** the getMaxServers method in order to have valid data
   */
   public boolean hasNonDevLicenseProduct()
   {
       return(hasNonDevLicense);
   }
}

