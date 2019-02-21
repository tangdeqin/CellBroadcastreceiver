/***************************************************************************************/
/*                                                                     Date : 10/2014  */
/*                                 PRESENTATION                                        */
/***************************************************************************************/
/*    This material is company confidential, cannot be reproduced in any               */
/*    form without the written permission of TCT Communications, Inc.                  */
/*-------------------------------------------------------------------------------------*/
/*   Author :  Liujia                                                                  */
/*   Role   :  QctWrapper                                                              */
/*   Reference documents :                                                             */
/*-------------------------------------------------------------------------------------*/
/* Modifications on Features list / Changes Request / Problems Report                  */
/*-------------------------------------------------------------------------------------*/
/* date        | author   | Key                    | comment                           */
/*-------------------------------------------------------------------------------------*/
/*=====================================================================================*/

package com.tct.wrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class QctWrapper extends AbsQctWrapper {

    private static class QctWrapperHolder {
        private static QctWrapper instance = new QctWrapper();
    }

    private QctWrapper(){
    }

    public static QctWrapper getInstance(){
        return QctWrapperHolder.instance;
    }
}
