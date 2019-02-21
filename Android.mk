# Copyright 2011 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_SRC_FILES += src/com/android/cellbroadcastreceiver/IListener.aidl
LOCAL_SRC_FILES += $(call all-java-files-under, tct-wrapper-src)
LOCAL_SRC_FILES += $(call all-java-files-under, tct-src)

LOCAL_JAVA_LIBRARIES := telephony-common
#LOCAL_JAVA_LIBRARIES += mediatek-framework
#LOCAL_JAVA_LIBRARIES += mediatek-telephony-base
#LOCAL_JAVA_LIBRARIES += mediatek-common
#LOCAL_JAVA_LIBRARIES += mediatek-telephony-common
LOCAL_STATIC_JAVA_LIBRARIES := tct-qct-mms-ext tct-mtk-mms-ext

LOCAL_PACKAGE_NAME := CellBroadcastReceiver
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_DEX_PREOPT := false

#include $(BUILD_PLF) remove by liang.zhang
include $(BUILD_PACKAGE)

# This finds and builds the test apk as well, so a single make does both.
include $(call all-makefiles-under,$(LOCAL_PATH))

