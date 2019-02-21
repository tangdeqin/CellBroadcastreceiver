/***************************************************************************************/
/*                                                                     Date : 10/2014  */
/*                                 PRESENTATION                                        */
/***************************************************************************************/
/*    This material is company confidential, cannot be reproduced in any               */
/*    form without the written permission of TCT Communications, Inc.                  */
/*-------------------------------------------------------------------------------------*/
/*   Author :  Liujia                                                                  */
/*   Role   :     MtkWrapper                                                           */
/*   Reference documents :                                                             */
/*-------------------------------------------------------------------------------------*/
/* Modifications on Features list / Changes Request / Problems Report                  */
/*-------------------------------------------------------------------------------------*/
/* date        | author   | Key                    | comment                           */
/*-------------------------------------------------------------------------------------*/
/*=====================================================================================*/

package com.tct.wrapper;



class MtkWrapper extends AbsMtkWrapper {

    private static class MtkWrapperHolder {
        private static MtkWrapper instance = new MtkWrapper();
    }

    private MtkWrapper(){
    }

    public static MtkWrapper getInstance(){
        return MtkWrapperHolder.instance;
    }

}