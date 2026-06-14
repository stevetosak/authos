package com.authos.config

import com.authos.getAuthosBaseUrl

fun getAuthosAuthorizeUrl() = "${getAuthosBaseUrl()}/oauth/authorize"