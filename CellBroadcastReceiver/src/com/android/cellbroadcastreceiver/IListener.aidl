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
/*  Author :  yugang.jia                                                      */
/*  Email  :  yugang.jia@tcl.com                                              */
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
/* 09/06/2013|      yugang.jia      |      FR-516039       |[SMS]Cell broadc- */
/*           |                      |                      |ast SMS support   */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.cellbroadcastreceiver;

interface IListener {
    /**
     * set listen for TctIccSmsInterfaceManager,when finish it can send info back
     */
    void onFinished(int num,String index,String cbable);
}
