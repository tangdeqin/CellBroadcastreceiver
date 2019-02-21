include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := tct-qct-mms-ext:libs/tct-qct-mms-encapsulation.jack
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += tct-mtk-mms-ext:libs/tct-mtk-mms-encapsulation.jar

include $(BUILD_MULTI_PREBUILT)

