#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
# Makefile - java/com/progress/common/util
#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
ifeq ($(TOPLEVEL),)
  local_targets = $(java/com/progress/common/util.all)
  JBUILD	:= yes
  JBUILD_ONLY	:= yes
  include ${RDLSRC}/Makefile
else
#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
# Local build macros

java/com/progress/common/util_CLASSPATH := $(CMKJAVASRC)/extjars/tuscany-sdo-tools-1.1.1.jar$${PD}$(CMKJAVASRC)/extjars/tuscany-sdo-api-r2.1-1.1.1.jar$${PD}$(CMKJAVASRC)/extjars/tuscany-sdo-impl-1.1.1.jar$${PD}$(CMKJAVASRC)/extjars/tuscany-sdo-lib-1.1.1.jar$${PD}$(CMKJAVASRC)/extjars/ecore-2.2.3.jar$${PD}$(CMKJAVASRC)/extjars/common-2.2.3.jar$${PD}$(CMKJAVASRC)/extjars/ecore-xmi-2.2.3.jar$${PD}$(CMKJAVASRC)/extjars/ecore-change-2.2.3.jar$${PD}$(CMKJAVASRC)/extjars/xsd-2.2.3.jar

#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
# Local Targets

java/com/progress/common/util_CLASSES := $(addsuffix ${CLASS}, \
	$(addprefix ${CMKCLASSDIR}/com/progress/common/util/, \
	AppService \
	Base64 \
	Environment \
	Format \
	Getopt \
	ICfgConst \
	ICmdConst \
	IDataValidation \
	IEAddEvent \
	IEClientSpecificEvent \
	IEDeleteEvent \
	IMessageCallback \
	IProWorkingCallback \
	IWatchable \
	InstallPath \
	JNIHandle \
	NetInfo \
	NetLib \
	NtKrnlException \
	PasswordString \
	PatchValidation \
	Port \
	ProVerTest \
	ProgressVersion \
	ProgressVersionInfo \
	PrologFile \
	PromsgsBundle \
	PromsgsFile \
	PropertyFilter \
	PscURLParser \
	RelativeTime \
	Typing \
	UUID \
	CompactUUID \
	WatchDog \
	acctAuthenticate \
	crypto \
	genPassword \
	genSpaCP \
	ThreadedStreamReader \
	StringPool \
	BufferedRandomAccessFile \
	IStreamMonitor \
	LoggingStreamMonitor \
	StreamMonitor \
	PrintStreamMonitor \
	FileUtil \
	ZipUtility \
	PASPropertyFilter \
	))

#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

java/com/progress/common/util.all	:= $(java/com/progress/common/util_CLASSES)
java/com/progress/common/util.gen	:= ${CMKCLASSDIR}/com/progress/common/util/ProgressVersionInfo.class

.PHONY					: java/com/progress/common/util
java/com/progress/common/util		: $(java/com/progress/common/util.all)

#%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
endif

