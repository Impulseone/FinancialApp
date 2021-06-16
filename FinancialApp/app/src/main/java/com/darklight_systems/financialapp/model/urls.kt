package com.darklight_systems.financialapp.model

val GET_ALL_CURRENCY_URL =
    { date: String -> "https://www.cbr.ru/scripts/XML_daily.asp?date_req=$date" }
val CURRENCY_HISTORY_URL =
    { from: String, to: String, currencyCode: String -> "http://www.cbr.ru/scripts/XML_dynamic.asp?date_req1=$from&date_req2=$to&VAL_NM_RQ=$currencyCode" }