package com.example.testing_kiosk.models

import java.text.SimpleDateFormat
import java.util.*

class DataBean {
    var m_iSeqID = 0
    var m_iFunID = -1
    private var m_strValue1 = ""
    private var m_strDataTime = ""

    constructor(iFunID: Int, strValue1: String) {
        this.m_iSeqID = m_iSeqID
        this.m_iFunID = m_iFunID
        this.m_strValue1 = m_strValue1
        this.m_strDataTime = m_strDataTime
    }

    //    DataBean(iFunID: Int, strValue1: String) {
//        m_iSeqID++
//        m_iFunID = iFunID
//        m_strValue1 = strValue1
//        val sDateFormat = SimpleDateFormat("hh:mm:ss")
//        m_strDataTime = sDateFormat.format(Date())
//    }
}