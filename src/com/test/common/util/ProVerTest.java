package com.progress.common.util;

import java.lang.*;
import java.util.*;
import com.progress.common.util.ProgressVersion;

class ProVerTest
{

private void dispVersions()
{
    System.out.println("Version " + ProgressVersion.getVersion());
    System.out.println("VersionString " + ProgressVersion.getVersionString());
    System.out.println("FullVersion " + ProgressVersion.getFullVersion());
    System.out.println("FullVersionString " + ProgressVersion.getFullVersionString());
    System.out.println("Build number " + ProgressVersion.getBuildNumber ());
    System.out.println("Major number " + ProgressVersion.getMajorNumber ());
    System.out.println("Minor number " + ProgressVersion.getMinorNumber ());
    System.out.println("Maintenance level " + ProgressVersion.getMaintenanceLevel ());
    System.out.println("Service Packnumber " + ProgressVersion.getServicePackNumber ());
    System.out.println("Temp Fixnumber " + ProgressVersion.getTemporaryFixNumber ());
    System.out.println("BaseVersion " + ProgressVersion.getBaseVersion ());
    boolean fValid = ProgressVersion.validPatchlevel("/tmp");
    System.out.println("ValidPatch " + fValid + " Min version is " + 
		       ProgressVersion.getMinVer());
}



public static void main(String args[])
{
    ProVerTest prover = new ProVerTest();
    prover.dispVersions();
}


}

