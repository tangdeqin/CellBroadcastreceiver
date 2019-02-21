/******************************************************************************/
/*                                                               Date:04/2013 */
/*                                PRESENTATION                                */
/*                                                                            */
/*       Copyright 2013 TCL Communication Technology Holdings Limited.        */
/*                                                                            */
/* This material is company confidential, cannot be reproduced in any form    */
/* without the written permission of TCL Communication Technology Holdings    */
/* Limited.                                                                   */
/*                                                                            */
/* -------------------------------------------------------------------------- */
/*  Author :  bo.xu                                                           */
/*  Email  :  Bo.Xu@tcl-mobile.com                                            */
/*  Role   :                                                                  */
/*  Reference documents :                                                     */
/* -------------------------------------------------------------------------- */
/*  Comments :                                                                */
/*  File     :                                                                */
/*  Labels   :                                                                */
/* -------------------------------------------------------------------------- */
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 04/06/2013|        bo.xu         |      FR-400302       |[SMS]Cell broadc- */
/*           |                      |                      |ast SMS support   */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.cellbroadcastreceiver;

import android.net.Uri;
import android.provider.BaseColumns;

public final class CellBroadcast {
    public static final String AUTHORITY = "com.jrd.provider.CellBroadcast";

    // This class cannot be instantiated
    private CellBroadcast() {
    }

    public static final class Cellbroadcasts implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
                + "/Cellbroadcasts");

        public static final String MESSAGE = "message";

        public static final String CHANNEL = "channel";

        public static final String CREATED = "created";

        public static final String DEFAULT_SORT_ORDER = "created DESC";

    }

    public static final class SpecialURI implements BaseColumns {
        // query: Cellbroadcasts left outer join Channel on Cellbroadcasts.
        public static final Uri CONTENT_URI_CBCH = Uri.parse("content://" + AUTHORITY + "/cbch");

    }

    public static final class CBLanguage implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/CBLanguage");

        public static final Uri CONTENT_URISIM1 = Uri.parse("content://" + AUTHORITY + "/CBLanguage/sub0");
        public static final Uri CONTENT_URISIM2 = Uri.parse("content://" + AUTHORITY + "/CBLanguage/sub1");
        public static final String CBLANGUAGE = "cblanguage";
    }

    public static final class CBRingtone implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/CBRingtone");

        public static final String CBRINGTONE = "cbringtone";
    }

    public static final class Channel implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Channel");

        public static final Uri CONTENT_URISIM1 = Uri.parse("content://" + AUTHORITY + "/Channel/sub0");
        public static final Uri CONTENT_URISIM2 = Uri.parse("content://" + AUTHORITY + "/Channel/sub1");
        public static final String INDEX = "mesid";

        public static final String NAME = "name";

        public static final String Enable = "isenable";

        public static final String CREATED = "created";

        public static final String DEFAULT_SORT_ORDER = "created DESC";

    }
}
